package com.chatbot.shared.penny.controller;

import com.chatbot.shared.penny.service.PennyMetricsService;
import com.chatbot.shared.penny.service.PennyHealthService;
import com.chatbot.shared.penny.service.PennyConfigurationService;
import com.chatbot.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Controller for Penny management operations
 */
@RestController
@RequestMapping("/api/penny/management")
@RequiredArgsConstructor
@Slf4j
public class PennyManagementController {

    private final PennyMetricsService metricsService;
    private final PennyHealthService healthService;
    private final PennyConfigurationService configurationService;

    /**
     * Get current metrics
     */
    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMetrics() {
        log.info("Getting Penny metrics");
        
        Map<String, Object> metrics = metricsService.getCurrentMetrics();
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    /**
     * Get metrics for time range
     */
    @GetMapping("/metrics/range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMetricsForTimeRange(
            @RequestParam Instant startTime,
            @RequestParam Instant endTime) {
        
        log.info("Getting Penny metrics for time range: {} to {}", startTime, endTime);
        
        Map<String, Object> metrics = metricsService.getMetricsForTimeRange(startTime, endTime);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    /**
     * Get provider metrics
     */
    @GetMapping("/metrics/provider/{providerType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProviderMetrics(
            @PathVariable String providerType) {
        
        log.info("Getting metrics for provider: {}", providerType);
        
        Map<String, Object> metrics = metricsService.getProviderMetrics(providerType);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    /**
     * Get bot metrics
     */
    @GetMapping("/metrics/bot/{botId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getBotMetrics(
            @PathVariable java.util.UUID botId) {
        
        log.info("Getting metrics for bot: {}", botId);
        
        Map<String, Object> metrics = metricsService.getBotMetrics(botId);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    /**
     * Get tenant metrics
     */
    @GetMapping("/metrics/tenant/{tenantId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTenantMetrics(
            @PathVariable Long tenantId) {
        
        log.info("Getting metrics for tenant: {}", tenantId);
        
        Map<String, Object> metrics = metricsService.getTenantMetrics(tenantId);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    /**
     * Export metrics
     */
    @GetMapping("/metrics/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> exportMetrics(
            @RequestParam(defaultValue = "json") String format) {
        
        log.info("Exporting metrics in format: {}", format);
        
        String exportedData = metricsService.exportMetrics(format);
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=penny-metrics." + format)
                .header("Content-Type", getContentType(format))
                .body(exportedData);
    }

    /**
     * Reset metrics
     */
    @PostMapping("/metrics/reset")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> resetMetrics() {
        log.info("Resetting Penny metrics");
        
        metricsService.resetMetrics();
        return ResponseEntity.ok(ApiResponse.success("Metrics reset successfully"));
    }

    /**
     * Get system health
     */
    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemHealth() {
        log.info("Getting Penny system health");
        
        Map<String, Object> health = healthService.getSystemHealth();
        return ResponseEntity.ok(ApiResponse.success(health));
    }

    /**
     * Get detailed health
     */
    @GetMapping("/health/detailed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDetailedHealth() {
        log.info("Getting detailed Penny health");
        
        Map<String, Object> health = healthService.getDetailedHealth();
        return ResponseEntity.ok(ApiResponse.success(health));
    }

    /**
     * Get async health
     */
    @GetMapping("/health/async")
    @PreAuthorize("hasRole('ADMIN')")
    public CompletableFuture<ResponseEntity<ApiResponse<Map<String, Object>>>> getSystemHealthAsync() {
        log.info("Getting async Penny system health");
        
        return healthService.getSystemHealthAsync()
                .thenApply(health -> ResponseEntity.ok(ApiResponse.success(health)));
    }

    /**
     * Get component health
     */
    @GetMapping("/health/component/{component}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getComponentHealth(
            @PathVariable String component) {
        
        log.info("Getting health for component: {}", component);
        
        Map<String, Object> health = healthService.getComponentHealth(component);
        return ResponseEntity.ok(ApiResponse.success(health));
    }

    /**
     * Check if system is healthy
     */
    @GetMapping("/health/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getHealthStatus() {
        log.info("Getting Penny health status");
        
        boolean isHealthy = healthService.isSystemHealthy();
        Map<String, Object> status = Map.of(
            "healthy", isHealthy,
            "status", isHealthy ? "HEALTHY" : "UNHEALTHY",
            "timestamp", Instant.now()
        );
        
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    /**
     * Get current configuration
     */
    @GetMapping("/configuration")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentConfiguration() {
        log.info("Getting current Penny configuration");
        
        Map<String, Object> config = configurationService.getCurrentConfiguration();
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    /**
     * Get provider configuration
     */
    @GetMapping("/configuration/provider/{providerType}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProviderConfiguration(
            @PathVariable String providerType) {
        
        log.info("Getting configuration for provider: {}", providerType);
        
        Map<String, Object> config = configurationService.getProviderConfiguration(providerType);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    /**
     * Update configuration
     */
    @PutMapping("/configuration/{component}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> updateConfiguration(
            @PathVariable String component,
            @RequestBody Map<String, Object> updates) {
        
        log.info("Updating configuration for component: {}", component);
        
        configurationService.updateConfiguration(component, updates);
        return ResponseEntity.ok(ApiResponse.success("Configuration updated successfully"));
    }

    /**
     * Reset configuration
     */
    @PostMapping("/configuration/reset/{component}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> resetConfiguration(
            @PathVariable String component) {
        
        log.info("Resetting configuration for component: {}", component);
        
        configurationService.resetConfiguration(component);
        return ResponseEntity.ok(ApiResponse.success("Configuration reset successfully"));
    }

    /**
     * Validate configuration
     */
    @GetMapping("/configuration/validate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateConfiguration() {
        log.info("Validating Penny configuration");
        
        Map<String, Object> validation = configurationService.validateConfiguration();
        return ResponseEntity.ok(ApiResponse.success(validation));
    }

    /**
     * Export configuration
     */
    @GetMapping("/configuration/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> exportConfiguration(
            @RequestParam(defaultValue = "json") String format) {
        
        log.info("Exporting configuration in format: {}", format);
        
        String exportedData = configurationService.exportConfiguration(format);
        
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=penny-config." + format)
                .header("Content-Type", getContentType(format))
                .body(exportedData);
    }

    /**
     * Import configuration
     */
    @PostMapping("/configuration/import")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> importConfiguration(
            @RequestParam String format,
            @RequestBody String configData) {
        
        log.info("Importing configuration in format: {}", format);
        
        configurationService.importConfiguration(format, configData);
        return ResponseEntity.ok(ApiResponse.success("Configuration imported successfully"));
    }

    /**
     * Get system overview
     */
    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSystemOverview() {
        log.info("Getting Penny system overview");
        
        Map<String, Object> overview = Map.of(
            "metrics", metricsService.getCurrentMetrics(),
            "health", healthService.getSystemHealth(),
            "configuration", configurationService.getCurrentConfiguration(),
            "timestamp", Instant.now()
        );
        
        return ResponseEntity.ok(ApiResponse.success(overview));
    }

    // Private helper methods

    private String getContentType(String format) {
        switch (format.toLowerCase()) {
            case "json":
                return "application/json";
            case "csv":
                return "text/csv";
            case "yaml":
                return "application/x-yaml";
            case "xml":
                return "application/xml";
            case "properties":
                return "text/plain";
            default:
                return "application/json";
        }
    }
}
