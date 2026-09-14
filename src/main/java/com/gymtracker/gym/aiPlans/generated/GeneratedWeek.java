package com.gymtracker.gym.aiPlans.generated;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record GeneratedWeek(
        @JsonPropertyDescription("1-indexed week number within the program")
        int weekNumber,

        @JsonPropertyDescription("Short label for this week's training emphasis, e.g. 'Accumulation', " +
                "'Intensification', 'Deload'")
        String focus,

        @JsonPropertyDescription("True if this is a deload (reduced volume/intensity) week")
        boolean isDeload,

        @JsonPropertyDescription("Optional notes specific to this week, e.g. why it's a deload or what changes " +
                "compared to the previous week")
        String notes,

        @JsonPropertyDescription("One entry per training day this week, in order")
        List<GeneratedDay> days
) {
}
