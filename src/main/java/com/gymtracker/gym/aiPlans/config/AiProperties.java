package com.gymtracker.gym.aiPlans.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "gymtracker.ai")
public record AiProperties(
        @DefaultValue("false") boolean enabled,
        String apiKey,
        @DefaultValue("claude-sonnet-5") String model,
        @DefaultValue("16000") int maxTokens,
        @DefaultValue("5") int dailyLimitPerUser,
        @DefaultValue("12") int historyWindowWeeks
) {
}
