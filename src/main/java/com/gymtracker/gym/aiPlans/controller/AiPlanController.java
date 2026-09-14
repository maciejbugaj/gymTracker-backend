package com.gymtracker.gym.aiPlans.controller;

import com.gymtracker.gym.aiPlans.dto.AiPlanGenerationResponse;
import com.gymtracker.gym.aiPlans.dto.GeneratePlanRequest;
import com.gymtracker.gym.aiPlans.dto.RegeneratePlanRequest;
import com.gymtracker.gym.aiPlans.service.AiPlanGenerationService;
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
@RequestMapping("/api/ai-plans")
public class AiPlanController {

    private final AiPlanGenerationService aiPlanGenerationService;

    @PostMapping("/generations")
    public ResponseEntity<AiPlanGenerationResponse> startGeneration(@Valid @RequestBody GeneratePlanRequest request,
                                                                     @CurrentUser Long userId) {
        AiPlanGenerationResponse response = aiPlanGenerationService.startGeneration(userId, request);
        return ResponseEntity.accepted()
                .location(URI.create("/api/ai-plans/generations/" + response.id()))
                .body(response);
    }

    @GetMapping("/generations/{id}")
    public ResponseEntity<AiPlanGenerationResponse> getGeneration(@PathVariable Long id, @CurrentUser Long userId) {
        return ResponseEntity.ok(aiPlanGenerationService.getGeneration(userId, id));
    }

    @GetMapping("/generations")
    public ResponseEntity<List<AiPlanGenerationResponse>> listGenerations(@CurrentUser Long userId) {
        return ResponseEntity.ok(aiPlanGenerationService.listGenerations(userId));
    }

    @PostMapping("/generations/{id}/regenerate")
    public ResponseEntity<AiPlanGenerationResponse> regenerate(@PathVariable Long id,
                                                                @Valid @RequestBody RegeneratePlanRequest request,
                                                                @CurrentUser Long userId) {
        AiPlanGenerationResponse response = aiPlanGenerationService.regenerate(userId, id, request.feedback());
        return ResponseEntity.accepted()
                .location(URI.create("/api/ai-plans/generations/" + response.id()))
                .body(response);
    }
}
