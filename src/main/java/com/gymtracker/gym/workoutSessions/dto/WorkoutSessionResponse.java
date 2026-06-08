package com.gymtracker.gym.workoutSessions.dto;

import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogResponse;

import java.time.LocalDateTime;
import java.util.List;

public record WorkoutSessionResponse(
        Long id,
        Long workoutTemplateId,
        String workoutTemplateName,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        Integer durationSeconds,
        String notes,
        List<ExerciseLogResponse> exerciseLogs
) {}
