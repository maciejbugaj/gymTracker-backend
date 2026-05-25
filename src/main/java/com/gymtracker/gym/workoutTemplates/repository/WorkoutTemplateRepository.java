package com.gymtracker.gym.workoutTemplates.repository;

import com.gymtracker.gym.workoutTemplates.model.WorkoutTemplate;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@NullMarked
@Repository
public interface WorkoutTemplateRepository extends JpaRepository<WorkoutTemplate, Long> {
}
