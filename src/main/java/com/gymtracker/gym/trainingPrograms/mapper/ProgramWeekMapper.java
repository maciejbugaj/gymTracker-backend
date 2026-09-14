package com.gymtracker.gym.trainingPrograms.mapper;

import com.gymtracker.gym.trainingPrograms.dto.ProgramWeekResponse;
import com.gymtracker.gym.trainingPrograms.model.ProgramWeek;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ProgramDayMapper.class)
public interface ProgramWeekMapper {
    // "deload" is the entity property (Lombok getter isDeload() -> property "deload" by JavaBean
    // convention), but the response record's component is literally named "isDeload" - the names
    // don't auto-match, so it needs an explicit @Mapping.
    @Mapping(source = "deload", target = "isDeload")
    @Mapping(source = "programDays", target = "days")
    ProgramWeekResponse toResponse(ProgramWeek week);
}
