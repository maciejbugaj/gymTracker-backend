package com.gymtracker.gym.workoutSessions.dto;

import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogResponse;
import com.gymtracker.gym.trainingPrograms.dto.ProgramDayExerciseResponse;

import java.time.LocalDateTime;
import java.util.List;

public record WorkoutSessionResponse(
        Long id,
        Long workoutTemplateId,
        String workoutTemplateName,
        Long programDayId,
        String programName,
        Integer weekNumber,
        String dayName,
        Boolean isDeload,
        List<ProgramDayExerciseResponse> prescribedExercises,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Integer durationSeconds,
        String notes,
        List<ExerciseLogResponse> exerciseLogs
) {}
