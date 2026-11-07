package com.chatbot.core.analytics.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Configurable funnel stage for conversion analytics.
 * matchType defines how conversations are counted into the stage:
 *  - STATUS: conversation.status equals matchValue
 *  - ESCALATED: conversation.currentEscalationTier is not null
 *  - FEEDBACK: customer has feedback
 *  - AGENT_ASSIGNED: conversation.agentAssignedId is not null
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "funnel_stage", indexes = {
    @Index(name = "idx_funnel_stage_tenant", columnList = "tenant_id, active")
})
@EqualsAndHashCode(callSuper = true)
public class FunnelStage extends BaseTenantEntity {

    public enum MatchType {
        STATUS,
        ESCALATED,
        FEEDBACK,
        AGENT_ASSIGNED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "stage_order", nullable = false)
    private Integer stageOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "match_type", nullable = false, length = 20)
    private MatchType matchType;

    @Column(name = "match_value", length = 50)
    private String matchValue;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Override
    public Long getId() {
        return id;
    }
}
