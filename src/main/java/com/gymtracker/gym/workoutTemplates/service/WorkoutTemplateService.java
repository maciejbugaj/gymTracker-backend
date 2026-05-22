package com.gymtracker.gym.workoutTemplates.service;

import com.gymtracker.gym.templateExecrcises.dto.TemplateExerciseResponse;
import com.gymtracker.gym.workoutTemplates.dto.WorkoutTemplateResponse;
import com.gymtracker.gym.workoutTemplates.repository.WorkoutTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkoutTemplateService {

    private final WorkoutTemplateRepository workoutTemplateRepository;

    @Transactional(readOnly = true)
    public List<WorkoutTemplateResponse> getAllWorkoutTemplates() {
        return workoutTemplateRepository.findAll().stream()
                .map(workoutTemplate -> WorkoutTemplateResponse.builder()
                        .id(workoutTemplate.getId())
                        .name(workoutTemplate.getName())
                        .description(workoutTemplate.getDescription())
                        .exercises(workoutTemplate.getExercises().stream().map(templateExercise -> TemplateExerciseResponse.builder()
                                .id(templateExercise.getId())
                                .exerciseName(templateExercise.getExerciseName())
                                .build()).toList())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutTemplateResponse getWorkoutTemplateById(Long workoutTemplateId) {
        return workoutTemplateRepository.findById(workoutTemplateId).map(workoutTemplate -> WorkoutTemplateResponse.builder()
                .id(workoutTemplate.getId())
                .name(workoutTemplate.getName())
                .description(workoutTemplate.getDescription())
                .exercises(workoutTemplate.getExercises().stream().map(templateExercise -> TemplateExerciseResponse.builder()
                        .id(templateExercise.getId())
                        .exerciseName(templateExercise.getExerciseName())
                        .defaultSets(templateExercise.getDefaultSets())
                        .defaultReps(templateExercise.getDefaultReps())
                        .defaultWeight(templateExercise.getDefaultWeightKg())
                        .build()).toList())
                .build()).orElseThrow(() -> new RuntimeException("Template Not Found " + workoutTemplateId));
    }


}
