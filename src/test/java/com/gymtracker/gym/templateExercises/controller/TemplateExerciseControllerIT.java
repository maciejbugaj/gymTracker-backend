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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    // --- happy path (wlasciciel) ---------------------------------------------------------
    // Testy negatywne powyzej koncza sie rzuconym NotFoundException, zanim serwis cokolwiek
    // zapisze. Ponizsze dwa jako jedyne przechodza przez sciezki zapisu w update/delete,
    // czyli przez kod, ktory potrzebuje @Transactional.

    @Test
    void ownerCanUpdateTemplateExerciseAndChangeIsPersisted() throws Exception {
        UUID subA = UUID.randomUUID();

        Long workoutTemplateId = createTemplateAs(mockMvc, subA, "subA@gmail.com");
        Long templateExerciseId = createTemplateExerciseInWorkoutTemplate(mockMvc, subA, "subA@gmail.com", workoutTemplateId);

        mockMvc.perform(put("/api/template-exercises/{id}", templateExerciseId)
                        .with(jwtFor(subA, "subA@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workoutTemplateId": %d,
                                 "exerciseName": "Leg Press",
                                 "defaultSets": 3,
                                 "defaultReps": 8,
                                 "defaultWeightKg": 200,
                                 "sortOrder": 2}
                                """.formatted(workoutTemplateId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(templateExerciseId))
                .andExpect(jsonPath("$.exerciseName").value("Leg Press"))
                .andExpect(jsonPath("$.defaultSets").value(3))
                .andExpect(jsonPath("$.defaultReps").value(8))
                .andExpect(jsonPath("$.defaultWeight").value(200));

        // Osobne zadanie HTTP = osobna transakcja: dopiero to dowodzi, ze zmiana wyladowala
        // w bazie, a nie tylko w odpowiedzi zmapowanej z obiektu w pamieci.
        mockMvc.perform(get("/api/workout-templates/{id}", workoutTemplateId)
                        .with(jwtFor(subA, "subA@gmail.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exercises.length()").value(1))
                .andExpect(jsonPath("$.exercises[0].id").value(templateExerciseId))
                .andExpect(jsonPath("$.exercises[0].exerciseName").value("Leg Press"))
                .andExpect(jsonPath("$.exercises[0].defaultSets").value(3));
    }

    @Test
    void ownerCanDeleteTemplateExerciseAndItIsGone() throws Exception {
        UUID subA = UUID.randomUUID();

        Long workoutTemplateId = createTemplateAs(mockMvc, subA, "subA@gmail.com");
        Long templateExerciseId = createTemplateExerciseInWorkoutTemplate(mockMvc, subA, "subA@gmail.com", workoutTemplateId);

        mockMvc.perform(delete("/api/template-exercises/{id}", templateExerciseId)
                        .with(jwtFor(subA, "subA@gmail.com")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/workout-templates/{id}", workoutTemplateId)
                        .with(jwtFor(subA, "subA@gmail.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.exercises.length()").value(0));
    }

    @Test
    void nullableDefaultsSurviveRoundTrip() throws Exception {
        UUID subA = UUID.randomUUID();

        Long workoutTemplateId = createTemplateAs(mockMvc, subA, "subA@gmail.com");

        // Pola opcjonalne pominiete calkowicie - tak jak pozwalal na to kontrakt sprzed
        // przepisania na Kotlin (Integer/BigDecimal bez @NotNull).
        mockMvc.perform(post("/api/template-exercises")
                        .with(jwtFor(subA, "subA@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"workoutTemplateId": %d,
                                 "exerciseName": "Pull ups"}
                                """.formatted(workoutTemplateId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.exerciseName").value("Pull ups"))
                .andExpect(jsonPath("$.defaultSets").doesNotExist())
                .andExpect(jsonPath("$.defaultReps").doesNotExist())
                .andExpect(jsonPath("$.defaultWeight").doesNotExist());
    }

}
