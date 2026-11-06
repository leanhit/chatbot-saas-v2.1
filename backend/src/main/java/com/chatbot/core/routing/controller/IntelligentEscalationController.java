package com.chatbot.core.routing.controller;

import com.chatbot.core.routing.dto.*;
import com.chatbot.core.routing.model.EscalationRule;
import com.chatbot.core.routing.service.IntelligentEscalationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routing/escalation-rules")
@RequiredArgsConstructor
@Slf4j
public class IntelligentEscalationController {

    private final IntelligentEscalationService escalationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<EscalationRuleDTO> createEscalationRule(@Valid @RequestBody	CreateEscalationRuleRequest request) {
        EscalationRuleDTO created = escalationService.createEscalationRule(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<EscalationRuleDTO> getEscalationRule(@PathVariable Long id) {
        EscalationRuleDTO rule = escalationService.getEscalationRuleById(id);
        return ResponseEntity.ok(rule);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<List<EscalationRuleDTO>> getAllEscalationRules() {
        List<EscalationRuleDTO> rules = escalationService.getAllEscalationRules();
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/trigger/{triggerType}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<List<EscalationRuleDTO>> getEscalationRulesByTriggerType(@PathVariable EscalationRule.TriggerType triggerType) {
        List<EscalationRuleDTO> rules = escalationService.getEscalationRulesByTriggerType(triggerType);
        return ResponseEntity.ok(rules);
    }

    @GetMapping("/action/{action}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<List<EscalationRuleDTO>> getEscalationRulesByAction(@PathVariable EscalationRule.EscalationAction action) {
        List<EscalationRuleDTO> rules = escalationService.getEscalationRulesByAction(action);
        return ResponseEntity.ok(rules);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR')")
    public ResponseEntity<EscalationRuleDTO> updateEscalationRule(
            @PathVariable Long id,
            @Valid @RequestBody UpdateEscalationRuleRequest request) {
        EscalationRuleDTO updated = escalationService.updateEscalationRule(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<Void> deleteEscalationRule(@PathVariable Long id) {
        escalationService.deleteEscalationRule(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/evaluate/{conversationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<EscalationResult> evaluateEscalation(
            @PathVariable Long conversationId,
            @RequestBody EscalationContext context) {
        EscalationResult result = escalationService.evaluateEscalation(conversationId, context);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/history/{conversationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<List<EscalationResult>> getEscalationHistory(@PathVariable Long conversationId) {
        List<EscalationResult> history = escalationService.getEscalationHistory(conversationId);
        return ResponseEntity.ok(history);
    }
}
