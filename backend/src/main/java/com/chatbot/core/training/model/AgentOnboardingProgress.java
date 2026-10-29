package com.chatbot.core.training.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Agent Onboarding Progress entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "agent_onboarding_progress")
@EqualsAndHashCode(callSuper = true)
public class AgentOnboardingProgress extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompletionStatus status;

    @Column(name = "started_at")
    private String startedAt;

    @Column(name = "completed_at")
    private String completedAt;

    @Column(name = "time_spent_seconds")
    private Integer timeSpentSeconds;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    private SupervisorApproval supervisorApproval;

    @Column(name = "supervisor_id")
    private Long supervisorId;

    @Column(name = "supervisor_comments", columnDefinition = "TEXT")
    private String supervisorComments;

    @Column(name = "approved_at")
    private String approvedAt;

    @Override
    public Long getId() {
        return id;
    }

    public enum CompletionStatus {
        PENDING, IN_PROGRESS, COMPLETED, SKIPPED
    }

    public enum SupervisorApproval {
        PENDING, APPROVED, REJECTED
    }
}
