package com.gymtracker.gym.trainingPrograms.service;

import com.gymtracker.gym.aiPlans.model.AiPlanGeneration;
import com.gymtracker.gym.aiPlans.model.AiPlanGenerationStatus;
import com.gymtracker.gym.aiPlans.repository.AiPlanGenerationRepository;
import com.gymtracker.gym.trainingPrograms.dto.ActiveProgramResponse;
import com.gymtracker.gym.trainingPrograms.dto.ProgramDayExerciseRequest;
import com.gymtracker.gym.trainingPrograms.dto.ProgramDayExerciseResponse;
import com.gymtracker.gym.trainingPrograms.dto.ProgramDayRequest;
import com.gymtracker.gym.trainingPrograms.dto.ProgramWeekRequest;
import com.gymtracker.gym.trainingPrograms.dto.TrainingProgramRequest;
import com.gymtracker.gym.trainingPrograms.dto.TrainingProgramSummaryResponse;
import com.gymtracker.gym.trainingPrograms.mapper.ProgramDayExerciseMapper;
import com.gymtracker.gym.trainingPrograms.mapper.TrainingProgramMapper;
import com.gymtracker.gym.trainingPrograms.model.ProgramDay;
import com.gymtracker.gym.trainingPrograms.model.ProgramDayExercise;
import com.gymtracker.gym.trainingPrograms.model.ProgramGoal;
import com.gymtracker.gym.trainingPrograms.model.ProgramStatus;
import com.gymtracker.gym.trainingPrograms.model.ProgramWeek;
import com.gymtracker.gym.trainingPrograms.model.TrainingProgram;
import com.gymtracker.gym.trainingPrograms.repository.TrainingProgramRepository;
import com.gymtracker.gym.workoutSessions.repository.WorkoutSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingProgramServiceTest {

    private static final long USER_ID = 1L;
    private static final TrainingProgramSummaryResponse SUMMARY_STUB =
            new TrainingProgramSummaryResponse(1L, "Test Program", ProgramGoal.STRENGTH, ProgramStatus.ACTIVE, 2, 2, null);
    private static final ProgramDayExerciseResponse EXERCISE_STUB =
            new ProgramDayExerciseResponse(1L, "Squat", 0, 3, 6, 10, 120, null);

    @Mock
    TrainingProgramRepository trainingProgramRepository;
    @Mock
    AiPlanGenerationRepository aiPlanGenerationRepository;
    @Mock
    WorkoutSessionRepository workoutSessionRepository;
    @Mock
    TrainingProgramMapper trainingProgramMapper;
    @Mock
    ProgramDayExerciseMapper programDayExerciseMapper;

    TrainingProgramService service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new TrainingProgramService(trainingProgramRepository, aiPlanGenerationRepository,
                workoutSessionRepository, trainingProgramMapper, programDayExerciseMapper);
    }

    // --- next day ---

    @Test
    void getActiveWithNextDayReturnsEmptyWhenNoActiveProgram() {
        when(trainingProgramRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(USER_ID, ProgramStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThat(service.getActiveWithNextDay(USER_ID)).isEmpty();
    }

    @Test
    void getActiveWithNextDayPicksFirstDayWhenNothingCompleted() {
        TrainingProgram program = twoWeekProgram();
        givenActiveProgram(program);
        when(workoutSessionRepository.findCompletedProgramDayIds(USER_ID)).thenReturn(Set.of());
        when(trainingProgramMapper.toSummaryResponse(program)).thenReturn(SUMMARY_STUB);
        when(programDayExerciseMapper.toResponse(any())).thenReturn(EXERCISE_STUB);

        ActiveProgramResponse response = service.getActiveWithNextDay(USER_ID).orElseThrow();

        assertThat(response.nextDay().weekNumber()).isEqualTo(1);
        assertThat(response.nextDay().dayNumber()).isEqualTo(1);
    }

    @Test
    void getActiveWithNextDaySkipsCompletedDays() {
        TrainingProgram program = twoWeekProgram();
        givenActiveProgram(program);
        Long day1Id = program.getProgramWeeks().get(0).getProgramDays().get(0).getId();
        when(workoutSessionRepository.findCompletedProgramDayIds(USER_ID)).thenReturn(Set.of(day1Id));
        when(trainingProgramMapper.toSummaryResponse(program)).thenReturn(SUMMARY_STUB);
        when(programDayExerciseMapper.toResponse(any())).thenReturn(EXERCISE_STUB);

        ActiveProgramResponse response = service.getActiveWithNextDay(USER_ID).orElseThrow();

        assertThat(response.nextDay().weekNumber()).isEqualTo(1);
        assertThat(response.nextDay().dayNumber()).isEqualTo(2);
    }

    @Test
    void getActiveWithNextDayReturnsNullNextDayWhenEverythingIsDone() {
        TrainingProgram program = twoWeekProgram();
        givenActiveProgram(program);
        Set<Long> allDayIds = program.getProgramWeeks().stream()
                .flatMap(w -> w.getProgramDays().stream())
                .map(ProgramDay::getId)
                .collect(Collectors.toSet());
        when(workoutSessionRepository.findCompletedProgramDayIds(USER_ID)).thenReturn(allDayIds);
        when(trainingProgramMapper.toSummaryResponse(program)).thenReturn(SUMMARY_STUB);

        ActiveProgramResponse response = service.getActiveWithNextDay(USER_ID).orElseThrow();

        assertThat(response.nextDay()).isNull();
        assertThat(response.program()).isEqualTo(SUMMARY_STUB);
    }

    // --- max one ACTIVE ---

    @Test
    void createFromGenerationArchivesThePreviouslyActiveProgram() {
        TrainingProgram previousActive = TrainingProgram.builder().id(10L).userId(USER_ID).status(ProgramStatus.ACTIVE).build();
        when(trainingProgramRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(USER_ID, ProgramStatus.ACTIVE))
                .thenReturn(Optional.of(previousActive));

        AiPlanGeneration generation = AiPlanGeneration.builder()
                .id(50L).userId(USER_ID).status(AiPlanGenerationStatus.SUCCEEDED).build();
        when(aiPlanGenerationRepository.findByIdAndUserId(50L, USER_ID)).thenReturn(Optional.of(generation));

        when(trainingProgramRepository.save(any(TrainingProgram.class))).thenAnswer(invocation -> {
            TrainingProgram program = invocation.getArgument(0);
            if (program.getId() == null) {
                program.setId(99L);
            }
            return program;
        });

        service.createFromGeneration(USER_ID, 50L, sampleRequest());

        ArgumentCaptor<TrainingProgram> captor = ArgumentCaptor.forClass(TrainingProgram.class);
        verify(trainingProgramRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .anySatisfy(p -> {
                    assertThat(p.getId()).isEqualTo(10L);
                    assertThat(p.getStatus()).isEqualTo(ProgramStatus.ARCHIVED);
                })
                .anySatisfy(p -> {
                    assertThat(p.getId()).isEqualTo(99L);
                    assertThat(p.getStatus()).isEqualTo(ProgramStatus.ACTIVE);
                });

        verify(aiPlanGenerationRepository).save(argThat(g -> g.getTrainingProgramId().equals(99L)));
    }

    @Test
    void activateArchivesTheOtherActiveProgramButNotItself() {
        TrainingProgram target = TrainingProgram.builder().id(5L).userId(USER_ID).status(ProgramStatus.DRAFT).build();
        TrainingProgram otherActive = TrainingProgram.builder().id(7L).userId(USER_ID).status(ProgramStatus.ACTIVE).build();
        when(trainingProgramRepository.findByIdAndUserId(5L, USER_ID)).thenReturn(Optional.of(target));
        when(trainingProgramRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(USER_ID, ProgramStatus.ACTIVE))
                .thenReturn(Optional.of(otherActive));
        when(trainingProgramRepository.save(any(TrainingProgram.class))).thenAnswer(inv -> inv.getArgument(0));

        service.activate(USER_ID, 5L);

        ArgumentCaptor<TrainingProgram> captor = ArgumentCaptor.forClass(TrainingProgram.class);
        verify(trainingProgramRepository, times(2)).save(captor.capture());
        assertThat(captor.getAllValues())
                .anySatisfy(p -> {
                    assertThat(p.getId()).isEqualTo(7L);
                    assertThat(p.getStatus()).isEqualTo(ProgramStatus.ARCHIVED);
                })
                .anySatisfy(p -> {
                    assertThat(p.getId()).isEqualTo(5L);
                    assertThat(p.getStatus()).isEqualTo(ProgramStatus.ACTIVE);
                });
    }

    @Test
    void activatingTheAlreadyActiveProgramDoesNotArchiveItself() {
        TrainingProgram program = TrainingProgram.builder().id(7L).userId(USER_ID).status(ProgramStatus.ACTIVE).build();
        when(trainingProgramRepository.findByIdAndUserId(7L, USER_ID)).thenReturn(Optional.of(program));
        when(trainingProgramRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(USER_ID, ProgramStatus.ACTIVE))
                .thenReturn(Optional.of(program));
        when(trainingProgramRepository.save(any(TrainingProgram.class))).thenAnswer(inv -> inv.getArgument(0));

        service.activate(USER_ID, 7L);

        verify(trainingProgramRepository, times(1)).save(any(TrainingProgram.class));
    }

    // --- helpers ---

    private void givenActiveProgram(TrainingProgram program) {
        when(trainingProgramRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(USER_ID, ProgramStatus.ACTIVE))
                .thenReturn(Optional.of(program));
        when(trainingProgramRepository.findByIdAndUserIdWithWeeks(program.getId(), USER_ID))
                .thenReturn(Optional.of(program));
    }

    /** 2 weeks x 2 days x 1 exercise, IDs set so tests can target a specific day. */
    private TrainingProgram twoWeekProgram() {
        TrainingProgram program = TrainingProgram.builder().id(1L).userId(USER_ID).status(ProgramStatus.ACTIVE).build();

        ProgramWeek week1 = ProgramWeek.builder().id(101L).trainingProgram(program).weekNumber(1).deload(false).build();
        addDay(week1, 201L, 1, "Squat");
        addDay(week1, 202L, 2, "Bench");

        ProgramWeek week2 = ProgramWeek.builder().id(102L).trainingProgram(program).weekNumber(2).deload(false).build();
        addDay(week2, 203L, 1, "Deadlift");
        addDay(week2, 204L, 2, "OHP");

        program.getProgramWeeks().add(week1);
        program.getProgramWeeks().add(week2);
        return program;
    }

    private void addDay(ProgramWeek week, long dayId, int dayNumber, String exerciseName) {
        ProgramDay day = ProgramDay.builder().id(dayId).programWeek(week).dayNumber(dayNumber).name("Day " + dayNumber).build();
        day.getProgramDayExercises().add(
                ProgramDayExercise.builder().id(dayId * 10).programDay(day).exerciseName(exerciseName).build());
        week.getProgramDays().add(day);
    }

    private TrainingProgramRequest sampleRequest() {
        ProgramDayExerciseRequest exercise = new ProgramDayExerciseRequest("Squat", 0, 3, 6, 10, 120, null);
        ProgramDayRequest day = new ProgramDayRequest(1, "Day 1", null, List.of(exercise));
        ProgramWeekRequest week = new ProgramWeekRequest(1, "Base", false, null, List.of(day));
        return new TrainingProgramRequest("My Program", "desc", ProgramGoal.STRENGTH, null, 1, 1, List.of(week));
    }
}
