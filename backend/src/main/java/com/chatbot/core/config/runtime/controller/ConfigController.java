package com.chatbot.core.config.runtime.controller;

import com.chatbot.core.config.runtime.dto.ConfigRequest;
import com.chatbot.core.config.runtime.dto.ConfigResponse;
import com.chatbot.core.config.runtime.service.ConfigCacheService;
import com.chatbot.core.config.runtime.service.RuntimeConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.responses.ApiResponse; // Use fully qualified name to avoid conflict
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Tag(name = "Configuration Management", description = "Runtime and environment configuration operations")
public class ConfigController {

    private final RuntimeConfigService runtimeConfigService;
    private final ConfigCacheService configCacheService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create configuration",
        description = "Create a new configuration entry",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Configuration created successfully",
                content = @Content(schema = @Schema(implementation = ConfigResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid configuration data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        }
    )
    public ResponseEntity<ConfigResponse> createConfig(@RequestBody ConfigRequest request) {
        log.info("Creating config: {} with scope: {}", request.getConfigKey(), request.getConfigScope());
        ConfigResponse response = runtimeConfigService.createConfig(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{configKey}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get configuration by key",
        description = "Retrieve a specific configuration by key with optional tenant/user scope",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Configuration retrieved successfully",
                content = @Content(schema = @Schema(implementation = ConfigResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Configuration not found")
        }
    )
    public ResponseEntity<ConfigResponse> getConfig(
            @PathVariable String configKey,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long userId) {
        
        ConfigResponse response = runtimeConfigService.getConfig(configKey, tenantId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get all configurations",
        description = "Retrieve all configurations with optional tenant/user filtering",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Configurations retrieved successfully",
                content = @Content(schema = @Schema(implementation = List.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied")
        }
    )
    public ResponseEntity<List<ConfigResponse>> getAllConfigs(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long userId) {
        
        List<ConfigResponse> configs = runtimeConfigService.getAllConfigs(tenantId, userId);
        return ResponseEntity.ok(configs);
    }

    @GetMapping("/{configKey}/value")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    @Operation(
        summary = "Get configuration value",
        description = "Retrieve only the value of a specific configuration",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Configuration value retrieved successfully",
                content = @Content(schema = @Schema(implementation = String.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Configuration not found")
        }
    )
    public ResponseEntity<String> getConfigValue(
            @PathVariable String configKey,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long userId) {
        
        String value = configCacheService.getConfigValue(configKey, tenantId, userId);
        return ResponseEntity.ok(value);
    }

    @PutMapping("/{configId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Update configuration",
        description = "Update an existing configuration entry",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Configuration updated successfully",
                content = @Content(schema = @Schema(implementation = ConfigResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid configuration data"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Configuration not found")
        }
    )
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
    @Operation(
        summary = "Delete configuration",
        description = "Delete a configuration entry (admin only)",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Configuration deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - Admin role required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Configuration not found")
        }
    )
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
    @Operation(
        summary = "Invalidate tenant cache",
        description = "Clear all cached configurations for a specific tenant",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tenant cache invalidated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        }
    )
    public ResponseEntity<Void> invalidateTenantCache(@PathVariable Long tenantId) {
        configCacheService.invalidateTenantConfigs(tenantId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cache/invalidate/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Invalidate user cache",
        description = "Clear all cached configurations for a specific user",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User cache invalidated successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        }
    )
    public ResponseEntity<Void> invalidateUserCache(@PathVariable Long userId) {
        configCacheService.invalidateUserConfigs(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cache/warmup")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Warmup configuration cache",
        description = "Preload frequently used configurations into cache",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Cache warmup completed successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied - Admin role required")
        }
    )
    public ResponseEntity<Void> warmupCache(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long userId) {
        
        configCacheService.warmupCache(tenantId, userId);
        return ResponseEntity.ok().build();
    }
}
