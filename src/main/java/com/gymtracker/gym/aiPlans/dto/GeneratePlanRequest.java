package com.gymtracker.gym.aiPlans.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record GeneratePlanRequest(
        @NotBlank String goal,
        String experienceLevel,
        @Min(1) @Max(7) int daysPerWeek,
        @Min(1) @Max(52) int durationWeeks,
        @Min(10) @Max(240) int sessionLengthMinutes,
        List<String> equipment,
        String splitPreference,
        List<String> focusMuscleGroups,
        String exclusionsOrInjuries,
        String notes
) {
}
