package com.gymtracker.gym.workoutSessions.repository;

import com.gymtracker.gym.workoutSessions.model.WorkoutSession;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@NullMarked
@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

    Optional<WorkoutSession> findTopByEndedAtIsNullOrderByStartedAtDesc();

    Optional<WorkoutSession> findTopByEndedAtIsNotNullOrderByEndedAtDesc();

    List<WorkoutSession> findAllByOrderByStartedAtDesc();

    Optional<WorkoutSession> findTopByWorkoutTemplateIdAndEndedAtIsNotNullOrderByEndedAtDesc(Long workoutTemplateId);

    @Query("SELECT s FROM WorkoutSession s LEFT JOIN FETCH s.exerciseLogs WHERE s.endedAt IS NULL ORDER BY s.startedAt DESC LIMIT 1")
    Optional<WorkoutSession> findOngoingSessionWithLogs();
}

