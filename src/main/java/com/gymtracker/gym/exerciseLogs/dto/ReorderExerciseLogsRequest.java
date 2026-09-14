package com.gymtracker.gym.exerciseLogs.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Renumbers every saved set of one exercise inside one session. {@code logIds} must list all of
 * them, in the order the rows are shown to the user — they get set_number 1..n in that order.
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReorderExerciseLogsRequest {

    @NotNull
    private Long workoutSessionId;
    @NotBlank
    private String exerciseName;
    @NotEmpty
    private List<Long> logIds;
}
