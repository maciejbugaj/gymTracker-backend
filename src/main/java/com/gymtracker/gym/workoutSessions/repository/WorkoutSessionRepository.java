package com.gymtracker.gym.workoutSessions.repository;

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
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

    @Query("SELECT s FROM WorkoutSession s LEFT JOIN FETCH s.exerciseLogs LEFT JOIN FETCH s.workoutTemplate WHERE s.userId = :userId ORDER BY s.startedAt DESC")
    List<WorkoutSession> findAllByOrderByStartedAtDesc(@Param("userId") Long userId);

    Optional<WorkoutSession> findTopByWorkoutTemplateIdAndEndedAtIsNotNullOrderByEndedAtDesc(Long workoutTemplateId);

    @Query("SELECT s.id FROM WorkoutSession s WHERE s.endedAt IS NOT NULL AND s.userId = :userId ORDER BY s.endedAt DESC LIMIT 1")
    Optional<Long> findTopIdByEndedAtIsNotNullOrderByEndedAtDesc(@Param("userId") Long userId);

    @Query("SELECT s.id FROM WorkoutSession s WHERE s.endedAt IS NULL AND s.userId = :userId ORDER BY s.startedAt DESC LIMIT 1")
    Optional<Long> findTopIdByEndedAtIsNullOrderByStartedAtDesc(@Param("userId") Long userId);

    @Query("SELECT s FROM WorkoutSession s LEFT JOIN FETCH s.exerciseLogs LEFT JOIN FETCH s.workoutTemplate WHERE s.id = :id AND s.userId = :userId")
    Optional<WorkoutSession> findByIdWithExerciseLogsAndTemplate(@Param("id") Long id, @Param("userId") Long userId);
}

