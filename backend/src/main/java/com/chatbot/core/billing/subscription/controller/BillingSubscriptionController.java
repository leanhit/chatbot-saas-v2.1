package com.chatbot.core.billing.subscription.controller;

import com.chatbot.core.billing.subscription.dto.SubscriptionRequest;
import com.chatbot.core.billing.subscription.dto.SubscriptionResponse;
import com.chatbot.core.billing.subscription.service.BillingSubscriptionService;
import com.chatbot.shared.dto.ApiResponse;
import com.chatbot.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/billing/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Billing Subscription Management", description = "APIs for managing billing subscriptions")
public class BillingSubscriptionController {

    private final BillingSubscriptionService subscriptionService;

    @Operation(summary = "Create subscription", description = "Create a new subscription for a tenant")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TENANT_OWNER')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @RequestParam Long tenantId,
            @Valid @RequestBody SubscriptionRequest request) {
        
        SubscriptionResponse response = subscriptionService.createSubscription(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Subscription created successfully"));
    }

    @Operation(summary = "Get subscription by tenant", description = "Retrieve subscription information for a specific tenant")
    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMember(#tenantId)")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscriptionByTenant(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId) {
        
        SubscriptionResponse response = subscriptionService.getSubscriptionByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get subscription by ID", description = "Retrieve subscription information by subscription ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @billingSecurity.canAccessSubscription(#id)")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscriptionById(
            @Parameter(description = "Subscription ID") @PathVariable Long id) {
        
        SubscriptionResponse response = subscriptionService.getSubscriptionById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Update subscription", description = "Update subscription information")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @billingSecurity.canManageSubscription(#id)")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> updateSubscription(
            @Parameter(description = "Subscription ID") @PathVariable Long id,
            @Valid @RequestBody SubscriptionRequest request) {
        
        SubscriptionResponse response = subscriptionService.updateSubscription(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Subscription updated successfully"));
    }

    @Operation(summary = "Cancel subscription", description = "Cancel a subscription")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or @billingSecurity.canManageSubscription(#id)")
    public ResponseEntity<ApiResponse<Void>> cancelSubscription(
            @Parameter(description = "Subscription ID") @PathVariable Long id,
            @RequestParam String reason) {
        
        subscriptionService.cancelSubscription(id, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "Subscription cancelled successfully"));
    }

    @Operation(summary = "Suspend subscription", description = "Suspend a subscription")
    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN') or @billingSecurity.canManageSubscription(#id)")
    public ResponseEntity<ApiResponse<Void>> suspendSubscription(
            @Parameter(description = "Subscription ID") @PathVariable Long id,
            @RequestParam String reason) {
        
        subscriptionService.suspendSubscription(id, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "Subscription suspended successfully"));
    }

    @Operation(summary = "Reactivate subscription", description = "Reactivate a suspended subscription")
    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN') or @billingSecurity.canManageSubscription(#id)")
    public ResponseEntity<ApiResponse<Void>> reactivateSubscription(
            @Parameter(description = "Subscription ID") @PathVariable Long id) {
        
        subscriptionService.reactivateSubscription(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Subscription reactivated successfully"));
    }

    @Operation(summary = "Process renewal", description = "Process subscription renewal")
    @PostMapping("/{id}/renew")
    @PreAuthorize("hasRole('ADMIN') or @billingSecurity.canManageSubscription(#id)")
    public ResponseEntity<ApiResponse<Void>> processRenewal(
            @Parameter(description = "Subscription ID") @PathVariable Long id) {
        
        subscriptionService.processRenewal(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Subscription renewed successfully"));
    }

    @Operation(summary = "Search subscriptions", description = "Search subscriptions by keyword")
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<SubscriptionResponse>>> searchSubscriptions(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<SubscriptionResponse> result = subscriptionService.searchSubscriptions(keyword, pageable);
        
        PageResponse<SubscriptionResponse> response = PageResponse.<SubscriptionResponse>builder()
                .content(result.getContent())
                .page(page)
                .size(size)
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get active subscriptions", description = "Retrieve all active subscriptions")
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getActiveSubscriptions() {
        List<SubscriptionResponse> subscriptions = subscriptionService.getActiveSubscriptions();
        return ResponseEntity.ok(ApiResponse.success(subscriptions));
    }

    @Operation(summary = "Get expired subscriptions", description = "Retrieve all expired subscriptions")
    @GetMapping("/expired")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getExpiredSubscriptions() {
        List<SubscriptionResponse> subscriptions = subscriptionService.getExpiredSubscriptions();
        return ResponseEntity.ok(ApiResponse.success(subscriptions));
    }

    @Operation(summary = "Get subscriptions ending soon", description = "Retrieve subscriptions ending within 7 days")
    @GetMapping("/ending-soon")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getSubscriptionsEndingSoon() {
        List<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionsEndingSoon();
        return ResponseEntity.ok(ApiResponse.success(subscriptions));
    }

    @Operation(summary = "Get trials ending soon", description = "Retrieve trial subscriptions ending within 7 days")
    @GetMapping("/trials-ending-soon")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getTrialsEndingSoon() {
        List<SubscriptionResponse> subscriptions = subscriptionService.getTrialsEndingSoon();
        return ResponseEntity.ok(ApiResponse.success(subscriptions));
    }

    @Operation(summary = "Get subscriptions needing billing", description = "Retrieve subscriptions that need billing")
    @GetMapping("/needs-billing")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<SubscriptionResponse>>> getSubscriptionsNeedingBilling() {
        List<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionsNeedingBilling();
        return ResponseEntity.ok(ApiResponse.success(subscriptions));
    }

    @Operation(summary = "Get monthly recurring revenue", description = "Get total monthly recurring revenue")
    @GetMapping("/revenue/mrr")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getMonthlyRecurringRevenue() {
        java.math.BigDecimal mrr = subscriptionService.getMonthlyRecurringRevenue();
        return ResponseEntity.ok(ApiResponse.success(mrr));
    }

    @Operation(summary = "Get yearly recurring revenue", description = "Get total yearly recurring revenue")
    @GetMapping("/revenue/arr")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getYearlyRecurringRevenue() {
        java.math.BigDecimal arr = subscriptionService.getYearlyRecurringRevenue();
        return ResponseEntity.ok(ApiResponse.success(arr));
    }
}
