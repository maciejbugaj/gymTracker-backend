package com.gymtracker.gym.testsupport;

import org.awaitility.Awaitility;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IntegrationTestSupport {

    public static @NotNull Long createTemplateAs(MockMvc mockMvc , UUID sub, String email) throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/workout-templates")
                        .with(jwtFor(sub, email))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Push Day","description":"Chest, shoulders, triceps"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String location = createResult.getResponse().getHeader("Location");
        assertThat(location).isNotNull();
        return Long.parseLong(location.substring(location.lastIndexOf("/") + 1));
    }

    public static @NotNull Long createTemplateExerciseInWorkoutTemplate(MockMvc mockMvc,
                                                                        UUID sub,
                                                                        String email,
                                                                        Long workoutTemplateId) throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/template-exercises")
                        .with(jwtFor(sub, email))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"workoutTemplateId": %d,
                         "exerciseName": "Bench Press",
                          "defaultSets": 5,
                           "defaultReps": 12,
                           "defaultWeightKg": 120,
                           "sortOrder": 1}
                        """.formatted(workoutTemplateId)))
                .andExpect(status().isCreated())
                .andReturn();

        String location = createResult.getResponse().getHeader("Location");
        assertThat(location).isNotNull();
        return Long.parseLong(location.substring(location.lastIndexOf("/") + 1));
    }

    public static @NotNull Long createWorkoutSessionAs(MockMvc mockMvc, UUID sub, String email, Long workoutTemplateId) throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/workout-sessions")
                .with(jwtFor(sub, email))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"workoutTemplateId": %d}
                        """.formatted(workoutTemplateId)))
                .andExpect(status().isCreated())
                .andReturn();
        String location = createResult.getResponse().getHeader("Location");
        assertThat(location).isNotNull();
        return Long.parseLong(location.substring(location.lastIndexOf("/") + 1));
    }

    public static @NotNull Long startAiPlanGenerationAs(MockMvc mockMvc, UUID sub, String email) throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/ai-plans/generations")
                        .with(jwtFor(sub, email))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"goal":"STRENGTH","daysPerWeek":2,"durationWeeks":1,"sessionLengthMinutes":45}
                                """))
                .andExpect(status().isAccepted())
                .andReturn();

        String location = createResult.getResponse().getHeader("Location");
        assertThat(location).isNotNull();
        return Long.parseLong(location.substring(location.lastIndexOf("/") + 1));
    }

    /** Polls GET /generations/{id} until status is SUCCEEDED (the stub client resolves almost
     * immediately, but it still runs through the real @Async dispatch, so this isn't instant). */
    public static @NotNull MvcResult awaitGenerationSucceeded(MockMvc mockMvc, Long generationId, UUID sub, String email) {
        AtomicReference<MvcResult> lastResult = new AtomicReference<>();
        Awaitility.await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
            MvcResult result = mockMvc.perform(get("/api/ai-plans/generations/{id}", generationId).with(jwtFor(sub, email)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCEEDED"))
                    .andReturn();
            lastResult.set(result);
        });
        return lastResult.get();
    }

    public static SecurityMockMvcRequestPostProcessors.@NotNull JwtRequestPostProcessor jwtFor(UUID subB, String mail) {
        return jwt().jwt(jwt -> jwt
                .claim("sub", subB.toString())
                .claim("email", mail));
    }
}
