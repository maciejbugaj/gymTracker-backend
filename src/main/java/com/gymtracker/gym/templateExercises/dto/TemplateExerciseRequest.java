package com.gymtracker.gym.templateExercises.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateExerciseRequest {

    @NotNull
    private Long workoutTemplateId;
    @NotBlank
    private String exerciseName;
    @PositiveOrZero
    private Integer defaultSets;
    @PositiveOrZero
    private Integer defaultReps;
    @PositiveOrZero
    private BigDecimal defaultWeightKg;
    @PositiveOrZero
    private Integer sortOrder;
}
