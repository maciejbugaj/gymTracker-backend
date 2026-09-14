package com.gymtracker.gym.exerciseLogs.controller;

import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogRequest;
import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogResponse;
import com.gymtracker.gym.exerciseLogs.dto.ReorderExerciseLogsRequest;
import com.gymtracker.gym.exerciseLogs.service.ExerciseLogService;
import com.gymtracker.gym.users.annotation.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@NullMarked
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exercise-logs")
public class ExerciseLogController {

    private final ExerciseLogService exerciseLogService;

    @PostMapping
    public ResponseEntity<ExerciseLogResponse> createExerciseLog(@Valid @RequestBody ExerciseLogRequest exerciseLogRequest,
                                                                 @CurrentUser Long userId) {
        ExerciseLogResponse exerciseLogResponse = exerciseLogService.createNewExerciseLog(exerciseLogRequest, userId);
        URI location = URI.create("/api/exercise-logs/" + exerciseLogResponse.id());
        return ResponseEntity.created(location).body(exerciseLogResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExerciseLog(@PathVariable Long id, @CurrentUser Long userId) {
        exerciseLogService.deleteExerciseLog(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/order")
    public ResponseEntity<Void> reorderExerciseLogs(@Valid @RequestBody ReorderExerciseLogsRequest request,
                                                    @CurrentUser Long userId) {
        exerciseLogService.reorderExerciseLogs(request, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{workoutSessionId}")
    public ResponseEntity<List<ExerciseLogResponse>> getAllExerciseLogsByWorkoutSessionId(@PathVariable Long workoutSessionId,
                                                                                          @CurrentUser Long userId){
        return ResponseEntity.ok(exerciseLogService.getAllExerciseLogByIdWorkoutSessionId(workoutSessionId, userId));
    }

    @GetMapping("/previous")
    public ResponseEntity<List<ExerciseLogResponse>> getPreviousSessionLogs(
            @RequestParam(required = false) Long workoutTemplateId,
            @RequestParam(required = false) Long programDayId,
            @CurrentUser Long userId) {
        if (programDayId != null) {
            return ResponseEntity.ok(exerciseLogService.getPreviousSetsByProgramDayId(programDayId, userId));
        }
        if (workoutTemplateId != null) {
            return ResponseEntity.ok(exerciseLogService.getPreviousSessionLogsByWorkoutTemplateId(workoutTemplateId, userId));
        }
        return ResponseEntity.badRequest().build();
    }
}
