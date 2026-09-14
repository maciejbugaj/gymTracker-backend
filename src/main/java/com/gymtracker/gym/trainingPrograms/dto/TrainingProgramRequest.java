package com.gymtracker.gym.trainingPrograms.dto;

import com.gymtracker.gym.trainingPrograms.model.ProgramGoal;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * The full, possibly user-edited program tree. Used both as the body of
 * POST /from-generation/{id} (the reviewed/edited AI output being saved for the first time)
 * and PUT /{id} (replacing an existing program's tree).
 */
public record TrainingProgramRequest(
        @NotBlank String name,
        String description,
        @NotNull ProgramGoal goal,
        String experienceLevel,
        @NotNull @Min(1) Integer durationWeeks,
        @NotNull @Min(1) Integer daysPerWeek,
        @NotEmpty @Valid List<ProgramWeekRequest> weeks
) {
}
