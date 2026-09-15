package com.gymtracker.gym.exerciseLogs.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class ReorderExerciseLogsRequest(

    val workoutSessionId: Long,
    @field:NotBlank val exerciseName: String,
    @field:NotEmpty val logIds: List<Long>
    )
