package com.chatbot.core.ai.dto;

import com.chatbot.core.ai.model.ConversationSentiment;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record SentimentDTO(
    Long id,
    Long conversationId,
    Long messageId,
    Double score,
    String label,
    String source,
    LocalDateTime createdAt
) {
    public static SentimentDTO from(ConversationSentiment s) {
        return SentimentDTO.builder()
            .id(s.getId())
            .conversationId(s.getConversationId())
            .messageId(s.getMessageId())
            .score(s.getScore())
            .label(s.getLabel() != null ? s.getLabel().name() : null)
            .source(s.getSource() != null ? s.getSource().name() : null)
            .createdAt(s.getCreatedAt())
            .build();
    }
}
