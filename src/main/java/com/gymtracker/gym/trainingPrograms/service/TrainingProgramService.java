package com.gymtracker.gym.trainingPrograms.service;

import com.gymtracker.gym.aiPlans.model.AiPlanGeneration;
import com.gymtracker.gym.aiPlans.model.AiPlanGenerationStatus;
import com.gymtracker.gym.aiPlans.repository.AiPlanGenerationRepository;
import com.gymtracker.gym.exceptions.ConflictException;
import com.gymtracker.gym.exceptions.NotFoundException;
import com.gymtracker.gym.trainingPrograms.dto.ActiveProgramResponse;
import com.gymtracker.gym.trainingPrograms.dto.NextProgramDayResponse;
import com.gymtracker.gym.trainingPrograms.dto.ProgramDayExerciseRequest;
import com.gymtracker.gym.trainingPrograms.dto.ProgramDayExerciseResponse;
import com.gymtracker.gym.trainingPrograms.dto.ProgramDayRequest;
import com.gymtracker.gym.trainingPrograms.dto.ProgramWeekRequest;
import com.gymtracker.gym.trainingPrograms.dto.TrainingProgramRequest;
import com.gymtracker.gym.trainingPrograms.dto.TrainingProgramResponse;
import com.gymtracker.gym.trainingPrograms.dto.TrainingProgramSummaryResponse;
import com.gymtracker.gym.trainingPrograms.mapper.ProgramDayExerciseMapper;
import com.gymtracker.gym.trainingPrograms.mapper.TrainingProgramMapper;
import com.gymtracker.gym.trainingPrograms.model.ProgramDay;
import com.gymtracker.gym.trainingPrograms.model.ProgramDayExercise;
import com.gymtracker.gym.trainingPrograms.model.ProgramSource;
import com.gymtracker.gym.trainingPrograms.model.ProgramStatus;
import com.gymtracker.gym.trainingPrograms.model.ProgramWeek;
import com.gymtracker.gym.trainingPrograms.model.TrainingProgram;
import com.gymtracker.gym.trainingPrograms.repository.TrainingProgramRepository;
import com.gymtracker.gym.workoutSessions.repository.WorkoutSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TrainingProgramService {

    private final TrainingProgramRepository trainingProgramRepository;
    private final AiPlanGenerationRepository aiPlanGenerationRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final TrainingProgramMapper trainingProgramMapper;
    private final ProgramDayExerciseMapper programDayExerciseMapper;

    @Transactional(readOnly = true)
    public List<TrainingProgramSummaryResponse> listPrograms(Long userId) {
        return trainingProgramRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(trainingProgramMapper::toSummaryResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TrainingProgramResponse getProgram(Long userId, Long id) {
        return trainingProgramMapper.toResponse(loadWithWeeks(userId, id));
    }

    @Transactional
    public TrainingProgramResponse createFromGeneration(Long userId, Long generationId, TrainingProgramRequest request) {
        AiPlanGeneration generation = aiPlanGenerationRepository.findByIdAndUserId(generationId, userId)
                .orElseThrow(() -> new NotFoundException("AI plan generation " + generationId + " not found"));
        if (generation.getStatus() != AiPlanGenerationStatus.SUCCEEDED) {
            throw new ConflictException("Can only save a program from a succeeded generation");
        }

        deactivateCurrentActive(userId, null);

        TrainingProgram program = TrainingProgram.builder()
                .userId(userId)
                .name(request.name())
                .description(request.description())
                .goal(request.goal())
                .experienceLevel(request.experienceLevel())
                .durationWeeks(request.durationWeeks())
                .daysPerWeek(request.daysPerWeek())
                .status(ProgramStatus.ACTIVE)
                .source(ProgramSource.AI_GENERATED)
                .aiGenerationId(generationId)
                .build();
        applyWeeks(program, request.weeks());
        program = trainingProgramRepository.save(program);

        generation.setTrainingProgramId(program.getId());
        aiPlanGenerationRepository.save(generation);

        return trainingProgramMapper.toResponse(program);
    }

    @Transactional
    public TrainingProgramResponse updateProgram(Long userId, Long id, TrainingProgramRequest request) {
        TrainingProgram program = loadWithWeeks(userId, id);

        program.setName(request.name());
        program.setDescription(request.description());
        program.setGoal(request.goal());
        program.setExperienceLevel(request.experienceLevel());
        program.setDurationWeeks(request.durationWeeks());
        program.setDaysPerWeek(request.daysPerWeek());
        applyWeeks(program, request.weeks());

        return trainingProgramMapper.toResponse(trainingProgramRepository.save(program));
    }

    @Transactional
    public void deleteProgram(Long userId, Long id) {
        if (!trainingProgramRepository.existsByIdAndUserId(id, userId)) {
            throw new NotFoundException("Training program " + id + " not found");
        }
        trainingProgramRepository.deleteByIdAndUserId(id, userId);
    }

    @Transactional
    public TrainingProgramResponse activate(Long userId, Long id) {
        TrainingProgram program = trainingProgramRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Training program " + id + " not found"));
        deactivateCurrentActive(userId, id);
        program.setStatus(ProgramStatus.ACTIVE);
        return trainingProgramMapper.toResponse(trainingProgramRepository.save(program));
    }

    @Transactional
    public TrainingProgramResponse archive(Long userId, Long id) {
        TrainingProgram program = trainingProgramRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundException("Training program " + id + " not found"));
        program.setStatus(ProgramStatus.ARCHIVED);
        return trainingProgramMapper.toResponse(trainingProgramRepository.save(program));
    }

    @Transactional(readOnly = true)
    public Optional<ActiveProgramResponse> getActiveWithNextDay(Long userId) {
        Optional<TrainingProgram> active = trainingProgramRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, ProgramStatus.ACTIVE)
                .flatMap(p -> trainingProgramRepository.findByIdAndUserIdWithWeeks(p.getId(), userId));
        if (active.isEmpty()) {
            return Optional.empty();
        }

        TrainingProgram program = active.get();
        Set<Long> completedDayIds = workoutSessionRepository.findCompletedProgramDayIds(userId);

        // programWeeks is @OrderBy("weekNumber ASC") and each week's programDays is
        // @OrderBy("dayNumber ASC"), so flatMap walks days in exactly (week, day) order —
        // the first one without a completed session is "next".
        NextProgramDayResponse nextDay = program.getProgramWeeks().stream()
                .flatMap(week -> week.getProgramDays().stream()
                        .filter(day -> !completedDayIds.contains(day.getId()))
                        .map(day -> toNextDayResponse(week, day)))
                .findFirst()
                .orElse(null);

        return Optional.of(new ActiveProgramResponse(trainingProgramMapper.toSummaryResponse(program), nextDay));
    }

    private TrainingProgram loadWithWeeks(Long userId, Long id) {
        return trainingProgramRepository.findByIdAndUserIdWithWeeks(id, userId)
                .orElseThrow(() -> new NotFoundException("Training program " + id + " not found"));
    }

    /** At most one ACTIVE program per user. excludeProgramId lets activate() re-activate itself without archiving itself first. */
    private void deactivateCurrentActive(Long userId, Long excludeProgramId) {
        trainingProgramRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, ProgramStatus.ACTIVE)
                .filter(current -> excludeProgramId == null || !current.getId().equals(excludeProgramId))
                .ifPresent(current -> {
                    current.setStatus(ProgramStatus.ARCHIVED);
                    trainingProgramRepository.save(current);
                });
    }

    private NextProgramDayResponse toNextDayResponse(ProgramWeek week, ProgramDay day) {
        List<ProgramDayExerciseResponse> exercises =
                day.getProgramDayExercises().stream()
                        .map(programDayExerciseMapper::toResponse)
                        .toList();
        return new NextProgramDayResponse(
                day.getId(), week.getWeekNumber(), day.getDayNumber(), day.getName(), week.isDeload(), exercises);
    }

    /** Replaces the whole tree; orphanRemoval on each @OneToMany deletes what's no longer referenced. */
    private void applyWeeks(TrainingProgram program, List<ProgramWeekRequest> weekRequests) {
        program.getProgramWeeks().clear();
        for (ProgramWeekRequest weekRequest : weekRequests) {
            ProgramWeek week = ProgramWeek.builder()
                    .trainingProgram(program)
                    .weekNumber(weekRequest.weekNumber())
                    .focus(weekRequest.focus())
                    .deload(weekRequest.isDeload())
                    .notes(weekRequest.notes())
                    .build();
            for (ProgramDayRequest dayRequest : weekRequest.days()) {
                ProgramDay day = ProgramDay.builder()
                        .programWeek(week)
                        .dayNumber(dayRequest.dayNumber())
                        .name(dayRequest.name())
                        .notes(dayRequest.notes())
                        .build();
                for (ProgramDayExerciseRequest exerciseRequest : dayRequest.exercises()) {
                    ProgramDayExercise exercise = ProgramDayExercise.builder()
                            .programDay(day)
                            .exerciseName(exerciseRequest.exerciseName())
                            .sortOrder(exerciseRequest.sortOrder())
                            .targetSets(exerciseRequest.targetSets())
                            .targetRepsMin(exerciseRequest.targetRepsMin())
                            .targetRepsMax(exerciseRequest.targetRepsMax())
                            .restSeconds(exerciseRequest.restSeconds())
                            .notes(exerciseRequest.notes())
                            .build();
                    day.getProgramDayExercises().add(exercise);
                }
                week.getProgramDays().add(day);
            }
            program.getProgramWeeks().add(week);
        }
    }
}
