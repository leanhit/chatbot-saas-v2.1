package com.chatbot.core.training.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Agent Training Progress entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "agent_training_progress")
@EqualsAndHashCode(callSuper = true)
public class AgentTrainingProgress extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Column(name = "training_id", nullable = false)
    private Long trainingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgressStatus status;

    @Column(nullable = false)
    private Integer progress;

    @Column(name = "quiz_score")
    private Integer quizScore;

    @Column(name = "started_at")
    private String startedAt;

    @Column(name = "completed_at")
    private String completedAt;

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Override
    public Long getId() {
        return id;
    }

    public enum ProgressStatus {
        NOT_STARTED, IN_PROGRESS, COMPLETED, FAILED
    }
}
