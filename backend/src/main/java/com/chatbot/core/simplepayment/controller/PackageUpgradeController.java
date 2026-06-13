package com.chatbot.core.simplepayment.controller;

import com.chatbot.core.simplepayment.model.PackageUpgradeAudit;
import com.chatbot.core.simplepayment.service.PaymentPackageUpgradeService;
import com.chatbot.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.chatbot.core.tenant.infra.TenantContext;

import java.util.List;

@RestController
@RequestMapping("/api/simple-payment/upgrade")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Package Upgrade Management", description = "API for managing package upgrades and audit")
public class PackageUpgradeController {

    private final PaymentPackageUpgradeService packageUpgradeService;

    /**
     * Get upgrade history for current tenant
     */
    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get upgrade history", description = "Get package upgrade history for the current tenant")
    public ResponseEntity<ApiResponse<List<PackageUpgradeAudit>>> getUpgradeHistory() {
        try {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Tenant context not found"));
            }
            
            List<PackageUpgradeAudit> history = packageUpgradeService.getTenantUpgradeHistory(tenantId);
            
            return ResponseEntity.ok(ApiResponse.success(history, "Upgrade history retrieved successfully"));
        } catch (Exception e) {
            log.error("❌ Error getting upgrade history: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Error getting upgrade history: " + e.getMessage()));
        }
    }

    /**
     * Get upgrade statistics (admin only)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get upgrade statistics", description = "Get package upgrade statistics (Admin only)")
    public ResponseEntity<ApiResponse<List<Object[]>>> getUpgradeStatistics() {
        try {
            List<Object[]> statistics = packageUpgradeService.getUpgradeStatistics();
            
            return ResponseEntity.ok(ApiResponse.success(statistics, "Upgrade statistics retrieved successfully"));
        } catch (Exception e) {
            log.error("❌ Error getting upgrade statistics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Error getting upgrade statistics: " + e.getMessage()));
        }
    }

    /**
     * Test package extraction from description
     */
    @GetMapping("/extract-package")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Test package extraction", description = "Test package ID extraction from description (Admin only)")
    public ResponseEntity<ApiResponse<String>> extractPackageFromDescription(@RequestParam String description) {
        try {
            String packageId = packageUpgradeService.extractPackageIdFromDescription(description);
            
            return ResponseEntity.ok(ApiResponse.success(packageId, "Package extraction completed"));
        } catch (Exception e) {
            log.error("❌ Error extracting package from description: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Error extracting package: " + e.getMessage()));
        }
    }
}
