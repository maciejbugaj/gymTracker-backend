package com.gymtracker.gym.workoutSessions.model;

import com.gymtracker.gym.exerciseLogs.model.ExerciseLog;
import com.gymtracker.gym.workoutTemplates.model.WorkoutTemplate;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name="workout_sessions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkoutSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="template_id")
    private WorkoutTemplate workoutTemplate;

    @CreationTimestamp
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer durationSeconds;
    private String notes;

    @OneToMany(mappedBy = "workoutSession", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("loggedAt ASC")
    @Builder.Default
    private List<ExerciseLog> exerciseLogs = new ArrayList<>();
}
