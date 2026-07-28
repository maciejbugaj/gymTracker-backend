package com.gymtracker.gym.workoutTemplates.controller;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class WorkoutTemplateControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;


    @Test
    void userBCannotAccessUserAWorkoutTemplate() throws Exception {
        UUID subA = UUID.randomUUID();
        UUID subB = UUID.randomUUID();

        Long templateId = createTemplateAs(subA, "subA@gmail.com");

        mockMvc.perform(get("/api/workout-templates/{id}", templateId)
                        .with(jwtFor(subB, "b@test.com")))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnUnauthorizedWithoutToken() throws Exception{
        mockMvc.perform(get("/api/workout-templates"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userBCannotDeleteUserAWorkoutTemplate() throws Exception {
        UUID subA = UUID.randomUUID();
        UUID subB = UUID.randomUUID();
        
        Long templateId = createTemplateAs(subA, "subA@gmail.com");

        mockMvc.perform(delete("/api/workout-templates/{id}", templateId)
                .with(jwtFor(subB, "b@test.com")))
                .andExpect(status().isNotFound());
    }

    @Test
    void userBCannotReplaceUserAWorkoutTemplate() throws Exception {
        UUID subA = UUID.randomUUID();
        UUID subB = UUID.randomUUID();

        Long templateId = createTemplateAs(subA, "subA@gmail.com");

        mockMvc.perform(put("/api/workout-templates/{id}", templateId)
                        .with(jwtFor(subB, "b@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Pull Day","description":"Fingers, shoulders, triceps"}
                                """))
                .andExpect(status().isNotFound());
    }
    
    private @NotNull Long createTemplateAs(UUID sub, String email) throws Exception {
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

    private static SecurityMockMvcRequestPostProcessors.@NotNull JwtRequestPostProcessor jwtFor(UUID subB, String mail) {
        return jwt().jwt(jwt -> jwt
                .claim("sub", subB.toString())
                .claim("email", mail));
    }

}