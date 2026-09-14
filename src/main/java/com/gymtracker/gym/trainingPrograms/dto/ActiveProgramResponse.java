package com.gymtracker.gym.trainingPrograms.dto;

/** nextDay is null once every day of the program has a completed session. */
public record ActiveProgramResponse(
        TrainingProgramSummaryResponse program,
        NextProgramDayResponse nextDay
) {
}
