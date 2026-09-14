package com.gymtracker.gym.aiPlans.generated;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.gymtracker.gym.trainingPrograms.model.ProgramGoal;

import java.util.List;

public record GeneratedProgram(
        @JsonPropertyDescription("Short, descriptive name for the training program, e.g. 'Upper/Lower Strength Split'")
        String name,

        @JsonPropertyDescription("1-3 sentence description of the program's overall approach and focus")
        String description,

        @JsonPropertyDescription("Primary training goal this program is optimized for")
        ProgramGoal goal,

        @JsonPropertyDescription("Total number of weeks in the program; must match the athlete's requested duration exactly")
        int durationWeeks,

        @JsonPropertyDescription("Number of training days per week; must match the athlete's requested frequency exactly")
        int daysPerWeek,

        @JsonPropertyDescription("One entry per week of the program, in order starting from week 1")
        List<GeneratedWeek> weeks,

        @JsonPropertyDescription("Coaching notes for the athlete: how to progress week to week, what to watch " +
                "for, safety considerations")
        String coachNotes
) {
}
