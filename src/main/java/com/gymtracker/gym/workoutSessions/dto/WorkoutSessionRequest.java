package com.gymtracker.gym.workoutSessions.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WorkoutSessionRequest {

    @NotNull
    private Long workoutTemplateId;
}
