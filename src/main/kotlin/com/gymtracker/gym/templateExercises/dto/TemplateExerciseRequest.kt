package com.gymtracker.gym.templateExercises.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

data class TemplateExerciseRequest(
    val workoutTemplateId: Long,
    @field:NotBlank val exerciseName: String,
    @field:PositiveOrZero val defaultSets: Int?,
    @field:PositiveOrZero val defaultReps: Int?,
    @field:PositiveOrZero val defaultWeightKg: BigDecimal?,
    @field:PositiveOrZero val sortOrder: Int?,
)
