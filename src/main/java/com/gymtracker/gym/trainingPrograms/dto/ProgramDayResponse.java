package com.gymtracker.gym.trainingPrograms.dto;

import java.util.List;

public record ProgramDayResponse(
        Long id,
        Integer dayNumber,
        String name,
        String notes,
        List<ProgramDayExerciseResponse> exercises
) {
}
