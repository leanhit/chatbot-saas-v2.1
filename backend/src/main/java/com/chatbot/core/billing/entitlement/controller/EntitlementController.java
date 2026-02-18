package com.chatbot.core.billing.entitlement.controller;

import com.chatbot.core.billing.entitlement.model.Entitlement;
import com.chatbot.core.billing.entitlement.model.Feature;
import com.chatbot.core.billing.entitlement.model.UsageLimit;
import com.chatbot.core.billing.entitlement.service.EntitlementService;
import com.chatbot.core.billing.entitlement.service.EntitlementValidationService;
import com.chatbot.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/billing/entitlements")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Billing Entitlement Management", description = "APIs for managing billing entitlements and usage limits")
public class EntitlementController {

    private final EntitlementService entitlementService;
    private final EntitlementValidationService validationService;

    @Operation(summary = "Create entitlement", description = "Create a new entitlement")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Entitlement>> createEntitlement(@RequestBody Entitlement entitlement) {
        Entitlement created = entitlementService.createEntitlement(entitlement);
        return ResponseEntity.ok(ApiResponse.success(created, "Entitlement created successfully"));
    }

    @Operation(summary = "Get entitlements by tenant", description = "Retrieve all entitlements for a specific tenant")
    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMember(#tenantId)")
    public ResponseEntity<ApiResponse<List<Entitlement>>> getEntitlementsByTenant(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId) {
        
        List<Entitlement> entitlements = entitlementService.getEntitlementsByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success(entitlements));
    }

    @Operation(summary = "Get enabled entitlements by tenant", description = "Retrieve enabled entitlements for a specific tenant")
    @GetMapping("/tenant/{tenantId}/enabled")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMember(#tenantId)")
    public ResponseEntity<ApiResponse<List<Entitlement>>> getEnabledEntitlementsByTenant(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId) {
        
        List<Entitlement> entitlements = entitlementService.getEnabledEntitlementsByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success(entitlements));
    }

    @Operation(summary = "Get entitlement by tenant and feature", description = "Retrieve specific entitlement by tenant and feature")
    @GetMapping("/tenant/{tenantId}/feature/{feature}")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMember(#tenantId)")
    public ResponseEntity<ApiResponse<Entitlement>> getEntitlementByTenantAndFeature(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId,
            @Parameter(description = "Feature") @PathVariable Feature feature) {
        
        Entitlement entitlement = entitlementService.getEntitlementByTenantAndFeature(tenantId, feature);
        return ResponseEntity.ok(ApiResponse.success(entitlement));
    }

    @Operation(summary = "Update entitlement", description = "Update an existing entitlement")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Entitlement>> updateEntitlement(
            @Parameter(description = "Entitlement ID") @PathVariable Long id,
            @RequestBody Entitlement entitlement) {
        
        Entitlement updated = entitlementService.updateEntitlement(id, entitlement);
        return ResponseEntity.ok(ApiResponse.success(updated, "Entitlement updated successfully"));
    }

    @Operation(summary = "Delete entitlement", description = "Delete an entitlement")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteEntitlement(
            @Parameter(description = "Entitlement ID") @PathVariable Long id) {
        
        entitlementService.deleteEntitlement(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Entitlement deleted successfully"));
    }

    @Operation(summary = "Check feature access", description = "Validate if tenant can access a feature")
    @GetMapping("/tenant/{tenantId}/feature/{feature}/validate")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMember(#tenantId)")
    public ResponseEntity<ApiResponse<EntitlementValidationService.ValidationResult>> validateFeatureAccess(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId,
            @Parameter(description = "Feature") @PathVariable Feature feature) {
        
        EntitlementValidationService.ValidationResult result = 
                validationService.validateFeatureAccess(tenantId, feature);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "Validate and consume usage", description = "Validate and consume usage for a feature")
    @PostMapping("/tenant/{tenantId}/feature/{feature}/consume")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMember(#tenantId)")
    public ResponseEntity<ApiResponse<EntitlementValidationService.ValidationResult>> validateAndConsumeUsage(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId,
            @Parameter(description = "Feature") @PathVariable Feature feature,
            @RequestParam long amount) {
        
        EntitlementValidationService.ValidationResult result = 
                validationService.validateAndConsumeUsage(tenantId, feature, amount);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "Check usage limits", description = "Check all usage limits for a tenant")
    @GetMapping("/tenant/{tenantId}/usage-check")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMember(#tenantId)")
    public ResponseEntity<ApiResponse<EntitlementValidationService.UsageCheckResult>> checkUsageLimits(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId) {
        
        EntitlementValidationService.UsageCheckResult result = 
                validationService.checkUsageLimits(tenantId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "Get usage limit", description = "Get specific usage limit for a tenant")
    @GetMapping("/tenant/{tenantId}/limit/{limitType}")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMember(#tenantId)")
    public ResponseEntity<ApiResponse<Long>> getUsageLimit(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId,
            @Parameter(description = "Limit Type") @PathVariable UsageLimit limitType) {
        
        Long limit = validationService.getUsageLimit(tenantId, limitType);
        return ResponseEntity.ok(ApiResponse.success(limit));
    }

    @Operation(summary = "Can create user", description = "Check if tenant can create more users")
    @GetMapping("/tenant/{tenantId}/can-create-user")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMember(#tenantId)")
    public ResponseEntity<ApiResponse<EntitlementValidationService.ValidationResult>> canCreateUser(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId) {
        
        EntitlementValidationService.ValidationResult result = validationService.canCreateUser(tenantId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "Can create bot", description = "Check if tenant can create more bots")
    @GetMapping("/tenant/{tenantId}/can-create-bot")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMember(#tenantId)")
    public ResponseEntity<ApiResponse<EntitlementValidationService.ValidationResult>> canCreateBot(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId) {
        
        EntitlementValidationService.ValidationResult result = validationService.canCreateBot(tenantId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "Can upload file", description = "Check if tenant can upload file of specified size")
    @GetMapping("/tenant/{tenantId}/can-upload-file")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMember(#tenantId)")
    public ResponseEntity<ApiResponse<EntitlementValidationService.ValidationResult>> canUploadFile(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId,
            @RequestParam long fileSize) {
        
        EntitlementValidationService.ValidationResult result = validationService.canUploadFile(tenantId, fileSize);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "Can make API call", description = "Check if tenant can make API calls")
    @GetMapping("/tenant/{tenantId}/can-make-api-call")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMember(#tenantId)")
    public ResponseEntity<ApiResponse<EntitlementValidationService.ValidationResult>> canMakeApiCall(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId) {
        
        EntitlementValidationService.ValidationResult result = validationService.canMakeApiCall(tenantId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "Get entitlement status", description = "Get comprehensive entitlement status for tenant")
    @GetMapping("/tenant/{tenantId}/status")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMember(#tenantId)")
    public ResponseEntity<ApiResponse<EntitlementValidationService.EntitlementStatusResult>> getEntitlementStatus(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId) {
        
        EntitlementValidationService.EntitlementStatusResult result = 
                validationService.getEntitlementStatus(tenantId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @Operation(summary = "Get entitlement summary", description = "Get entitlement summary for tenant")
    @GetMapping("/tenant/{tenantId}/summary")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMember(#tenantId)")
    public ResponseEntity<ApiResponse<Map<Feature, EntitlementService.EntitlementSummary>>> getEntitlementSummary(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId) {
        
        Map<Feature, EntitlementService.EntitlementSummary> summary = 
                entitlementService.getEntitlementSummary(tenantId);
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    @Operation(summary = "Get entitlements at limit", description = "Get entitlements at or near usage limit")
    @GetMapping("/tenant/{tenantId}/at-limit")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMember(#tenantId)")
    public ResponseEntity<ApiResponse<List<Entitlement>>> getEntitlementsAtOrNearLimit(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId) {
        
        List<Entitlement> entitlements = entitlementService.getEntitlementsAtOrNearLimit(tenantId);
        return ResponseEntity.ok(ApiResponse.success(entitlements));
    }

    @Operation(summary = "Get entitlements over limit", description = "Get entitlements over usage limit")
    @GetMapping("/tenant/{tenantId}/over-limit")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMember(#tenantId)")
    public ResponseEntity<ApiResponse<List<Entitlement>>> getEntitlementsOverLimit(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId) {
        
        List<Entitlement> entitlements = entitlementService.getEntitlementsOverLimit(tenantId);
        return ResponseEntity.ok(ApiResponse.success(entitlements));
    }

    @Operation(summary = "Process usage resets", description = "Process periodic usage resets")
    @PostMapping("/process-resets")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> processUsageResets() {
        entitlementService.processUsageResets();
        return ResponseEntity.ok(ApiResponse.success(null, "Usage resets processed successfully"));
    }

    @Operation(summary = "Send usage warning", description = "Send usage warning for entitlement")
    @PostMapping("/{entitlementId}/send-warning")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> sendUsageWarning(
            @Parameter(description = "Entitlement ID") @PathVariable Long entitlementId) {
        
        entitlementService.sendUsageWarning(entitlementId);
        return ResponseEntity.ok(ApiResponse.success(null, "Usage warning sent successfully"));
    }
}
