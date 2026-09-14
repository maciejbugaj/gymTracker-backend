package com.gymtracker.gym.trainingPrograms.dto;

import java.util.List;

public record ProgramWeekResponse(
        Long id,
        Integer weekNumber,
        String focus,
        boolean isDeload,
        String notes,
        List<ProgramDayResponse> days
) {
}
