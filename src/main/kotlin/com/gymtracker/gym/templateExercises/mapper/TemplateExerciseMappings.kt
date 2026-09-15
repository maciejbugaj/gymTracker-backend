package com.gymtracker.gym.templateExercises.mapper

import com.gymtracker.gym.templateExercises.dto.TemplateExerciseResponse
import com.gymtracker.gym.templateExercises.model.TemplateExercise

fun TemplateExercise.toResponse() = TemplateExerciseResponse(
    id = requireNotNull(id) { "TemplateExercise bez id — encja nie została zapisana" },
    exerciseName = requireNotNull(exerciseName) { "exercise_name jest NOT NULL w bazie" },
    defaultSets = defaultSets,
    defaultReps = defaultReps,
    defaultWeight = defaultWeightKg,
)