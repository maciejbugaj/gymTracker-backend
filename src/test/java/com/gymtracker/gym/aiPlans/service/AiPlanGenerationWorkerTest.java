package com.gymtracker.gym.aiPlans.service;

import com.gymtracker.gym.aiPlans.client.AiGenerationResult;
import com.gymtracker.gym.aiPlans.client.StubWorkoutPlanAiClient;
import com.gymtracker.gym.aiPlans.client.WorkoutPlanAiClient;
import com.gymtracker.gym.aiPlans.config.AiProperties;
import com.gymtracker.gym.aiPlans.dto.GeneratePlanRequest;
import com.gymtracker.gym.aiPlans.generated.GeneratedProgram;
import com.gymtracker.gym.aiPlans.history.TrainingHistorySummary;
import com.gymtracker.gym.aiPlans.history.TrainingHistorySummaryService;
import com.gymtracker.gym.aiPlans.model.AiPlanGeneration;
import com.gymtracker.gym.aiPlans.model.AiPlanGenerationStatus;
import com.gymtracker.gym.aiPlans.repository.AiPlanGenerationRepository;
import com.gymtracker.gym.trainingPrograms.model.ProgramGoal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * generateAsync/regenerateAsync are @Async, but called directly here (no Spring context / proxy) —
 * so they simply run synchronously on the test thread, which is exactly what a unit test wants.
 */
@ExtendWith(MockitoExtension.class)
class AiPlanGenerationWorkerTest {

    private static final AiProperties AI_PROPERTIES =
            new AiProperties(false, "", "claude-opus-5", 16000, 5, 12);

    @Mock
    AiPlanGenerationRepository aiPlanGenerationRepository;
    @Mock
    TrainingHistorySummaryService historySummaryService;

    private AiPlanGenerationWorker workerWith(WorkoutPlanAiClient client) {
        return new AiPlanGenerationWorker(
                aiPlanGenerationRepository, historySummaryService, client, AI_PROPERTIES, new ObjectMapper());
    }

    private GeneratePlanRequest request(int weeks, int daysPerWeek) {
        return new GeneratePlanRequest("STRENGTH", null, daysPerWeek, weeks, 60, null, null, null, null, null);
    }

    private TrainingHistorySummary emptyHistory() {
        return new TrainingHistorySummary(LocalDate.now().minusWeeks(12), LocalDate.now(), 0, 0.0, 0, List.of());
    }

    @Test
    void generateAsyncMarksSucceededOnValidStubProgram() {
        AiPlanGenerationWorker worker = workerWith(new StubWorkoutPlanAiClient());
        AiPlanGeneration generation = AiPlanGeneration.builder().id(1L).userId(1L)
                .status(AiPlanGenerationStatus.PENDING).build();
        when(aiPlanGenerationRepository.findById(1L)).thenReturn(Optional.of(generation));
        when(historySummaryService.summarize(eq(1L), eq(12))).thenReturn(emptyHistory());

        worker.generateAsync(1L, 1L, request(4, 3));

        ArgumentCaptor<AiPlanGeneration> captor = ArgumentCaptor.forClass(AiPlanGeneration.class);
        verify(aiPlanGenerationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AiPlanGenerationStatus.SUCCEEDED);
        assertThat(captor.getValue().getRawResponse()).contains("Stub Program");
        assertThat(captor.getValue().getHistorySummaryJson()).isNotBlank();
        assertThat(captor.getValue().getErrorMessage()).isNull();
    }

    @Test
    void generateAsyncMarksFailedWhenProgramShapeIsInvalid() {
        WorkoutPlanAiClient brokenClient = new WorkoutPlanAiClient() {
            @Override
            public AiGenerationResult generate(GeneratePlanRequest req, TrainingHistorySummary history) {
                // returns 0 weeks even though the request asked for req.durationWeeks()
                GeneratedProgram program = new GeneratedProgram(
                        "Broken", "desc", ProgramGoal.GENERAL, req.durationWeeks(), req.daysPerWeek(),
                        List.of(), "notes");
                return new AiGenerationResult(program, 10, 20);
            }

            @Override
            public AiGenerationResult regenerate(GeneratePlanRequest req, TrainingHistorySummary history,
                                                  GeneratedProgram previousProgram, String feedback) {
                throw new UnsupportedOperationException();
            }
        };
        AiPlanGenerationWorker worker = workerWith(brokenClient);
        AiPlanGeneration generation = AiPlanGeneration.builder().id(2L).userId(1L)
                .status(AiPlanGenerationStatus.PENDING).build();
        when(aiPlanGenerationRepository.findById(2L)).thenReturn(Optional.of(generation));
        when(historySummaryService.summarize(eq(1L), eq(12))).thenReturn(emptyHistory());

        worker.generateAsync(2L, 1L, request(4, 3));

        ArgumentCaptor<AiPlanGeneration> captor = ArgumentCaptor.forClass(AiPlanGeneration.class);
        verify(aiPlanGenerationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AiPlanGenerationStatus.FAILED);
        assertThat(captor.getValue().getErrorMessage()).contains("Expected 4 weeks");
        assertThat(captor.getValue().getRawResponse()).isNull();
    }

    @Test
    void generateAsyncMarksFailedWhenClientThrows() {
        WorkoutPlanAiClient throwingClient = new WorkoutPlanAiClient() {
            @Override
            public AiGenerationResult generate(GeneratePlanRequest req, TrainingHistorySummary history) {
                throw new RuntimeException("Claude is down");
            }

            @Override
            public AiGenerationResult regenerate(GeneratePlanRequest req, TrainingHistorySummary history,
                                                  GeneratedProgram previousProgram, String feedback) {
                throw new UnsupportedOperationException();
            }
        };
        AiPlanGenerationWorker worker = workerWith(throwingClient);
        AiPlanGeneration generation = AiPlanGeneration.builder().id(3L).userId(1L)
                .status(AiPlanGenerationStatus.PENDING).build();
        when(aiPlanGenerationRepository.findById(3L)).thenReturn(Optional.of(generation));
        when(historySummaryService.summarize(eq(1L), eq(12))).thenReturn(emptyHistory());

        worker.generateAsync(3L, 1L, request(4, 3));

        ArgumentCaptor<AiPlanGeneration> captor = ArgumentCaptor.forClass(AiPlanGeneration.class);
        verify(aiPlanGenerationRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AiPlanGenerationStatus.FAILED);
        assertThat(captor.getValue().getErrorMessage()).isEqualTo("Claude is down");
    }
}
