package com.gymtracker.gym.workoutSessions.mapper;

import com.gymtracker.gym.exerciseLogs.mapper.ExerciseLogMappingsKt;
import com.gymtracker.gym.trainingPrograms.mapper.ProgramDayExerciseMapper;
import com.gymtracker.gym.workoutSessions.dto.WorkoutSessionResponse;
import com.gymtracker.gym.workoutSessions.model.WorkoutSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ExerciseLogMappingsKt.class, ProgramDayExerciseMapper.class})
public interface WorkoutSessionMapper {

    @Mapping(source = "workoutTemplate.id", target = "workoutTemplateId")
    @Mapping(source = "workoutTemplate.name", target = "workoutTemplateName")
    @Mapping(source = "programDay.id", target = "programDayId")
    @Mapping(source = "programDay.programWeek.trainingProgram.name", target = "programName")
    @Mapping(source = "programDay.programWeek.weekNumber", target = "weekNumber")
    @Mapping(source = "programDay.name", target = "dayName")
    @Mapping(source = "programDay.programWeek.deload", target = "isDeload")
    @Mapping(source = "programDay.programDayExercises", target = "prescribedExercises")
    WorkoutSessionResponse toResponse(WorkoutSession workoutSession);
}
