package com.gymtracker.gym.templateExercises.controller

import com.gymtracker.gym.templateExercises.dto.TemplateExerciseRequest
import com.gymtracker.gym.templateExercises.dto.TemplateExerciseResponse
import com.gymtracker.gym.templateExercises.service.TemplateExerciseService
import com.gymtracker.gym.users.annotation.CurrentUser
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/template-exercises")
class TemplateExerciseController(
    private val templateExerciseService: TemplateExerciseService
) {

    @PostMapping
    fun createTemplateExercise(
        @Valid @RequestBody request: TemplateExerciseRequest,
        @CurrentUser userId: Long
    ): ResponseEntity<TemplateExerciseResponse> {
        val response = templateExerciseService.createTemplateExercise(request, userId)
        val location = URI.create("/api/template-exercises/${response.id}")
        return ResponseEntity.created(location).body(response)
    }

    @PutMapping("/{exerciseId}")
    fun updateTemplateExercise(
        @PathVariable exerciseId: Long, @Valid @RequestBody request: TemplateExerciseRequest, @CurrentUser userId: Long
    ): ResponseEntity<TemplateExerciseResponse> {
        return ResponseEntity.ok(templateExerciseService.updateTemplateExercise(exerciseId, request, userId))
    }

    @DeleteMapping("/{exerciseId}")
    fun deleteTemplateExercise(@PathVariable exerciseId: Long, @CurrentUser userId: Long): ResponseEntity<Void> {
        templateExerciseService.deleteTemplateExercise(exerciseId, userId)
        return ResponseEntity.noContent().build()
    }

}