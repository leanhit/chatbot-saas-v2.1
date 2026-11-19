package com.chatbot.core.message.store.controller;

import com.chatbot.core.message.store.model.SLAConfiguration;
import com.chatbot.core.message.store.repository.SLAConfigurationRepository;
import com.chatbot.core.message.store.service.SLAMonitorService;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.service.TenantPermissionValidator;
import com.chatbot.core.tenant.exception.InsufficientPermissionException;
import com.chatbot.shared.exceptions.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.chatbot.core.message.store.dto.SLAConfigurationResponse;

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
    private final TenantPermissionValidator permissionValidator;

    private void requireOwnerOrEditor() {
        Long tenantId = TenantContext.getTenantId();
        String email = permissionValidator.getCurrentUserEmail();
        if (tenantId == null || email == null || !permissionValidator.isOwnerOrEditor(tenantId, email)) {
            throw new InsufficientPermissionException(ErrorCode.INSUFFICIENT_PERMISSION,
                    "Only tenant owner or editor can modify SLA configuration");
        }
    }

    /**
     * Get all SLA configurations for current tenant
     */
    @GetMapping
    public ResponseEntity<List<SLAConfigurationResponse>> getSLAConfigurations() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<SLAConfiguration> configs = slaConfigurationRepository.findByTenantId(tenantId);
        return ResponseEntity.ok(configs.stream().map(SLAConfigurationResponse::from).toList());
    }

    /**
     * Get SLA configuration by customer tier
     */
    @GetMapping("/tier/{customerTier}")
    public ResponseEntity<SLAConfigurationResponse> getSLAConfigurationByTier(@PathVariable String customerTier) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        SLAConfiguration config = slaMonitorService.getSLAConfiguration(tenantId, customerTier);
        if (config == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(SLAConfigurationResponse.from(config));
    }

    /**
     * Create a new SLA configuration
     */
    @PostMapping
    public ResponseEntity<SLAConfigurationResponse> createSLAConfiguration(@Valid @RequestBody com.chatbot.core.message.store.dto.SLAConfigurationRequest request) {
        requireOwnerOrEditor();
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        SLAConfiguration config = toEntity(request);
        config.setTenantId(tenantId);
        SLAConfiguration createdConfig = slaConfigurationRepository.save(config);
        return ResponseEntity.ok(SLAConfigurationResponse.from(createdConfig));
    }

    /**
     * Update an existing SLA configuration
     */
    @PutMapping("/{id}")
    public ResponseEntity<SLAConfigurationResponse> updateSLAConfiguration(
            @PathVariable Long id,
            @Valid @RequestBody com.chatbot.core.message.store.dto.SLAConfigurationRequest request) {
        requireOwnerOrEditor();
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        SLAConfiguration config = toEntity(request);
        SLAConfiguration updatedConfig = slaMonitorService.updateSLAConfiguration(id, config);
        return ResponseEntity.ok(SLAConfigurationResponse.from(updatedConfig));
    }

    /**
     * Delete an SLA configuration
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSLAConfiguration(@PathVariable Long id) {
        requireOwnerOrEditor();
        slaMonitorService.deleteSLAConfiguration(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Create default SLA configurations for current tenant
     */
    @PostMapping("/defaults")
    public ResponseEntity<Void> createDefaultSLAConfigurations() {
        requireOwnerOrEditor();
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        slaMonitorService.createDefaultSLAConfigurations(tenantId);
        return ResponseEntity.noContent().build();
    }

    private SLAConfiguration toEntity(com.chatbot.core.message.store.dto.SLAConfigurationRequest r) {
        SLAConfiguration config = new SLAConfiguration();
        config.setCustomerTier(r.getCustomerTier());
        config.setExpectedResponseTime(r.getExpectedResponseTime());
        config.setMaxBreachCount(r.getMaxBreachCount());
        config.setActive(r.getActive());
        config.setDescription(r.getDescription());
        return config;
    }
}
