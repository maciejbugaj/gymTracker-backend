package com.gymtracker.gym.aiPlans.history;

import com.gymtracker.gym.aiPlans.history.TrainingHistorySummary.ExerciseHistory;
import com.gymtracker.gym.aiPlans.history.TrainingHistorySummary.Trend;
import com.gymtracker.gym.exerciseLogs.model.ExerciseLog;
import com.gymtracker.gym.exerciseLogs.repository.ExerciseLogRepository;
import com.gymtracker.gym.workoutSessions.model.WorkoutSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingHistorySummaryServiceTest {

    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-09-09T00:00:01Z"), ZoneId.of("Europe/Warsaw"));
    private static final LocalDate TODAY = LocalDate.now(CLOCK);
    private static final long USER_ID = 1L;
    private static final int WINDOW_WEEKS = 8;

    @Mock
    ExerciseLogRepository exerciseLogRepository;

    TrainingHistorySummaryService service;

    @BeforeEach
    void setUp() {
        service = new TrainingHistorySummaryService(exerciseLogRepository, CLOCK);
    }

    @Test
    void shouldReturnEmptySummaryWhenNoSets() {
        givenCompletedSets(List.of());

        TrainingHistorySummary summary = service.summarize(USER_ID, WINDOW_WEEKS);

        assertThat(summary.windowStart()).isEqualTo(TODAY.minusWeeks(WINDOW_WEEKS));
        assertThat(summary.windowEnd()).isEqualTo(TODAY);
        assertThat(summary.totalSessions()).isZero();
        assertThat(summary.sessionsPerWeek()).isZero();
        assertThat(summary.distinctExercises()).isZero();
        assertThat(summary.exercises()).isEmpty();
    }

    @Test
    void shouldReportInsufficientDataTrendForSingleSession() {
        givenCompletedSets(List.of(set(1L, TODAY.minusWeeks(2), "Bench Press", 5, 100.0)));

        ExerciseHistory bench = service.summarize(USER_ID, WINDOW_WEEKS).exercises().getFirst();

        assertThat(bench.estimatedOneRepMaxKg()).isEqualTo(116.667, within(0.01));
        assertThat(bench.bestSet().e1rmKg()).isEqualTo(116.667, within(0.01));
        assertThat(bench.trend()).isEqualTo(Trend.INSUFFICIENT_DATA);
    }

    @ParameterizedTest
    @MethodSource("trendCases")
    void shouldComputeTrendFromFirstVsSecondHalfOfSessions(double firstSessionKg, double secondSessionKg, Trend expected) {
        givenCompletedSets(List.of(
                set(1L, TODAY.minusWeeks(2), "Bench Press", 5, firstSessionKg),
                set(2L, TODAY.minusWeeks(1), "Bench Press", 5, secondSessionKg)
        ));

        ExerciseHistory bench = service.summarize(USER_ID, WINDOW_WEEKS).exercises().getFirst();

        assertThat(bench.trend()).isEqualTo(expected);
    }

    static Stream<Arguments> trendCases() {
        return Stream.of(
                Arguments.of(100.0, 105.0, Trend.UP),
                Arguments.of(100.0, 95.0, Trend.DOWN),
                Arguments.of(100.0, 100.0, Trend.FLAT)
        );
    }

    @Test
    void shouldComputeSessionsPerWeekOverSpanBetweenFirstAndLastSession() {
        givenCompletedSets(List.of(
                set(1L, TODAY.minusDays(21), "Bench Press", 5, 100.0),
                set(2L, TODAY.minusDays(17), "Bench Press", 5, 100.0),
                set(3L, TODAY.minusDays(13), "Bench Press", 5, 100.0),
                set(4L, TODAY.minusDays(9), "Bench Press", 5, 100.0),
                set(5L, TODAY.minusDays(5), "Bench Press", 5, 100.0),
                set(6L, TODAY, "Bench Press", 5, 100.0)
        ));

        TrainingHistorySummary summary = service.summarize(USER_ID, WINDOW_WEEKS);

        assertThat(summary.totalSessions()).isEqualTo(6);
        assertThat(summary.sessionsPerWeek()).isEqualTo(2.0, within(1e-9));
    }

    @Test
    void shouldClampSessionsPerWeekDenominatorToOneWeek() {
        givenCompletedSets(List.of(
                set(1L, TODAY, "Bench Press", 5, 100.0),
                set(2L, TODAY, "Bench Press", 5, 100.0),
                set(3L, TODAY, "Bench Press", 5, 100.0)
        ));

        TrainingHistorySummary summary = service.summarize(USER_ID, WINDOW_WEEKS);

        assertThat(summary.totalSessions()).isEqualTo(3);
        assertThat(summary.sessionsPerWeek()).isEqualTo(3.0, within(1e-9));
    }

    @Test
    void shouldIgnoreSetsWithNonPositiveReps_nullReps_orNullWeight() {
        LocalDate d1 = TODAY.minusDays(14);
        LocalDate d2 = TODAY.minusDays(7);
        givenCompletedSets(List.of(
                set(10L, d1, "Squat", 5, 100.0),
                set(10L, d1, "Squat", 8, 100.0),
                set(11L, d2, "Squat", 8, 105.0),
                set(11L, d2, "Squat", 10, 105.0),
                set(11L, d2, "Squat", 0, 105.0),
                set(11L, d2, "Squat", null, 105.0),
                set(11L, d2, "Squat", 6, null)
        ));

        ExerciseHistory squat = service.summarize(USER_ID, WINDOW_WEEKS).exercises().getFirst();

        assertThat(squat.exerciseName()).isEqualTo("Squat");
        assertThat(squat.totalSets()).isEqualTo(4);
        assertThat(squat.sessionCount()).isEqualTo(2);
        assertThat(squat.minReps()).isEqualTo(5);
        assertThat(squat.maxReps()).isEqualTo(10);
        assertThat(squat.avgReps()).isEqualTo(7.75, within(1e-9));
        assertThat(squat.firstSeen()).isEqualTo(d1);
        assertThat(squat.lastSeen()).isEqualTo(d2);
        assertThat(squat.bestSet().weightKg()).isEqualByComparingTo(BigDecimal.valueOf(105));
        assertThat(squat.bestSet().reps()).isEqualTo(10);
        assertThat(squat.bestSet().e1rmKg()).isEqualTo(140.0, within(1e-9));
        assertThat(squat.estimatedOneRepMaxKg()).isEqualTo(140.0, within(1e-9));
    }

    @Test
    void shouldOrderExercisesByTotalSetsDescending() {
        givenCompletedSets(List.of(
                set(20L, TODAY.minusDays(3), "Deadlift", 5, 140.0),
                set(21L, TODAY.minusDays(2), "Barbell Row", 8, 70.0),
                set(21L, TODAY.minusDays(2), "Barbell Row", 8, 70.0),
                set(21L, TODAY.minusDays(2), "Barbell Row", 8, 70.0)
        ));

        TrainingHistorySummary summary = service.summarize(USER_ID, WINDOW_WEEKS);

        assertThat(summary.distinctExercises()).isEqualTo(2);
        assertThat(summary.exercises())
                .extracting(ExerciseHistory::exerciseName)
                .containsExactly("Barbell Row", "Deadlift");
    }

    private void givenCompletedSets(List<ExerciseLog> sets) {
        LocalDate windowStart = TODAY.minusWeeks(WINDOW_WEEKS);
        when(exerciseLogRepository.findCompletedSetsForUserSince(USER_ID, windowStart.atStartOfDay()))
                .thenReturn(sets);
    }

    private static ExerciseLog set(long sessionId, LocalDate sessionDate, String exercise, Integer reps, Double weightKg) {
        WorkoutSession session = new WorkoutSession();
        session.setId(sessionId);
        session.setStartedAt(sessionDate.atTime(10, 0));
        BigDecimal weight = weightKg == null ? null : BigDecimal.valueOf(weightKg);
        return new ExerciseLog(null, session, exercise, 1, reps, weight, session.getStartedAt());
    }
}
