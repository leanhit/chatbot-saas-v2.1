package com.chatbot.modules.app.core.guard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * LOCAL-STUB ONLY: Pricing resolver for Chatbot app
 * Returns fixed pricing (free) for local development
 */
@Component
@Slf4j
public class ChatbotPricingResolver {
    
    /**
     * Resolve pricing for chatbot actions
     * LOCAL-STUB ONLY: Always free in local mode
     */
    public PricingResult resolve(GuardRequest request) {
        log.debug("Resolving pricing for request: {}", request);
        
        // LOCAL-STUB ONLY: always free
        return PricingResult.builder()
                .billable(false)
                .cost(BigDecimal.ZERO)
                .tier("free")
                .build();
    }
    
    /**
     * Pricing result holder
     */
    @lombok.Data
    @lombok.Builder
    public static class PricingResult {
        private boolean billable;
        private BigDecimal cost;
        private String tier;
    }
}
