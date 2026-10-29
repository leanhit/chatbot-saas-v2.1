package com.chatbot.core.training.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Agent Onboarding Task entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "agent_onboarding_tasks")
@EqualsAndHashCode(callSuper = true)
public class AgentOnboardingTask extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private TaskType type;

    @Column(name = "training_id")
    private Long trainingId;

    @Column(nullable = false)
    private Boolean required;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "external_url")
    private String externalUrl;

    @Override
    public Long getId() {
        return id;
    }

    public enum TaskType {
        TRAINING, DOCUMENT, VIDEO, QUIZ, TASK, MEETING
    }

    public enum TaskStatus {
        ACTIVE, INACTIVE
    }
}
