package com.gymtracker.gym.workoutSessions.service;

import com.gymtracker.gym.exceptions.ConflictException;
import com.gymtracker.gym.exceptions.NotFoundException;
import com.gymtracker.gym.workoutSessions.dto.WorkoutSessionRequest;
import com.gymtracker.gym.workoutSessions.dto.WorkoutSessionResponse;
import com.gymtracker.gym.workoutSessions.mapper.WorkoutSessionMapper;
import com.gymtracker.gym.workoutSessions.model.WorkoutSession;
import com.gymtracker.gym.workoutSessions.repository.WorkoutSessionRepository;
import com.gymtracker.gym.workoutTemplates.model.WorkoutTemplate;
import com.gymtracker.gym.workoutTemplates.repository.WorkoutTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkoutSessionService {

    private final WorkoutSessionRepository workoutSessionRepository;
    private final WorkoutTemplateRepository workoutTemplateRepository;
    private final WorkoutSessionMapper workoutSessionMapper;

    @Transactional(readOnly = true)
    public Optional<WorkoutSessionResponse> getWorkoutSessionById(Long workoutSessionId) {
        return workoutSessionRepository.findByIdWithExerciseLogsAndTemplate(workoutSessionId).map(workoutSessionMapper::toResponse);
    }

    @Transactional
    public WorkoutSessionResponse createWorkoutSession(WorkoutSessionRequest workoutSessionRequest) {
        WorkoutTemplate workoutTemplate = workoutTemplateRepository.findById(workoutSessionRequest.getWorkoutTemplateId())
                .orElseThrow(() -> new NotFoundException("Template Not Found " + workoutSessionRequest.getWorkoutTemplateId()));

        WorkoutSession workoutSession = WorkoutSession.builder()
                .workoutTemplate(workoutTemplate)
                .startedAt(LocalDateTime.now())
                .build();
        return workoutSessionMapper.toResponse(workoutSessionRepository.save(workoutSession));
    }

    @Transactional
    public WorkoutSessionResponse finishWorkoutSession(Long workoutSessionId) {
        WorkoutSession workoutSession = workoutSessionRepository.findByIdWithExerciseLogsAndTemplate(workoutSessionId)
                .orElseThrow(() -> new NotFoundException("Workout Session with Id: " + workoutSessionId + " not found"));

        if (workoutSession.getEndedAt() != null) {
            throw new ConflictException("Workout session already finished: " + workoutSessionId);
        }

        workoutSession.setEndedAt(LocalDateTime.now());
        workoutSession.setDurationSeconds((int) Duration.between(workoutSession.getStartedAt(), workoutSession.getEndedAt()).getSeconds());

        return workoutSessionMapper.toResponse(workoutSessionRepository.save(workoutSession));
    }

    @Transactional(readOnly = true)
    public List<WorkoutSessionResponse> getAllWorkoutSessions() {
        return workoutSessionRepository.findAllByOrderByStartedAtDesc().stream()
                .map(workoutSessionMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<WorkoutSessionResponse> getLatestWorkoutSession() {
        return workoutSessionRepository.findTopIdByEndedAtIsNotNullOrderByEndedAtDesc()
                .flatMap(workoutSessionRepository::findByIdWithExerciseLogsAndTemplate)
                .map(workoutSessionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Optional<WorkoutSessionResponse> getLatestOngoingWorkoutSession() {
        return workoutSessionRepository.findTopIdByEndedAtIsNullOrderByStartedAtDesc()
                .flatMap(workoutSessionRepository::findByIdWithExerciseLogsAndTemplate)
                .map(workoutSessionMapper::toResponse);
    }
}
