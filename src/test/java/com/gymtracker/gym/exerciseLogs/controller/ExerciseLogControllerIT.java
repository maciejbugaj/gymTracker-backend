package com.gymtracker.gym.exerciseLogs.controller;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class ExerciseLogControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    MockMvc mockMvc;

    @Test
    void userBCannotCreateExerciseLogWithUserAWorkoutSessionId() throws Exception {
        UUID subA = UUID.randomUUID();
        UUID subB = UUID.randomUUID();

        Long templateId = createTemplateAs(mockMvc, subA, "subA@gamil.com");
        Long workoutSessionId = createWorkoutSessionAs(mockMvc, subA, "suba@Gmail.com", templateId);

        mockMvc.perform(post("/api/exercise-logs")
                .with(jwtFor(subB, "subB@gmail.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"workoutSessionId":%d,
                        "exerciseName": "Legs"}
                        """.formatted(workoutSessionId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void userBCannotAccessUserAExerciseLogs() throws Exception{
        UUID subA = UUID.randomUUID();
        UUID subB = UUID.randomUUID();

        Long templateId = createTemplateAs(mockMvc, subA, "subA@gamil.com");
        Long workoutSessionId = createWorkoutSessionAs(mockMvc, subA, "suba@Gmail.com", templateId);

        mockMvc.perform(get("/api/exercise-logs/{id}", workoutSessionId)
                .with(jwtFor(subB, "subB@gmail.co")))
                .andExpect(status().isNotFound());
    }

    @Test
    void userBCannotAccessUserAPreviousLogs() throws Exception{
        UUID subA = UUID.randomUUID();
        UUID subB = UUID.randomUUID();

        Long templateId = createTemplateAs(mockMvc, subA, "subA@gamil.com");

        mockMvc.perform(get("/api/exercise-logs/previous?workoutTemplateId={id}", templateId)
                        .with(jwtFor(subB, "subB@gmail.co")))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}