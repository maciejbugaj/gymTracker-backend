package com.gymtracker.gym.templateExecrcises.dto;

import java.math.BigDecimal;

public record TemplateExerciseResponse(
        Long id,
        String exerciseName,
        Integer defaultSets,
        Integer defaultReps,
        BigDecimal defaultWeight
) {}
