package com.chatbot.core.ai.controller;

import com.chatbot.core.ai.dto.SentimentDTO;
import com.chatbot.core.ai.service.SentimentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Sentiment endpoints (Phase 5). Display/alert only — no automatic actions.
 */
@RestController
@RequestMapping("/api/ai/sentiment")
@RequiredArgsConstructor
@Slf4j
public class SentimentController {

    private final SentimentService sentimentService;

    @PostMapping("/score/{conversationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<?> scoreConversation(@PathVariable Long conversationId) {
        var result = sentimentService.scoreConversation(conversationId);
        if (result.isPresent()) {
            return ResponseEntity.ok(SentimentDTO.from(result.get()));
        }
        return ResponseEntity.ok(Map.of(
            "status", "unavailable",
            "message", "Sentiment scoring unavailable (no user messages or tenant context missing)"));
    }

    @GetMapping("/conversation/{conversationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<?> getLatestSentiment(@PathVariable Long conversationId) {
        return sentimentService.getLatestSentiment(conversationId)
            .map(s -> ResponseEntity.ok(SentimentDTO.from(s)))
            .orElse(ResponseEntity.notFound().build());
    }
}
