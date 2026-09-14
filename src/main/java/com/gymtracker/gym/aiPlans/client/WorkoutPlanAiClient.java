package com.gymtracker.gym.aiPlans.client;

import com.gymtracker.gym.aiPlans.dto.GeneratePlanRequest;
import com.gymtracker.gym.aiPlans.generated.GeneratedProgram;
import com.gymtracker.gym.aiPlans.history.TrainingHistorySummary;

public interface WorkoutPlanAiClient {

    AiGenerationResult generate(GeneratePlanRequest request, TrainingHistorySummary history);

    AiGenerationResult regenerate(GeneratePlanRequest request, TrainingHistorySummary history,
                                   GeneratedProgram previousProgram, String feedback);
}
