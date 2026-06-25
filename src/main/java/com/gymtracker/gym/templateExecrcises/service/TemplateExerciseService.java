package com.gymtracker.gym.templateExecrcises.service;

import com.gymtracker.gym.exceptions.NotFoundException;
import com.gymtracker.gym.templateExecrcises.dto.TemplateExerciseRequest;
import com.gymtracker.gym.templateExecrcises.dto.TemplateExerciseResponse;
import com.gymtracker.gym.templateExecrcises.mapper.TemplateExerciseMapper;
import com.gymtracker.gym.templateExecrcises.model.TemplateExercise;
import com.gymtracker.gym.templateExecrcises.repository.TemplateExerciseRepository;
import com.gymtracker.gym.workoutTemplates.model.WorkoutTemplate;
import com.gymtracker.gym.workoutTemplates.repository.WorkoutTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TemplateExerciseService {

    private final TemplateExerciseRepository templateExerciseRepository;
    private final WorkoutTemplateRepository workoutTemplateRepository;
    private final TemplateExerciseMapper templateExerciseMapper;

    @Transactional
    public TemplateExerciseResponse createTemplateExercise(TemplateExerciseRequest request) {
        WorkoutTemplate workoutTemplate = workoutTemplateRepository.findById(request.getWorkoutTemplateId())
                .orElseThrow(() -> new NotFoundException("Workout template with id: " + request.getWorkoutTemplateId() + " not found"));

        TemplateExercise templateExercise = TemplateExercise.builder()
                .workoutTemplate(workoutTemplate)
                .exerciseName(request.getExerciseName())
                .defaultSets(request.getDefaultSets())
                .defaultReps(request.getDefaultReps())
                .defaultWeightKg(request.getDefaultWeightKg())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        return templateExerciseMapper.toResponse(templateExerciseRepository.save(templateExercise));
    }

    @Transactional
    public TemplateExerciseResponse updateTemplateExercise(Long exerciseId, TemplateExerciseRequest request) {
        TemplateExercise templateExercise = templateExerciseRepository.findById(exerciseId)
                .orElseThrow(() -> new NotFoundException("Template exercise with id: " + exerciseId + " not found"));

        templateExercise.setExerciseName(request.getExerciseName());
        templateExercise.setDefaultSets(request.getDefaultSets());
        templateExercise.setDefaultReps(request.getDefaultReps());
        templateExercise.setDefaultWeightKg(request.getDefaultWeightKg());
        if (request.getSortOrder() != null) {
            templateExercise.setSortOrder(request.getSortOrder());
        }

        return templateExerciseMapper.toResponse(templateExerciseRepository.save(templateExercise));
    }

    @Transactional
    public void deleteTemplateExercise(Long exerciseId) {
        if (!templateExerciseRepository.existsById(exerciseId)) {
            throw new NotFoundException("Template exercise with id: " + exerciseId + " not found");
        }
        templateExerciseRepository.deleteById(exerciseId);
    }
}
