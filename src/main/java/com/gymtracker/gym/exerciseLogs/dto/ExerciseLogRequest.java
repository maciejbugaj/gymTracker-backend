package com.gymtracker.gym.exerciseLogs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseLogRequest {

    @NotNull
    private Long workoutSessionId;
    @NotBlank
    private String exerciseName;
    @Positive
    private Integer reps;
    @Positive
    private BigDecimal weightKg;
}
