package com.gymtracker.gym.exerciseLogs.mapper;

import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogResponse;
import com.gymtracker.gym.exerciseLogs.model.ExerciseLog;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ExerciseLogMapper {
    ExerciseLogResponse toResponse(ExerciseLog exerciseLog);
}
