package com.chatbot.core.message.store.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.chatbot.shared.utils.DateUtils;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Routing rule entity for attribute-based conversation routing
 * Implements Phase 1.3: Attribute-based Routing
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "routing_rules",
    indexes = {
        @Index(name = "idx_routing_rule_tenant", columnList = "tenant_id"),
        @Index(name = "idx_routing_rule_priority", columnList = "priority"),
        @Index(name = "idx_routing_rule_active", columnList = "active")
    }
)
@EqualsAndHashCode(callSuper = true)
public class RoutingRule extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Rule name
     */
    @Column(nullable = false)
    @NotBlank(message = "Rule name is required")
    private String name;

    /**
     * Rule description
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Priority (higher = more important)
     */
    @Column(nullable = false)
    @Builder.Default
    @Min(value = 0, message = "Priority must be non-negative")
    private Integer priority = 0;

    /**
     * Conditions for matching (stored as JSON)
     * e.g., {"customerTier": "VIP", "language": "en"}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> conditions;

    /**
     * Action to take when rule matches
     * e.g., {"action": "assign_to_agent", "agentId": 123}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> action;

    /**
     * Whether this rule is active
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Rule type (AUTO_ASSIGN, ROUTE_TO_QUEUE, ESCALATE, BLOCK)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Rule type is required")
    private RoutingRuleType ruleType;

    // Timestamp
    @CreationTimestamp
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime updatedAt;

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
     * Routing Rule Type Enum
     */
    public enum RoutingRuleType {
        AUTO_ASSIGN,      // Auto-assign to specific agent
        ROUTE_TO_QUEUE,   // Route to specific queue
        ESCALATE,         // Escalate immediately
        BLOCK,            // Block conversation
        CUSTOM            // Custom action
    }
}
