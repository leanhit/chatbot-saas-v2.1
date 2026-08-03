package com.chatbot.core.message.store.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.chatbot.shared.utils.DateUtils;
import java.time.LocalDateTime;

/**
 * EscalationTier entity for multi-tier escalation system
 * Defines escalation tiers with timeout thresholds
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "escalation_tiers",
    indexes = {
        @Index(name = "idx_escalation_tier_tenant", columnList = "tenant_id"),
        @Index(name = "idx_escalation_tier_level", columnList = "level")
    }
)
@EqualsAndHashCode(callSuper = true)
public class EscalationTier extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Escalation level (1 = Agent, 2 = Team Lead, 3 = Supervisor)
     */
    @Column(nullable = false)
    private Integer level;

    /**
     * Tier name for display
     */
    @Column(nullable = false)
    private String name;

    /**
     * Timeout in seconds before escalating to next tier
     * Tier 1: 300 seconds (5 minutes)
     * Tier 2: 900 seconds (15 minutes)
     * Tier 3: 1800 seconds (30 minutes)
     */
    @Column(nullable = false)
    private Long timeoutSeconds;

    /**
     * Whether this tier is active
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Description of this tier's responsibilities
     */
    private String description;

    /**
     * Required role for agents at this tier
     */
    private String requiredRole;

    // Timestamp
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
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
}
