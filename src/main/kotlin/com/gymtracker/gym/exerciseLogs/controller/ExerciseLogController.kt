package com.gymtracker.gym.exerciseLogs.controller

import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogRequest
import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogResponse
import com.gymtracker.gym.exerciseLogs.dto.ReorderExerciseLogsRequest
import com.gymtracker.gym.exerciseLogs.service.ExerciseLogService
import com.gymtracker.gym.users.annotation.CurrentUser
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/exercise-logs")
class ExerciseLogController(
    private val exerciseLogService: ExerciseLogService
) {

    @PostMapping
    fun createExerciseLog(
        @RequestBody @Valid exerciseLogRequest: ExerciseLogRequest,
        @CurrentUser userId: Long
    ): ResponseEntity<ExerciseLogResponse> {
        val exerciseLogResponse = exerciseLogService.createNewExerciseLog(exerciseLogRequest, userId)
        val location = URI.create("/api/exercise-logs/" + exerciseLogResponse.id)
        return ResponseEntity.created(location).body(exerciseLogResponse)
    }

    @DeleteMapping("/{id}")
    fun deleteExerciseLog(@PathVariable id: Long, @CurrentUser userId: Long): ResponseEntity<Void> {
        exerciseLogService.deleteExerciseLog(id, userId)
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/order")
    fun reorderExerciseLogs(
        @RequestBody @Valid request: ReorderExerciseLogsRequest,
        @CurrentUser userId: Long
    ): ResponseEntity<Void> {
        exerciseLogService.reorderExerciseLogs(request, userId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{workoutSessionId}")
    fun getAllExerciseLogsByWorkoutSessionId(
        @PathVariable workoutSessionId: Long,
        @CurrentUser userId: Long
    ): ResponseEntity<List<ExerciseLogResponse>> {
        return ResponseEntity.ok(
            exerciseLogService.getAllExerciseLogByIdWorkoutSessionId(
                workoutSessionId,
                userId
            )
        )
    }

    @GetMapping("/previous")
    fun getPreviousSessionLogs(
        @RequestParam(required = false) workoutTemplateId: Long?,
        @RequestParam(required = false) programDayId: Long?,
        @CurrentUser userId: Long
    ): ResponseEntity<List<ExerciseLogResponse>> {
        if (programDayId != null) {
            return ResponseEntity.ok(
                exerciseLogService.getPreviousSetsByProgramDayId(
                    programDayId,
                    userId
                )
            )
        }
        if (workoutTemplateId != null) {
            return ResponseEntity.ok(
                exerciseLogService.getPreviousSessionLogsByWorkoutTemplateId(
                    workoutTemplateId,
                    userId
                )
            )
        }
        return ResponseEntity.badRequest().build()
    }

}