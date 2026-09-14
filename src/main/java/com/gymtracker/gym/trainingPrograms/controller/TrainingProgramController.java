package com.gymtracker.gym.trainingPrograms.controller;

import com.gymtracker.gym.trainingPrograms.dto.ActiveProgramResponse;
import com.gymtracker.gym.trainingPrograms.dto.TrainingProgramRequest;
import com.gymtracker.gym.trainingPrograms.dto.TrainingProgramResponse;
import com.gymtracker.gym.trainingPrograms.dto.TrainingProgramSummaryResponse;
import com.gymtracker.gym.trainingPrograms.service.TrainingProgramService;
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
@RequestMapping("/api/training-programs")
public class TrainingProgramController {

    private final TrainingProgramService trainingProgramService;

    @GetMapping
    public ResponseEntity<List<TrainingProgramSummaryResponse>> listPrograms(@CurrentUser Long userId) {
        return ResponseEntity.ok(trainingProgramService.listPrograms(userId));
    }

    @GetMapping("/active")
    public ResponseEntity<ActiveProgramResponse> getActive(@CurrentUser Long userId) {
        return trainingProgramService.getActiveWithNextDay(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TrainingProgramResponse> getProgram(@PathVariable Long id, @CurrentUser Long userId) {
        return ResponseEntity.ok(trainingProgramService.getProgram(userId, id));
    }

    @PostMapping("/from-generation/{generationId}")
    public ResponseEntity<TrainingProgramResponse> createFromGeneration(@PathVariable Long generationId,
                                                                         @Valid @RequestBody TrainingProgramRequest request,
                                                                         @CurrentUser Long userId) {
        TrainingProgramResponse response = trainingProgramService.createFromGeneration(userId, generationId, request);
        return ResponseEntity.created(URI.create("/api/training-programs/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TrainingProgramResponse> updateProgram(@PathVariable Long id,
                                                                  @Valid @RequestBody TrainingProgramRequest request,
                                                                  @CurrentUser Long userId) {
        return ResponseEntity.ok(trainingProgramService.updateProgram(userId, id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProgram(@PathVariable Long id, @CurrentUser Long userId) {
        trainingProgramService.deleteProgram(userId, id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<TrainingProgramResponse> activate(@PathVariable Long id, @CurrentUser Long userId) {
        return ResponseEntity.ok(trainingProgramService.activate(userId, id));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<TrainingProgramResponse> archive(@PathVariable Long id, @CurrentUser Long userId) {
        return ResponseEntity.ok(trainingProgramService.archive(userId, id));
    }
}
