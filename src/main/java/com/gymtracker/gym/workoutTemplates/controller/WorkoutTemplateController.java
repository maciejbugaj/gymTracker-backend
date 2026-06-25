package com.gymtracker.gym.workoutTemplates.controller;

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
    public ResponseEntity<List<WorkoutTemplateResponse>> getAllWorkoutTemplate() {
        return ResponseEntity.ok(workoutTemplateService.getAllWorkoutTemplates());
    }

    @GetMapping("/{workoutTemplateId}")
    public ResponseEntity<WorkoutTemplateResponse> getWorkoutTemplateById(@PathVariable Long workoutTemplateId) {
        return workoutTemplateService.getWorkoutTemplateById(workoutTemplateId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<WorkoutTemplateResponse> createWorkoutTemplate(@Valid @RequestBody WorkoutTemplateRequest request) {
        WorkoutTemplateResponse response = workoutTemplateService.createWorkoutTemplate(request);
        URI location = URI.create("/api/workout-templates/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{workoutTemplateId}")
    public ResponseEntity<WorkoutTemplateResponse> updateWorkoutTemplate(
            @PathVariable Long workoutTemplateId,
            @Valid @RequestBody WorkoutTemplateRequest request) {
        return ResponseEntity.ok(workoutTemplateService.updateWorkoutTemplate(workoutTemplateId, request));
    }

    @DeleteMapping("/{workoutTemplateId}")
    public ResponseEntity<Void> deleteWorkoutTemplate(@PathVariable Long workoutTemplateId) {
        workoutTemplateService.deleteWorkoutTemplate(workoutTemplateId);
        return ResponseEntity.noContent().build();
    }
}
