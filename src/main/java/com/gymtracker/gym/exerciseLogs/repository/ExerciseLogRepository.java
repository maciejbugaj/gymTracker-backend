package com.gymtracker.gym.exerciseLogs.repository;

import com.gymtracker.gym.exerciseLogs.model.ExerciseLog;
import com.gymtracker.gym.workoutSessions.model.WorkoutSession;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
