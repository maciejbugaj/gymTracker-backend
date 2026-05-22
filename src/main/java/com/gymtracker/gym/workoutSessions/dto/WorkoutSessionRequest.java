package com.gymtracker.gym.workoutSessions.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkoutSessionRequest {
    private Long workoutTemplateId;
}
