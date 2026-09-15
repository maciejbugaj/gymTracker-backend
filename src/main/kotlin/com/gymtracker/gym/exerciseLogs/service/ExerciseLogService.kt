package com.gymtracker.gym.exerciseLogs.service

import com.gymtracker.gym.exceptions.ConflictException
import com.gymtracker.gym.exceptions.NotFoundException
import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogRequest
import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogResponse
import com.gymtracker.gym.exerciseLogs.dto.ReorderExerciseLogsRequest
import com.gymtracker.gym.exerciseLogs.mapper.toResponse
import com.gymtracker.gym.exerciseLogs.model.ExerciseLog
import com.gymtracker.gym.exerciseLogs.repository.ExerciseLogRepository
import com.gymtracker.gym.trainingPrograms.repository.ProgramDayRepository
import com.gymtracker.gym.workoutSessions.repository.WorkoutSessionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ExerciseLogService(
    private val exerciseLogRepository: ExerciseLogRepository,
    private val workoutSessionRepository: WorkoutSessionRepository,
    private val programDayRepository: ProgramDayRepository,
) {

    @Transactional
    fun createNewExerciseLog(exerciseLogRequest: ExerciseLogRequest, userId: Long): ExerciseLogResponse {
        val session = workoutSessionRepository
            .findByIdAndUserId(exerciseLogRequest.workoutSessionId, userId)
            .orElseThrow {
                NotFoundException("Workout Session with id: ${exerciseLogRequest.workoutSessionId} not found")
            }

        // The set table sends the row position it is ticking; only older clients (and Swagger)
        // leave it out, and those keep the append-at-the-end behaviour.
        val nextSetNumber = exerciseLogRequest.setNumber
            ?: exerciseLogRepository
                .findLatestBySessionAndExerciseName(session, exerciseLogRequest.exerciseName)
                ?.let { it.setNumber + 1 }
            ?: 1

        val exerciseLog = ExerciseLog().apply {
            loggedAt = LocalDateTime.now()
            exerciseName = exerciseLogRequest.exerciseName
            setNumber = nextSetNumber
            reps = exerciseLogRequest.reps
            weightKg = exerciseLogRequest.weightKg
            workoutSession = session
        }

        return exerciseLogRepository.save(exerciseLog).toResponse()
    }

    @Transactional
    fun deleteExerciseLog(exerciseLogId: Long, userId: Long) {
        val exerciseLog = exerciseLogRepository.findById(exerciseLogId)
            .filter { it.workoutSession?.userId == userId }
            .orElseThrow { NotFoundException("Exercise log with id: $exerciseLogId not found") }

        exerciseLogRepository.delete(exerciseLog)
    }

    /**
     * Renumbers the sets of one exercise to 1..n in the given order. Needed because the user can
     * drop a set row from the middle of the table, which shifts every row below it up.
     */
    @Transactional
    fun reorderExerciseLogs(request: ReorderExerciseLogsRequest, userId: Long) {
        val workoutSession = workoutSessionRepository
            .findByIdAndUserId(request.workoutSessionId, userId)
            .orElseThrow {
                NotFoundException("Workout Session with id: ${request.workoutSessionId} not found")
            }

        val savedLogs = exerciseLogRepository
            .findByWorkoutSessionAndExerciseNameOrderBySetNumberAsc(workoutSession, request.exerciseName)

        val requestedIds = request.logIds.toSet()
        val savedIds = savedLogs.mapNotNull { it.id }.toSet()
        if (requestedIds.size != request.logIds.size || requestedIds != savedIds) {
            throw ConflictException(
                "Reorder must list every saved set of ${request.exerciseName} in this session exactly once"
            )
        }

        val logsById = savedLogs.associateBy { it.id }

        // Two passes: park the rows on negative numbers first, otherwise shifting one set onto a
        // number another row still holds trips the unique index mid-update.
        var parking = -1
        for (logId in requestedIds) {
            logsById.getValue(logId).setNumber = parking--
        }
        exerciseLogRepository.flush()

        var setNumber = 1
        for (logId in requestedIds) {
            logsById.getValue(logId).setNumber = setNumber++
        }
        exerciseLogRepository.flush()
    }

    @Transactional(readOnly = true)
    fun getAllExerciseLogByIdWorkoutSessionId(workoutSessionId: Long, userId: Long): List<ExerciseLogResponse> {
        val workoutSession = workoutSessionRepository
            .findByIdAndUserId(workoutSessionId, userId)
            .orElseThrow { NotFoundException("Workout Session with id: $workoutSessionId not found") }

        return exerciseLogRepository.findAllByWorkoutSession(workoutSession)
            .map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getPreviousSessionLogsByWorkoutTemplateId(workoutTemplateId: Long, userId: Long): List<ExerciseLogResponse> =
        workoutSessionRepository
            .findTopByWorkoutTemplateIdAndEndedAtIsNotNullAndUserIdOrderByEndedAtDesc(workoutTemplateId, userId)
            .map { session ->
                exerciseLogRepository.findByWorkoutSessionOrderBySetNumberAsc(session)
                    .map { it.toResponse() }
            }
            .orElse(emptyList())

    /**
     * Per exercise in the program day, every set from the last finished session in which the user
     * logged that exact exercise name — regardless of which session/template it came from, so the
     * set table can show a per-row "previous". An exercise the user has never logged before is
     * simply absent from the result (no entries, not null ones).
     */
    @Transactional(readOnly = true)
    fun getPreviousSetsByProgramDayId(programDayId: Long, userId: Long): List<ExerciseLogResponse> {
        val programDay = programDayRepository.findByIdAndUserId(programDayId, userId)
            .orElseThrow { NotFoundException("Program day $programDayId not found") }

        return programDay.programDayExercises
            .flatMap { previousSetsForExercise(userId, it.exerciseName) }
            .map { it.toResponse() }
    }

    private fun previousSetsForExercise(userId: Long, exerciseName: String): List<ExerciseLog> =
        exerciseLogRepository.findLatestSessionIdByUserIdAndExerciseName(userId, exerciseName)
            ?.let { exerciseLogRepository.findByWorkoutSessionIdAndExerciseNameOrderBySetNumberAsc(it, exerciseName) }
            ?: emptyList()
}
