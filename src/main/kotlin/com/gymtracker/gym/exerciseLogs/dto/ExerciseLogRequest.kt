package com.gymtracker.gym.exerciseLogs.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import java.math.BigDecimal

data class ExerciseLogRequest(
    val workoutSessionId: Long,
    @field:NotBlank val exerciseName: String,
    /** Row position in the client's set table. When null the server falls back to "last + 1". */
    @field:Positive val setNumber: Int?,
    @field:PositiveOrZero val reps: Int?,
    @field:PositiveOrZero val weightKg: BigDecimal?,
)
