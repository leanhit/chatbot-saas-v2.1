package com.chatbot.shared.penny.providers.llm;

import com.chatbot.shared.penny.model.LlmProviderType;

/**
 * Standardized interface for all LLM providers
 * 
 * This abstraction layer allows the system to work with multiple LLM providers
 * (OpenAI, Claude, Gemini, Ollama) through a unified API.
 */
public interface LlmProvider {

    /**
     * Generate a response for the given LLM request
     * 
     * @param request The LLM request containing prompt, temperature, etc.
     * @return LLM response with generated text and metadata
     */
    LlmResponse generateResponse(LlmRequest request);

    /**
     * Check if the provider is healthy and available
     * 
     * @return true if provider is healthy, false otherwise
     */
    boolean isHealthy();

    /**
     * Get the provider type
     * 
     * @return The provider type (OPENAI, CLAUDE, GEMINI, OLLAMA)
     */
    LlmProviderType getProviderType();

    /**
     * Get the model name this provider is configured to use
     * 
     * @return Model name (e.g., "gpt-4o-mini", "claude-3-5-sonnet")
     */
    String getModelName();
}
