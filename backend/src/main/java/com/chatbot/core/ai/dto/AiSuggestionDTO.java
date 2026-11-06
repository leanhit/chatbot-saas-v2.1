package com.chatbot.core.ai.dto;

import com.chatbot.core.ai.model.AiSuggestion;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AiSuggestionDTO(
    Long id,
    Long conversationId,
    Long agentId,
    String suggestionText,
    Double confidence,
    String provider,
    String model,
    String status,
    String responseNote,
    LocalDateTime respondedAt,
    LocalDateTime createdAt
) {
    public static AiSuggestionDTO from(AiSuggestion s) {
        return AiSuggestionDTO.builder()
            .id(s.getId())
            .conversationId(s.getConversationId())
            .agentId(s.getAgentId())
            .suggestionText(s.getSuggestionText())
            .confidence(s.getConfidence())
            .provider(s.getProvider())
            .model(s.getModel())
            .status(s.getStatus() != null ? s.getStatus().name() : null)
            .responseNote(s.getResponseNote())
            .respondedAt(s.getRespondedAt())
            .createdAt(s.getCreatedAt())
            .build();
    }
}
