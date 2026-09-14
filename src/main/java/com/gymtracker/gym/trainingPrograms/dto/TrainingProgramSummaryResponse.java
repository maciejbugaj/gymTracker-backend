package com.gymtracker.gym.trainingPrograms.dto;

import com.gymtracker.gym.trainingPrograms.model.ProgramGoal;
import com.gymtracker.gym.trainingPrograms.model.ProgramStatus;

import java.time.LocalDateTime;

/** Lightweight view for list screens — no nested weeks/days/exercises. */
public record TrainingProgramSummaryResponse(
        Long id,
        String name,
        ProgramGoal goal,
        ProgramStatus status,
        Integer durationWeeks,
        Integer daysPerWeek,
        LocalDateTime createdAt
) {
}
