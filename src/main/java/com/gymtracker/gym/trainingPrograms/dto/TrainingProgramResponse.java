package com.gymtracker.gym.trainingPrograms.dto;

import com.gymtracker.gym.trainingPrograms.model.ProgramGoal;
import com.gymtracker.gym.trainingPrograms.model.ProgramSource;
import com.gymtracker.gym.trainingPrograms.model.ProgramStatus;

import java.time.LocalDateTime;
import java.util.List;

public record TrainingProgramResponse(
        Long id,
        String name,
        String description,
        ProgramGoal goal,
        String experienceLevel,
        Integer durationWeeks,
        Integer daysPerWeek,
        ProgramStatus status,
        ProgramSource source,
        LocalDateTime createdAt,
        List<ProgramWeekResponse> weeks
) {
}
