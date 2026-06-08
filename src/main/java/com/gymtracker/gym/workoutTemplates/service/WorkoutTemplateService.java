package com.gymtracker.gym.workoutTemplates.service;

import com.gymtracker.gym.workoutTemplates.dto.WorkoutTemplateResponse;
import com.gymtracker.gym.workoutTemplates.mapper.WorkoutTemplateMapper;
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
        return workoutTemplateRepository.findAll().stream()
                .map(workoutTemplateMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<WorkoutTemplateResponse> getWorkoutTemplateById(Long workoutTemplateId) {
        return workoutTemplateRepository.findById(workoutTemplateId).map(workoutTemplateMapper::toResponse);
    }
}
