package com.gymtracker.gym.trainingPrograms.mapper;

import com.gymtracker.gym.trainingPrograms.dto.ProgramDayExerciseResponse;
import com.gymtracker.gym.trainingPrograms.model.ProgramDayExercise;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ProgramDayExerciseMapper {
    ProgramDayExerciseResponse toResponse(ProgramDayExercise exercise);
}
