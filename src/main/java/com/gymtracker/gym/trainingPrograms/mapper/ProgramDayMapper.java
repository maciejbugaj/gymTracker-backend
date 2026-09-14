package com.gymtracker.gym.trainingPrograms.mapper;

import com.gymtracker.gym.trainingPrograms.dto.ProgramDayResponse;
import com.gymtracker.gym.trainingPrograms.model.ProgramDay;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ProgramDayExerciseMapper.class)
public interface ProgramDayMapper {
    @Mapping(source = "programDayExercises", target = "exercises")
    ProgramDayResponse toResponse(ProgramDay day);
}
