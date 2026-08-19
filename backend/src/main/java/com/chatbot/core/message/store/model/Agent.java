package com.chatbot.core.message.store.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.chatbot.shared.utils.DateUtils;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Agent entity for agent management system
 * Implements Phase 3.1: Agent Management System
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "agents",
    indexes = {
        @Index(name = "idx_agent_tenant", columnList = "tenant_id"),
        @Index(name = "idx_agent_status", columnList = "status"),
        @Index(name = "idx_agent_user", columnList = "user_id")
    }
)
@EqualsAndHashCode(callSuper = true)
public class Agent extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reference to the user account
     */
    @Column(name = "user_id")
    private Long userId;

    /**
     * Agent name (may differ from user name)
     */
    @Column(nullable = false)
    private String name;

    /**
     * Agent email
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Agent role (AGENT, TEAM_LEAD, SUPERVISOR, ADMIN)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AgentRole role;

    /**
     * Availability status (ONLINE, OFFLINE, AWAY, BUSY)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AgentStatus status = AgentStatus.OFFLINE;

    /**
     * Current load (number of active conversations)
     */
    @Column(name = "current_load", nullable = false)
    @Builder.Default
    private Integer currentLoad = 0;

    /**
     * Maximum concurrent conversations allowed
     */
    @Column(name = "max_concurrent_conversations", nullable = false)
    @Builder.Default
    private Integer maxConcurrentConversations = 10;

    /**
     * Agent skills (stored as JSON array)
     * e.g., ["billing", "technical", "sales", "vietnamese", "english"]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private Set<String> skills;

    /**
     * Assignment preferences (stored as JSON)
     * e.g., {"preferredChannels": ["FACEBOOK", "ZALO"], "preferredLanguages": ["vi"]}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "assignment_preferences")
    private String assignmentPreferences;

    /**
     * Whether agent is active
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Agent profile/bio
     */
    @Column(columnDefinition = "TEXT")
    private String bio;

    /**
     * Phone number
     */
    @Column(name = "phone_number")
    private String phoneNumber;

    /**
     * Avatar URL
     */
    @Column(name = "avatar_url")
    private String avatarUrl;

    // Timestamp
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime updatedAt;

    /**
     * Last activity timestamp
     */
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    @Column(name = "last_activity_at")
    private LocalDateTime lastActivityAt;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Long getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * Check if agent can accept more conversations
     */
    public boolean canAcceptMoreConversations() {
        return currentLoad < maxConcurrentConversations && status == AgentStatus.ONLINE;
    }

    /**
     * Increment current load
     */
    public void incrementLoad() {
        this.currentLoad++;
    }

    /**
     * Decrement current load
     */
    public void decrementLoad() {
        if (this.currentLoad > 0) {
            this.currentLoad--;
        }
    }

    /**
     * Agent Role Enum
     */
    public enum AgentRole {
        AGENT,
        TEAM_LEAD,
        SUPERVISOR,
        ADMIN
    }

    /**
     * Agent Status Enum
     */
    public enum AgentStatus {
        ONLINE,
        OFFLINE,
        AWAY,
        BUSY
    }
}
