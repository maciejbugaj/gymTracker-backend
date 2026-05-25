package com.gymtracker.gym.exerciseLogs.service;

import com.gymtracker.gym.exceptions.NotFoundException;
import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogRequest;
import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogResponse;
import com.gymtracker.gym.exerciseLogs.model.ExerciseLog;
import com.gymtracker.gym.exerciseLogs.repository.ExerciseLogRepository;
import com.gymtracker.gym.workoutSessions.model.WorkoutSession;
import com.gymtracker.gym.workoutSessions.repository.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExerciseLogService {

    private final ExerciseLogRepository exerciseLogRepository;
    private final WorkoutSessionRepository workoutSessionRepository;

    @Transactional
    public ExerciseLogResponse createNewExerciseLog(ExerciseLogRequest exerciseLogRequest) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(exerciseLogRequest.getWorkoutSessionId())
                .orElseThrow(() -> new NotFoundException("Workout Session with id:" + exerciseLogRequest.getWorkoutSessionId() + " not found"));

        int nextSetNumber = exerciseLogRepository.findLatestBySessionAndExerciseName(workoutSession, exerciseLogRequest.getExerciseName())
                .map(last -> last.getSetNumber() + 1)
                .orElse(1);

        ExerciseLog exerciseLog = ExerciseLog.builder()
                .loggedAt(LocalDateTime.now())
                .exerciseName(exerciseLogRequest.getExerciseName())
                .setNumber(nextSetNumber)
                .reps(exerciseLogRequest.getReps())
                .weightKg(exerciseLogRequest.getWeightKg())
                .workoutSession(workoutSession)
                .build();

        return toResponse(exerciseLogRepository.save(exerciseLog));
    }

    @Transactional(readOnly = true)
    public List<ExerciseLogResponse> getAllExerciseLogByIdWorkoutSessionId(Long workoutSessionId) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(workoutSessionId)
                .orElseThrow(() -> new NotFoundException("Workout Session with id:" + workoutSessionId + " not found"));

        return exerciseLogRepository.findAllByWorkoutSession(workoutSession).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ExerciseLogResponse> getPreviousSessionLogsByWorkoutTemplateId (Long workoutTemplateId) {
        return workoutSessionRepository.findTopByWorkoutTemplateIdAndEndedAtIsNotNullOrderByEndedAtDesc(workoutTemplateId)
                .map(session -> exerciseLogRepository
                        .findByWorkoutSessionOrderBySetNumberAsc(session)
                        .stream()
                        .map(this::toResponse)
                        .toList())
                .orElse(List.of());
    }


    private ExerciseLogResponse toResponse(ExerciseLog exerciseLog) {
        return ExerciseLogResponse.builder()
                .id(exerciseLog.getId())
                .exerciseName(exerciseLog.getExerciseName())
                .setNumber(exerciseLog.getSetNumber())
                .reps(exerciseLog.getReps())
                .weightKg(exerciseLog.getWeightKg())
                .loggedAt(exerciseLog.getLoggedAt())
                .build();
    }
}
