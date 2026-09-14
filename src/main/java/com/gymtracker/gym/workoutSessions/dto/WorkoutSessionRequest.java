package com.gymtracker.gym.workoutSessions.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutSessionRequest {

    private Long workoutTemplateId;
    private Long programDayId;

    // Hibernate Validator picks up any isXxx()/getXxx() boolean method annotated with a
    // constraint as a property-level check — no custom ConstraintValidator class needed.
    @AssertTrue(message = "Exactly one of workoutTemplateId or programDayId must be provided")
    public boolean isExactlyOneSourceProvided() {
        return (workoutTemplateId == null) != (programDayId == null);
    }
}
