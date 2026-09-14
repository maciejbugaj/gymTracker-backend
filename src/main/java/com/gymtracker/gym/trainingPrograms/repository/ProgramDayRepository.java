package com.gymtracker.gym.trainingPrograms.repository;

import com.gymtracker.gym.trainingPrograms.model.ProgramDay;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@NullMarked
@Repository
public interface ProgramDayRepository extends JpaRepository<ProgramDay, Long> {

    // Scoped through programWeek -> trainingProgram -> userId: a bare programDayId from a
    // request body/query param must never resolve to another user's day.
    @Query("""
            SELECT d FROM ProgramDay d
            LEFT JOIN FETCH d.programDayExercises
            WHERE d.id = :id AND d.programWeek.trainingProgram.userId = :userId
            """)
    Optional<ProgramDay> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
