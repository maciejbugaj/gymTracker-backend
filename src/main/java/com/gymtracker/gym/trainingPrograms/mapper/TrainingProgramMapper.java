package com.gymtracker.gym.trainingPrograms.mapper;

import com.gymtracker.gym.trainingPrograms.dto.TrainingProgramResponse;
import com.gymtracker.gym.trainingPrograms.dto.TrainingProgramSummaryResponse;
import com.gymtracker.gym.trainingPrograms.model.TrainingProgram;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ProgramWeekMapper.class)
public interface TrainingProgramMapper {

    @Mapping(source = "programWeeks", target = "weeks")
    TrainingProgramResponse toResponse(TrainingProgram program);

    // Flat fields only, same names on both sides -> no @Mapping needed.
    TrainingProgramSummaryResponse toSummaryResponse(TrainingProgram program);
}
