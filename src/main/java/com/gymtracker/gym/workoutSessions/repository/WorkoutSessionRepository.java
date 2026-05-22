package com.gymtracker.gym.workoutSessions.repository;

import com.gymtracker.gym.workoutSessions.model.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, Long> {

    Optional<WorkoutSession> findTopByEndedAtIsNullOrderByStartedAtDesc();

    WorkoutSession findTopByEndedAtIsNotNullOrderByEndedAtDesc();

    @Transactional
    List<WorkoutSession> findAllByOrderByStartedAtDesc();

    Optional<WorkoutSession> findTopByWorkoutTemplateIdAndEndedAtIsNotNullOrderByEndedAtDesc(Long workoutTemplateId);
}

