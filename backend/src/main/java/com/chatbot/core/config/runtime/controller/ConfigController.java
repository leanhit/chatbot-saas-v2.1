package com.chatbot.core.config.runtime.controller;

import com.chatbot.core.config.runtime.dto.ConfigRequest;
import com.chatbot.core.config.runtime.dto.ConfigResponse;
import com.chatbot.core.config.runtime.service.ConfigCacheService;
import com.chatbot.core.config.runtime.service.RuntimeConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/configs")
@RequiredArgsConstructor
@Slf4j
public class ConfigController {

    private final RuntimeConfigService runtimeConfigService;
    private final ConfigCacheService configCacheService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfigResponse> createConfig(@RequestBody ConfigRequest request) {
        log.info("Creating config: {} with scope: {}", request.getConfigKey(), request.getConfigScope());
        ConfigResponse response = runtimeConfigService.createConfig(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{configKey}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ConfigResponse> getConfig(
            @PathVariable String configKey,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long userId) {
        
        ConfigResponse response = runtimeConfigService.getConfig(configKey, tenantId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<ConfigResponse>> getAllConfigs(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long userId) {
        
        List<ConfigResponse> configs = runtimeConfigService.getAllConfigs(tenantId, userId);
        return ResponseEntity.ok(configs);
    }

    @GetMapping("/{configKey}/value")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<String> getConfigValue(
            @PathVariable String configKey,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long userId) {
        
        String value = configCacheService.getConfigValue(configKey, tenantId, userId);
        return ResponseEntity.ok(value);
    }

    @PutMapping("/{configId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ConfigResponse> updateConfig(
            @PathVariable Long configId,
            @RequestBody ConfigRequest request) {
        
        ConfigResponse response = runtimeConfigService.updateConfig(configId, request);
        
        // Invalidate cache for this config
        configCacheService.invalidateConfig(request.getConfigKey(), request.getTenantId(), request.getUserId());
        
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{configId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteConfig(@PathVariable Long configId) {
        // Get config before deletion to invalidate cache
        ConfigResponse config = runtimeConfigService.getConfig(configId.toString(), null, null);
        
        runtimeConfigService.deleteConfig(configId);
        
        // Invalidate cache
        configCacheService.invalidateConfig(config.getConfigKey(), config.getTenantId(), config.getUserId());
        
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cache/invalidate/tenant/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> invalidateTenantCache(@PathVariable Long tenantId) {
        configCacheService.invalidateTenantConfigs(tenantId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cache/invalidate/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> invalidateUserCache(@PathVariable Long userId) {
        configCacheService.invalidateUserConfigs(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cache/warmup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> warmupCache(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long userId) {
        
        configCacheService.warmupCache(tenantId, userId);
        return ResponseEntity.ok().build();
    }
}
