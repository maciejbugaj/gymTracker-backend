package com.gymtracker.gym.aiPlans.repository;

import com.gymtracker.gym.aiPlans.model.AiPlanGeneration;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@NullMarked
@Repository
public interface AiPlanGenerationRepository extends JpaRepository<AiPlanGeneration, Long> {

    Optional<AiPlanGeneration> findByIdAndUserId(Long id, Long userId);

    List<AiPlanGeneration> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndCreatedAtAfter(Long userId, LocalDateTime since);
}
