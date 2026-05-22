package com.gymtracker.gym.workoutSessions.controller;

import com.gymtracker.gym.workoutSessions.dto.WorkoutSessionRequest;
import com.gymtracker.gym.workoutSessions.dto.WorkoutSessionResponse;
import com.gymtracker.gym.workoutSessions.service.WorkoutSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workout-sessions")
public class WorkoutSessionController {

    private final WorkoutSessionService workoutSessionService;

    @GetMapping
    public List<WorkoutSessionResponse> getAllWorkoutSessions() {
        return workoutSessionService.getAllWorkoutSessions();
    }

    @PostMapping
    public WorkoutSessionResponse createWorkoutSession(@RequestBody WorkoutSessionRequest workoutSessionRequest) {
        return workoutSessionService.createWorkoutSession(workoutSessionRequest);
    }

    @GetMapping
    @RequestMapping("/last")
    public WorkoutSessionResponse getLatestWorkoutSession() {
        return workoutSessionService.getLatestWorkoutSession();
    }

    @GetMapping("/last/ongoing")
    public ResponseEntity<WorkoutSessionResponse> getLatestOngoingWorkoutSession() {
        return workoutSessionService.getLatestOngoingWorkoutSession().map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{workoutSessionId}/finish")
    public WorkoutSessionResponse finishWorkoutSession(@PathVariable Long workoutSessionId) {
        return workoutSessionService.finishWorkoutSession(workoutSessionId);
    }

}
