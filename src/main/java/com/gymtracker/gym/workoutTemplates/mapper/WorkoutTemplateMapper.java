package com.gymtracker.gym.workoutTemplates.mapper;

import com.gymtracker.gym.templateExercises.mapper.TemplateExerciseMappingsKt;
import com.gymtracker.gym.workoutTemplates.dto.WorkoutTemplateResponse;
import com.gymtracker.gym.workoutTemplates.model.WorkoutTemplate;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = TemplateExerciseMappingsKt.class)
public interface WorkoutTemplateMapper {
    WorkoutTemplateResponse toResponse(WorkoutTemplate workoutTemplate);
}
