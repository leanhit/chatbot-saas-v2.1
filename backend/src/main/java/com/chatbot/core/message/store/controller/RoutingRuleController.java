package com.chatbot.core.message.store.controller;

import com.chatbot.core.message.store.model.RoutingRule;
import com.chatbot.core.message.store.service.RoutingRuleService;
import com.chatbot.core.tenant.infra.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Routing Rule management
 * Implements Phase 1.3: Attribute-based Routing
 */
@RestController
@RequestMapping("/api/routing-rules")
@RequiredArgsConstructor
@Slf4j
public class RoutingRuleController {

    private final RoutingRuleService routingRuleService;

    /**
     * Get all routing rules for current tenant
     */
    @GetMapping
    public ResponseEntity<List<RoutingRule>> getRoutingRules() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<RoutingRule> rules = routingRuleService.getRoutingRules(tenantId);
        return ResponseEntity.ok(rules);
    }

    /**
     * Get active routing rules for current tenant
     */
    @GetMapping("/active")
    public ResponseEntity<List<RoutingRule>> getActiveRoutingRules() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<RoutingRule> rules = routingRuleService.getActiveRoutingRules(tenantId);
        return ResponseEntity.ok(rules);
    }

    /**
     * Create a new routing rule
     */
    @PostMapping
    public ResponseEntity<RoutingRule> createRoutingRule(@RequestBody RoutingRule rule) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        rule.setTenantId(tenantId);
        RoutingRule createdRule = routingRuleService.createRoutingRule(rule);
        return ResponseEntity.ok(createdRule);
    }

    /**
     * Update an existing routing rule
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoutingRule> updateRoutingRule(
            @PathVariable Long id,
            @RequestBody RoutingRule rule) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        rule.setTenantId(tenantId);
        RoutingRule updatedRule = routingRuleService.updateRoutingRule(id, rule);
        return ResponseEntity.ok(updatedRule);
    }

    /**
     * Delete a routing rule
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoutingRule(@PathVariable Long id) {
        routingRuleService.deleteRoutingRule(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Create default routing rules for current tenant
     */
    @PostMapping("/defaults")
    public ResponseEntity<Void> createDefaultRoutingRules() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        routingRuleService.createDefaultRoutingRules(tenantId);
        return ResponseEntity.noContent().build();
    }
}
