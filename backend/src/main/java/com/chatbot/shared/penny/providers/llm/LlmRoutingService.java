package com.chatbot.shared.penny.providers.llm;

import com.chatbot.shared.penny.model.LlmProviderType;
import com.chatbot.shared.penny.model.PennyBot;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * LlmRoutingService — Routes LLM requests to appropriate provider with failover
 *
 * Implements circuit breaker pattern for automatic failover between providers.
 * Supports primary/fallback configuration per bot.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LlmRoutingService {

    private final OpenAiLlmProvider openAiProvider;
    private final ClaudeLlmProvider claudeProvider;
    private final GeminiLlmProvider geminiProvider;
    private final OllamaLlmProvider ollamaProvider;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    private final Map<LlmProviderType, CircuitBreaker> circuitBreakers = new HashMap<>();

    /**
     * Generate response using bot's configured provider with automatic failover
     * 
     * @param request LLM request
     * @param bot Bot configuration
     * @return LLM response
     */
    public LlmResponse generateResponse(LlmRequest request, PennyBot bot) {
        LlmProviderType primaryProvider = bot.getProviderType();
        String modelName = bot.getModelName();
        
        // Configure model name for primary provider
        configureProviderModel(primaryProvider, modelName);

        log.info("Routing LLM request to primary provider: {}, model: {}", primaryProvider, modelName);

        // Try primary provider with circuit breaker
        LlmResponse response = tryProvider(request, primaryProvider);

        // If primary fails, try fallback providers in order
        if (!response.isSuccess()) {
            log.warn("Primary provider {} failed, trying fallback providers", primaryProvider);
            response = tryFallbackProviders(request, primaryProvider);
        }

        return response;
    }

    /**
     * Try a specific provider with circuit breaker protection
     */
    private LlmResponse tryProvider(LlmRequest request, LlmProviderType providerType) {
        LlmProvider provider = getProvider(providerType);
        if (provider == null) {
            log.warn("Provider {} not available", providerType);
            return LlmResponse.failure("Provider " + providerType + " not available");
        }

        if (!provider.isHealthy()) {
            log.warn("Provider {} is not healthy", providerType);
            return LlmResponse.failure("Provider " + providerType + " is not healthy");
        }

        CircuitBreaker circuitBreaker = getCircuitBreaker(providerType);

        try {
            LlmResponse response = CircuitBreaker.decorateSupplier(circuitBreaker, 
                () -> provider.generateResponse(request)).get();
            
            if (response.isSuccess()) {
                log.info("Provider {} succeeded", providerType);
            } else {
                log.warn("Provider {} returned error: {}", providerType, response.getErrorMessage());
            }
            
            return response;
        } catch (Exception e) {
            log.error("Provider {} circuit breaker error: {}", providerType, e.getMessage());
            return LlmResponse.failure("Provider " + providerType + " error: " + e.getMessage());
        }
    }

    /**
     * Try fallback providers in order of preference
     * Fallback order: OpenAI -> Claude -> Gemini -> Ollama
     */
    private LlmResponse tryFallbackProviders(LlmRequest request, LlmProviderType failedProvider) {
        LlmProviderType[] fallbackOrder = {
            LlmProviderType.OPENAI,
            LlmProviderType.CLAUDE,
            LlmProviderType.GEMINI,
            LlmProviderType.OLLAMA
        };

        for (LlmProviderType providerType : fallbackOrder) {
            if (providerType == failedProvider) {
                continue; // Skip the failed provider
            }

            log.info("Trying fallback provider: {}", providerType);
            LlmResponse response = tryProvider(request, providerType);
            
            if (response.isSuccess()) {
                log.info("Fallback provider {} succeeded", providerType);
                return response;
            }
        }

        log.error("All providers failed");
        return LlmResponse.failure("All LLM providers failed");
    }

    /**
     * Get provider instance by type
     */
    private LlmProvider getProvider(LlmProviderType type) {
        return switch (type) {
            case OPENAI -> openAiProvider;
            case CLAUDE -> claudeProvider;
            case GEMINI -> geminiProvider;
            case OLLAMA -> ollamaProvider;
        };
    }

    /**
     * Configure model name for a provider
     */
    private void configureProviderModel(LlmProviderType type, String modelName) {
        switch (type) {
            case OPENAI -> openAiProvider.setModelName(modelName);
            case CLAUDE -> claudeProvider.setModelName(modelName);
            case GEMINI -> geminiProvider.setModelName(modelName);
            case OLLAMA -> ollamaProvider.setModelName(modelName);
        }
    }

    /**
     * Get or create circuit breaker for a provider
     */
    private CircuitBreaker getCircuitBreaker(LlmProviderType type) {
        return circuitBreakers.computeIfAbsent(type, t -> {
            String name = "llmProvider-" + t.name().toLowerCase();
            return circuitBreakerRegistry.circuitBreaker(name);
        });
    }

    /**
     * Check if a specific provider is healthy
     */
    public boolean isProviderHealthy(LlmProviderType type) {
        LlmProvider provider = getProvider(type);
        return provider != null && provider.isHealthy();
    }

    /**
     * Get health status of all providers
     */
    public Map<LlmProviderType, Boolean> getAllProvidersHealth() {
        Map<LlmProviderType, Boolean> health = new HashMap<>();
        for (LlmProviderType type : LlmProviderType.values()) {
            health.put(type, isProviderHealthy(type));
        }
        return health;
    }
}
