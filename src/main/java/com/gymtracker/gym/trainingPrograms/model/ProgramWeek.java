package com.gymtracker.gym.trainingPrograms.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "program_weeks")
public class ProgramWeek {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private TrainingProgram trainingProgram;
    private Integer weekNumber;
    private String focus;
    @Column(name = "is_deload")
    private boolean deload;
    private String notes;

    @OneToMany(mappedBy = "programWeek", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dayNumber ASC")
    @BatchSize(size = 32)
    @Builder.Default
    private List<ProgramDay> programDays = new ArrayList<>();
}
