package com.gymtracker.gym.exerciseLogs.controller;

import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogRequest;
import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogResponse;
import com.gymtracker.gym.exerciseLogs.service.ExerciseLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exercise-log")
public class ExerciseLogController {

    private final ExerciseLogService exerciseLogService;

    @PostMapping
    public ExerciseLogResponse createExerciseLog(@RequestBody ExerciseLogRequest exerciseLogRequest) {
        return exerciseLogService.createNewExerciseLog(exerciseLogRequest);
    }

    @GetMapping("/{workoutSessionId}")
    public List<ExerciseLogResponse> getAllExerciseLogsByWorkoutSessionId(@PathVariable Long workoutSessionId){
        return exerciseLogService.getAllExerciseLogByIdWorkoutSessionId(workoutSessionId);
    }

    @GetMapping("/previous")
    public ResponseEntity<List<ExerciseLogResponse>> getPreviousSessionLogs(@RequestParam Long workoutTemplateId) {
        return ResponseEntity.ok(exerciseLogService.getPreviousSessionLogsByWorkoutTemplateId(workoutTemplateId));
    }
}
