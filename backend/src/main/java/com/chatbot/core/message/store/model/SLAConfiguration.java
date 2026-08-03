package com.chatbot.core.message.store.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.chatbot.shared.utils.DateUtils;
import java.time.LocalDateTime;

/**
 * SLA Configuration entity for storing tenant-specific SLA thresholds
 * Implements Phase 2.1: SLA Monitoring - Database Configuration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "sla_configurations",
    indexes = {
        @Index(name = "idx_sla_config_tenant", columnList = "tenant_id"),
        @Index(name = "idx_sla_config_tier", columnList = "customer_tier")
    }
)
@EqualsAndHashCode(callSuper = true)
public class SLAConfiguration extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Customer tier (VIP, Enterprise, Standard)
     */
    @Column(name = "customer_tier", nullable = false)
    @NotBlank(message = "Customer tier is required")
    private String customerTier;

    /**
     * Expected response time in seconds
     */
    @Column(nullable = false)
    @NotNull(message = "Expected response time is required")
    @Positive(message = "Expected response time must be positive")
    private Long expectedResponseTime;

    /**
     * Maximum allowed SLA breaches before escalation
     */
    @Column(nullable = false)
    @Builder.Default
    @Min(value = 1, message = "Max breach count must be at least 1")
    private Integer maxBreachCount = 3;

    /**
     * Whether this configuration is active
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Configuration description
     */
    @Column(columnDefinition = "TEXT")
    private String description;

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
