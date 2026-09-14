package com.gymtracker.gym.trainingPrograms.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProgramDayExerciseRequest(
        @NotBlank String exerciseName,
        @NotNull Integer sortOrder,
        @NotNull @Min(1) Integer targetSets,
        @NotNull @Min(1) Integer targetRepsMin,
        @NotNull @Min(1) Integer targetRepsMax,
        Integer restSeconds,
        String notes
) {
}
