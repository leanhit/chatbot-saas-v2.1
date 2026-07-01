package com.chatbot.core.message.store.controller;

import com.chatbot.core.message.store.model.EscalationTier;
import com.chatbot.core.message.store.repository.EscalationTierRepository;
import com.chatbot.core.message.store.service.EscalationService;
import com.chatbot.core.tenant.infra.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Escalation Tier management
 * Implements Phase 2.2: Multi-tier Escalation
 */
@RestController
@RequestMapping("/api/escalation-tiers")
@RequiredArgsConstructor
@Slf4j
public class EscalationTierController {

    private final EscalationTierRepository escalationTierRepository;
    private final EscalationService escalationService;

    /**
     * Get all escalation tiers for current tenant
     */
    @GetMapping
    public ResponseEntity<List<EscalationTier>> getEscalationTiers() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<EscalationTier> tiers = escalationTierRepository.findByTenantId(tenantId);
        return ResponseEntity.ok(tiers);
    }

    /**
     * Get escalation tier by level
     */
    @GetMapping("/level/{level}")
    public ResponseEntity<EscalationTier> getEscalationTierByLevel(@PathVariable Integer level) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        EscalationTier tier = escalationTierRepository.findByTenantIdAndLevel(tenantId, level);
        if (tier == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(tier);
    }

    /**
     * Create a new escalation tier
     */
    @PostMapping
    public ResponseEntity<EscalationTier> createEscalationTier(@RequestBody EscalationTier tier) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        tier.setTenantId(tenantId);
        EscalationTier createdTier = escalationTierRepository.save(tier);
        return ResponseEntity.ok(createdTier);
    }

    /**
     * Update an existing escalation tier
     */
    @PutMapping("/{id}")
    public ResponseEntity<EscalationTier> updateEscalationTier(
            @PathVariable Long id,
            @RequestBody EscalationTier tier) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        tier.setTenantId(tenantId);
        tier.setId(id);
        EscalationTier updatedTier = escalationTierRepository.save(tier);
        return ResponseEntity.ok(updatedTier);
    }

    /**
     * Delete an escalation tier
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEscalationTier(@PathVariable Long id) {
        escalationTierRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Create default escalation tiers for current tenant
     */
    @PostMapping("/defaults")
    public ResponseEntity<Void> createDefaultEscalationTiers() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        escalationService.createDefaultEscalationTiers(tenantId);
        return ResponseEntity.noContent().build();
    }
}
