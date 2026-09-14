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

import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

import static com.gymtracker.gym.testsupport.IntegrationTestSupport.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @Test
    void userBCannotDeleteUserAExerciseLog() throws Exception {
        UUID subA = UUID.randomUUID();
        UUID subB = UUID.randomUUID();

        Long templateId = createTemplateAs(mockMvc, subA, "subA@gmail.com");
        Long workoutSessionId = createWorkoutSessionAs(mockMvc, subA, "subA@gmail.com", templateId);
        Long logId = logSet(subA, "subA@gmail.com", workoutSessionId, "Bench Press", 1, 5, 100);

        mockMvc.perform(delete("/api/exercise-logs/{id}", logId)
                        .with(jwtFor(subB, "subB@gmail.com")))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/exercise-logs/{id}", workoutSessionId)
                        .with(jwtFor(subA, "subA@gmail.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void deletingASetRemovesItFromTheSession() throws Exception {
        UUID sub = UUID.randomUUID();

        Long templateId = createTemplateAs(mockMvc, sub, "sub@gmail.com");
        Long workoutSessionId = createWorkoutSessionAs(mockMvc, sub, "sub@gmail.com", templateId);
        Long firstSet = logSet(sub, "sub@gmail.com", workoutSessionId, "Bench Press", 1, 5, 100);
        logSet(sub, "sub@gmail.com", workoutSessionId, "Bench Press", 2, 5, 100);

        mockMvc.perform(delete("/api/exercise-logs/{id}", firstSet)
                        .with(jwtFor(sub, "sub@gmail.com")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/exercise-logs/{id}", workoutSessionId)
                        .with(jwtFor(sub, "sub@gmail.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].setNumber").value(2));
    }

    @Test
    void reorderRenumbersRemainingSetsFromOne() throws Exception {
        UUID sub = UUID.randomUUID();

        Long templateId = createTemplateAs(mockMvc, sub, "sub@gmail.com");
        Long workoutSessionId = createWorkoutSessionAs(mockMvc, sub, "sub@gmail.com", templateId);
        Long firstSet = logSet(sub, "sub@gmail.com", workoutSessionId, "Bench Press", 1, 5, 100);
        Long secondSet = logSet(sub, "sub@gmail.com", workoutSessionId, "Bench Press", 2, 5, 105);
        Long thirdSet = logSet(sub, "sub@gmail.com", workoutSessionId, "Bench Press", 3, 5, 110);

        // the user drops the middle row, so the third set has to become the second one
        mockMvc.perform(delete("/api/exercise-logs/{id}", secondSet)
                        .with(jwtFor(sub, "sub@gmail.com")))
                .andExpect(status().isNoContent());

        mockMvc.perform(put("/api/exercise-logs/order")
                        .with(jwtFor(sub, "sub@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workoutSessionId":%d,"exerciseName":"Bench Press","logIds":[%d,%d]}
                                """.formatted(workoutSessionId, firstSet, thirdSet)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/exercise-logs/{id}", workoutSessionId)
                        .with(jwtFor(sub, "sub@gmail.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.id == %d)].setNumber".formatted(firstSet)).value(1))
                .andExpect(jsonPath("$[?(@.id == %d)].setNumber".formatted(thirdSet)).value(2));
    }

    @Test
    void reorderRejectsAnIncompleteListOfSets() throws Exception {
        UUID sub = UUID.randomUUID();

        Long templateId = createTemplateAs(mockMvc, sub, "sub@gmail.com");
        Long workoutSessionId = createWorkoutSessionAs(mockMvc, sub, "sub@gmail.com", templateId);
        Long firstSet = logSet(sub, "sub@gmail.com", workoutSessionId, "Bench Press", 1, 5, 100);
        logSet(sub, "sub@gmail.com", workoutSessionId, "Bench Press", 2, 5, 105);

        mockMvc.perform(put("/api/exercise-logs/order")
                        .with(jwtFor(sub, "sub@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workoutSessionId":%d,"exerciseName":"Bench Press","logIds":[%d]}
                                """.formatted(workoutSessionId, firstSet)))
                .andExpect(status().isConflict());
    }

    @Test
    void userBCannotReorderUserASets() throws Exception {
        UUID subA = UUID.randomUUID();
        UUID subB = UUID.randomUUID();

        Long templateId = createTemplateAs(mockMvc, subA, "subA@gmail.com");
        Long workoutSessionId = createWorkoutSessionAs(mockMvc, subA, "subA@gmail.com", templateId);
        Long logId = logSet(subA, "subA@gmail.com", workoutSessionId, "Bench Press", 1, 5, 100);

        mockMvc.perform(put("/api/exercise-logs/order")
                        .with(jwtFor(subB, "subB@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workoutSessionId":%d,"exerciseName":"Bench Press","logIds":[%d]}
                                """.formatted(workoutSessionId, logId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void theSameSetNumberCannotBeSavedTwice() throws Exception {
        UUID sub = UUID.randomUUID();

        Long templateId = createTemplateAs(mockMvc, sub, "sub@gmail.com");
        Long workoutSessionId = createWorkoutSessionAs(mockMvc, sub, "sub@gmail.com", templateId);
        logSet(sub, "sub@gmail.com", workoutSessionId, "Bench Press", 1, 5, 100);

        mockMvc.perform(post("/api/exercise-logs")
                        .with(jwtFor(sub, "sub@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workoutSessionId":%d,"exerciseName":"Bench Press","setNumber":1,"reps":5,"weightKg":100}
                                """.formatted(workoutSessionId)))
                .andExpect(status().isConflict());
    }

    /** POSTs one set row the way the session screen does and returns the created log id. */
    private Long logSet(UUID sub, String email, Long workoutSessionId, String exerciseName,
                        int setNumber, int reps, int weightKg) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/exercise-logs")
                        .with(jwtFor(sub, email))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workoutSessionId":%d,"exerciseName":"%s","setNumber":%d,"reps":%d,"weightKg":%d}
                                """.formatted(workoutSessionId, exerciseName, setNumber, reps, weightKg)))
                .andExpect(status().isCreated())
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        assertThat(location).isNotNull();
        return Long.parseLong(location.substring(location.lastIndexOf("/") + 1));
    }
}
