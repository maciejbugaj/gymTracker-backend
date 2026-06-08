package com.gymtracker.gym.workoutSessions.mapper;

import com.gymtracker.gym.exerciseLogs.mapper.ExerciseLogMapper;
import com.gymtracker.gym.workoutSessions.dto.WorkoutSessionResponse;
import com.gymtracker.gym.workoutSessions.model.WorkoutSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ExerciseLogMapper.class)
public interface WorkoutSessionMapper {
    @Mapping(source = "workoutTemplate.id", target = "workoutTemplateId")
    @Mapping(source = "workoutTemplate.name", target = "workoutTemplateName")
    WorkoutSessionResponse toResponse(WorkoutSession workoutSession);
}
