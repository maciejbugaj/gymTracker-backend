package com.gymtracker.gym.workoutSessions.service;

import com.gymtracker.gym.exceptions.ConflictException;
import com.gymtracker.gym.exceptions.NotFoundException;
import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogResponse;
import com.gymtracker.gym.workoutSessions.dto.WorkoutSessionRequest;
import com.gymtracker.gym.workoutSessions.dto.WorkoutSessionResponse;
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

    @Transactional(readOnly = true)
    public Optional<WorkoutSessionResponse> getWorkoutSessionById(Long workoutSessionId) {
        return workoutSessionRepository.findById(workoutSessionId).map(this::toResponse);
    }

    @Transactional
    public WorkoutSessionResponse createWorkoutSession(WorkoutSessionRequest workoutSessionRequest) {
        WorkoutTemplate workoutTemplate = workoutTemplateRepository.findById(workoutSessionRequest.getWorkoutTemplateId())
                .orElseThrow(() -> new RuntimeException("Template Not Found " + workoutSessionRequest.getWorkoutTemplateId()));

        WorkoutSession workoutSession = WorkoutSession.builder()
                .workoutTemplate(workoutTemplate)
                .startedAt(LocalDateTime.now())
                .build();
        return toResponse(workoutSessionRepository.save(workoutSession));
    }

    @Transactional
    public WorkoutSessionResponse finishWorkoutSession(Long workoutSessionId) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(workoutSessionId)
                .orElseThrow(() -> new NotFoundException("Workout Session with Id: " + workoutSessionId + " not found"));

        if (workoutSession.getEndedAt() != null) {
            throw new ConflictException("Workout session already finished: " + workoutSessionId);
        }

        workoutSession.setEndedAt(LocalDateTime.now());
        workoutSession.setDurationSeconds((int) Duration.between(workoutSession.getStartedAt(), workoutSession.getEndedAt()).getSeconds());

        return toResponse(workoutSessionRepository.save(workoutSession));
    }

    @Transactional(readOnly = true)
    public List<WorkoutSessionResponse> getAllWorkoutSessions() {
        return workoutSessionRepository.findAllByOrderByStartedAtDesc().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Optional<WorkoutSessionResponse> getLatestWorkoutSession() {
        return workoutSessionRepository.findTopByEndedAtIsNotNullOrderByEndedAtDesc().map(this::toResponse);
    }

    @Transactional
    public Optional<WorkoutSessionResponse> getLatestOngoingWorkoutSession() {
        return workoutSessionRepository.findOngoingSessionWithLogs().map(this::toResponse);

    }

    private WorkoutSessionResponse toResponse(WorkoutSession workoutSession) {

        List<ExerciseLogResponse> logs = workoutSession.getExerciseLogs()
                .stream()
                .map(log -> ExerciseLogResponse.builder()
                        .id(log.getId())
                        .exerciseName(log.getExerciseName())
                        .setNumber(log.getSetNumber())
                        .reps(log.getReps())
                        .weightKg(log.getWeightKg())
                        .loggedAt(log.getLoggedAt())
                        .build())
                .toList();

        return WorkoutSessionResponse.builder()
                .id(workoutSession.getId())
                .workoutTemplateId(workoutSession.getWorkoutTemplate() != null ? workoutSession.getWorkoutTemplate().getId() : null)
                .workoutTemplateName(workoutSession.getWorkoutTemplate() != null ? workoutSession.getWorkoutTemplate().getName() : null)
                .startedAt(workoutSession.getStartedAt())
                .endedAt(workoutSession.getEndedAt())
                .durationSeconds(workoutSession.getDurationSeconds())
                .notes(workoutSession.getNotes())
                .exerciseLogs(logs)
                .build();
    }

}
