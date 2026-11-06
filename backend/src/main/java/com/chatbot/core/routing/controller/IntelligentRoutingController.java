package com.chatbot.core.routing.controller;

import com.chatbot.core.routing.dto.*;
import com.chatbot.core.routing.model.RoutingRule;
import com.chatbot.core.routing.service.IntelligentRoutingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/routing/rules")
@RequiredArgsConstructor
@Slf4j
public class IntelligentRoutingController {

    private final IntelligentRoutingService routingService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<RoutingRuleDTO> createRoutingRule(@Valid @RequestBody CreateRoutingRuleRequest request) {
        RoutingRuleDTO created = routingService.createRoutingRule(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<RoutingRuleDTO> getRoutingRule(@PathVariable Long id) {
        RoutingRuleDTO rule = routingService.getRoutingRuleById(id);
        return ResponseEntity.ok(rule);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<List<RoutingRuleDTO>> getAllRoutingRules() {
        List<RoutingRuleDTO> rules = routingService.getAllRoutingRules();
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/type/{ruleType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<List<RoutingRuleDTO>> getRoutingRulesByType(@PathVariable RoutingRule.RoutingRuleType ruleType) {
        List<RoutingRuleDTO> rules = routingService.getRoutingRulesByType(ruleType);
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/target/{targetType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<List<RoutingRuleDTO>> getRoutingRulesByTargetType(@PathVariable RoutingRule.TargetType targetType) {
        List<RoutingRuleDTO> rules = routingService.getRoutingRulesByTargetType(targetType);
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/default")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<RoutingRuleDTO> getDefaultRoutingRule() {
        RoutingRuleDTO rule = routingService.getDefaultRoutingRule();
        return ResponseEntity.ok(rule);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<RoutingRuleDTO> updateRoutingRule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoutingRuleRequest request) {
        RoutingRuleDTO updated = routingService.updateRoutingRule(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteRoutingRule(@PathVariable Long id) {
        routingService.deleteRoutingRule(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/route/{conversationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<RoutingAssignmentResult> routeConversation(
            @PathVariable Long conversationId,
            @RequestBody Map<String, Object> conversationContext) {
        RoutingAssignmentResult result = routingService.routeConversation(conversationId, conversationContext);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history/{conversationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<List<RoutingAssignmentResult>> getRoutingHistory(@PathVariable Long conversationId) {
        List<RoutingAssignmentResult> history = routingService.getRoutingHistory(conversationId);
        return ResponseEntity.ok(history);
    }
}
