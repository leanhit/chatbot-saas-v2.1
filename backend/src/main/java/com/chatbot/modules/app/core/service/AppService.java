package com.chatbot.modules.app.core.service;

import com.chatbot.modules.app.core.dto.TenantAppResponse;
import com.chatbot.modules.app.core.model.AppCode;
import com.chatbot.modules.app.core.model.TenantApp;
import com.chatbot.modules.app.core.repository.TenantAppRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * App service for v0.1
 * Core app management functionality
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AppService {

    private final TenantAppRepository tenantAppRepository;

    /**
     * Get all apps for a tenant
     * Returns all available apps with their enable/disable status
     * TODO: Validate tenant is ACTIVE through Tenant Hub
     */
    @Transactional(readOnly = true)
    public List<TenantAppResponse> getTenantApps(UUID tenantId) {
        // TODO: Validate tenant is ACTIVE via Tenant Hub API
        // TODO: Validate user has access to tenant
        
        // Get all enabled/disabled apps for tenant
        List<TenantApp> tenantApps = tenantAppRepository.findByTenantId(tenantId);
        
        // Ensure all apps are represented (create missing ones as disabled)
        for (AppCode appCode : AppCode.values()) {
            boolean exists = tenantApps.stream()
                    .anyMatch(app -> app.getAppCode() == appCode);
            
            if (!exists) {
                TenantApp newApp = TenantApp.builder()
                        .tenantId(tenantId)
                        .appCode(appCode)
                        .enabled(false)
                        .build();
                tenantApps.add(newApp);
            }
        }
        
        return tenantApps.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Enable an app for a tenant
     * Idempotent operation - safe to call multiple times
     * TODO: Validate tenant is ACTIVE through Tenant Hub
     */
    @Transactional
    public void enableApp(UUID tenantId, AppCode appCode) {
        // TODO: Validate tenant is ACTIVE via Tenant Hub API
        // TODO: Validate user has permission to manage apps
        
        TenantApp tenantApp = tenantAppRepository
                .findByTenantIdAndAppCode(tenantId, appCode)
                .orElseGet(() -> createTenantApp(tenantId, appCode));
        
        if (!tenantApp.isEnabled()) {
            tenantApp.enable();
            tenantAppRepository.save(tenantApp);
            log.info("Enabled app: {} for tenant: {}", appCode, tenantId);
        } else {
            log.info("App {} already enabled for tenant: {}", appCode, tenantId);
        }
    }

    /**
     * Disable an app for a tenant
     * Idempotent operation - safe to call multiple times
     * TODO: Validate tenant is ACTIVE through Tenant Hub
     */
    @Transactional
    public void disableApp(UUID tenantId, AppCode appCode) {
        // TODO: Validate tenant is ACTIVE via Tenant Hub API
        // TODO: Validate user has permission to manage apps
        
        TenantApp tenantApp = tenantAppRepository
                .findByTenantIdAndAppCode(tenantId, appCode)
                .orElseGet(() -> createTenantApp(tenantId, appCode));
        
        if (tenantApp.isEnabled()) {
            tenantApp.disable();
            tenantAppRepository.save(tenantApp);
            log.info("Disabled app: {} for tenant: {}", appCode, tenantId);
        } else {
            log.info("App {} already disabled for tenant: {}", appCode, tenantId);
        }
    }

    private TenantApp createTenantApp(UUID tenantId, AppCode appCode) {
        TenantApp tenantApp = TenantApp.builder()
                .tenantId(tenantId)
                .appCode(appCode)
                .enabled(false)
                .build();
        
        return tenantAppRepository.save(tenantApp);
    }

    private TenantAppResponse mapToResponse(TenantApp tenantApp) {
        return TenantAppResponse.builder()
                .id(tenantApp.getId())
                .tenantId(tenantApp.getTenantId())
                .appCode(tenantApp.getAppCode())
                .appDisplayName(tenantApp.getAppCode().getDisplayName())
                .appDescription(tenantApp.getAppCode().getDescription())
                .enabled(tenantApp.isEnabled())
                .enabledAt(tenantApp.getEnabledAt())
                .createdAt(tenantApp.getCreatedAt())
                .updatedAt(tenantApp.getUpdatedAt())
                .build();
    }
}
