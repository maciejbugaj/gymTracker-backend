package com.gymtracker.gym.exerciseLogs.repository;

import com.gymtracker.gym.exerciseLogs.model.ExerciseLog;
import com.gymtracker.gym.workoutSessions.model.WorkoutSession;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@NullMarked
@Repository
public interface ExerciseLogRepository extends JpaRepository<ExerciseLog, Long> {

    List<ExerciseLog> findAllByWorkoutSession(WorkoutSession workoutSession);

    @Query("""
            SELECT e FROM ExerciseLog e
            WHERE e.workoutSession = :session
            AND e.exerciseName = :exerciseName
            ORDER BY e.setNumber DESC
            LIMIT 1
            """)
    Optional<ExerciseLog> findLatestBySessionAndExerciseName(@Param("session")WorkoutSession session, @Param("exerciseName") String exerciseName);

    List<ExerciseLog> findByWorkoutSessionOrderBySetNumberAsc(WorkoutSession workoutSession);

    List<ExerciseLog> findByWorkoutSessionAndExerciseNameOrderBySetNumberAsc(WorkoutSession workoutSession,
                                                                            String exerciseName);

    @Query("""
        SELECT e FROM ExerciseLog e
        JOIN FETCH e.workoutSession s
        WHERE s.userId = :userId
          AND s.endedAt IS NOT NULL
          AND s.startedAt >= :since
        ORDER BY e.exerciseName ASC, s.startedAt ASC, e.setNumber ASC
        """)
    List<ExerciseLog> findCompletedSetsForUserSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    // For "previous" on a program day: the most recent session in which this user logged this
    // exercise name at all, regardless of which template/program day it came from.
    @Query("""
            SELECT e.workoutSession.id FROM ExerciseLog e
            WHERE e.workoutSession.userId = :userId
              AND e.exerciseName = :exerciseName
              AND e.workoutSession.endedAt IS NOT NULL
            ORDER BY e.loggedAt DESC
            LIMIT 1
            """)
    Optional<Long> findLatestSessionIdByUserIdAndExerciseName(@Param("userId") Long userId,
                                                              @Param("exerciseName") String exerciseName);

    // ...and every set of that exercise from that one session, so the set table can show a
    // per-row "previous" instead of a single last set.
    List<ExerciseLog> findByWorkoutSessionIdAndExerciseNameOrderBySetNumberAsc(Long workoutSessionId,
                                                                              String exerciseName);
}
