package com.chatbot.shared.penny.providers.llm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO from LLM providers
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LlmResponse {

    /**
     * Generated text response
     */
    private String text;

    /**
     * Whether the request was successful
     */
    @Builder.Default
    private boolean success = true;

    /**
     * Error message if request failed
     */
    private String errorMessage;

    /**
     * Number of tokens used in the request
     */
    private Integer promptTokens;

    /**
     * Number of tokens generated in the response
     */
    private Integer completionTokens;

    /**
     * Total tokens used
     */
    private Integer totalTokens;

    /**
     * Model used for generation
     */
    private String model;

    /**
     * Finish reason (stop, length, content_filter, etc.)
     */
    private String finishReason;

    /**
     * Time taken to generate response in milliseconds
     */
    private Long processingTimeMs;

    /**
     * Additional metadata from provider
     */
    private Map<String, Object> metadata;

    /**
     * Create a failed response
     */
    public static LlmResponse failure(String errorMessage) {
        return LlmResponse.builder()
            .success(false)
            .errorMessage(errorMessage)
            .build();
    }

    /**
     * Create a successful response
     */
    public static LlmResponse success(String text) {
        return LlmResponse.builder()
            .success(true)
            .text(text)
            .build();
    }
}
