package com.gymtracker.gym.exerciseLogs.service;

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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExerciseLogService {

    private final ExerciseLogRepository exerciseLogRepository;
    private final WorkoutSessionRepository workoutSessionRepository;

    public ExerciseLogResponse createNewExerciseLog(ExerciseLogRequest exerciseLogRequest) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(exerciseLogRequest.getWorkoutSessionId())
                .orElseThrow(() -> new RuntimeException("Workout Session with id:" + exerciseLogRequest.getWorkoutSessionId() + " not found"));

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

    public List<ExerciseLogResponse> getAllExerciseLogByIdWorkoutSessionId(Long workoutSessionId) {
        WorkoutSession workoutSession = workoutSessionRepository.findById(workoutSessionId)
                .orElseThrow(() -> new RuntimeException("Workout Session with id:" + workoutSessionId + " not found"));

        return exerciseLogRepository.findAllByWorkoutSession(workoutSession).stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<ExerciseLogResponse> getPreviousSessionLogsByWorkoutTemplateId (Long workoutTemplateId) {
        WorkoutSession workoutSession = workoutSessionRepository.findTopByWorkoutTemplateIdAndEndedAtIsNotNullOrderByEndedAtDesc(workoutTemplateId)
                .orElseThrow(() -> new IllegalArgumentException("Previous workout session with template id: " + workoutTemplateId +" not found"));

        return exerciseLogRepository.findByWorkoutSessionOrderBySetNumberAsc(workoutSession)
                .stream()
                .map(this::toResponse)
                .toList();
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
