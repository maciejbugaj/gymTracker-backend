package com.gymtracker.gym.templateExercises.dto

import java.math.BigDecimal

data class TemplateExerciseResponse(

    val id: Long,
    val exerciseName: String,
    val defaultSets: Int?,
    val defaultReps: Int?,
    val defaultWeight: BigDecimal?
)
