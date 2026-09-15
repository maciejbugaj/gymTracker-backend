package com.gymtracker.gym.templateExercises.model

import com.gymtracker.gym.workoutTemplates.model.WorkoutTemplate
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal

@Entity
@Table(name= "template_exercises")
class TemplateExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    var workoutTemplate: WorkoutTemplate? = null

    lateinit var exerciseName: String
    var defaultSets: Int? = null
    var defaultReps: Int? = null
    var defaultWeightKg: BigDecimal? = null
    var sortOrder: Int = 0
}