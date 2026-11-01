package com.chatbot.core.routing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "escalation_history", indexes = {
    @Index(name = "idx_escalation_history_tenant", columnList = "tenantId"),
    @Index(name = "idx_escalation_history_conversation", columnList = "conversationId"),
    @Index(name = "idx_escalation_history_agent", columnList = "escalatedToAgentId"),
    @Index(name = "idx_escalation_history_team", columnList = "escalatedToTeamId"),
    @Index(name = "idx_escalation_history_rule", columnList = "escalationRuleId"),
    @Index(name = "idx_escalation_history_created_at", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscalationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "escalated_to_agent_id")
    private Long escalatedToAgentId;

    @Column(name = "escalated_to_team_id")
    private Long escalatedToTeamId;

    @Column(name = "escalation_rule_id")
    private Long escalationRuleId;

    @Column(name = "escalation_trigger_type", nullable = false)
    private String escalationTriggerType;

    @Column(name = "escalation_action", nullable = false)
    private String escalationAction;

    @Column(name = "escalated_to_supervisor")
    private Boolean escalatedToSupervisor;

    @Column(name = "supervisor_notified")
    private Boolean supervisorNotified;

    @Column(name = "escalated", nullable = false)
    private Boolean escalated;

    @Column(name = "reason")
    private String reason;

    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
