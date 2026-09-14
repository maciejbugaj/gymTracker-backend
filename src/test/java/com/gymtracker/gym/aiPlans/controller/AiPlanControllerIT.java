package com.gymtracker.gym.aiPlans.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static com.gymtracker.gym.testsupport.IntegrationTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// gymtracker.ai.enabled=false wires the deterministic StubWorkoutPlanAiClient — no real Claude
// call, no API key needed, no cost. This overrides application-dev.yml's enabled=true, which
// would otherwise apply here too since IT tests inherit the active "dev" profile.
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = "gymtracker.ai.enabled=false")
class AiPlanControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    MockMvc mockMvc;

    @Test
    void generationGoesFromPendingToSucceeded() throws Exception {
        UUID sub = UUID.randomUUID();
        String email = "genuser@gmail.com";

        Long generationId = startAiPlanGenerationAs(mockMvc, sub, email);

        MvcResult succeeded = awaitGenerationSucceeded(mockMvc, generationId, sub, email);
        assertThat(succeeded.getResponse().getContentAsString()).contains("\"weeks\"");
    }

    @Test
    void listGenerationsReturnsUsersOwnHistory() throws Exception {
        UUID sub = UUID.randomUUID();
        String email = "historyuser@gmail.com";

        Long generationId = startAiPlanGenerationAs(mockMvc, sub, email);
        awaitGenerationSucceeded(mockMvc, generationId, sub, email);

        mockMvc.perform(get("/api/ai-plans/generations").with(jwtFor(sub, email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(generationId));
    }

    @Test
    void startGenerationRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/ai-plans/generations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"goal":"STRENGTH","daysPerWeek":2,"durationWeeks":1,"sessionLengthMinutes":45}
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userBCannotSeeUserAGeneration() throws Exception {
        UUID subA = UUID.randomUUID();
        UUID subB = UUID.randomUUID();

        Long generationId = startAiPlanGenerationAs(mockMvc, subA, "suba@gmail.com");

        mockMvc.perform(get("/api/ai-plans/generations/{id}", generationId).with(jwtFor(subB, "subb@gmail.com")))
                .andExpect(status().isNotFound());
    }
}
