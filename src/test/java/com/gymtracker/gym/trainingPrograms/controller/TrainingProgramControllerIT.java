package com.gymtracker.gym.trainingPrograms.controller;

import com.gymtracker.gym.aiPlans.dto.AiPlanGenerationResponse;
import com.gymtracker.gym.aiPlans.generated.GeneratedDay;
import com.gymtracker.gym.aiPlans.generated.GeneratedExercise;
import com.gymtracker.gym.aiPlans.generated.GeneratedProgram;
import com.gymtracker.gym.aiPlans.generated.GeneratedWeek;
import com.gymtracker.gym.trainingPrograms.dto.ProgramDayExerciseRequest;
import com.gymtracker.gym.trainingPrograms.dto.ProgramDayRequest;
import com.gymtracker.gym.trainingPrograms.dto.ProgramWeekRequest;
import com.gymtracker.gym.trainingPrograms.dto.TrainingProgramRequest;
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
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static com.gymtracker.gym.testsupport.IntegrationTestSupport.*;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Same stub override as AiPlanControllerIT — a program can only be saved from a SUCCEEDED
// generation, so this needs the deterministic stub too.
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestPropertySource(properties = "gymtracker.ai.enabled=false")
class TrainingProgramControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;

    @Test
    void savedProgramBecomesActiveAndDrivesNextDayAndSessionStart() throws Exception {
        UUID sub = UUID.randomUUID();
        String email = "programuser@gmail.com";

        // Same flow the frontend follows: generate, wait for SUCCEEDED, then save *that*
        // generation's (possibly edited) tree back against its own id.
        Long generationId = startAiPlanGenerationAs(mockMvc, sub, email);
        MvcResult succeeded = awaitGenerationSucceeded(mockMvc, generationId, sub, email);
        GeneratedProgram generatedProgram = objectMapper.readValue(
                succeeded.getResponse().getContentAsString(), AiPlanGenerationResponse.class).program();
        TrainingProgramRequest request = toProgramRequest(generatedProgram);

        MvcResult saveResult = mockMvc.perform(post("/api/training-programs/from-generation/{id}", generationId)
                        .with(jwtFor(sub, email))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn();
        long programId = objectMapper.readTree(saveResult.getResponse().getContentAsString()).get("id").asLong();

        // shows up in the list
        mockMvc.perform(get("/api/training-programs").with(jwtFor(sub, email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(programId));

        // full tree readable — 1 week x 2 days x 4 exercises (the stub's fixed shape)
        mockMvc.perform(get("/api/training-programs/{id}", programId).with(jwtFor(sub, email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weeks", hasSize(1)))
                .andExpect(jsonPath("$.weeks[0].days[0].exercises", hasSize(4)));

        // active program -> next day is week 1 / day 1 (nothing completed yet)
        MvcResult activeResult = mockMvc.perform(get("/api/training-programs/active").with(jwtFor(sub, email)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.program.id").value(programId))
                .andExpect(jsonPath("$.nextDay.weekNumber").value(1))
                .andExpect(jsonPath("$.nextDay.dayNumber").value(1))
                .andReturn();
        long programDayId = objectMapper.readTree(activeResult.getResponse().getContentAsString())
                .get("nextDay").get("programDayId").asLong();

        // starting a session from that day carries program_day_id + prescribedExercises
        mockMvc.perform(post("/api/workout-sessions")
                        .with(jwtFor(sub, email))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"programDayId": %d}
                                """.formatted(programDayId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.programDayId").value(programDayId))
                .andExpect(jsonPath("$.prescribedExercises", hasSize(4)));
    }

    @Test
    void activeEndpointReturnsNoContentWhenNothingIsActive() throws Exception {
        mockMvc.perform(get("/api/training-programs/active").with(jwtFor(UUID.randomUUID(), "noprogram@gmail.com")))
                .andExpect(status().isNoContent());
    }

    @Test
    void userBCannotSaveFromUserAGeneration() throws Exception {
        UUID subA = UUID.randomUUID();
        UUID subB = UUID.randomUUID();

        Long generationIdA = startAiPlanGenerationAs(mockMvc, subA, "suba@gmail.com");
        awaitGenerationSucceeded(mockMvc, generationIdA, subA, "suba@gmail.com");

        // valid body of B's own, submitted against A's generation id
        TrainingProgramRequest request = generateAndBuildRequest(subB, "subb@gmail.com");

        mockMvc.perform(post("/api/training-programs/from-generation/{id}", generationIdA)
                        .with(jwtFor(subB, "subb@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void trainingProgramsEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/training-programs"))
                .andExpect(status().isUnauthorized());
    }

    /** Runs a generation to SUCCEEDED and converts its result into a save-able request body —
     * mirrors the frontend's toProgramRequest (GeneratedProgram and TrainingProgramRequest are
     * structurally identical in JSON, but Java records aren't structurally typed like TS, so
     * this walks the tree explicitly instead of just reusing the object). */
    private TrainingProgramRequest generateAndBuildRequest(UUID sub, String email) throws Exception {
        Long generationId = startAiPlanGenerationAs(mockMvc, sub, email);
        MvcResult succeeded = awaitGenerationSucceeded(mockMvc, generationId, sub, email);
        GeneratedProgram program = objectMapper.readValue(
                succeeded.getResponse().getContentAsString(), AiPlanGenerationResponse.class).program();
        return toProgramRequest(program);
    }

    private static TrainingProgramRequest toProgramRequest(GeneratedProgram program) {
        List<ProgramWeekRequest> weeks = program.weeks().stream().map(TrainingProgramControllerIT::toWeekRequest).toList();
        return new TrainingProgramRequest(program.name(), program.description(), program.goal(),
                null, program.durationWeeks(), program.daysPerWeek(), weeks);
    }

    private static ProgramWeekRequest toWeekRequest(GeneratedWeek week) {
        List<ProgramDayRequest> days = week.days().stream().map(TrainingProgramControllerIT::toDayRequest).toList();
        return new ProgramWeekRequest(week.weekNumber(), week.focus(), week.isDeload(), week.notes(), days);
    }

    private static ProgramDayRequest toDayRequest(GeneratedDay day) {
        List<ProgramDayExerciseRequest> exercises = day.exercises().stream().map(TrainingProgramControllerIT::toExerciseRequest).toList();
        return new ProgramDayRequest(day.dayNumber(), day.name(), day.notes(), exercises);
    }

    private static ProgramDayExerciseRequest toExerciseRequest(GeneratedExercise exercise) {
        return new ProgramDayExerciseRequest(exercise.exerciseName(), exercise.sortOrder(), exercise.targetSets(),
                exercise.targetRepsMin(), exercise.targetRepsMax(), exercise.restSeconds(), exercise.notes());
    }
}
