package com.gymtracker.gym.aiPlans.generated;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public record GeneratedExercise(
        @JsonPropertyDescription("Name of the exercise, e.g. 'Barbell Back Squat'")
        String exerciseName,

        @JsonPropertyDescription("0-indexed position of this exercise within the day")
        int sortOrder,

        @JsonPropertyDescription("Target number of working sets")
        int targetSets,

        @JsonPropertyDescription("Lower bound of the target rep range")
        int targetRepsMin,

        @JsonPropertyDescription("Upper bound of the target rep range")
        int targetRepsMax,

        @JsonPropertyDescription("Suggested rest time between sets, in seconds")
        int restSeconds,

        @JsonPropertyDescription("Progression guidance for this exercise, e.g. 'Add 2.5kg once you hit the top " +
                "of the rep range on all sets'. Never mention RPE.")
        String notes
) {
}
