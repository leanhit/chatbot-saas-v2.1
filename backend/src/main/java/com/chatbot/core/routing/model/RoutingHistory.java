package com.chatbot.core.routing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "routing_history", indexes = {
    @Index(name = "idx_routing_history_tenant", columnList = "tenantId"),
    @Index(name = "idx_routing_history_conversation", columnList = "conversationId"),
    @Index(name = "idx_routing_history_agent", columnList = "assignedAgentId"),
    @Index(name = "idx_routing_history_created_at", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoutingHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "conversation_id", nullable = false)
    private Long conversationId;

    @Column(name = "assigned_agent_id")
    private Long assignedAgentId;

    @Column(name = "assigned_team_id")
    private Long assignedTeamId;

    @Column(name = "routing_rule_id")
    private Long routingRuleId;

    @Column(name = "routing_strategy", nullable = false)
    private String routingStrategy;

    @Column(name = "conditions")
    private String conditions;

    @Column(name = "assigned", nullable = false)
    private Boolean assigned;

    @Column(name = "reason")
    private String reason;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
