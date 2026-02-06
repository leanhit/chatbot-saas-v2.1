package com.chatbot.modules.app.core.guard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Context object returned by guard after successful validation
 * Only returned on success - guard throws exception on failure
 */
@Data
@Builder
public class GuardPassContext {
    
    private String reason;
    private boolean billable;
    private BigDecimal cost;
    private String pricingTier;
    
    public static GuardPassContext of(String reason, boolean billable, BigDecimal cost, String pricingTier) {
        return GuardPassContext.builder()
                .reason(reason)
                .billable(billable)
                .cost(cost)
                .pricingTier(pricingTier)
                .build();
    }
}
