package com.gymtracker.gym.aiPlans.dto;

import jakarta.validation.constraints.NotBlank;

public record RegeneratePlanRequest(
        @NotBlank String feedback
) {
}
