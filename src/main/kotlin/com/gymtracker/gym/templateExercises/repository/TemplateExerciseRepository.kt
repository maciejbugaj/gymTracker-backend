package com.gymtracker.gym.templateExercises.repository

import com.gymtracker.gym.templateExercises.model.TemplateExercise
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TemplateExerciseRepository : JpaRepository<TemplateExercise, Long> {

    fun findByIdAndWorkoutTemplate_UserId(id: Long, userId: Long): TemplateExercise?

    fun existsByIdAndWorkoutTemplate_UserId(id: Long, userId: Long): Boolean

    fun deleteByIdAndWorkoutTemplate_UserId(id: Long, userId: Long)
}