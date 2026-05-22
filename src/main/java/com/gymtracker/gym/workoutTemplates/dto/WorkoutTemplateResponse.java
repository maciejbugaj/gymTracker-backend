package com.gymtracker.gym.workoutTemplates.dto;

import com.gymtracker.gym.templateExecrcises.dto.TemplateExerciseResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class WorkoutTemplateResponse {
    private Long id;
    private String name;
    private String description;
    private List<TemplateExerciseResponse> exercises;
}
