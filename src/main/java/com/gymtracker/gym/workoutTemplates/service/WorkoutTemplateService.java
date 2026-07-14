package com.gymtracker.gym.workoutTemplates.service;

import com.gymtracker.gym.exceptions.NotFoundException;
import com.gymtracker.gym.workoutTemplates.dto.WorkoutTemplateRequest;
import com.gymtracker.gym.workoutTemplates.dto.WorkoutTemplateResponse;
import com.gymtracker.gym.workoutTemplates.mapper.WorkoutTemplateMapper;
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
    private final WorkoutTemplateMapper workoutTemplateMapper;

    @Transactional(readOnly = true)
    public List<WorkoutTemplateResponse> getAllWorkoutTemplates() {
        return workoutTemplateRepository.findAllWithExercises().stream()
                .map(workoutTemplateMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<WorkoutTemplateResponse> getWorkoutTemplateById(Long workoutTemplateId) {
        return workoutTemplateRepository.findByIdWithExercises(workoutTemplateId).map(workoutTemplateMapper::toResponse);
    }

    @Transactional
    public WorkoutTemplateResponse createWorkoutTemplate(WorkoutTemplateRequest request) {
        WorkoutTemplate workoutTemplate = WorkoutTemplate.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return workoutTemplateMapper.toResponse(workoutTemplateRepository.save(workoutTemplate));
    }

    @Transactional
    public WorkoutTemplateResponse updateWorkoutTemplate(Long workoutTemplateId, WorkoutTemplateRequest request) {
        WorkoutTemplate workoutTemplate = workoutTemplateRepository.findByIdWithExercises(workoutTemplateId)
                .orElseThrow(() -> new NotFoundException("Workout template with id: " + workoutTemplateId + " not found"));
        workoutTemplate.setName(request.getName());
        workoutTemplate.setDescription(request.getDescription());
        return workoutTemplateMapper.toResponse(workoutTemplateRepository.save(workoutTemplate));
    }

    @Transactional
    public void deleteWorkoutTemplate(Long workoutTemplateId) {
        if (!workoutTemplateRepository.existsById(workoutTemplateId)) {
            throw new NotFoundException("Workout template with id: " + workoutTemplateId + " not found");
        }
        workoutTemplateRepository.deleteById(workoutTemplateId);
    }
}
