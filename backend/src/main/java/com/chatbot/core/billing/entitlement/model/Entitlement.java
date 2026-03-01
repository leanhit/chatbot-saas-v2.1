package com.chatbot.core.billing.entitlement.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entitlement Entity
 * Manages feature entitlements and usage limits for tenants
 */
@Entity
@Table(name = "entitlements", 
       indexes = {
           @Index(name = "idx_entitlement_tenant", columnList = "tenant_id"),
           @Index(name = "idx_entitlement_feature", columnList = "feature"),
           @Index(name = "idx_entitlement_subscription", columnList = "subscription_id")
       })
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Entitlement extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Remove problematic relationship for now
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "subscription_id")
    // private com.chatbot.core.billing.subscription.model.BillingSubscription subscription;
    
    // Add subscriptionId field as workaround
    @Column(name = "subscription_id")
    private Long subscriptionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "feature", nullable = false)
    private Feature feature;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_limit_type")
    private UsageLimit usageLimitType;

    @Column(name = "limit_value")
    private Long limitValue;

    @Column(name = "current_usage")
    private Long currentUsage;

    @Column(name = "reset_period")
    private String resetPeriod; // MONTHLY, DAILY, YEARLY, NEVER

    @Column(name = "last_reset_at")
    private LocalDateTime lastResetAt;

    @Column(name = "next_reset_at")
    private LocalDateTime nextResetAt;

    @Column(name = "is_enabled")
    @Builder.Default
    private Boolean isEnabled = true;

    @Column(name = "is_unlimited")
    @Builder.Default
    private Boolean isUnlimited = false;

    @Column(name = "overage_allowed")
    @Builder.Default
    private Boolean overageAllowed = false;

    @Column(name = "overage_rate", precision = 10, scale = 4)
    private BigDecimal overageRate; // Cost per unit over limit

    @Column(name = "soft_limit")
    @Builder.Default
    private Boolean softLimit = false; // If true, allow usage but warn

    @Column(name = "warning_threshold_percentage")
    @Builder.Default
    private Integer warningThresholdPercentage = 80; // Send warning at 80%

    @Column(name = "last_warning_at")
    private LocalDateTime lastWarningAt;

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public Long getTenantId() {
        return super.getTenantId();
    }

    @Override
    public void setTenantId(Long tenantId) {
        super.setTenantId(tenantId);
    }

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        
        // Set initial reset schedule
        if (resetPeriod != null && lastResetAt == null) {
            resetUsage();
        }
    }

    // Helper methods
    public boolean isAtLimit() {
        if (isUnlimited || limitValue == null) return false;
        return currentUsage >= limitValue;
    }

    public boolean isOverLimit() {
        if (isUnlimited || limitValue == null) return false;
        return currentUsage > limitValue;
    }

    public boolean isNearLimit() {
        if (isUnlimited || limitValue == null) return false;
        double threshold = limitValue * (warningThresholdPercentage / 100.0);
        return currentUsage >= threshold;
    }

    public long getRemainingUsage() {
        if (isUnlimited || limitValue == null) return Long.MAX_VALUE;
        return Math.max(0, limitValue - currentUsage);
    }

    public double getUsagePercentage() {
        if (isUnlimited || limitValue == null) return 0.0;
        return limitValue == 0 ? 100.0 : (double) currentUsage / limitValue * 100.0;
    }

    /**
     * Reset usage counter based on reset period
     */
    public void resetUsage() {
        LocalDateTime now = LocalDateTime.now();
        this.currentUsage = 0L;
        this.lastResetAt = now;

        // Calculate next reset time
        switch (resetPeriod) {
            case "DAILY":
                this.nextResetAt = now.plusDays(1);
                break;
            case "WEEKLY":
                this.nextResetAt = now.plusWeeks(1);
                break;
            case "MONTHLY":
                this.nextResetAt = now.plusMonths(1);
                break;
            case "YEARLY":
                this.nextResetAt = now.plusYears(1);
                break;
            default:
                this.nextResetAt = null;
                break;
        }
    }

    /**
     * Check if reset is needed
     */
    public boolean needsReset() {
        if (resetPeriod == null || nextResetAt == null) return false;
        return LocalDateTime.now().isAfter(nextResetAt) || LocalDateTime.now().isEqual(nextResetAt);
    }

    /**
     * Add usage and check limits
     */
    public boolean addUsage(long amount) {
        if (isUnlimited) {
            currentUsage += amount;
            return true;
        }

        if (needsReset()) {
            resetUsage();
        }

        if (!overageAllowed && isAtLimit()) {
            return false; // Block usage
        }

        currentUsage += amount;
        return true;
    }

    /**
     * Check if warning should be sent
     */
    public boolean shouldSendWarning() {
        if (lastWarningAt != null) {
            // Don't send warning more than once per day
            LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);
            if (lastWarningAt.isAfter(dayAgo)) {
                return false;
            }
        }
        return isNearLimit() && !isAtLimit();
    }
}
