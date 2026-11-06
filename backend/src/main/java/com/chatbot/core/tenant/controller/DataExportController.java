package com.chatbot.core.tenant.controller;

import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.service.DataExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Map;

/**
 * GDPR data export endpoints (Phase 6).
 * Admin-only: exports all tenant-owned data as JSON.
 */
@RestController
@RequestMapping("/api/tenant/export")
@RequiredArgsConstructor
@Slf4j
public class DataExportController {

    private final DataExportService dataExportService;

    @GetMapping("/data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> exportTenantData() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Tenant context missing"));
        }
        try {
            String json = dataExportService.exportTenantData(tenantId);
            String filename = "tenant-" + tenantId + "-export-" + LocalDate.now() + ".json";
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("[DataExport] Export failed for tenant {}: {}", tenantId, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Export failed: " + e.getMessage()));
        }
    }
}
