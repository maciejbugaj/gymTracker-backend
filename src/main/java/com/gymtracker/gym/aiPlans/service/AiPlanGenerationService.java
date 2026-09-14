package com.gymtracker.gym.aiPlans.service;

import com.gymtracker.gym.aiPlans.config.AiProperties;
import com.gymtracker.gym.aiPlans.dto.AiPlanGenerationResponse;
import com.gymtracker.gym.aiPlans.dto.GeneratePlanRequest;
import com.gymtracker.gym.aiPlans.generated.GeneratedProgram;
import com.gymtracker.gym.aiPlans.model.AiPlanGeneration;
import com.gymtracker.gym.aiPlans.model.AiPlanGenerationStatus;
import com.gymtracker.gym.aiPlans.repository.AiPlanGenerationRepository;
import com.gymtracker.gym.exceptions.ConflictException;
import com.gymtracker.gym.exceptions.DailyGenerationLimitExceededException;
import com.gymtracker.gym.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AiPlanGenerationService {

    private final AiPlanGenerationRepository aiPlanGenerationRepository;
    private final AiPlanGenerationWorker worker;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Transactional
    public AiPlanGenerationResponse startGeneration(Long userId, GeneratePlanRequest request) {
        enforceDailyLimit(userId);

        AiPlanGeneration generation = AiPlanGeneration.builder()
                .userId(userId)
                .status(AiPlanGenerationStatus.PENDING)
                .requestJson(writeJson(request))
                .model(aiProperties.model())
                .build();
        generation = aiPlanGenerationRepository.save(generation);

        worker.generateAsync(generation.getId(), userId, request);
        return toResponse(generation, null);
    }

    @Transactional
    public AiPlanGenerationResponse regenerate(Long userId, Long generationId, String feedback) {
        AiPlanGeneration previous = aiPlanGenerationRepository.findByIdAndUserId(generationId, userId)
                .orElseThrow(() -> new NotFoundException("AI plan generation " + generationId + " not found"));
        if (previous.getStatus() != AiPlanGenerationStatus.SUCCEEDED) {
            throw new ConflictException("Can only regenerate from a succeeded generation");
        }
        enforceDailyLimit(userId);

        GeneratePlanRequest request = readJson(previous.getRequestJson(), GeneratePlanRequest.class);
        GeneratedProgram previousProgram = readJson(previous.getRawResponse(), GeneratedProgram.class);

        AiPlanGeneration generation = AiPlanGeneration.builder()
                .userId(userId)
                .status(AiPlanGenerationStatus.PENDING)
                .requestJson(previous.getRequestJson())
                .model(aiProperties.model())
                .previousGenerationId(previous.getId())
                .build();
        generation = aiPlanGenerationRepository.save(generation);

        worker.regenerateAsync(generation.getId(), userId, request, previousProgram, feedback);
        return toResponse(generation, null);
    }

    @Transactional(readOnly = true)
    public AiPlanGenerationResponse getGeneration(Long userId, Long id) {
        AiPlanGeneration generation = aiPlanGenerationRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("AI plan generation " + id + " not found"));
        return toResponse(generation, readProgram(generation));
    }

    @Transactional(readOnly = true)
    public List<AiPlanGenerationResponse> listGenerations(Long userId) {
        return aiPlanGenerationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(generation -> toResponse(generation, null))
                .toList();
    }

    private void enforceDailyLimit(Long userId) {
        LocalDateTime since = LocalDateTime.now(clock).minusHours(24);
        long count = aiPlanGenerationRepository.countByUserIdAndCreatedAtAfter(userId, since);
        if (count >= aiProperties.dailyLimitPerUser()) {
            throw new DailyGenerationLimitExceededException(
                    "Daily AI plan generation limit (%d) reached".formatted(aiProperties.dailyLimitPerUser()));
        }
    }

    private GeneratedProgram readProgram(AiPlanGeneration generation) {
        if (generation.getStatus() != AiPlanGenerationStatus.SUCCEEDED) {
            return null;
        }
        return readJson(generation.getRawResponse(), GeneratedProgram.class);
    }

    private AiPlanGenerationResponse toResponse(AiPlanGeneration generation, GeneratedProgram program) {
        return new AiPlanGenerationResponse(
                generation.getId(),
                generation.getStatus(),
                program,
                generation.getErrorMessage(),
                generation.getInputTokens(),
                generation.getOutputTokens(),
                generation.getCreatedAt()
        );
    }

    // Jackson 3's writeValueAsString/readValue throw JacksonException, which is unchecked —
    // no try/catch needed.
    private String writeJson(Object value) {
        return objectMapper.writeValueAsString(value);
    }

    private <T> T readJson(String json, Class<T> type) {
        return objectMapper.readValue(json, type);
    }
}
