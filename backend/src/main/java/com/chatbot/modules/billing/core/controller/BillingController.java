package com.chatbot.modules.billing.core.controller;

import com.chatbot.modules.app.core.model.AppCode;
import com.chatbot.modules.billing.core.dto.EntitlementResponse;
import com.chatbot.modules.billing.core.dto.TenantPlanResponse;
import com.chatbot.modules.billing.core.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Billing controller for v0.1
 * READ-ONLY policy endpoints
 */
@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
@Slf4j
public class BillingController {

    private final BillingService billingService;

    /**
     * Get tenant plan information
     * Returns plan details with default FREE plan if none assigned
     */
    @GetMapping("/tenants/{tenantId}/plan")
    public ResponseEntity<TenantPlanResponse> getTenantPlan(@PathVariable UUID tenantId) {
        TenantPlanResponse plan = billingService.getTenantPlan(tenantId);
        return ResponseEntity.ok(plan);
    }

    /**
     * Check tenant entitlement for specific app
     * Combines App Hub (enabled) and Billing Hub (plan allows) checks
     */
    @GetMapping("/entitlement")
    public ResponseEntity<EntitlementResponse> checkEntitlement(
            @RequestParam UUID tenantId,
            @RequestParam AppCode appCode) {
        EntitlementResponse entitlement = billingService.checkEntitlement(tenantId, appCode);
        return ResponseEntity.ok(entitlement);
    }
}
