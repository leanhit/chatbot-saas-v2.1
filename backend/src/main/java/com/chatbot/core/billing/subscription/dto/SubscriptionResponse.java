package com.chatbot.core.billing.subscription.dto;

import com.chatbot.core.billing.subscription.model.SubscriptionPlan;
import com.chatbot.core.billing.subscription.model.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {

    private Long id;
    private String subscriptionNumber;
    private Long billingAccountId;
    private SubscriptionPlan plan;
    private SubscriptionStatus status;
    private String planName;
    private String planDescription;
    private String billingCycle;
    private BigDecimal price;
    private String currency;
    private LocalDateTime trialStart;
    private LocalDateTime trialEnd;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private Boolean autoRenew;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
    private LocalDateTime suspendedAt;
    private String suspensionReason;
    private LocalDateTime lastBillingDate;
    private LocalDateTime nextBillingDate;
    private Integer maxUsers;
    private Long maxStorageMb;
    private Long maxApiCallsPerMonth;
    private Boolean isTrial;
    private Boolean isActive;
    private Boolean isExpired;
    private Long trialDaysRemaining;
    private Long daysUntilExpiry;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long tenantId;
}
