package com.gymtracker.gym.workoutTemplates.service;

import com.gymtracker.gym.templateExecrcises.dto.TemplateExerciseResponse;
import com.gymtracker.gym.templateExecrcises.model.TemplateExercise;
import com.gymtracker.gym.workoutTemplates.dto.WorkoutTemplateResponse;
import com.gymtracker.gym.workoutTemplates.model.WorkoutTemplate;
import com.gymtracker.gym.workoutTemplates.repository.WorkoutTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkoutTemplateService {

    private final WorkoutTemplateRepository workoutTemplateRepository;

    @Transactional(readOnly = true)
    public List<WorkoutTemplateResponse> getAllWorkoutTemplates() {
        return workoutTemplateRepository.findAll().stream()
                .map(this::toWorkoutTemplateResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<WorkoutTemplateResponse> getWorkoutTemplateById(Long workoutTemplateId) {
        return workoutTemplateRepository.findById(workoutTemplateId).map(this::toWorkoutTemplateResponse);
    }

    private WorkoutTemplateResponse toWorkoutTemplateResponse(WorkoutTemplate workoutTemplate) {
        return WorkoutTemplateResponse.builder()
                .id(workoutTemplate.getId())
                .name(workoutTemplate.getName())
                .description(workoutTemplate.getDescription())
                .exercises(workoutTemplate.getExercises().stream().map(this::toTemplateExerciseResponse).toList())
                .build();
    }

    private TemplateExerciseResponse toTemplateExerciseResponse(TemplateExercise templateExercise) {
        return TemplateExerciseResponse.builder()
                .id(templateExercise.getId())
                .exerciseName(templateExercise.getExerciseName())
                .defaultSets(templateExercise.getDefaultSets())
                .defaultReps(templateExercise.getDefaultReps())
                .defaultWeight(templateExercise.getDefaultWeightKg())
                .build();
    }
}
