package com.gymtracker.gym.templateExercises.controller;

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
class TemplateExerciseControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;


    @Test
    void userBCannotCreateTemplateExerciseInUserAWorkoutTemplate() throws Exception {
        UUID subA = UUID.randomUUID();
        UUID subB = UUID.randomUUID();

        Long workoutTemplateIdUserA = createTemplateAs(mockMvc, subA, "subA@gmail.com");

        mockMvc.perform(post("/api/template-exercises")
                .with(jwtFor(subB, "subB@gmail.com"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"workoutTemplateId": %d,
                         "exerciseName": "Bench Press",
                          "defaultSets": 5,
                           "defaultReps": 12,
                           "defaultWeightKg": 120,
                           "sortOrder": 1}
                        """.formatted(workoutTemplateIdUserA)))
                .andExpect(status().isNotFound());
    }

    @Test
    void userBCannotUpdateTemplateExerciseInUserAWorkoutTemplate() throws Exception {
        UUID subA = UUID.randomUUID();
        UUID subB = UUID.randomUUID();

        Long workoutTemplateIdUserA = createTemplateAs(mockMvc, subA, "subA@gmail.com");
        Long templateExerciseUserA = createTemplateExerciseInWorkoutTemplate(mockMvc, subA, "subA@gmail.com", workoutTemplateIdUserA);

        mockMvc.perform(put("/api/template-exercises/{id}", templateExerciseUserA)
                        .with(jwtFor(subB, "subB@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {"workoutTemplateId": %d,
                         "exerciseName": "Leg Press",
                          "defaultSets": 5,
                           "defaultReps": 12,
                           "defaultWeightKg": 120,
                           "sortOrder": 1}
                        """.formatted(workoutTemplateIdUserA)))
                .andExpect(status().isNotFound());
    }

    @Test
    void userBCannotDeleteTemplateExerciseInUserAWorkoutTemplate() throws Exception {
        UUID subA = UUID.randomUUID();
        UUID subB = UUID.randomUUID();

        Long workoutTemplateIdUserA = createTemplateAs(mockMvc, subA, "subA@gmail.com");
        Long templateExerciseUserA = createTemplateExerciseInWorkoutTemplate(mockMvc, subA, "subA@gmail.com", workoutTemplateIdUserA);

        mockMvc.perform(delete("/api/template-exercises/{id}", templateExerciseUserA)
                        .with(jwtFor(subB, "subB@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}