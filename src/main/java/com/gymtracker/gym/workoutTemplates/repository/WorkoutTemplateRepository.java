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

    @Query("SELECT wt FROM WorkoutTemplate wt LEFT JOIN FETCH wt.exercises WHERE wt.userId = :userId")
    List<WorkoutTemplate> findAllWithExercises(@Param("userId") Long userId);

    @Query("SELECT wt FROM WorkoutTemplate wt LEFT JOIN FETCH wt.exercises WHERE wt.id = :workoutTemplateId AND wt.userId = :userId")
    Optional<WorkoutTemplate> findByIdWithExercises(@Param("workoutTemplateId") Long workoutTemplateId, @Param("userId") Long userId);

    boolean existsByIdAndUserId(Long workoutTemplateId, Long userId);

    void deleteByIdAndUserId(Long workoutTemplateId, Long userId);

    Optional<WorkoutTemplate> findByIdAndUserId(Long workoutTemplateId, Long userId);
}
