package com.gymtracker.gym.trainingPrograms.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "training_programs")
public class TrainingProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String name;
    private String description;
    @Enumerated(EnumType.STRING)
    private ProgramGoal goal;
    private String experienceLevel;
    private Integer durationWeeks;
    private Integer daysPerWeek;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProgramStatus status = ProgramStatus.DRAFT;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProgramSource source = ProgramSource.AI_GENERATED;
    private Long aiGenerationId;
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "trainingProgram", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("weekNumber ASC")
    @BatchSize(size = 32)
    @Builder.Default
    private List<ProgramWeek> programWeeks = new ArrayList<>();

}
