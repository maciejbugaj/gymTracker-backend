package com.gymtracker.gym.templateExecrcises.controller;

import com.gymtracker.gym.templateExecrcises.dto.TemplateExerciseRequest;
import com.gymtracker.gym.templateExecrcises.dto.TemplateExerciseResponse;
import com.gymtracker.gym.templateExecrcises.service.TemplateExerciseService;
import com.gymtracker.gym.users.annotation.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@NullMarked
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/template-exercises")
public class TemplateExerciseController {

    private final TemplateExerciseService templateExerciseService;

    @PostMapping
    public ResponseEntity<TemplateExerciseResponse> createTemplateExercise(@Valid @RequestBody TemplateExerciseRequest request,
                                                                           @CurrentUser Long userId) {
        TemplateExerciseResponse response = templateExerciseService.createTemplateExercise(request, userId);
        URI location = URI.create("/api/template-exercises/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping("/{exerciseId}")
    public ResponseEntity<TemplateExerciseResponse> updateTemplateExercise(
            @PathVariable Long exerciseId,
            @Valid @RequestBody TemplateExerciseRequest request,
            @CurrentUser Long userId) {
        return ResponseEntity.ok(templateExerciseService.updateTemplateExercise(exerciseId, request, userId));
    }

    @DeleteMapping("/{exerciseId}")
    public ResponseEntity<Void> deleteTemplateExercise(@PathVariable Long exerciseId, @CurrentUser Long userId) {
        templateExerciseService.deleteTemplateExercise(exerciseId, userId);
        return ResponseEntity.noContent().build();
    }
}
