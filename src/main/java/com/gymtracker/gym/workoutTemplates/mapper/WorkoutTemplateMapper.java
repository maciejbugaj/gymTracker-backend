package com.gymtracker.gym.workoutTemplates.mapper;

import com.gymtracker.gym.templateExecrcises.mapper.TemplateExerciseMapper;
import com.gymtracker.gym.workoutTemplates.dto.WorkoutTemplateResponse;
import com.gymtracker.gym.workoutTemplates.model.WorkoutTemplate;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = TemplateExerciseMapper.class)
public interface WorkoutTemplateMapper {
    WorkoutTemplateResponse toResponse(WorkoutTemplate workoutTemplate);
}
