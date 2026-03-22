package com.chatbot.core.billing.usage.controller;

import com.chatbot.core.billing.entitlement.service.EntitlementService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing/usage")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Billing Usage Management", description = "APIs for managing billing usage")
public class UsageController {

    private final EntitlementService entitlementService;
    private final TenantService tenantService;

    @Operation(summary = "Get usage data by tenant key", description = "Retrieve usage data for a specific tenant by tenant key")
    @GetMapping("/{tenantKey}")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMemberByKey(#tenantKey)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUsageByTenantKey(
            @Parameter(description = "Tenant Key") @PathVariable String tenantKey) {
        
        log.info("Getting usage data for tenant key: {}", tenantKey);
        
        // Get tenantId from tenantKey
        Long tenantId = tenantService.getTenantIdByKey(tenantKey);
        if (tenantId == null) {
            throw new RuntimeException("Tenant not found for tenant key: " + tenantKey);
        }
        
        // Get entitlements which contain usage information
        List<com.chatbot.core.billing.entitlement.model.Entitlement> entitlements = 
            entitlementService.getEntitlementsByTenant(tenantId);
        
        // Transform to usage data format
        Map<String, Object> usageData = new HashMap<>();
        for (com.chatbot.core.billing.entitlement.model.Entitlement entitlement : entitlements) {
            Map<String, Object> usage = new HashMap<>();
            usage.put("limit", entitlement.getLimitValue());
            usage.put("used", entitlement.getCurrentUsage());
            usage.put("remaining", entitlement.getLimitValue() - entitlement.getCurrentUsage());
            usage.put("percentage", entitlement.getLimitValue() > 0 ? 
                (double) entitlement.getCurrentUsage() / entitlement.getLimitValue() * 100 : 0);
            usageData.put(entitlement.getUsageLimitType().name(), usage);
        }
        
        return ResponseEntity.ok(ApiResponse.success(usageData));
    }
}
