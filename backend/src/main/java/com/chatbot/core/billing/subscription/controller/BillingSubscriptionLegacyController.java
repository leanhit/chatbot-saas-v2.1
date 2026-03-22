package com.chatbot.core.billing.subscription.controller;

import com.chatbot.core.billing.subscription.dto.SubscriptionRequest;
import com.chatbot.core.billing.subscription.dto.SubscriptionResponse;
import com.chatbot.core.billing.subscription.service.BillingSubscriptionService;
import com.chatbot.core.tenant.service.TenantService;
import com.chatbot.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/billing/subscription")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Billing Subscription Legacy", description = "Legacy APIs for billing subscriptions")
public class BillingSubscriptionLegacyController {

    private final BillingSubscriptionService subscriptionService;
    private final TenantService tenantService;

    @Operation(summary = "Get subscription by tenant key", description = "Retrieve subscription information by tenant key")
    @GetMapping("/{tenantKey}")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMemberByKey(#tenantKey)")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscriptionByTenantKey(
            @Parameter(description = "Tenant Key") @PathVariable String tenantKey) {
        
        log.info("Getting subscription for tenant key: {}", tenantKey);
        SubscriptionResponse response = subscriptionService.getSubscriptionByTenantKey(tenantKey);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Create subscription by tenant key", description = "Create new subscription by tenant key")
    @PostMapping("/{tenantKey}")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMemberByKey(#tenantKey)")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscriptionByTenantKey(
            @Parameter(description = "Tenant Key") @PathVariable String tenantKey,
            @Valid @RequestBody SubscriptionRequest request) {
        
        log.info("Creating subscription for tenant key: {} with plan: {}", tenantKey, request.getPlan());
        Long tenantId = tenantService.getTenantIdByKey(tenantKey);
        if (tenantId == null) {
            throw new RuntimeException("Tenant not found for tenant key: " + tenantKey);
        }
        SubscriptionResponse response = subscriptionService.createSubscription(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Subscription created successfully"));
    }
}
