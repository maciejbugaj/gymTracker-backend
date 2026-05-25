package com.gymtracker.gym.workoutTemplates.controller;

import com.gymtracker.gym.workoutTemplates.dto.WorkoutTemplateResponse;
import com.gymtracker.gym.workoutTemplates.service.WorkoutTemplateService;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
