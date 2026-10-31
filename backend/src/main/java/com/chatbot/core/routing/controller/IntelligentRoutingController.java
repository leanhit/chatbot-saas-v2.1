package com.chatbot.core.routing.controller;

import com.chatbot.core.routing.dto.*;
import com.chatbot.core.routing.model.RoutingRule;
import com.chatbot.core.routing.service.IntelligentRoutingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routing/rules")
@RequiredArgsConstructor
@Slf4j
public class IntelligentRoutingController {

    private final IntelligentRoutingService routingService;

    @PostMapping
    public ResponseEntity<RoutingRuleDTO> createRoutingRule(@Valid @RequestBody CreateRoutingRuleRequest request) {
        RoutingRuleDTO created = routingService.createRoutingRule(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoutingRuleDTO> getRoutingRule(@PathVariable Long id) {
        RoutingRuleDTO rule = routingService.getRoutingRuleById(id);
        return ResponseEntity.ok(rule);
    }

    @GetMapping
    public ResponseEntity<List<RoutingRuleDTO>> getAllRoutingRules() {
        List<RoutingRuleDTO> rules = routingService.getAllRoutingRules();
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/type/{ruleType}")
    public ResponseEntity<List<RoutingRuleDTO>> getRoutingRulesByType(@PathVariable RoutingRule.RoutingRuleType ruleType) {
        List<RoutingRuleDTO> rules = routingService.getRoutingRulesByType(ruleType);
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/target/{targetType}")
    public ResponseEntity<List<RoutingRuleDTO>> getRoutingRulesByTargetType(@PathVariable RoutingRule.TargetType targetType) {
        List<RoutingRuleDTO> rules = routingService.getRoutingRulesByTargetType(targetType);
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/default")
    public ResponseEntity<RoutingRuleDTO> getDefaultRoutingRule() {
        RoutingRuleDTO rule = routingService.getDefaultRoutingRule();
        return ResponseEntity.ok(rule);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoutingRuleDTO> updateRoutingRule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateRoutingRuleRequest request) {
        RoutingRuleDTO updated = routingService.updateRoutingRule(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoutingRule(@PathVariable Long id) {
        routingService.deleteRoutingRule(id);
        return ResponseEntity.noContent().build();
    }
}
