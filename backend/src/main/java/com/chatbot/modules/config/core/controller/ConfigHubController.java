package com.chatbot.modules.config.core.controller;

import com.chatbot.modules.config.core.model.ConfigEntry;
import com.chatbot.modules.config.core.service.ConfigHubService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Config Hub controller for SaaS Ecosystem
 * READ ONLY API for configuration management
 */
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
@Slf4j
public class ConfigHubController {

    private final ConfigHubService configHubService;

    /**
     * Get system configuration by key
     */
    @GetMapping("/system/{key}")
    public ResponseEntity<ConfigResponse> getSystemConfig(@PathVariable String key) {
        log.debug("API: Get system config: {}", key);
        Optional<String> value = configHubService.getSystemConfig(key);
        
        if (value.isPresent()) {
            return ResponseEntity.ok(new ConfigResponse(key, value.get(), ConfigEntry.ConfigScope.SYSTEM, null));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get tenant configuration by key
     */
    @GetMapping("/tenants/{tenantId}/{key}")
    public ResponseEntity<ConfigResponse> getTenantConfig(
            @PathVariable UUID tenantId, 
            @PathVariable String key) {
        log.debug("API: Get tenant config: {} for tenant: {}", key, tenantId);
        Optional<String> value = configHubService.getTenantConfig(key, tenantId);
        
        if (value.isPresent()) {
            return ResponseEntity.ok(new ConfigResponse(key, value.get(), ConfigEntry.ConfigScope.TENANT, tenantId));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get app configuration by key
     */
    @GetMapping("/apps/{tenantId}/{key}")
    public ResponseEntity<ConfigResponse> getAppConfig(
            @PathVariable UUID tenantId, 
            @PathVariable String key) {
        log.debug("API: Get app config: {} for tenant: {}", key, tenantId);
        Optional<String> value = configHubService.getAppConfig(key, tenantId);
        
        if (value.isPresent()) {
            return ResponseEntity.ok(new ConfigResponse(key, value.get(), ConfigEntry.ConfigScope.APP, tenantId));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all system configurations
     */
    @GetMapping("/system")
    public ResponseEntity<List<ConfigEntry>> getAllSystemConfigs() {
        log.debug("API: Get all system configs");
        List<ConfigEntry> configs = configHubService.getAllSystemConfigs();
        return ResponseEntity.ok(configs);
    }

    /**
     * Get all tenant configurations
     */
    @GetMapping("/tenants/{tenantId}")
    public ResponseEntity<List<ConfigEntry>> getAllTenantConfigs(@PathVariable UUID tenantId) {
        log.debug("API: Get all configs for tenant: {}", tenantId);
        List<ConfigEntry> configs = configHubService.getAllTenantConfigs(tenantId);
        return ResponseEntity.ok(configs);
    }

    /**
     * Get all app configurations for tenant
     */
    @GetMapping("/apps/{tenantId}")
    public ResponseEntity<List<ConfigEntry>> getAllTenantAppConfigs(@PathVariable UUID tenantId) {
        log.debug("API: Get all app configs for tenant: {}", tenantId);
        List<ConfigEntry> configs = configHubService.getAllTenantAppConfigs(tenantId);
        return ResponseEntity.ok(configs);
    }

    /**
     * Check if configuration exists
     */
    @GetMapping("/exists/{scope}/{key}")
    public ResponseEntity<Map<String, Boolean>> checkConfigExists(
            @PathVariable ConfigEntry.ConfigScope scope,
            @PathVariable String key,
            @RequestParam(required = false) UUID scopeId) {
        log.debug("API: Check config exists: {} scope: {} scopeId: {}", key, scope, scopeId);
        boolean exists = configHubService.hasConfig(key, scope, scopeId);
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    /**
     * Configuration response DTO
     */
    public static class ConfigResponse {
        private final String key;
        private final String value;
        private final ConfigEntry.ConfigScope scope;
        private final UUID scopeId;

        public ConfigResponse(String key, String value, ConfigEntry.ConfigScope scope, UUID scopeId) {
            this.key = key;
            this.value = value;
            this.scope = scope;
            this.scopeId = scopeId;
        }

        public String getKey() { return key; }
        public String getValue() { return value; }
        public ConfigEntry.ConfigScope getScope() { return scope; }
        public UUID getScopeId() { return scopeId; }
    }
}
