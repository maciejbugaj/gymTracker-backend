package com.gymtracker.gym.aiPlans.dto;

import com.gymtracker.gym.aiPlans.generated.GeneratedProgram;
import com.gymtracker.gym.aiPlans.model.AiPlanGenerationStatus;

import java.time.LocalDateTime;

public record AiPlanGenerationResponse(
        Long id,
        AiPlanGenerationStatus status,
        GeneratedProgram program,
        String errorMessage,
        Integer inputTokens,
        Integer outputTokens,
        LocalDateTime createdAt
) {
}
