package com.gymtracker.gym.trainingPrograms.repository;

import com.gymtracker.gym.trainingPrograms.model.ProgramStatus;
import com.gymtracker.gym.trainingPrograms.model.TrainingProgram;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@NullMarked
@Repository
public interface TrainingProgramRepository extends JpaRepository<TrainingProgram, Long> {

    List<TrainingProgram> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<TrainingProgram> findByIdAndUserId(Long id, Long userId);

    Optional<TrainingProgram> findFirstByUserIdAndStatusOrderByCreatedAtDesc(Long userId, ProgramStatus status);

    boolean existsByIdAndUserId(Long id, Long userId);

    void deleteByIdAndUserId(Long id, Long userId);

    @Query("""
            SELECT tp FROM TrainingProgram tp
            LEFT JOIN FETCH tp.programWeeks
            WHERE tp.id = :id AND tp.userId = :userId
            """)
    Optional<TrainingProgram> findByIdAndUserIdWithWeeks(@Param("id") Long id, @Param("userId") Long userId);
}
