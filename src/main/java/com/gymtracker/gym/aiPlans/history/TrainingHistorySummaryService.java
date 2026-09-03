package com.gymtracker.gym.aiPlans.history;

import com.gymtracker.gym.aiPlans.history.TrainingHistorySummary.BestSet;
import com.gymtracker.gym.aiPlans.history.TrainingHistorySummary.ExerciseHistory;
import com.gymtracker.gym.aiPlans.history.TrainingHistorySummary.Trend;
import com.gymtracker.gym.exerciseLogs.model.ExerciseLog;
import com.gymtracker.gym.exerciseLogs.repository.ExerciseLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingHistorySummaryService {

    private static final double TREND_THRESHOLD = 0.025; // ±2.5%

    private final ExerciseLogRepository exerciseLogRepository;
    private final Clock clock;

    @Transactional(readOnly = true)
    public TrainingHistorySummary summarize(Long userId, int windowWeeks) {
        LocalDate today = LocalDate.now(clock);
        LocalDate windowStart = today.minusWeeks(windowWeeks);
        LocalDateTime since = windowStart.atStartOfDay();

        List<ExerciseLog> sets = exerciseLogRepository.findCompletedSetsForUserSince(userId, since).stream()
                .filter(s -> s.getReps() != null && s.getReps() > 0 && s.getWeightKg() != null)
                .toList();

        if (sets.isEmpty()) {
            return new TrainingHistorySummary(windowStart, today, 0, 0.0, 0, List.of());
        }

        Map<String, List<ExerciseLog>> byExercise = sets.stream()
                .collect(Collectors.groupingBy(ExerciseLog::getExerciseName));

        List<ExerciseHistory> exercises = byExercise.entrySet().stream()
                .map(e -> buildExerciseHistory(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingInt(ExerciseHistory::totalSets).reversed())
                .toList();

        List<LocalDate> sessionDates = sets.stream()
                .collect(Collectors.groupingBy(s -> s.getWorkoutSession().getId()))
                .values().stream()
                .map(sessionSets -> sessionSets.get(0).getWorkoutSession().getStartedAt().toLocalDate())
                .sorted()
                .toList();

        int totalSessions = sessionDates.size();
        double spanWeeks = Math.max(1.0,
                ChronoUnit.DAYS.between(sessionDates.get(0), sessionDates.get(totalSessions - 1)) / 7.0);
        double sessionsPerWeek = totalSessions / spanWeeks;

        return new TrainingHistorySummary(
                windowStart,
                today,
                totalSessions,
                sessionsPerWeek,
                byExercise.size(),
                exercises
        );
    }

    private ExerciseHistory buildExerciseHistory(String name, List<ExerciseLog> sets) {
        Set<Long> sessionIds = new HashSet<>();
        int minReps = Integer.MAX_VALUE;
        int maxReps = Integer.MIN_VALUE;
        long repsSum = 0;
        BestSet bestSet = null;
        LocalDate firstSeen = null;
        LocalDate lastSeen = null;

        for (ExerciseLog log : sets) {
            sessionIds.add(log.getWorkoutSession().getId());

            int reps = log.getReps();
            minReps = Math.min(minReps, reps);
            maxReps = Math.max(maxReps, reps);
            repsSum += reps;

            double e1rm = epleyE1rm(log.getWeightKg(), reps);
            if (bestSet == null || e1rm > bestSet.e1rmKg()) {
                bestSet = new BestSet(log.getWeightKg(), reps, e1rm);
            }

            LocalDate day = log.getWorkoutSession().getStartedAt().toLocalDate();
            if (firstSeen == null || day.isBefore(firstSeen)) firstSeen = day;
            if (lastSeen == null || day.isAfter(lastSeen)) lastSeen = day;
        }

        return new ExerciseHistory(
                name,
                sessionIds.size(),
                sets.size(),
                minReps,
                maxReps,
                (double) repsSum / sets.size(),
                bestSet,
                bestSet.e1rmKg(),
                firstSeen,
                lastSeen,
                trend(sets)
        );
    }

    /**
     * Zwija każdą sesję do średniego e1RM, sortuje po dacie, porównuje średnią pierwszej
     * połowy sesji ze średnią drugiej połowy. < 2 sesje → INSUFFICIENT_DATA.
     */
    private Trend trend(List<ExerciseLog> sets) {
        record SessionStrength(LocalDateTime date, double e1rm) {}

        List<Double> e1rmByDate = sets.stream()
                .collect(Collectors.groupingBy(s -> s.getWorkoutSession().getId()))
                .values().stream()
                .map(sessionSets -> new SessionStrength(
                        sessionSets.get(0).getWorkoutSession().getStartedAt(),
                        sessionSets.stream()
                                .mapToDouble(x -> epleyE1rm(x.getWeightKg(), x.getReps()))
                                .average().orElse(0.0)))
                .sorted(Comparator.comparing(SessionStrength::date))
                .map(SessionStrength::e1rm)
                .toList();

        if (e1rmByDate.size() < 2) {
            return Trend.INSUFFICIENT_DATA;
        }

        int mid = e1rmByDate.size() / 2;
        double firstAvg = mean(e1rmByDate.subList(0, mid));
        double secondAvg = mean(e1rmByDate.subList(mid, e1rmByDate.size()));
        if (firstAvg == 0.0) {
            return Trend.INSUFFICIENT_DATA;
        }

        double change = (secondAvg - firstAvg) / firstAvg;
        if (change > TREND_THRESHOLD) return Trend.UP;
        if (change < -TREND_THRESHOLD) return Trend.DOWN;
        return Trend.FLAT;
    }

    private static double epleyE1rm(BigDecimal weightKg, int reps) {
        return weightKg.doubleValue() * (1.0 + reps / 30.0);
    }

    private static double mean(List<Double> xs) {
        return xs.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
}
