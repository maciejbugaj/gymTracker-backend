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

    @Query("""
        SELECT e FROM ExerciseLog e
        JOIN FETCH e.workoutSession s
        WHERE s.userId = :userId
          AND s.endedAt IS NOT NULL
          AND s.startedAt >= :since
        ORDER BY e.exerciseName ASC, s.startedAt ASC, e.setNumber ASC
        """)
    List<ExerciseLog> findCompletedSetsForUserSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    // For "previous" on a program day: the most recent set ever logged for this exercise name,
    // regardless of which session/template it came from (unlike findLatestBySessionAndExerciseName,
    // which is scoped to one specific session).
    @Query("""
            SELECT e FROM ExerciseLog e
            WHERE e.workoutSession.userId = :userId AND e.exerciseName = :exerciseName
            ORDER BY e.loggedAt DESC
            LIMIT 1
            """)
    Optional<ExerciseLog> findLatestByUserIdAndExerciseName(@Param("userId") Long userId,
                                                             @Param("exerciseName") String exerciseName);
}
