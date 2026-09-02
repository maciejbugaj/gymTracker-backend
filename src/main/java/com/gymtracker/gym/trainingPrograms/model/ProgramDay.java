package com.gymtracker.gym.trainingPrograms.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "program_days")
public class ProgramDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_week_id", nullable = false)
    private ProgramWeek programWeek;
    private Integer dayNumber;
    private String name;
    private String notes;

    @OneToMany(mappedBy = "programDay", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @BatchSize(size = 64)
    @Builder.Default
    private List<ProgramDayExercise> programDayExercises = new ArrayList<>();

}
