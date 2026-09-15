package com.gymtracker.gym.exerciseLogs.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class ExerciseLogResponse (
    val id: Long,
    val exerciseName: String,
    val setNumber: Int,
    val reps: Int?,
    val weightKg: BigDecimal?,
    val loggedAt: LocalDateTime
)