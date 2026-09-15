package com.gymtracker.gym.exerciseLogs.model

import com.gymtracker.gym.workoutSessions.model.WorkoutSession
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
@Table(name = "exercise_logs")
class ExerciseLog(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    var workoutSession: WorkoutSession? = null,

    var exerciseName: String = "",
    var setNumber: Int = 0,
    var reps: Int? = null,
    var weightKg: BigDecimal? = null,

    @CreationTimestamp
    var loggedAt: LocalDateTime = LocalDateTime.now(),
)
