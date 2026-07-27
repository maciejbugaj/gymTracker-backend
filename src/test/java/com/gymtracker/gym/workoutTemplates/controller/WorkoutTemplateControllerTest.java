package com.gymtracker.gym.workoutTemplates.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class WorkoutTemplateControllerTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private MockMvc mockMvc;


    @Test
    void userBCannotAccessUserAsWorkoutTemplate() throws Exception {
        UUID subA = UUID.randomUUID();
        UUID subB = UUID.randomUUID();

        MvcResult createResult = mockMvc.perform(post("/api/workout-templates")
                        .with(jwt().jwt(jwt -> jwt
                                .claim("sub", subA.toString())
                                .claim("email", "a@test.com")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Push Day","description":"Chest, shoulders, triceps"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String location = createResult.getResponse().getHeader("Location");
        assertThat(location).isNotNull();
        Long templateId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        mockMvc.perform(get("/api/workout-templates/{id}", templateId)
                        .with(jwt().jwt(jwt -> jwt.claim("sub", subB.toString()).claim("email", "b@test.com"))))
                .andExpect(status().isNotFound());
    }

}