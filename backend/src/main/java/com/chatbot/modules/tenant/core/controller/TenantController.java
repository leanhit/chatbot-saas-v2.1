package com.chatbot.modules.tenant.core.controller;

import com.chatbot.modules.tenant.core.dto.CreateTenantRequest;
import com.chatbot.modules.tenant.core.dto.TenantResponse;
import com.chatbot.modules.tenant.core.dto.TenantSearchRequest;
import com.chatbot.modules.tenant.core.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * Tenant controller for v0.1
 * Core tenant management endpoints
 */
@RestController
@RequestMapping("/api/tenants")
@RequiredArgsConstructor
@Slf4j
public class TenantController {

    private final TenantService tenantService;

    /**
     * Get all tenants for the current user
     */
    @GetMapping("/me")
    public ResponseEntity<List<TenantResponse>> getUserTenants() {
        List<TenantResponse> tenants = tenantService.getUserTenants();
        return ResponseEntity.ok(tenants);
    }

    /**
     * Create a new tenant
     */
    @PostMapping
    public ResponseEntity<TenantResponse> createTenant(@Valid @RequestBody CreateTenantRequest request) {
        TenantResponse tenant = tenantService.createTenant(request);
        return ResponseEntity.ok(tenant);
    }

    /**
     * Search tenants
     */
    @GetMapping("/search")
    public ResponseEntity<List<TenantResponse>> searchTenants(@RequestParam String keyword) {
        TenantSearchRequest searchRequest = new TenantSearchRequest();
        searchRequest.setKeyword(keyword);
        List<TenantResponse> tenants = tenantService.searchTenants(searchRequest);
        return ResponseEntity.ok(tenants);
    }

    /**
     * Switch active tenant context
     */
    @PostMapping("/{id}/switch")
    public ResponseEntity<TenantResponse> switchTenant(@PathVariable UUID id) {
        TenantResponse tenant = tenantService.switchTenant(id);
        return ResponseEntity.ok(tenant);
    }

    /**
     * Get tenant details
     */
    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getTenant(@PathVariable UUID id) {
        TenantResponse tenant = tenantService.getTenant(id);
        return ResponseEntity.ok(tenant);
    }
}
