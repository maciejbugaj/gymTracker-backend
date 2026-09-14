package com.gymtracker.gym.aiPlans.dto;

import java.util.List;

/**
 * Placeholder shape — Etap 4 will likely tighten this (enums for goal/splitPreference,
 * Bean Validation annotations) once the generator form/controller are built.
 */
public record GeneratePlanRequest(
        String goal,
        String experienceLevel,
        int daysPerWeek,
        int durationWeeks,
        int sessionLengthMinutes,
        List<String> equipment,
        String splitPreference,
        List<String> focusMuscleGroups,
        String exclusionsOrInjuries,
        String notes
) {
}
