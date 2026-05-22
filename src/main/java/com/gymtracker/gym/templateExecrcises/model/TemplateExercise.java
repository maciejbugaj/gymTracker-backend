package com.gymtracker.gym.templateExecrcises.model;

import com.gymtracker.gym.workoutTemplates.model.WorkoutTemplate;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@Table(name="template_exercises")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="template_id")
    private WorkoutTemplate workoutTemplate;

    private String exerciseName;
    private Integer defaultSets;
    private Integer defaultReps;
    private BigDecimal defaultWeightKg;
    @Builder.Default
    private Integer sortOrder = 0;
}
