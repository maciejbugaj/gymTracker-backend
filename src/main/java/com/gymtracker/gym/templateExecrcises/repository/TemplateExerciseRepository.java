package com.gymtracker.gym.templateExecrcises.repository;

import com.gymtracker.gym.templateExecrcises.model.TemplateExercise;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@NullMarked
@Repository
public interface TemplateExerciseRepository extends JpaRepository<TemplateExercise, Long> {

    Optional<TemplateExercise> findByIdAndWorkoutTemplate_UserId(Long id, Long userId);

    boolean existsByIdAndWorkoutTemplate_UserId(Long id, Long userId);

    void deleteByIdAndWorkoutTemplate_UserId(Long id, Long userId);


}
