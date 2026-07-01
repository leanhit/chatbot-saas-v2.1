package com.chatbot.core.message.store.controller;

import com.chatbot.core.message.store.service.AIEscalationService;
import com.chatbot.core.tenant.infra.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for AI-based Escalation
 * Implements Phase 3.3: AI-based Escalation
 */
@RestController
@RequestMapping("/api/ai-escalation")
@RequiredArgsConstructor
@Slf4j
public class AIEscalationController {

    private final AIEscalationService aiEscalationService;

    /**
     * Trigger AI analysis for a specific conversation
     */
    @PostMapping("/analyze/{conversationId}")
    public ResponseEntity<Void> triggerAIAnalysis(@PathVariable Long conversationId) {
        try {
            aiEscalationService.triggerAIAnalysis(conversationId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error triggering AI analysis for conversation {}", conversationId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get AI analysis results for a conversation
     */
    @GetMapping("/analysis/{conversationId}")
    public ResponseEntity<Map<String, Object>> getAIAnalysis(@PathVariable Long conversationId) {
        Map<String, Object> analysis = aiEscalationService.getAIAnalysis(conversationId);
        if (analysis == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(analysis);
    }

    /**
     * Check if LLM client is enabled
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Map<String, Object> status = Map.of(
            "enabled", aiEscalationService.getLLMClient().isEnabled(),
            "provider", aiEscalationService.getLLMClient().getProvider()
        );
        return ResponseEntity.ok(status);
    }
}
