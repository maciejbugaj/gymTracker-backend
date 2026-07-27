package com.gymtracker.gym.users.service;

import com.gymtracker.gym.exceptions.NotFoundException;
import com.gymtracker.gym.users.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class UserServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    public void afterEach() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(UUID sub, String email) {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").claim("sub", sub.toString()).claim("email", email).build();
        JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthenticationToken);
    }


    @Test
    public void getCurrentUserShouldReturnCorrectId() {
        UUID sub = UUID.randomUUID();
        String email = "test@gmail.com";
        authenticateAs(sub, email);

        Long userId = userService.getCurrentUser();

        assertThat(userRepository.findByKeycloakId(sub).orElseThrow(() -> new NotFoundException("User not created")).getId()).isEqualTo(userId);
    }

    @Test
    public void getCurrentUserCalledTwoTimesShouldReturnSameId() {
        UUID sub = UUID.randomUUID();
        String email = "test@gmail.com";
        authenticateAs(sub, email);

        Long userId = userService.getCurrentUser();
        Long userId2 = userService.getCurrentUser();

        assertThat(userId).isEqualTo(userId2);
    }
}