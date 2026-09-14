package com.gymtracker.gym.trainingPrograms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProgramDayRequest(
        @NotNull Integer dayNumber,
        String name,
        String notes,
        @NotEmpty @Valid List<ProgramDayExerciseRequest> exercises
) {
}
