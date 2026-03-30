package com.chatbot.core.tenant.controller;

import com.chatbot.core.simplepayment.model.Package;
import com.chatbot.core.tenant.service.TenantPackageService;
import com.chatbot.shared.constants.ApiConstants;
import com.chatbot.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.BASE_PATH + "/tenant-packages")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tenant Package Management", description = "API for managing tenant packages")
public class TenantPackageController {

    private final TenantPackageService tenantPackageService;

    /**
     * Get current package for current tenant
     */
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current tenant package", description = "Get the current package for the authenticated tenant")
    public ResponseEntity<ApiResponse<Package>> getCurrentPackage() {
        try {
            // This would typically get tenant ID from security context
            // For now, we'll use a mock implementation
            Long tenantId = 1L; // TODO: Get from security context
            
            Package currentPackage = tenantPackageService.getCurrentTenantPackage(tenantId);
            
            if (currentPackage == null) {
                return ResponseEntity.ok(ApiResponse.success(null, "No package assigned"));
            }
            
            return ResponseEntity.ok(ApiResponse.success(currentPackage, "Current package retrieved successfully"));
        } catch (Exception e) {
            log.error("❌ Error getting current package: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Error getting current package: " + e.getMessage()));
        }
    }

    /**
     * Get current package by tenant key
     */
    @GetMapping("/current/{tenantKey}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current tenant package by key", description = "Get the current package for a specific tenant by key")
    public ResponseEntity<ApiResponse<Package>> getCurrentPackageByKey(@PathVariable String tenantKey) {
        try {
            Package currentPackage = tenantPackageService.getCurrentTenantPackageByKey(tenantKey);
            
            if (currentPackage == null) {
                return ResponseEntity.ok(ApiResponse.success(null, "No package assigned"));
            }
            
            return ResponseEntity.ok(ApiResponse.success(currentPackage, "Current package retrieved successfully"));
        } catch (Exception e) {
            log.error("❌ Error getting current package for tenant {}: {}", tenantKey, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Error getting current package: " + e.getMessage()));
        }
    }

    /**
     * Upgrade tenant package (admin only)
     */
    @PostMapping("/upgrade/{tenantId}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Upgrade tenant package", description = "Upgrade a tenant to a specific package (Admin only)")
    public ResponseEntity<ApiResponse<String>> upgradeTenantPackage(
            @PathVariable Long tenantId,
            @RequestParam String packageId) {
        try {
            tenantPackageService.upgradeTenantPackage(tenantId, packageId);
            return ResponseEntity.ok(ApiResponse.success("Package upgraded successfully", "Tenant package upgraded to " + packageId));
        } catch (Exception e) {
            log.error("❌ Error upgrading package for tenant {}: {}", tenantId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Error upgrading package: " + e.getMessage()));
        }
    }

    /**
     * Check if tenant has specific package
     */
    @GetMapping("/has-package/{tenantId}/{packageId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check tenant package", description = "Check if a tenant has a specific package")
    public ResponseEntity<ApiResponse<Boolean>> hasTenantPackage(
            @PathVariable Long tenantId,
            @PathVariable String packageId) {
        try {
            boolean hasPackage = tenantPackageService.hasTenantPackage(tenantId, packageId);
            return ResponseEntity.ok(ApiResponse.success(hasPackage, "Package check completed"));
        } catch (Exception e) {
            log.error("❌ Error checking package for tenant {}: {}", tenantId, e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Error checking package: " + e.getMessage()));
        }
    }

    /**
     * Initialize existing tenants with free package (admin only)
     */
    @PostMapping("/initialize-existing")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    @Operation(summary = "Initialize existing tenants", description = "Assign free package to all existing tenants without packages (Admin only)")
    public ResponseEntity<ApiResponse<String>> initializeExistingTenants() {
        try {
            tenantPackageService.initializeExistingTenants();
            return ResponseEntity.ok(ApiResponse.success("Initialization completed", "Existing tenants initialized with free package"));
        } catch (Exception e) {
            log.error("❌ Error initializing existing tenants: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ApiResponse.error("Error initializing tenants: " + e.getMessage()));
        }
    }
}
