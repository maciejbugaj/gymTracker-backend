package com.gymtracker.gym.exerciseLogs.model;

import com.gymtracker.gym.workoutSessions.model.WorkoutSession;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name="exercise_logs")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private WorkoutSession workoutSession;

    private String exerciseName;
    private Integer setNumber;
    private Integer reps;
    private BigDecimal weightKg;

    @CreationTimestamp
    private LocalDateTime loggedAt;


}
