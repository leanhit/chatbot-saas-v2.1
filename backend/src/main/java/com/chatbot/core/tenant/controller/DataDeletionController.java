package com.chatbot.core.tenant.controller;

import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.model.DataDeletionRequest;
import com.chatbot.core.tenant.service.DataDeletionService;
import com.chatbot.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * GDPR data deletion request endpoints (Phase 6).
 * Admin-only. Requests have a 30-day grace period before
 * scheduled auto-processing; admins can also trigger manually.
 */
@RestController
@RequestMapping("/api/tenant/deletion-requests")
@RequiredArgsConstructor
@Slf4j
public class DataDeletionController {

    private final DataDeletionService dataDeletionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DataDeletionRequest>> create(
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails user,
            @RequestBody Map<String, String> body) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Tenant context missing"));
        }
        String reason = body.getOrDefault("reason", "");
        DataDeletionRequest request = dataDeletionService.createRequest(
            tenantId, TenantContext.getCurrentTenant(), user.getUsername(), reason);
        return ResponseEntity.ok(ApiResponse.success(request, "Deletion request created"));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<DataDeletionRequest>>> list() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Tenant context missing"));
        }
        return ResponseEntity.ok(ApiResponse.success(dataDeletionService.listRequests(tenantId)));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DataDeletionRequest>> cancel(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails user) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Tenant context missing"));
        }
        try {
            DataDeletionRequest request = dataDeletionService.cancelRequest(id, tenantId, user.getUsername());
            return ResponseEntity.ok(ApiResponse.success(request, "Deletion request cancelled"));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DataDeletionRequest>> process(
            @PathVariable Long id,
            @AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails user) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Tenant context missing"));
        }
        try {
            DataDeletionRequest request = dataDeletionService.processRequest(id, user.getUsername());
            return ResponseEntity.ok(ApiResponse.success(request, "Deletion processed: " + request.getStatus()));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
