package com.gymtracker.gym.workoutSessions.dto;

import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class WorkoutSessionResponse {
    private Long id;
    private Long workoutTemplateId;
    private String workoutTemplateName;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer durationSeconds;
    private String notes;
    private List<ExerciseLogResponse> exerciseLogs;
}
