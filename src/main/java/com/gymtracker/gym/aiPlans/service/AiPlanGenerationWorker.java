package com.gymtracker.gym.aiPlans.service;

import com.gymtracker.gym.aiPlans.client.AiGenerationResult;
import com.gymtracker.gym.aiPlans.client.WorkoutPlanAiClient;
import com.gymtracker.gym.aiPlans.config.AiProperties;
import com.gymtracker.gym.aiPlans.dto.GeneratePlanRequest;
import com.gymtracker.gym.aiPlans.generated.GeneratedDay;
import com.gymtracker.gym.aiPlans.generated.GeneratedExercise;
import com.gymtracker.gym.aiPlans.generated.GeneratedProgram;
import com.gymtracker.gym.aiPlans.generated.GeneratedWeek;
import com.gymtracker.gym.aiPlans.history.TrainingHistorySummary;
import com.gymtracker.gym.aiPlans.history.TrainingHistorySummaryService;
import com.gymtracker.gym.aiPlans.model.AiPlanGeneration;
import com.gymtracker.gym.aiPlans.model.AiPlanGenerationStatus;
import com.gymtracker.gym.aiPlans.repository.AiPlanGenerationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * Runs the actual Claude call off the request thread. Lives as its own bean (not a method on
 * AiPlanGenerationService) because @Async only works through the Spring proxy — a self-call
 * from within the same class would run synchronously and silently block the caller.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AiPlanGenerationWorker {

    private final AiPlanGenerationRepository aiPlanGenerationRepository;
    private final TrainingHistorySummaryService historySummaryService;
    private final WorkoutPlanAiClient aiClient;
    private final AiProperties aiProperties;
    private final ObjectMapper objectMapper;

    @Async("aiPlanGenerationExecutor")
    public void generateAsync(Long generationId, Long userId, GeneratePlanRequest request) {
        try {
            TrainingHistorySummary history = summarize(userId);
            AiGenerationResult result = aiClient.generate(request, history);
            finish(generationId, request, history, result);
        } catch (Exception e) {
            fail(generationId, e);
        }
    }

    @Async("aiPlanGenerationExecutor")
    public void regenerateAsync(Long generationId, Long userId, GeneratePlanRequest request,
                                 GeneratedProgram previousProgram, String feedback) {
        try {
            TrainingHistorySummary history = summarize(userId);
            AiGenerationResult result = aiClient.regenerate(request, history, previousProgram, feedback);
            finish(generationId, request, history, result);
        } catch (Exception e) {
            fail(generationId, e);
        }
    }

    private TrainingHistorySummary summarize(Long userId) {
        return historySummaryService.summarize(userId, aiProperties.historyWindowWeeks());
    }

    @Transactional
    void finish(Long generationId, GeneratePlanRequest request, TrainingHistorySummary history,
                AiGenerationResult result) {
        AiPlanGeneration generation = aiPlanGenerationRepository.findById(generationId)
                .orElseThrow(() -> new IllegalStateException("Generation " + generationId + " disappeared mid-flight"));

        String validationError = validate(result.program(), request);
        if (validationError != null) {
            generation.setStatus(AiPlanGenerationStatus.FAILED);
            generation.setErrorMessage(validationError);
        } else {
            generation.setStatus(AiPlanGenerationStatus.SUCCEEDED);
            generation.setRawResponse(writeJson(result.program()));
        }
        generation.setHistorySummaryJson(writeJson(history));
        generation.setInputTokens(result.inputTokens());
        generation.setOutputTokens(result.outputTokens());
        aiPlanGenerationRepository.save(generation);
    }

    @Transactional
    void fail(Long generationId, Exception e) {
        log.error("AI plan generation {} failed", generationId, e);
        aiPlanGenerationRepository.findById(generationId).ifPresent(generation -> {
            generation.setStatus(AiPlanGenerationStatus.FAILED);
            generation.setErrorMessage(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            aiPlanGenerationRepository.save(generation);
        });
    }

    private String validate(GeneratedProgram program, GeneratePlanRequest request) {
        if (program.weeks() == null || program.weeks().size() != request.durationWeeks()) {
            int actual = program.weeks() == null ? 0 : program.weeks().size();
            return "Expected %d weeks, got %d".formatted(request.durationWeeks(), actual);
        }
        for (GeneratedWeek week : program.weeks()) {
            if (week.days() == null || week.days().isEmpty()) {
                return "Week %d has no days".formatted(week.weekNumber());
            }
            for (GeneratedDay day : week.days()) {
                if (day.exercises() == null || day.exercises().isEmpty()) {
                    return "Week %d day %d has no exercises".formatted(week.weekNumber(), day.dayNumber());
                }
                for (GeneratedExercise exercise : day.exercises()) {
                    if (exercise.targetSets() <= 0 || exercise.targetRepsMin() <= 0
                            || exercise.targetRepsMax() < exercise.targetRepsMin()) {
                        return "Invalid prescription for '%s' in week %d day %d"
                                .formatted(exercise.exerciseName(), week.weekNumber(), day.dayNumber());
                    }
                }
            }
        }
        return null;
    }

    private String writeJson(Object value) {
        // Jackson 3's writeValueAsString throws JacksonException, which is unchecked — no try/catch needed.
        return objectMapper.writeValueAsString(value);
    }
}
