package com.gymtracker.gym.exerciseLogs.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExerciseLogResponse(
        Long id,
        String exerciseName,
        Integer setNumber,
        Integer reps,
        BigDecimal weightKg,
        LocalDateTime loggedAt
) {}
