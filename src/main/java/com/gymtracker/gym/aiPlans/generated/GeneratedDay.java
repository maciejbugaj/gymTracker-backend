package com.gymtracker.gym.aiPlans.generated;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record GeneratedDay(
        @JsonPropertyDescription("1-indexed day number within the week")
        int dayNumber,

        @JsonPropertyDescription("Short name for this training day, e.g. 'Upper Body Push', 'Lower Body Squat Focus'")
        String name,

        @JsonPropertyDescription("Optional notes for this day, e.g. warm-up guidance")
        String notes,

        @JsonPropertyDescription("Exercises for this day, in the order they should be performed")
        List<GeneratedExercise> exercises
) {
}
