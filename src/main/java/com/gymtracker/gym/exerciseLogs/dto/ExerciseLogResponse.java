package com.gymtracker.gym.exerciseLogs.dto;

import com.gymtracker.gym.workoutSessions.dto.WorkoutSessionResponse;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ExerciseLogResponse {

    private Long id;
    private String exerciseName;
    private Integer setNumber;
    private Integer reps;
    private BigDecimal weightKg;
    private LocalDateTime loggedAt;
}
