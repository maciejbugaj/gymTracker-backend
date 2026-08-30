package com.gymtracker.gym.users.repository;

import com.gymtracker.gym.users.model.User;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@NullMarked
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKeycloakId (UUID keycloakId);
}
