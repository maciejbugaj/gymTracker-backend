package com.gymtracker.gym.templateExecrcises.repository;

import com.gymtracker.gym.templateExecrcises.model.TemplateExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateExerciseRepository extends JpaRepository<TemplateExercise, Long> {


}
