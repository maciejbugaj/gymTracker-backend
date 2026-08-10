package com.gymtracker.gym.testsupport;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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




    public static SecurityMockMvcRequestPostProcessors.@NotNull JwtRequestPostProcessor jwtFor(UUID subB, String mail) {
        return jwt().jwt(jwt -> jwt
                .claim("sub", subB.toString())
                .claim("email", mail));
    }
}
