package com.gymtracker.gym.workoutTemplates.controller;

import com.gymtracker.gym.users.annotation.CurrentUser;
import com.gymtracker.gym.workoutTemplates.dto.WorkoutTemplateRequest;
import com.gymtracker.gym.workoutTemplates.dto.WorkoutTemplateResponse;
import com.gymtracker.gym.workoutTemplates.service.WorkoutTemplateService;
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
@RequestMapping("/api/workout-templates")
public class WorkoutTemplateController {

    private final WorkoutTemplateService workoutTemplateService;

    @GetMapping
    public ResponseEntity<List<WorkoutTemplateResponse>> getAllWorkoutTemplate(@CurrentUser Long userId) {
        return ResponseEntity.ok(workoutTemplateService.getAllWorkoutTemplates(userId));
    }

    @GetMapping("/{workoutTemplateId}")
    public ResponseEntity<WorkoutTemplateResponse> getWorkoutTemplateById(@PathVariable Long workoutTemplateId,
                                                                          @CurrentUser Long userId) {
        return workoutTemplateService.getWorkoutTemplateById(workoutTemplateId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<WorkoutTemplateResponse> createWorkoutTemplate(@Valid @RequestBody WorkoutTemplateRequest request,
                                                                         @CurrentUser Long userId) {
        WorkoutTemplateResponse response = workoutTemplateService.createWorkoutTemplate(request, userId);
        URI location = URI.create("/api/workout-templates/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{workoutTemplateId}")
    public ResponseEntity<WorkoutTemplateResponse> updateWorkoutTemplate(
            @PathVariable Long workoutTemplateId,
            @Valid @RequestBody WorkoutTemplateRequest request,
            @CurrentUser Long userId) {
        return ResponseEntity.ok(workoutTemplateService.updateWorkoutTemplate(workoutTemplateId, request, userId));
    }

    @DeleteMapping("/{workoutTemplateId}")
    public ResponseEntity<Void> deleteWorkoutTemplate(@PathVariable Long workoutTemplateId,
                                                      @CurrentUser Long userId) {
        workoutTemplateService.deleteWorkoutTemplate(workoutTemplateId, userId);
        return ResponseEntity.noContent().build();
    }
}
