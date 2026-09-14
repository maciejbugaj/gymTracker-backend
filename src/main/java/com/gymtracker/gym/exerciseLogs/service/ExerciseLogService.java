package com.gymtracker.gym.exerciseLogs.service;

import com.gymtracker.gym.exceptions.ConflictException;
import com.gymtracker.gym.exceptions.NotFoundException;
import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogRequest;
import com.gymtracker.gym.exerciseLogs.dto.ExerciseLogResponse;
import com.gymtracker.gym.exerciseLogs.dto.ReorderExerciseLogsRequest;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

        // The set table sends the row position it is ticking; only older clients (and Swagger)
        // leave it out, and those keep the append-at-the-end behaviour.
        Integer setNumber = exerciseLogRequest.getSetNumber() != null
                ? exerciseLogRequest.getSetNumber()
                : exerciseLogRepository.findLatestBySessionAndExerciseName(workoutSession, exerciseLogRequest.getExerciseName())
                .map(last -> last.getSetNumber() + 1)
                .orElse(1);

        ExerciseLog exerciseLog = ExerciseLog.builder()
                .loggedAt(LocalDateTime.now())
                .exerciseName(exerciseLogRequest.getExerciseName())
                .setNumber(setNumber)
                .reps(exerciseLogRequest.getReps())
                .weightKg(exerciseLogRequest.getWeightKg())
                .workoutSession(workoutSession)
                .build();

        return exerciseLogMapper.toResponse(exerciseLogRepository.save(exerciseLog));
    }

    @Transactional
    public void deleteExerciseLog(Long exerciseLogId, Long userId) {
        ExerciseLog exerciseLog = exerciseLogRepository.findById(exerciseLogId)
                .filter(log -> userId.equals(log.getWorkoutSession().getUserId()))
                .orElseThrow(() -> new NotFoundException("Exercise log with id:" + exerciseLogId + " not found"));

        exerciseLogRepository.delete(exerciseLog);
    }

    /**
     * Renumbers the sets of one exercise to 1..n in the given order. Needed because the user can
     * drop a set row from the middle of the table, which shifts every row below it up.
     */
    @Transactional
    public void reorderExerciseLogs(ReorderExerciseLogsRequest request, Long userId) {
        WorkoutSession workoutSession = workoutSessionRepository.findByIdAndUserId(request.getWorkoutSessionId(), userId)
                .orElseThrow(() -> new NotFoundException("Workout Session with id:" +
                        request.getWorkoutSessionId() + " not found"));

        List<ExerciseLog> savedLogs = exerciseLogRepository
                .findByWorkoutSessionAndExerciseNameOrderBySetNumberAsc(workoutSession, request.getExerciseName());

        Set<Long> requestedIds = new LinkedHashSet<>(request.getLogIds());
        if (requestedIds.size() != request.getLogIds().size()
                || requestedIds.size() != savedLogs.size()
                || !requestedIds.containsAll(savedLogs.stream().map(ExerciseLog::getId).toList())) {
            throw new ConflictException("Reorder must list every saved set of " + request.getExerciseName() +
                    " in this session exactly once");
        }

        Map<Long, ExerciseLog> logsById = savedLogs.stream()
                .collect(Collectors.toMap(ExerciseLog::getId, Function.identity()));

        // Two passes: park the rows on negative numbers first, otherwise shifting one set onto a
        // number another row still holds trips the unique index mid-update.
        int parking = -1;
        for (Long logId : requestedIds) {
            logsById.get(logId).setSetNumber(parking--);
        }
        exerciseLogRepository.flush();

        int setNumber = 1;
        for (Long logId : requestedIds) {
            logsById.get(logId).setSetNumber(setNumber++);
        }
        exerciseLogRepository.flush();
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
     * Per exercise in the program day, every set from the last finished session in which the user
     * logged that exact exercise name — regardless of which session/template it came from, so the
     * set table can show a per-row "previous". An exercise the user has never logged before is
     * simply absent from the result (no entries, not null ones).
     */
    @Transactional(readOnly = true)
    public List<ExerciseLogResponse> getPreviousSetsByProgramDayId(Long programDayId, Long userId) {
        ProgramDay programDay = programDayRepository.findByIdAndUserId(programDayId, userId)
                .orElseThrow(() -> new NotFoundException("Program day " + programDayId + " not found"));

        return programDay.getProgramDayExercises().stream()
                .flatMap(exercise -> previousSetsForExercise(userId, exercise.getExerciseName()).stream())
                .map(exerciseLogMapper::toResponse)
                .toList();
    }

    private List<ExerciseLog> previousSetsForExercise(Long userId, String exerciseName) {
        return exerciseLogRepository.findLatestSessionIdByUserIdAndExerciseName(userId, exerciseName)
                .map(sessionId -> exerciseLogRepository
                        .findByWorkoutSessionIdAndExerciseNameOrderBySetNumberAsc(sessionId, exerciseName))
                .orElse(List.of());
    }
}
