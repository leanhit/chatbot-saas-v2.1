package com.chatbot.core.billing.subscription.dto;

import com.chatbot.core.billing.subscription.model.SubscriptionPlan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SubscriptionRequest {

    @NotNull(message = "Plan is required")
    private SubscriptionPlan plan;

    @NotBlank(message = "Plan name is required")
    private String planName;

    private String planDescription;

    @NotBlank(message = "Billing cycle is required")
    private String billingCycle = "MONTHLY";

    private BigDecimal price;

    @NotBlank(message = "Currency is required")
    private String currency = "USD";

    private Boolean autoRenew = true;

    private Integer maxUsers;

    private Long maxStorageMb;

    private Long maxApiCallsPerMonth;

    // Trial settings
    private Boolean enableTrial = false;
    private Integer trialDays = 14;
}
