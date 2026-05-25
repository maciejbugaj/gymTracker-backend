package com.gymtracker.gym.exerciseLogs.controller;

import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogRequest;
import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogResponse;
import com.gymtracker.gym.exerciseLogs.service.ExerciseLogService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@NullMarked
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exercise-log")
public class ExerciseLogController {

    private final ExerciseLogService exerciseLogService;

    @PostMapping
    public ResponseEntity<ExerciseLogResponse> createExerciseLog(@RequestBody ExerciseLogRequest exerciseLogRequest) {
        return ResponseEntity.ok(exerciseLogService.createNewExerciseLog(exerciseLogRequest));
    }

    @GetMapping("/{workoutSessionId}")
    public ResponseEntity<List<ExerciseLogResponse>> getAllExerciseLogsByWorkoutSessionId(@PathVariable Long workoutSessionId){
        return ResponseEntity.ok(exerciseLogService.getAllExerciseLogByIdWorkoutSessionId(workoutSessionId));
    }

    @GetMapping("/previous")
    public ResponseEntity<List<ExerciseLogResponse>> getPreviousSessionLogs(@RequestParam Long workoutTemplateId) {
        return ResponseEntity.ok(exerciseLogService.getPreviousSessionLogsByWorkoutTemplateId(workoutTemplateId));
    }
}
