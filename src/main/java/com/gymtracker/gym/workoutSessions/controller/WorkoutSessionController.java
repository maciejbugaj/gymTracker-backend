package com.gymtracker.gym.workoutSessions.controller;

import com.gymtracker.gym.users.annotation.CurrentUser;
import com.gymtracker.gym.workoutSessions.dto.WorkoutSessionRequest;
import com.gymtracker.gym.workoutSessions.dto.WorkoutSessionResponse;
import com.gymtracker.gym.workoutSessions.service.WorkoutSessionService;
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
@RequestMapping("/api/workout-sessions")
public class WorkoutSessionController {

    private final WorkoutSessionService workoutSessionService;

    @GetMapping
    public ResponseEntity<List<WorkoutSessionResponse>> getAllWorkoutSessions(@CurrentUser Long userId) {
        return ResponseEntity.ok(workoutSessionService.getAllWorkoutSessions(userId));
    }

    @PostMapping
    public ResponseEntity<WorkoutSessionResponse> createWorkoutSession(@Valid @RequestBody WorkoutSessionRequest workoutSessionRequest, @CurrentUser Long userId) {
        WorkoutSessionResponse workoutSessionResponse = workoutSessionService.createWorkoutSession(workoutSessionRequest, userId);
        URI location = URI.create("/api/workout-sessions/" + workoutSessionResponse.id());
        return ResponseEntity.created(location).body(workoutSessionResponse);
    }

    @GetMapping("/{workoutSessionId}")
    public ResponseEntity<WorkoutSessionResponse> getWorkoutSessionById(@PathVariable Long workoutSessionId, @CurrentUser Long userId) {
        return workoutSessionService.getWorkoutSessionById(workoutSessionId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/last")
    public ResponseEntity<WorkoutSessionResponse> getLatestWorkoutSession(@CurrentUser Long userId) {
        return workoutSessionService.getLatestWorkoutSession(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/last/ongoing")
    public ResponseEntity<WorkoutSessionResponse> getLatestOngoingWorkoutSession(@CurrentUser Long userId) {
        return workoutSessionService.getLatestOngoingWorkoutSession(userId).map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/{workoutSessionId}/finish")
    public ResponseEntity<WorkoutSessionResponse> finishWorkoutSession(@PathVariable Long workoutSessionId, @CurrentUser Long userId) {
        return ResponseEntity.ok(workoutSessionService.finishWorkoutSession(workoutSessionId, userId));
    }

}
