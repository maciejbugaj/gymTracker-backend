package com.gymtracker.gym.workoutSessions.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

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
}
