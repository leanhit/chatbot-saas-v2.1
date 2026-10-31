package com.chatbot.core.routing.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversation_routing")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationRouting extends BaseTenantEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Override
    public Object getId() {
        return id;
    }

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "routing_status", nullable = false)
    private RoutingStatus routingStatus;

    @Column(name = "assigned_agent_id")
    private Long assignedAgentId;

    @Column(name = "assigned_team_id")
    private Long assignedTeamId;

    @Column(name = "routing_rule_id")
    private Long routingRuleId;

    @Column(name = "priority", nullable = false)
    @Builder.Default
    private Integer priority = 5;

    @Column(name = "queue_position")
    private Integer queuePosition;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @Column(name = "escalation_rule_id")
    private Long escalationRuleId;

    @Column(name = "escalated_from_agent_id")
    private Long escalatedFromAgentId;

    @Column(name = "escalated_to_agent_id")
    private Long escalatedToAgentId;

    @Column(name = "escalation_reason")
    private String escalationReason;

    @Column(name = "wait_time_seconds")
    private Integer waitTimeSeconds;

    @Column(name = "handling_time_seconds")
    private Integer handlingTimeSeconds;

    @Column(name = "is_escalated")
    @Builder.Default
    private Boolean isEscalated = false;

    public enum RoutingStatus {
        QUEUED,
        ASSIGNED,
        IN_PROGRESS,
        ESCALATED,
        COMPLETED,
        CANCELLED
    }
}
