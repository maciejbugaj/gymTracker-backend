package com.gymtracker.gym.exerciseLogs.controller;

import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogRequest;
import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogResponse;
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

    @GetMapping("/{workoutSessionId}")
    public ResponseEntity<List<ExerciseLogResponse>> getAllExerciseLogsByWorkoutSessionId(@PathVariable Long workoutSessionId,
                                                                                          @CurrentUser Long userId){
        return ResponseEntity.ok(exerciseLogService.getAllExerciseLogByIdWorkoutSessionId(workoutSessionId, userId));
    }

    @GetMapping("/previous")
    public ResponseEntity<List<ExerciseLogResponse>> getPreviousSessionLogs(@RequestParam Long workoutTemplateId,
                                                                            @CurrentUser Long userId) {
        return ResponseEntity.ok(exerciseLogService.getPreviousSessionLogsByWorkoutTemplateId(workoutTemplateId, userId));
    }
}
