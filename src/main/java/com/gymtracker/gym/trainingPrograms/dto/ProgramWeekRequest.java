package com.gymtracker.gym.trainingPrograms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProgramWeekRequest(
        @NotNull Integer weekNumber,
        String focus,
        boolean isDeload,
        String notes,
        @NotEmpty @Valid List<ProgramDayRequest> days
) {
}
