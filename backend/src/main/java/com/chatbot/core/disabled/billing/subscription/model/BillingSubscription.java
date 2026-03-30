package com.chatbot.core.billing.subscription.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Billing Subscription Entity
 * Manages subscription information for tenants
 */
@Entity
@Table(name = "billing_subscriptions", 
       indexes = {
           @Index(name = "idx_subscription_tenant", columnList = "tenant_id"),
           @Index(name = "idx_subscription_plan", columnList = "plan"),
           @Index(name = "idx_subscription_status", columnList = "status"),
           @Index(name = "idx_subscription_billing_account", columnList = "billing_account_id")
       })
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillingSubscription extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscription_number", unique = true, nullable = false)
    private String subscriptionNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "billing_account_id", nullable = false)
    private com.chatbot.core.billing.account.model.BillingAccount billingAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(name = "plan_description")
    private String planDescription;

    @Column(name = "billing_cycle", nullable = false)
    private String billingCycle; // MONTHLY, YEARLY

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "trial_start")
    private LocalDateTime trialStart;

    @Column(name = "trial_end")
    private LocalDateTime trialEnd;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @Column(name = "ends_at")
    private LocalDateTime endsAt;

    @Column(name = "auto_renew")
    @Builder.Default
    private Boolean autoRenew = true;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    @Column(name = "suspension_reason")
    private String suspensionReason;

    @Column(name = "last_billing_date")
    private LocalDateTime lastBillingDate;

    @Column(name = "next_billing_date")
    private LocalDateTime nextBillingDate;

    @Column(name = "max_users")
    private Integer maxUsers;

    @Column(name = "max_storage_mb")
    private Long maxStorageMb;

    @Column(name = "max_api_calls_per_month")
    private Long maxApiCallsPerMonth;

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
        // Generate subscription number if not set
        if (subscriptionNumber == null || subscriptionNumber.isBlank()) {
            subscriptionNumber = "SUB-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 1000);
        }
    }

    // Helper methods
    public boolean isTrial() {
        return status == SubscriptionStatus.TRIAL;
    }

    public boolean isActive() {
        return status.isActive();
    }

    public boolean isTerminated() {
        return status.isTerminated();
    }

    public boolean isExpired() {
        if (endsAt == null) return false;
        return LocalDateTime.now().isAfter(endsAt);
    }

    public boolean isInTrialPeriod() {
        if (trialStart == null || trialEnd == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(trialStart) && now.isBefore(trialEnd);
    }

    public boolean needsBilling() {
        if (nextBillingDate == null) return false;
        return LocalDateTime.now().isAfter(nextBillingDate) || LocalDateTime.now().isEqual(nextBillingDate);
    }

    public long getDaysUntilExpiry() {
        if (endsAt == null) return Long.MAX_VALUE;
        return java.time.Duration.between(LocalDateTime.now(), endsAt).toDays();
    }

    public long getTrialDaysRemaining() {
        if (trialEnd == null) return 0;
        return java.time.Duration.between(LocalDateTime.now(), trialEnd).toDays();
    }
}
