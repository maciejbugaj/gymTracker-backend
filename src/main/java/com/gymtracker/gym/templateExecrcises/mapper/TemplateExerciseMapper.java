package com.gymtracker.gym.templateExecrcises.mapper;

import com.gymtracker.gym.templateExecrcises.dto.TemplateExerciseResponse;
import com.gymtracker.gym.templateExecrcises.model.TemplateExercise;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TemplateExerciseMapper {
    TemplateExerciseResponse toResponse(TemplateExercise templateExercise);
}
