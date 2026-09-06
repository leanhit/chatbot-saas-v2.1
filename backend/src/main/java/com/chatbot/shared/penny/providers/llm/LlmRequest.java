package com.chatbot.shared.penny.providers.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for LLM generation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmRequest {

    /**
     * System prompt / instructions
     */
    private String systemPrompt;

    /**
     * User message / query
     */
    private String userMessage;

    /**
     * Conversation history for context
     */
    private List<Message> conversationHistory;

    /**
     * Temperature (0.0 - 1.0)
     * Lower = more focused/deterministic, Higher = more creative/random
     */
    @Builder.Default
    private Float temperature = 0.7f;

    /**
     * Maximum tokens to generate
     */
    @Builder.Default
    private Integer maxTokens = 1000;

    /**
     * Top-p sampling (nucleus sampling)
     */
    @Builder.Default
    private Float topP = 1.0f;

    /**
     * Frequency penalty (-2.0 to 2.0)
     */
    @Builder.Default
    private Float frequencyPenalty = 0.0f;

    /**
     * Presence penalty (-2.0 to 2.0)
     */
    @Builder.Default
    private Float presencePenalty = 0.0f;

    /**
     * Additional parameters specific to provider
     */
    private Map<String, Object> additionalParams;

    /**
     * Message in conversation history
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role; // system, user, assistant
        private String content;
    }
}
