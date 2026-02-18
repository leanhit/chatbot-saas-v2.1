package com.chatbot.core.billing.account.controller;

import com.chatbot.core.billing.account.dto.BillingAccountRequest;
import com.chatbot.core.billing.account.dto.BillingAccountResponse;
import com.chatbot.core.billing.account.service.BillingAccountService;
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
@RequestMapping("/api/billing/accounts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Billing Account Management", description = "APIs for managing billing accounts")
public class BillingAccountController {

    private final BillingAccountService billingAccountService;

    @Operation(summary = "Create billing account", description = "Create a new billing account for a tenant")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('TENANT_OWNER')")
    public ResponseEntity<ApiResponse<BillingAccountResponse>> createBillingAccount(
            @RequestParam Long tenantId,
            @Valid @RequestBody BillingAccountRequest request) {
        
        BillingAccountResponse response = billingAccountService.createBillingAccount(tenantId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Billing account created successfully"));
    }

    @Operation(summary = "Get billing account by tenant", description = "Retrieve billing account information for a specific tenant")
    @GetMapping("/tenant/{tenantId}")
    @PreAuthorize("hasRole('ADMIN') or @tenantSecurity.isTenantMember(#tenantId)")
    public ResponseEntity<ApiResponse<BillingAccountResponse>> getBillingAccountByTenant(
            @Parameter(description = "Tenant ID") @PathVariable Long tenantId) {
        
        BillingAccountResponse response = billingAccountService.getBillingAccountByTenant(tenantId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Get billing account by ID", description = "Retrieve billing account information by account ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @billingSecurity.canAccessAccount(#id)")
    public ResponseEntity<ApiResponse<BillingAccountResponse>> getBillingAccountById(
            @Parameter(description = "Account ID") @PathVariable Long id) {
        
        BillingAccountResponse response = billingAccountService.getBillingAccountById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Update billing account", description = "Update billing account information")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @billingSecurity.canManageAccount(#id)")
    public ResponseEntity<ApiResponse<BillingAccountResponse>> updateBillingAccount(
            @Parameter(description = "Account ID") @PathVariable Long id,
            @Valid @RequestBody BillingAccountRequest request) {
        
        BillingAccountResponse response = billingAccountService.updateBillingAccount(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Billing account updated successfully"));
    }

    @Operation(summary = "Suspend billing account", description = "Suspend a billing account")
    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN') or @billingSecurity.canManageAccount(#id)")
    public ResponseEntity<ApiResponse<Void>> suspendBillingAccount(
            @Parameter(description = "Account ID") @PathVariable Long id,
            @RequestParam String reason) {
        
        billingAccountService.suspendBillingAccount(id, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "Billing account suspended successfully"));
    }

    @Operation(summary = "Reactivate billing account", description = "Reactivate a suspended billing account")
    @PostMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN') or @billingSecurity.canManageAccount(#id)")
    public ResponseEntity<ApiResponse<Void>> reactivateBillingAccount(
            @Parameter(description = "Account ID") @PathVariable Long id) {
        
        billingAccountService.reactivateBillingAccount(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Billing account reactivated successfully"));
    }

    @Operation(summary = "Search billing accounts", description = "Search billing accounts by keyword")
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<BillingAccountResponse>>> searchAccounts(
            @Parameter(description = "Search keyword") @RequestParam(required = false) String keyword,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BillingAccountResponse> result = billingAccountService.searchAccounts(keyword, pageable);
        
        PageResponse<BillingAccountResponse> response = PageResponse.<BillingAccountResponse>builder()
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

    @Operation(summary = "Get active billing accounts", description = "Retrieve all active billing accounts")
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BillingAccountResponse>>> getActiveAccounts() {
        List<BillingAccountResponse> accounts = billingAccountService.getActiveAccounts();
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @Operation(summary = "Get accounts needing suspension", description = "Retrieve accounts that need suspension due to credit limit")
    @GetMapping("/needs-suspension")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<BillingAccountResponse>>> getAccountsNeedingSuspension() {
        List<BillingAccountResponse> accounts = billingAccountService.getAccountsNeedingSuspension();
        return ResponseEntity.ok(ApiResponse.success(accounts));
    }

    @Operation(summary = "Get total outstanding balance", description = "Get total outstanding balance for all active accounts")
    @GetMapping("/total-outstanding")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<java.math.BigDecimal>> getTotalOutstandingBalance() {
        java.math.BigDecimal total = billingAccountService.getTotalOutstandingBalance();
        return ResponseEntity.ok(ApiResponse.success(total));
    }
}
