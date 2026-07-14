package com.gymtracker.gym.workoutTemplates.repository;

import com.gymtracker.gym.workoutTemplates.model.WorkoutTemplate;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@NullMarked
@Repository
public interface WorkoutTemplateRepository extends JpaRepository<WorkoutTemplate, Long> {

    @Query("SELECT wt FROM WorkoutTemplate wt LEFT JOIN FETCH wt.exercises")
    List<WorkoutTemplate> findAllWithExercises();

    @Query("SELECT wt FROM WorkoutTemplate wt LEFT JOIN FETCH wt.exercises WHERE wt.id = :workoutTemplateId")
    Optional<WorkoutTemplate> findByIdWithExercises(@Param("workoutTemplateId") Long workoutTemplateId);

}
