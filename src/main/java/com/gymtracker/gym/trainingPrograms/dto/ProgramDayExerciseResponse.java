package com.gymtracker.gym.trainingPrograms.dto;

public record ProgramDayExerciseResponse(
        Long id,
        String exerciseName,
        Integer sortOrder,
        Integer targetSets,
        Integer targetRepsMin,
        Integer targetRepsMax,
        Integer restSeconds,
        String notes
) {
}
