package com.gymtracker.gym.aiPlans.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "ai_plan_generations")
public class AiPlanGeneration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    @Enumerated(EnumType.STRING)
    private AiPlanGenerationStatus status;
    @JdbcTypeCode(SqlTypes.JSON)
    private String requestJson;
    @JdbcTypeCode(SqlTypes.JSON)
    private String historySummaryJson;
    private String model;
    private Integer inputTokens;
    private Integer outputTokens;
    private String rawResponse;
    private String errorMessage;
    private Long trainingProgramId;
    private Long previousGenerationId;
    @CreationTimestamp
    private LocalDateTime createdAt;

}
