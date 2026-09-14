package com.gymtracker.gym.trainingPrograms.dto;

import java.util.List;

/** The next uncompleted day of the user's active program — feeds the "active program" card on Home. */
public record NextProgramDayResponse(
        Long programDayId,
        Integer weekNumber,
        Integer dayNumber,
        String dayName,
        boolean isDeload,
        List<ProgramDayExerciseResponse> exercises
) {
}
