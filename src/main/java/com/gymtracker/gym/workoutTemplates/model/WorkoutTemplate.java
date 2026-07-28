package com.gymtracker.gym.workoutTemplates.model;

import com.gymtracker.gym.templateExercises.model.TemplateExercise;
import com.gymtracker.gym.workoutSessions.model.WorkoutSession;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name="workout_templates")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @CreationTimestamp
    private LocalDateTime createdAt;


    @OneToMany(mappedBy = "workoutTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<TemplateExercise> exercises = new ArrayList<>();

    @OneToMany(mappedBy = "workoutTemplate")
    @Builder.Default
    private List<WorkoutSession> sessions = new ArrayList<>();

    private Long userId;
}
