package com.chatbot.core.message.store.controller;

import com.chatbot.core.message.store.model.RoutingRule;
import com.chatbot.core.message.store.service.RoutingRuleService;
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
import com.chatbot.core.message.store.dto.RoutingRuleResponse;

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
    private final TenantPermissionValidator permissionValidator;

    private void requireOwnerOrEditor() {
        Long tenantId = TenantContext.getTenantId();
        String email = permissionValidator.getCurrentUserEmail();
        if (tenantId == null || email == null || !permissionValidator.isOwnerOrEditor(tenantId, email)) {
            throw new InsufficientPermissionException(ErrorCode.INSUFFICIENT_PERMISSION,
                    "Only tenant owner or editor can modify routing rules");
        }
    }

    /**
     * Get all routing rules for current tenant
     */
    @GetMapping
    public ResponseEntity<List<RoutingRuleResponse>> getRoutingRules() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<RoutingRule> rules = routingRuleService.getRoutingRules(tenantId);
        return ResponseEntity.ok(rules.stream().map(RoutingRuleResponse::from).toList());
    }

    /**
     * Get active routing rules for current tenant
     */
    @GetMapping("/active")
    public ResponseEntity<List<RoutingRuleResponse>> getActiveRoutingRules() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<RoutingRule> rules = routingRuleService.getActiveRoutingRules(tenantId);
        return ResponseEntity.ok(rules.stream().map(RoutingRuleResponse::from).toList());
    }

    /**
     * Create a new routing rule
     */
    @PostMapping
    public ResponseEntity<RoutingRuleResponse> createRoutingRule(@Valid @RequestBody com.chatbot.core.message.store.dto.RoutingRuleRequest request) {
        requireOwnerOrEditor();
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        RoutingRule rule = toEntity(request);
        rule.setTenantId(tenantId);
        RoutingRule createdRule = routingRuleService.createRoutingRule(rule);
        return ResponseEntity.ok(RoutingRuleResponse.from(createdRule));
    }

    /**
     * Update an existing routing rule
     */
    @PutMapping("/{id}")
    public ResponseEntity<RoutingRuleResponse> updateRoutingRule(
            @PathVariable Long id,
            @Valid @RequestBody com.chatbot.core.message.store.dto.RoutingRuleRequest request) {
        requireOwnerOrEditor();
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        RoutingRule rule = toEntity(request);
        rule.setTenantId(tenantId);
        RoutingRule updatedRule = routingRuleService.updateRoutingRule(id, rule);
        return ResponseEntity.ok(RoutingRuleResponse.from(updatedRule));
    }

    /**
     * Delete a routing rule
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoutingRule(@PathVariable Long id) {
        requireOwnerOrEditor();
        routingRuleService.deleteRoutingRule(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Create default routing rules for current tenant
     */
    @PostMapping("/defaults")
    public ResponseEntity<Void> createDefaultRoutingRules() {
        requireOwnerOrEditor();
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        routingRuleService.createDefaultRoutingRules(tenantId);
        return ResponseEntity.noContent().build();
    }

    private RoutingRule toEntity(com.chatbot.core.message.store.dto.RoutingRuleRequest r) {
        RoutingRule rule = new RoutingRule();
        rule.setName(r.getName());
        rule.setDescription(r.getDescription());
        rule.setPriority(r.getPriority());
        rule.setConditions(r.getConditions());
        rule.setAction(r.getAction());
        rule.setActive(r.getActive());
        rule.setRuleType(r.getRuleType());
        return rule;
    }
}
