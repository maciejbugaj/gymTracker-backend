package com.gymtracker.gym.aiPlans.prompt;

import com.gymtracker.gym.aiPlans.dto.GeneratePlanRequest;
import com.gymtracker.gym.aiPlans.history.TrainingHistorySummary;
import com.gymtracker.gym.aiPlans.history.TrainingHistorySummary.BestSet;
import com.gymtracker.gym.aiPlans.history.TrainingHistorySummary.ExerciseHistory;
import com.gymtracker.gym.aiPlans.history.TrainingHistorySummary.Trend;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WorkoutPlanPromptBuilderTest {

    private final WorkoutPlanPromptBuilder promptBuilder = new WorkoutPlanPromptBuilder(new ObjectMapper());

    @Test
    void systemPromptMentionsDeloadGuidance() {
        assertThat(promptBuilder.buildSystemPrompt()).containsIgnoringCase("deload");
    }

    @Test
    void userPromptContainsGoalAndFrequency() {
        GeneratePlanRequest request = new GeneratePlanRequest(
                "STRENGTH", "INTERMEDIATE", 4, 8, 60,
                List.of("barbell", "dumbbells"), "UPPER_LOWER", null, null, null);

        String prompt = promptBuilder.buildUserPrompt(request, emptyHistory());

        assertThat(prompt).contains("STRENGTH");
        assertThat(prompt).contains("Training days per week: 4");
        assertThat(prompt).contains("Program length: 8 weeks");
    }

    @Test
    void userPromptListsTopExercisesFromHistory() {
        GeneratePlanRequest request = new GeneratePlanRequest(
                "HYPERTROPHY", null, 3, 4, 45, null, null, null, null, null);

        ExerciseHistory bench = new ExerciseHistory(
                "Bench Press", 5, 15, 6, 10, 8.0,
                new BestSet(BigDecimal.valueOf(100), 8, 133.3),
                133.3, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 1), Trend.UP);
        TrainingHistorySummary history = new TrainingHistorySummary(
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 9, 1), 5, 1.25, 1, List.of(bench));

        String prompt = promptBuilder.buildUserPrompt(request, history);

        assertThat(prompt).contains("Bench Press");
        assertThat(prompt).contains("UP");
    }

    @Test
    void userPromptFallsBackToConservativeGuidanceWhenNoHistory() {
        GeneratePlanRequest request = new GeneratePlanRequest(
                "GENERAL", null, 3, 4, 45, null, null, null, null, null);

        String prompt = promptBuilder.buildUserPrompt(request, emptyHistory());

        assertThat(prompt).containsIgnoringCase("none available");
    }

    private TrainingHistorySummary emptyHistory() {
        return new TrainingHistorySummary(LocalDate.now().minusWeeks(8), LocalDate.now(), 0, 0.0, 0, List.of());
    }
}
