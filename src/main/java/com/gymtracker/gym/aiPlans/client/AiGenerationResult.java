package com.gymtracker.gym.aiPlans.client;

import com.gymtracker.gym.aiPlans.generated.GeneratedProgram;

public record AiGenerationResult(GeneratedProgram program, int inputTokens, int outputTokens) {
}
