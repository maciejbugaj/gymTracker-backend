package com.gymtracker.gym.workoutTemplates.repository;

import com.gymtracker.gym.workoutTemplates.model.WorkoutTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkoutTemplateRepository extends JpaRepository<WorkoutTemplate, Long> {
}
