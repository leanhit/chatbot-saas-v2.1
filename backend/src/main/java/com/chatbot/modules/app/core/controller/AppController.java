package com.chatbot.modules.app.core.controller;

import com.chatbot.modules.app.core.dto.TenantAppResponse;
import com.chatbot.modules.app.core.model.AppCode;
import com.chatbot.modules.app.core.service.AppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * App controller for v0.1
 * Core app management endpoints
 */
@RestController
@RequestMapping("/api/tenants/{tenantId}/apps")
@RequiredArgsConstructor
@Slf4j
public class AppController {

    private final AppService appService;

    /**
     * Get all apps for a tenant
     * Returns all available apps with their enable/disable status
     */
    @GetMapping
    public ResponseEntity<List<TenantAppResponse>> getTenantApps(@PathVariable UUID tenantId) {
        List<TenantAppResponse> apps = appService.getTenantApps(tenantId);
        return ResponseEntity.ok(apps);
    }

    /**
     * Enable an app for a tenant
     * Idempotent operation - safe to call multiple times
     */
    @PostMapping("/{appCode}/enable")
    public ResponseEntity<Void> enableApp(
            @PathVariable UUID tenantId,
            @PathVariable AppCode appCode) {
        appService.enableApp(tenantId, appCode);
        return ResponseEntity.ok().build();
    }

    /**
     * Disable an app for a tenant
     * Idempotent operation - safe to call multiple times
     */
    @PostMapping("/{appCode}/disable")
    public ResponseEntity<Void> disableApp(
            @PathVariable UUID tenantId,
            @PathVariable AppCode appCode) {
        appService.disableApp(tenantId, appCode);
        return ResponseEntity.ok().build();
    }
}
