package com.chatbot.core.penny.providers.llm;

import com.chatbot.core.penny.model.LlmProviderType;
import com.chatbot.core.penny.model.PennyBot;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private final ObjectProvider<MeterRegistry> meterRegistryProvider;

    private final Map<LlmProviderType, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

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
        
        // Pass model per-request — providers are shared singletons and must not
        // be mutated per request (avoids cross-bot model leakage under concurrency).
        if (modelName != null && !modelName.isBlank()) {
            request.setModelName(modelName);
        }

        log.info("Routing LLM request to primary provider: {}, model: {}", primaryProvider, modelName);

        // Try primary provider with circuit breaker
        LlmResponse response = tryProvider(request, primaryProvider);

        // If primary fails, try fallback providers in order.
        // Clear the per-request model override so fallback providers use their
        // own configured defaults (bot's model name may not exist on other providers).
        if (!response.isSuccess()) {
            log.warn("Primary provider {} failed, trying fallback providers", primaryProvider);
            safeIncrement("penny.llm.provider.failure", "provider", primaryProvider.name());
            request.setModelName(null);
            response = tryFallbackProviders(request, bot, primaryProvider);
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
     * Try fallback providers. If the bot configures a preferred fallback
     * provider it is tried first, then the default order
     * (OpenAI -> Claude -> Gemini -> Ollama) excluding already-tried providers.
     */
    private LlmResponse tryFallbackProviders(LlmRequest request, PennyBot bot, LlmProviderType failedProvider) {
        java.util.List<LlmProviderType> order = new java.util.ArrayList<>();

        LlmProviderType preferred = bot != null ? bot.getFallbackProviderType() : null;
        if (preferred != null && preferred != failedProvider) {
            order.add(preferred);
        }

        LlmProviderType[] defaultOrder = {
            LlmProviderType.OPENAI,
            LlmProviderType.CLAUDE,
            LlmProviderType.GEMINI,
            LlmProviderType.OLLAMA
        };
        for (LlmProviderType type : defaultOrder) {
            if (type != failedProvider && !order.contains(type)) {
                order.add(type);
            }
        }

        for (LlmProviderType providerType : order) {
            log.info("Trying fallback provider: {}", providerType);
            LlmResponse response = tryProvider(request, providerType);
            
            if (response.isSuccess()) {
                log.info("Fallback provider {} succeeded", providerType);
                safeIncrement("penny.llm.provider.fallback.success", "provider", providerType.name());
                return response;
            }
            safeIncrement("penny.llm.provider.failure", "provider", providerType.name());
        }

        log.error("All providers failed");
        safeIncrement("penny.llm.all_providers_failed");
        return LlmResponse.failure("All LLM providers failed");
    }

    private void safeIncrement(String name, String... tags) {
        try {
            MeterRegistry registry = meterRegistryProvider != null ? meterRegistryProvider.getIfAvailable() : null;
            if (registry != null) {
                registry.counter(name, tags).increment();
            }
        } catch (Exception ignored) {
            // metrics must never break routing
        }
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
        Map<LlmProviderType, Boolean> health = new ConcurrentHashMap<>();
        for (LlmProviderType type : LlmProviderType.values()) {
            health.put(type, isProviderHealthy(type));
        }
        return health;
    }
}
