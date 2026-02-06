package com.chatbot.modules.config.core.service;

import com.chatbot.modules.config.core.model.ConfigEntry;
import com.chatbot.modules.config.core.repository.ConfigEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Config Hub service for SaaS Ecosystem
 * READ ONLY configuration management service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConfigHubService {

    private final ConfigEntryRepository configEntryRepository;

    /**
     * Get system configuration value
     */
    public Optional<String> getSystemConfig(String key) {
        log.debug("Getting system config: {}", key);
        return configEntryRepository.findByKeyAndScope(key, ConfigEntry.ConfigScope.SYSTEM)
                .map(ConfigEntry::getValue);
    }

    /**
     * Get tenant configuration value with system fallback
     */
    public Optional<String> getTenantConfig(String key, UUID tenantId) {
        log.debug("Getting tenant config: {} for tenant: {}", key, tenantId);
        return configEntryRepository.findTenantConfigOrSystem(key, tenantId)
                .map(ConfigEntry::getValue);
    }

    /**
     * Get app configuration value with fallback chain
     * Priority: TENANT_APP > APP > SYSTEM
     */
    public Optional<String> getAppConfig(String key, UUID tenantId) {
        log.debug("Getting app config: {} for tenant: {}", key, tenantId);
        return configEntryRepository.findAppConfigWithFallback(key, tenantId)
                .map(ConfigEntry::getValue);
    }

    /**
     * Get tenant-specific app configuration
     */
    public Optional<String> getTenantAppConfig(String key, UUID tenantId) {
        log.debug("Getting tenant-app config: {} for tenant: {}", key, tenantId);
        return configEntryRepository.findByKeyAndScopeAndScopeId(key, ConfigEntry.ConfigScope.TENANT_APP, tenantId)
                .map(ConfigEntry::getValue);
    }

    /**
     * Get all system configurations
     */
    public List<ConfigEntry> getAllSystemConfigs() {
        return configEntryRepository.findByScope(ConfigEntry.ConfigScope.SYSTEM);
    }

    /**
     * Get all tenant configurations
     */
    public List<ConfigEntry> getAllTenantConfigs(UUID tenantId) {
        return configEntryRepository.findByScopeAndScopeId(ConfigEntry.ConfigScope.TENANT, tenantId);
    }

    /**
     * Get all app configurations for tenant
     */
    public List<ConfigEntry> getAllTenantAppConfigs(UUID tenantId) {
        return configEntryRepository.findByScopeAndScopeId(ConfigEntry.ConfigScope.TENANT_APP, tenantId);
    }

    /**
     * Get configuration value with default fallback
     */
    public String getConfig(String key, ConfigEntry.ConfigScope scope, UUID scopeId, String defaultValue) {
        Optional<String> value = switch (scope) {
            case SYSTEM -> getSystemConfig(key);
            case TENANT -> getTenantConfig(key, scopeId);
            case APP, TENANT_APP -> getAppConfig(key, scopeId);
        };
        return value.orElse(defaultValue);
    }

    /**
     * Check if configuration exists
     */
    public boolean hasConfig(String key, ConfigEntry.ConfigScope scope, UUID scopeId) {
        return switch (scope) {
            case SYSTEM -> configEntryRepository.findByKeyAndScope(key, scope).isPresent();
            case TENANT -> configEntryRepository.findByKeyAndScopeAndScopeId(key, scope, scopeId).isPresent();
            case APP, TENANT_APP -> configEntryRepository.findAppConfigWithFallback(key, scopeId).isPresent();
        };
    }
}
