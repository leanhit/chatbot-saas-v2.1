package com.chatbot.core.message.store.controller;

import com.chatbot.core.message.store.model.SLAConfiguration;
import com.chatbot.core.message.store.repository.SLAConfigurationRepository;
import com.chatbot.core.message.store.service.SLAMonitorService;
import com.chatbot.core.tenant.infra.TenantContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for SLA Configuration management
 * Implements Phase 2.1: SLA Monitoring
 */
@RestController
@RequestMapping("/api/sla-configurations")
@RequiredArgsConstructor
@Slf4j
public class SLAConfigurationController {

    private final SLAMonitorService slaMonitorService;
    private final SLAConfigurationRepository slaConfigurationRepository;

    /**
     * Get all SLA configurations for current tenant
     */
    @GetMapping
    public ResponseEntity<List<SLAConfiguration>> getSLAConfigurations() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<SLAConfiguration> configs = slaConfigurationRepository.findByTenantId(tenantId);
        return ResponseEntity.ok(configs);
    }

    /**
     * Get SLA configuration by customer tier
     */
    @GetMapping("/tier/{customerTier}")
    public ResponseEntity<SLAConfiguration> getSLAConfigurationByTier(@PathVariable String customerTier) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        SLAConfiguration config = slaMonitorService.getSLAConfiguration(tenantId, customerTier);
        if (config == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(config);
    }

    /**
     * Create a new SLA configuration
     */
    @PostMapping
    public ResponseEntity<SLAConfiguration> createSLAConfiguration(@RequestBody SLAConfiguration config) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        config.setTenantId(tenantId);
        SLAConfiguration createdConfig = slaConfigurationRepository.save(config);
        return ResponseEntity.ok(createdConfig);
    }

    /**
     * Update an existing SLA configuration
     */
    @PutMapping("/{id}")
    public ResponseEntity<SLAConfiguration> updateSLAConfiguration(
            @PathVariable Long id,
            @Valid @RequestBody SLAConfiguration config) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        SLAConfiguration updatedConfig = slaMonitorService.updateSLAConfiguration(id, config);
        return ResponseEntity.ok(updatedConfig);
    }

    /**
     * Delete an SLA configuration
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSLAConfiguration(@PathVariable Long id) {
        slaMonitorService.deleteSLAConfiguration(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Create default SLA configurations for current tenant
     */
    @PostMapping("/defaults")
    public ResponseEntity<Void> createDefaultSLAConfigurations() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        slaMonitorService.createDefaultSLAConfigurations(tenantId);
        return ResponseEntity.noContent().build();
    }
}
