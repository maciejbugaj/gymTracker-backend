package com.gymtracker.gym.workoutSessions.controller;

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

import static com.gymtracker.gym.testsupport.IntegrationTestSupport.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class WorkoutSessionControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;


    @Test
    void userBCannotAccessUserAWorkoutSession() throws Exception {
        UUID subA = UUID.randomUUID();
        UUID subB = UUID.randomUUID();
        String subAEmail = "suba@gmail.com";

        Long templateId = createTemplateAs(mockMvc, subA, subAEmail);

        Long workoutSessionTemplateId = createWorkoutSessionAs(mockMvc, subA, subAEmail, templateId);

        mockMvc.perform(get("/api/workout-sessions/{id}", workoutSessionTemplateId)
                .with(jwtFor(subB, "subb@gmail.com")))
                .andExpect(status().isNotFound());
    }

    @Test
    void userBCannotCreateWorkoutSessionWithUserATemplateId() throws Exception {
        UUID subA = UUID.randomUUID();
        UUID subB = UUID.randomUUID();
        String subAEmail = "suba@gmail.com";

        Long templateId = createTemplateAs(mockMvc, subA, subAEmail);

        mockMvc.perform(post("/api/workout-sessions")
                        .with(jwtFor(subB, "subB@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"workoutTemplateId": %d}
                        """.formatted(templateId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void userBCannotFinishUserAWorkoutSession() throws Exception {
        UUID subA = UUID.randomUUID();
        UUID subB = UUID.randomUUID();
        String subAEmail = "suba@gmail.com";

        Long templateId = createTemplateAs(mockMvc, subA, subAEmail);

        Long workoutSessionTemplateId = createWorkoutSessionAs(mockMvc, subA, subAEmail, templateId);

        mockMvc.perform(post("/api/workout-sessions/{id}/finish", workoutSessionTemplateId)
                        .with(jwtFor(subB, "subb@gmail.com")))
                .andExpect(status().isNotFound());
    }
}