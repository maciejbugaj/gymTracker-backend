package com.gymtracker.gym.aiPlans.client;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.gymtracker.gym.aiPlans.config.AiProperties;
import com.gymtracker.gym.aiPlans.dto.GeneratePlanRequest;
import com.gymtracker.gym.aiPlans.generated.GeneratedProgram;
import com.gymtracker.gym.aiPlans.history.TrainingHistorySummary;
import com.gymtracker.gym.aiPlans.prompt.WorkoutPlanPromptBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "gymtracker.ai.enabled", havingValue = "true")
@RequiredArgsConstructor
public class AnthropicWorkoutPlanAiClient implements WorkoutPlanAiClient {

    private final AnthropicClient anthropicClient;
    private final AiProperties aiProperties;
    private final WorkoutPlanPromptBuilder promptBuilder;

    @Override
    public AiGenerationResult generate(GeneratePlanRequest request, TrainingHistorySummary history) {
        return call(promptBuilder.buildUserPrompt(request, history));
    }

    @Override
    public AiGenerationResult regenerate(GeneratePlanRequest request, TrainingHistorySummary history,
                                          GeneratedProgram previousProgram, String feedback) {
        return call(promptBuilder.buildRegenerationUserPrompt(request, history, previousProgram, feedback));
    }

    private AiGenerationResult call(String userPrompt) {
        StructuredMessageCreateParams<GeneratedProgram> params = MessageCreateParams.builder()
                .model(aiProperties.model())
                .maxTokens((long) aiProperties.maxTokens())
                .outputConfig(GeneratedProgram.class)
                .system(promptBuilder.buildSystemPrompt())
                .addUserMessage(userPrompt)
                .build();

        var response = anthropicClient.messages().create(params);

        GeneratedProgram program = response.content().stream()
                .flatMap(block -> block.text().stream())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Claude response had no structured text block"))
                .text();

        return new AiGenerationResult(
                program,
                (int) response.usage().inputTokens(),
                (int) response.usage().outputTokens()
        );
    }
}
