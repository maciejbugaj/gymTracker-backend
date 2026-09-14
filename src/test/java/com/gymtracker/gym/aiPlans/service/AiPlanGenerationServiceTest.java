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
import com.gymtracker.gym.trainingPrograms.model.ProgramGoal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiPlanGenerationServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-14T10:00:00Z"), ZoneOffset.UTC);
    private static final long USER_ID = 1L;

    @Mock
    AiPlanGenerationRepository aiPlanGenerationRepository;
    @Mock
    AiPlanGenerationWorker worker;

    AiPlanGenerationService service;

    @BeforeEach
    void setUp() {
        AiProperties aiProperties = new AiProperties(true, "key", "claude-opus-5", 16000, 5, 12);
        service = new AiPlanGenerationService(
                aiPlanGenerationRepository, worker, aiProperties, new ObjectMapper(), CLOCK);
        lenient().when(aiPlanGenerationRepository.save(any(AiPlanGeneration.class)))
                .thenAnswer(invocation -> {
                    AiPlanGeneration generation = invocation.getArgument(0);
                    if (generation.getId() == null) {
                        generation.setId(1L);
                    }
                    return generation;
                });
    }

    private GeneratePlanRequest sampleRequest() {
        return new GeneratePlanRequest("STRENGTH", "INTERMEDIATE", 4, 8, 60,
                List.of("barbell"), "UPPER_LOWER", null, null, null);
    }

    @Test
    void startGenerationSavesPendingRowAndDispatchesWorker() {
        when(aiPlanGenerationRepository.countByUserIdAndCreatedAtAfter(eq(USER_ID), any())).thenReturn(0L);

        AiPlanGenerationResponse response = service.startGeneration(USER_ID, sampleRequest());

        assertThat(response.status()).isEqualTo(AiPlanGenerationStatus.PENDING);
        assertThat(response.program()).isNull();
        assertThat(response.id()).isEqualTo(1L);

        ArgumentCaptor<AiPlanGeneration> captor = ArgumentCaptor.forClass(AiPlanGeneration.class);
        verify(aiPlanGenerationRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().getStatus()).isEqualTo(AiPlanGenerationStatus.PENDING);
        assertThat(captor.getValue().getRequestJson()).contains("STRENGTH");

        verify(worker).generateAsync(eq(1L), eq(USER_ID), eq(sampleRequest()));
    }

    @Test
    void startGenerationThrowsWhenDailyLimitReached() {
        when(aiPlanGenerationRepository.countByUserIdAndCreatedAtAfter(eq(USER_ID), any())).thenReturn(5L);

        assertThatThrownBy(() -> service.startGeneration(USER_ID, sampleRequest()))
                .isInstanceOf(DailyGenerationLimitExceededException.class);

        verifyNoInteractions(worker);
        verify(aiPlanGenerationRepository, never()).save(any());
    }

    @Test
    void regenerateThrowsWhenPreviousGenerationNotFound() {
        when(aiPlanGenerationRepository.findByIdAndUserId(99L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.regenerate(USER_ID, 99L, "more volume"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void regenerateThrowsWhenPreviousGenerationNotSucceeded() {
        AiPlanGeneration pending = AiPlanGeneration.builder().id(5L).userId(USER_ID)
                .status(AiPlanGenerationStatus.PENDING).build();
        when(aiPlanGenerationRepository.findByIdAndUserId(5L, USER_ID)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> service.regenerate(USER_ID, 5L, "more volume"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void regenerateDispatchesWorkerWithDeserializedRequestAndProgram() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        GeneratePlanRequest originalRequest = sampleRequest();
        GeneratedProgram previousProgram = new GeneratedProgram(
                "Prev", "desc", ProgramGoal.STRENGTH, 8, 4, List.of(), "notes");

        AiPlanGeneration previous = AiPlanGeneration.builder()
                .id(5L).userId(USER_ID).status(AiPlanGenerationStatus.SUCCEEDED)
                .requestJson(objectMapper.writeValueAsString(originalRequest))
                .rawResponse(objectMapper.writeValueAsString(previousProgram))
                .build();
        when(aiPlanGenerationRepository.findByIdAndUserId(5L, USER_ID)).thenReturn(Optional.of(previous));
        when(aiPlanGenerationRepository.countByUserIdAndCreatedAtAfter(eq(USER_ID), any())).thenReturn(0L);

        AiPlanGenerationResponse response = service.regenerate(USER_ID, 5L, "more volume");

        assertThat(response.status()).isEqualTo(AiPlanGenerationStatus.PENDING);

        ArgumentCaptor<AiPlanGeneration> captor = ArgumentCaptor.forClass(AiPlanGeneration.class);
        verify(aiPlanGenerationRepository).save(captor.capture());
        assertThat(captor.getValue().getPreviousGenerationId()).isEqualTo(5L);

        verify(worker).regenerateAsync(eq(1L), eq(USER_ID), eq(originalRequest), eq(previousProgram), eq("more volume"));
    }

    @Test
    void getGenerationReturnsParsedProgramWhenSucceeded() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        GeneratedProgram program = new GeneratedProgram(
                "Prog", "desc", ProgramGoal.HYPERTROPHY, 4, 3, List.of(), "notes");
        AiPlanGeneration generation = AiPlanGeneration.builder()
                .id(7L).userId(USER_ID).status(AiPlanGenerationStatus.SUCCEEDED)
                .rawResponse(objectMapper.writeValueAsString(program))
                .build();
        when(aiPlanGenerationRepository.findByIdAndUserId(7L, USER_ID)).thenReturn(Optional.of(generation));

        AiPlanGenerationResponse response = service.getGeneration(USER_ID, 7L);

        assertThat(response.program()).isEqualTo(program);
    }

    @Test
    void getGenerationReturnsNullProgramWhenPending() {
        AiPlanGeneration generation = AiPlanGeneration.builder()
                .id(8L).userId(USER_ID).status(AiPlanGenerationStatus.PENDING).build();
        when(aiPlanGenerationRepository.findByIdAndUserId(8L, USER_ID)).thenReturn(Optional.of(generation));

        AiPlanGenerationResponse response = service.getGeneration(USER_ID, 8L);

        assertThat(response.program()).isNull();
    }

    @Test
    void getGenerationThrowsWhenNotFound() {
        when(aiPlanGenerationRepository.findByIdAndUserId(404L, USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getGeneration(USER_ID, 404L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void listGenerationsDoesNotParseProgramsForEachRow() {
        AiPlanGeneration succeeded = AiPlanGeneration.builder().id(1L).userId(USER_ID)
                .status(AiPlanGenerationStatus.SUCCEEDED).rawResponse("{\"not\":\"checked\"}").build();
        when(aiPlanGenerationRepository.findByUserIdOrderByCreatedAtDesc(USER_ID)).thenReturn(List.of(succeeded));

        List<AiPlanGenerationResponse> responses = service.listGenerations(USER_ID);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().program()).isNull();
    }
}
