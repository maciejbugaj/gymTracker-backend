package com.gymtracker.gym.exerciseLogs.mapper

import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogResponse
import com.gymtracker.gym.exerciseLogs.model.ExerciseLog

fun ExerciseLog.toResponse() = ExerciseLogResponse(
    id = requireNotNull(id) { "ExerciseLog without id - entity not saved"},
    exerciseName = exerciseName,
    setNumber = setNumber,
    reps = reps,
    weightKg = weightKg,
    loggedAt = loggedAt,
)