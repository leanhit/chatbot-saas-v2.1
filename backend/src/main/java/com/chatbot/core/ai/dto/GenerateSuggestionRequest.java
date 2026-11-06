package com.chatbot.core.ai.dto;

import jakarta.validation.constraints.NotNull;

public record GenerateSuggestionRequest(
    @NotNull Long conversationId,
    Long agentId
) {
}
