package com.chatbot.core.ai.controller;

import com.chatbot.core.ai.dto.AiSuggestionDTO;
import com.chatbot.core.ai.dto.GenerateSuggestionRequest;
import com.chatbot.core.ai.dto.RejectSuggestionRequest;
import com.chatbot.core.ai.model.AiSuggestion;
import com.chatbot.core.ai.service.AiSuggestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI suggestion endpoints for agent assistance (Phase 5).
 * Display-only: suggestions never auto-modify conversations.
 */
@RestController
@RequestMapping("/api/ai/suggestions")
@RequiredArgsConstructor
@Slf4j
public class AiSuggestionController {

    private final AiSuggestionService aiSuggestionService;

    @PostMapping("/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<?> generateSuggestion(@Valid @RequestBody GenerateSuggestionRequest request) {
        var result = aiSuggestionService.generateSuggestion(request.conversationId(), request.agentId());
        if (result.isPresent()) {
            return ResponseEntity.ok(AiSuggestionDTO.from(result.get()));
        }
        return ResponseEntity.ok(Map.of(
            "status", "unavailable",
            "message", "AI suggestion could not be generated (provider unavailable or no context)"));
    }

    @GetMapping("/conversation/{conversationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<List<AiSuggestionDTO>> getSuggestionsForConversation(@PathVariable Long conversationId) {
        List<AiSuggestionDTO> suggestions = aiSuggestionService.getSuggestionsForConversation(conversationId)
            .stream().map(AiSuggestionDTO::from).toList();
        return ResponseEntity.ok(suggestions);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<List<AiSuggestionDTO>> getPendingSuggestions() {
        List<AiSuggestionDTO> suggestions = aiSuggestionService.getPendingSuggestions()
            .stream().map(AiSuggestionDTO::from).toList();
        return ResponseEntity.ok(suggestions);
    }

    @PostMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<?> acceptSuggestion(@PathVariable Long id) {
        return aiSuggestionService.acceptSuggestion(id)
            .map(s -> ResponseEntity.ok(AiSuggestionDTO.from(s)))
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'SUPERVISOR', 'AGENT')")
    public ResponseEntity<?> rejectSuggestion(@PathVariable Long id,
                                              @RequestBody(required = false) RejectSuggestionRequest request) {
        String note = request != null ? request.note() : null;
        return aiSuggestionService.rejectSuggestion(id, note)
            .map(s -> ResponseEntity.ok(AiSuggestionDTO.from(s)))
            .orElse(ResponseEntity.notFound().build());
    }
}
