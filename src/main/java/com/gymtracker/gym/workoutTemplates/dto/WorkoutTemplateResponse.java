package com.gymtracker.gym.workoutTemplates.dto;

import com.gymtracker.gym.templateExercises.dto.TemplateExerciseResponse;

import java.util.List;

public record WorkoutTemplateResponse(
        Long id,
        String name,
        String description,
        List<TemplateExerciseResponse> exercises
) {}
