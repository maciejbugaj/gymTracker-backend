package com.gymtracker.gym.aiPlans.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AnthropicClientConfig {

    @Bean
    @ConditionalOnProperty(name = "gymtracker.ai.enabled", havingValue = "true")
    public AnthropicClient anthropicClient(AiProperties aiProperties) {
        return AnthropicOkHttpClient.builder()
                .apiKey(aiProperties.apiKey())
                .build();
    }
}
