package com.gymtracker.gym.templateExecrcises.repository;

import com.gymtracker.gym.templateExecrcises.model.TemplateExercise;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@NullMarked
@Repository
public interface TemplateExerciseRepository extends JpaRepository<TemplateExercise, Long> {


}
