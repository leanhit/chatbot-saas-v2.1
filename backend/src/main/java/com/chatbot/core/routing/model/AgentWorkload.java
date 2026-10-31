package com.chatbot.core.routing.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "agent_workload", indexes = {
    @Index(name = "idx_agent_workload_tenant", columnList = "tenantId"),
    @Index(name = "idx_agent_workload_agent", columnList = "agentId"),
    @Index(name = "idx_agent_workload_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentWorkload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Column(name = "active_conversations", nullable = false)
    @Builder.Default
    private Integer activeConversations = 0;

    @Column(name = "max_concurrent_conversations", nullable = false)
    @Builder.Default
    private Integer maxConcurrentConversations = 10;

    @Column(name = "total_conversations_today", nullable = false)
    @Builder.Default
    private Integer totalConversationsToday = 0;

    @Column(name = "total_conversations_this_week", nullable = false)
    @Builder.Default
    private Integer totalConversationsThisWeek = 0;

    @Column(name = "average_response_time_seconds")
    private Integer averageResponseTimeSeconds;

    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private AgentStatus status = AgentStatus.AVAILABLE;

    @Column(name = "priority_score")
    @Builder.Default
    private Integer priorityScore = 50;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public enum AgentStatus {
        AVAILABLE,
        BUSY,
        OFFLINE,
        AWAY
    }

    public boolean isAvailable() {
        return status == AgentStatus.AVAILABLE 
                && activeConversations < maxConcurrentConversations;
    }

    public double getWorkloadPercentage() {
        if (maxConcurrentConversations == 0) {
            return 0.0;
        }
        return (double) activeConversations / maxConcurrentConversations * 100;
    }
}
