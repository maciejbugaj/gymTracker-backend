package com.gymtracker.gym.trainingPrograms.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "program_day_exercises")
public class ProgramDayExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_day_id", nullable = false)
    private ProgramDay programDay;

    private String exerciseName;
    @Builder.Default
    private Integer sortOrder = 0;
    private Integer targetSets;
    private Integer targetRepsMin;
    private Integer targetRepsMax;
    private Integer restSeconds;
    private String notes;
}
