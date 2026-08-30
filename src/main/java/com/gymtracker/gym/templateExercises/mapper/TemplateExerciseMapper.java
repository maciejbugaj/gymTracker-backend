package com.gymtracker.gym.templateExercises.mapper;

import com.gymtracker.gym.templateExercises.dto.TemplateExerciseResponse;
import com.gymtracker.gym.templateExercises.model.TemplateExercise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TemplateExerciseMapper {
    @Mapping(source = "defaultWeightKg", target = "defaultWeight")
    TemplateExerciseResponse toResponse(TemplateExercise templateExercise);
}
