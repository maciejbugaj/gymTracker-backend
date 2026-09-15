package com.gymtracker.gym.templateExercises.service

import com.gymtracker.gym.exceptions.NotFoundException
import com.gymtracker.gym.templateExercises.dto.TemplateExerciseRequest
import com.gymtracker.gym.templateExercises.dto.TemplateExerciseResponse
import com.gymtracker.gym.templateExercises.mapper.toResponse
import com.gymtracker.gym.templateExercises.model.TemplateExercise
import com.gymtracker.gym.templateExercises.repository.TemplateExerciseRepository
import com.gymtracker.gym.workoutTemplates.repository.WorkoutTemplateRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service

@Service
class TemplateExerciseService(
    private val templateExerciseRepository: TemplateExerciseRepository,
    private val workoutTemplateRepository: WorkoutTemplateRepository,
) {

    @Transactional
    fun createTemplateExercise(request: TemplateExerciseRequest, userId: Long): TemplateExerciseResponse {

        val template =
            workoutTemplateRepository.findByIdWithExercises(request.workoutTemplateId, userId)
                .orElseThrow{ NotFoundException("Workout template with id: ${request.workoutTemplateId} not found") }

        val templateExercise = TemplateExercise().apply {
            workoutTemplate = template
            exerciseName = request.exerciseName
            defaultSets = request.defaultSets
            defaultReps = request.defaultReps
            defaultWeightKg = request.defaultWeightKg
            sortOrder = request.sortOrder ?: 0
        }

        return templateExerciseRepository.save(templateExercise).toResponse()
    }
    @Transactional
    fun updateTemplateExercise(
        exerciseId: Long,
        request: TemplateExerciseRequest,
        userId: Long
    ): TemplateExerciseResponse {
        val templateExercise =
            templateExerciseRepository.findByIdAndWorkoutTemplate_UserId(exerciseId, userId) ?: throw NotFoundException(
                "Template exercise with id: $exerciseId not found"
            )

        templateExercise.exerciseName = request.exerciseName
        templateExercise.defaultSets = request.defaultSets
        templateExercise.defaultReps = request.defaultReps
        templateExercise.defaultWeightKg = request.defaultWeightKg
        templateExercise.sortOrder = request.sortOrder ?: templateExercise.sortOrder

        return templateExerciseRepository.save(templateExercise).toResponse()
    }

    @Transactional
    fun deleteTemplateExercise(exerciseId: Long, userId: Long) {
        if (!templateExerciseRepository.existsByIdAndWorkoutTemplate_UserId(exerciseId, userId)) {
            throw NotFoundException("Template exercise with id: $exerciseId not found")
        }

        templateExerciseRepository.deleteByIdAndWorkoutTemplate_UserId(exerciseId, userId)
    }

}