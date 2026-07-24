package com.gymtracker.gym.workoutTemplates.service;

import com.gymtracker.gym.exceptions.NotFoundException;
import com.gymtracker.gym.users.annotation.CurrentUser;
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
    public List<WorkoutTemplateResponse> getAllWorkoutTemplates(Long userId) {
        return workoutTemplateRepository.findAllWithExercises(userId).stream()
                .map(workoutTemplateMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<WorkoutTemplateResponse> getWorkoutTemplateById(Long workoutTemplateId, Long userId) {
        return workoutTemplateRepository.findByIdWithExercises(workoutTemplateId, userId).map(workoutTemplateMapper::toResponse);
    }

    @Transactional
    public WorkoutTemplateResponse createWorkoutTemplate(WorkoutTemplateRequest request, Long userId) {
        WorkoutTemplate workoutTemplate = WorkoutTemplate.builder()
                .name(request.getName())
                .description(request.getDescription())
                .userId(userId)
                .build();
        return workoutTemplateMapper.toResponse(workoutTemplateRepository.save(workoutTemplate));
    }

    @Transactional
    public WorkoutTemplateResponse updateWorkoutTemplate(Long workoutTemplateId, WorkoutTemplateRequest request, Long userId) {
        WorkoutTemplate workoutTemplate = workoutTemplateRepository.findByIdWithExercises(workoutTemplateId, userId)
                .orElseThrow(() -> new NotFoundException("Workout template with id: " + workoutTemplateId + " not found"));
        workoutTemplate.setName(request.getName());
        workoutTemplate.setDescription(request.getDescription());
        return workoutTemplateMapper.toResponse(workoutTemplateRepository.save(workoutTemplate));
    }

    @Transactional
    public void deleteWorkoutTemplate(Long workoutTemplateId, Long userId) {
        if (!workoutTemplateRepository.existsByIdAndUserId(workoutTemplateId, userId)) {
            throw new NotFoundException("Workout template with id: " + workoutTemplateId + " not found");
        }
        workoutTemplateRepository.deleteByIdAndUserId(workoutTemplateId, userId);
    }
}
