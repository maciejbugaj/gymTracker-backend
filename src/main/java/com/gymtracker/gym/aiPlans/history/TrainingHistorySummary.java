package com.gymtracker.gym.aiPlans.history;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TrainingHistorySummary(
        LocalDate windowStart,
        LocalDate windowEnd,
        int totalSessions,
        double sessionsPerWeek,
        int distinctExercises,
        List<ExerciseHistory> exercises
) {
    public enum Trend {
        UP, FLAT, DOWN, INSUFFICIENT_DATA
    }

    public record ExerciseHistory(
            String exerciseName,
            int sessionCount,
            int totalSets,
            int minReps,
            int maxReps,
            double avgReps,
            BestSet bestSet,
            double estimatedOneRepMaxKg,
            LocalDate firstSeen,
            LocalDate lastSeen,
            Trend trend
    ) {}

    public record BestSet(
            BigDecimal weightKg,
            int reps,
            double e1rmKg
    ) {}
}
