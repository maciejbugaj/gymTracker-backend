package com.gymtracker.gym.workoutSessions.service;

import com.gymtracker.gym.workoutSessions.dto.WorkoutSessionRequest;
import com.gymtracker.gym.workoutSessions.dto.WorkoutSessionResponse;
import com.gymtracker.gym.workoutSessions.model.WorkoutSession;
import com.gymtracker.gym.workoutSessions.repository.WorkoutSessionRepository;
import com.gymtracker.gym.workoutTemplates.model.WorkoutTemplate;
import com.gymtracker.gym.workoutTemplates.repository.WorkoutTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cglib.core.Local;
import org.springframework.http.ResponseEntity;
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

    public WorkoutSession saveWorkoutSession(WorkoutSession workoutSession) {
//        int durationSeconds = Math.toIntExact(Duration.between(workoutSession.getStartedAt(), workoutSession.getEndedAt()).getSeconds());
//        workoutSession.setDurationSeconds(durationSeconds);
        return workoutSessionRepository.save(workoutSession);
    }

    @Transactional
    public WorkoutSessionResponse createWorkoutSession(WorkoutSessionRequest workoutSessionRequest) {
        WorkoutTemplate workoutTemplate = workoutTemplateRepository.findById(workoutSessionRequest.getWorkoutTemplateId())
                .orElseThrow( () -> new RuntimeException("Template Not Found " + workoutSessionRequest.getWorkoutTemplateId()));
        WorkoutSession workoutSession = WorkoutSession.builder()
                .workoutTemplate(workoutTemplate)
                .startedAt(LocalDateTime.now())
                .build();
        return toResponse(workoutSessionRepository.save(workoutSession));
    }

    @Transactional
    public WorkoutSessionResponse finishWorkoutSession(Long workoutSessionId) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(workoutSessionId).orElseThrow(() -> new IllegalArgumentException ("Workout Session with Id: " + workoutSessionId + " not found"));

        workoutSession.setEndedAt(LocalDateTime.now());
        workoutSession.setDurationSeconds((int)Duration.between(workoutSession.getStartedAt(), workoutSession.getEndedAt())
                .getSeconds());

        return toResponse(workoutSessionRepository.save(workoutSession));
    }

    @Transactional(readOnly = true)
    public List<WorkoutSessionResponse> getAllWorkoutSessions() {
        return workoutSessionRepository.findAllByOrderByStartedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkoutSessionResponse getLatestWorkoutSession() {
        return toResponse(workoutSessionRepository.findTopByEndedAtIsNotNullOrderByEndedAtDesc());
    }

    @Transactional
    public Optional<WorkoutSessionResponse> getLatestOngoingWorkoutSession() {
        return workoutSessionRepository.findTopByEndedAtIsNullOrderByStartedAtDesc().map(this::toResponse);

    }

    private WorkoutSessionResponse toResponse(WorkoutSession workoutSession) {
        return WorkoutSessionResponse.builder()
                .id(workoutSession.getId())
                .workoutTemplateId(workoutSession.getWorkoutTemplate() != null ? workoutSession.getWorkoutTemplate().getId() : null)
                .workoutTemplateName(workoutSession.getWorkoutTemplate() != null ? workoutSession.getWorkoutTemplate().getName() : null)
                .startedAt(workoutSession.getStartedAt())
                .endedAt(workoutSession.getEndedAt())
                .durationSeconds(workoutSession.getDurationSeconds())
                .notes(workoutSession.getNotes())
                .build();
    }

}
