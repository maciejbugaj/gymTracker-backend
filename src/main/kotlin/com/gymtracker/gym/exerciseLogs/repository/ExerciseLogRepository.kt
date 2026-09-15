package com.gymtracker.gym.exerciseLogs.repository

import com.gymtracker.gym.exerciseLogs.model.ExerciseLog
import com.gymtracker.gym.workoutSessions.model.WorkoutSession
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ExerciseLogRepository: JpaRepository<ExerciseLog, Long> {

    fun findAllByWorkoutSession(workoutSession: WorkoutSession): List<ExerciseLog>

    @Query("""
            SELECT e FROM ExerciseLog e
            WHERE e.workoutSession = :session
            AND e.exerciseName = :exerciseName
            ORDER BY e.setNumber DESC
            LIMIT 1
            """
    )
    fun findLatestBySessionAndExerciseName(
        @Param("session") session: WorkoutSession,
        @Param("exerciseName") exerciseName: String
    ): ExerciseLog?

    fun findByWorkoutSessionOrderBySetNumberAsc(workoutSession: WorkoutSession): List<ExerciseLog>

    fun findByWorkoutSessionAndExerciseNameOrderBySetNumberAsc(
        workoutSession: WorkoutSession,
        exerciseName: String
    ): List<ExerciseLog>

    @Query("""
        SELECT e FROM ExerciseLog e
        JOIN FETCH e.workoutSession s
        WHERE s.userId = :userId
          AND s.endedAt IS NOT NULL
          AND s.startedAt >= :since
        ORDER BY e.exerciseName ASC, s.startedAt ASC, e.setNumber ASC
        """
    )
    fun findCompletedSetsForUserSince(
        @Param("userId") userId: Long,
        @Param("since") since: LocalDateTime
    ): List<ExerciseLog>

    // For "previous" on a program day: the most recent session in which this user logged this
    // exercise name at all, regardless of which template/program day it came from.
    @Query("""
            SELECT e.workoutSession.id FROM ExerciseLog e
            WHERE e.workoutSession.userId = :userId
              AND e.exerciseName = :exerciseName
              AND e.workoutSession.endedAt IS NOT NULL
            ORDER BY e.loggedAt DESC
            LIMIT 1
            """
    )
    fun findLatestSessionIdByUserIdAndExerciseName(
        @Param("userId") userId: Long,
        @Param("exerciseName") exerciseName: String
    ): Long?

    // ...and every set of that exercise from that one session, so the set table can show a
    // per-row "previous" instead of a single last set.
    fun findByWorkoutSessionIdAndExerciseNameOrderBySetNumberAsc(
        workoutSessionId: Long,
        exerciseName: String
    ): List<ExerciseLog>
}