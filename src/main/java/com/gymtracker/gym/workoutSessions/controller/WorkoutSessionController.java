package com.gymtracker.gym.workoutSessions.controller;

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
    public ResponseEntity<List<WorkoutSessionResponse>> getAllWorkoutSessions() {
        return ResponseEntity.ok(workoutSessionService.getAllWorkoutSessions());
    }

    @PostMapping
    public ResponseEntity<WorkoutSessionResponse> createWorkoutSession(@Valid @RequestBody WorkoutSessionRequest workoutSessionRequest) {
        WorkoutSessionResponse workoutSessionResponse = workoutSessionService.createWorkoutSession(workoutSessionRequest);
        URI location = URI.create("/api/workout-sessions/" + workoutSessionResponse.id());
        return ResponseEntity.created(location).body(workoutSessionResponse);
    }

    @GetMapping("/{workoutSessionId}")
    public ResponseEntity<WorkoutSessionResponse> getWorkoutSessionById(@PathVariable Long workoutSessionId) {
        return workoutSessionService.getWorkoutSessionById(workoutSessionId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/last")
    public ResponseEntity<WorkoutSessionResponse> getLatestWorkoutSession() {
        return workoutSessionService.getLatestWorkoutSession()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/last/ongoing")
    public ResponseEntity<WorkoutSessionResponse> getLatestOngoingWorkoutSession() {
        return workoutSessionService.getLatestOngoingWorkoutSession().map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/{workoutSessionId}/finish")
    public ResponseEntity<WorkoutSessionResponse> finishWorkoutSession(@PathVariable Long workoutSessionId) {
        return ResponseEntity.ok(workoutSessionService.finishWorkoutSession(workoutSessionId));
    }

}
