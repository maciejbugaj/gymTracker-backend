package com.gymtracker.gym.aiPlans.prompt;

import com.gymtracker.gym.aiPlans.dto.GeneratePlanRequest;
import com.gymtracker.gym.aiPlans.history.TrainingHistorySummary;
import com.gymtracker.gym.aiPlans.history.TrainingHistorySummary.ExerciseHistory;
import org.springframework.stereotype.Component;

import java.time.temporal.ChronoUnit;

@Component
public class WorkoutPlanPromptBuilder {

    private static final String SYSTEM_PROMPT = """
            You are an experienced strength & conditioning coach who designs safe, progressive, \
            periodized training programs.

            Rules:
            - Build a program that spans exactly the requested number of weeks and trains the \
            requested number of days per week.
            - If the program is 6 weeks or longer, include at least one deload week (reduced \
            volume/intensity) roughly every 4-6 weeks.
            - Prefer exercises the athlete has already logged, and only add new ones when they are \
            a reasonable fit for the stated goal and available equipment.
            - Respect the athlete's available equipment and session length.
            - Express progression as sets x rep range plus a short instruction in each exercise's \
            notes field (e.g. "add 2.5kg once you hit the top of the rep range on all sets"). \
            Never mention or reference RPE anywhere in the program.
            - Base starting weights and volume on the athlete's training history when it is \
            available; when history is sparse or absent, default to conservative, technique-first \
            loading.
            """;

    public String buildSystemPrompt() {
        return SYSTEM_PROMPT;
    }

    public String buildUserPrompt(GeneratePlanRequest request, TrainingHistorySummary history) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Design a training program for this athlete.\n\n");
        appendRequest(prompt, request);
        prompt.append('\n');
        appendHistory(prompt, history);
        return prompt.toString();
    }

    private void appendRequest(StringBuilder prompt, GeneratePlanRequest request) {
        prompt.append("Goal: ").append(request.goal()).append('\n');
        if (request.experienceLevel() != null) {
            prompt.append("Experience level: ").append(request.experienceLevel()).append('\n');
        }
        prompt.append("Training days per week: ").append(request.daysPerWeek()).append('\n');
        prompt.append("Program length: ").append(request.durationWeeks()).append(" weeks\n");
        prompt.append("Session length: ").append(request.sessionLengthMinutes()).append(" minutes\n");
        if (request.equipment() != null && !request.equipment().isEmpty()) {
            prompt.append("Available equipment: ").append(String.join(", ", request.equipment())).append('\n');
        }
        if (request.splitPreference() != null) {
            prompt.append("Split preference: ").append(request.splitPreference()).append('\n');
        }
        if (request.focusMuscleGroups() != null && !request.focusMuscleGroups().isEmpty()) {
            prompt.append("Muscle groups to emphasize: ")
                    .append(String.join(", ", request.focusMuscleGroups())).append('\n');
        }
        if (request.exclusionsOrInjuries() != null && !request.exclusionsOrInjuries().isBlank()) {
            prompt.append("Exclusions / injuries to work around: ").append(request.exclusionsOrInjuries()).append('\n');
        }
        if (request.notes() != null && !request.notes().isBlank()) {
            prompt.append("Additional notes from the athlete: ").append(request.notes()).append('\n');
        }
    }

    private void appendHistory(StringBuilder prompt, TrainingHistorySummary history) {
        if (history == null || history.totalSessions() == 0) {
            prompt.append("Training history: none available. Default to conservative, technique-first loading.\n");
            return;
        }

        long windowWeeks = ChronoUnit.WEEKS.between(history.windowStart(), history.windowEnd());
        prompt.append("Training history (last ").append(windowWeeks).append(" weeks): ")
                .append(history.totalSessions()).append(" sessions, ~")
                .append("%.1f".formatted(history.sessionsPerWeek())).append("/week, ")
                .append(history.distinctExercises()).append(" distinct exercises.\n");

        for (ExerciseHistory exercise : history.exercises()) {
            prompt.append("- ").append(exercise.exerciseName()).append(": ")
                    .append(exercise.sessionCount()).append(" sessions, ")
                    .append(exercise.minReps()).append('-').append(exercise.maxReps()).append(" reps, best set ")
                    .append(exercise.bestSet().weightKg()).append("kg x").append(exercise.bestSet().reps())
                    .append(", est. 1RM ~").append("%.0f".formatted(exercise.estimatedOneRepMaxKg()))
                    .append("kg, trend ").append(exercise.trend()).append('\n');
        }
    }
}
