package com.gymtracker.gym.exerciseLogs.service;

import com.gymtracker.gym.exceptions.NotFoundException;
import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogRequest;
import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogResponse;
import com.gymtracker.gym.exerciseLogs.mapper.ExerciseLogMapper;
import com.gymtracker.gym.exerciseLogs.model.ExerciseLog;
import com.gymtracker.gym.exerciseLogs.repository.ExerciseLogRepository;
import com.gymtracker.gym.trainingPrograms.model.ProgramDay;
import com.gymtracker.gym.trainingPrograms.repository.ProgramDayRepository;
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
    private final ProgramDayRepository programDayRepository;
    private final ExerciseLogMapper exerciseLogMapper;

    @Transactional
    public ExerciseLogResponse createNewExerciseLog(ExerciseLogRequest exerciseLogRequest, Long userId) {
        WorkoutSession workoutSession = workoutSessionRepository.findByIdAndUserId(exerciseLogRequest.getWorkoutSessionId(),
                        userId)
                .orElseThrow(() -> new NotFoundException("Workout Session with id:" +
                        exerciseLogRequest.getWorkoutSessionId() + " not found"));

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

        return exerciseLogMapper.toResponse(exerciseLogRepository.save(exerciseLog));
    }

    @Transactional(readOnly = true)
    public List<ExerciseLogResponse> getAllExerciseLogByIdWorkoutSessionId(Long workoutSessionId, Long userId) {
        WorkoutSession workoutSession = workoutSessionRepository.findByIdAndUserId(workoutSessionId,
                        userId)
                .orElseThrow(() -> new NotFoundException("Workout Session with id:" + workoutSessionId + " not found"));

        return exerciseLogRepository.findAllByWorkoutSession(workoutSession).stream()
                .map(exerciseLogMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExerciseLogResponse> getPreviousSessionLogsByWorkoutTemplateId(Long workoutTemplateId, Long userId) {
        return workoutSessionRepository.findTopByWorkoutTemplateIdAndEndedAtIsNotNullAndUserIdOrderByEndedAtDesc(workoutTemplateId,
                        userId)
                .map(session -> exerciseLogRepository
                        .findByWorkoutSessionOrderBySetNumberAsc(session)
                        .stream()
                        .map(exerciseLogMapper::toResponse)
                        .toList())
                .orElse(List.of());
    }

    /**
     * Per exercise in the program day, the most recent set the user ever logged for that exact
     * exercise name — regardless of which session/template it came from. An exercise the user
     * has never logged before is simply absent from the result (no entry, not a null one).
     */
    @Transactional(readOnly = true)
    public List<ExerciseLogResponse> getPreviousSetsByProgramDayId(Long programDayId, Long userId) {
        ProgramDay programDay = programDayRepository.findByIdAndUserId(programDayId, userId)
                .orElseThrow(() -> new NotFoundException("Program day " + programDayId + " not found"));

        return programDay.getProgramDayExercises().stream()
                .map(exercise -> exerciseLogRepository.findLatestByUserIdAndExerciseName(userId, exercise.getExerciseName()))
                .flatMap(Optional::stream)
                .map(exerciseLogMapper::toResponse)
                .toList();
    }
}
