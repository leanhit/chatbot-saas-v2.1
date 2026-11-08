package com.chatbot.core.routing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "escalation_history", indexes = {
    @Index(name = "idx_escalation_history_tenant", columnList = "tenant_id"),
    @Index(name = "idx_escalation_history_conversation", columnList = "conversation_id"),
    @Index(name = "idx_escalation_history_agent", columnList = "escalated_to_agent_id"),
    @Index(name = "idx_escalation_history_team", columnList = "escalated_to_team_id"),
    @Index(name = "idx_escalation_history_rule", columnList = "escalation_rule_id"),
    @Index(name = "idx_escalation_history_created_at", columnList = "created_at")
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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
