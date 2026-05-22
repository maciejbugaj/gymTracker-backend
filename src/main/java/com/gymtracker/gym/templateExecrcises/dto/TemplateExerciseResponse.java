package com.gymtracker.gym.templateExecrcises.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TemplateExerciseResponse {

    private Long id;
    private String exerciseName;
    private Integer defaultSets;
    private Integer defaultReps;
    private BigDecimal defaultWeight;
}
