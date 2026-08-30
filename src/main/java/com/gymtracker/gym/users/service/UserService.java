package com.gymtracker.gym.users.service;

import com.gymtracker.gym.users.model.User;
import com.gymtracker.gym.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Long getCurrentUser() {
        JwtAuthenticationToken jwtAuthenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = jwtAuthenticationToken.getToken();
        UUID currentUserUUID = UUID.fromString(jwt.getClaimAsString("sub"));

        Long currentUserId;
        try {
            currentUserId = userRepository.findByKeycloakId(currentUserUUID).orElseGet(() -> userRepository.save(User.builder()
                    .email(jwt.getClaimAsString("email"))
                    .keycloakId(currentUserUUID)
                    .build())).getId();
        }
        catch (DataIntegrityViolationException e) {
            currentUserId = userRepository.findByKeycloakId(currentUserUUID).orElseThrow(() -> e).getId();
        }

        return currentUserId;
    }
}
