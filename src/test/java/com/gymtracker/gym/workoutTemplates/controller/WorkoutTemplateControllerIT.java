package com.gymtracker.gym.workoutTemplates.controller;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static com.gymtracker.gym.testsupport.IntegrationTestSupport.createTemplateAs;
import static com.gymtracker.gym.testsupport.IntegrationTestSupport.jwtFor;
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

        Long templateId = createTemplateAs(mockMvc, subA, "subA@gmail.com");

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
        
        Long templateId = createTemplateAs(mockMvc, subA, "subA@gmail.com");

        mockMvc.perform(delete("/api/workout-templates/{id}", templateId)
                .with(jwtFor(subB, "b@test.com")))
                .andExpect(status().isNotFound());
    }

    @Test
    void userBCannotReplaceUserAWorkoutTemplate() throws Exception {
        UUID subA = UUID.randomUUID();
        UUID subB = UUID.randomUUID();

        Long templateId = createTemplateAs(mockMvc, subA, "subA@gmail.com");

        mockMvc.perform(put("/api/workout-templates/{id}", templateId)
                        .with(jwtFor(subB, "b@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Pull Day","description":"Fingers, shoulders, triceps"}
                                """))
                .andExpect(status().isNotFound());
    }
}