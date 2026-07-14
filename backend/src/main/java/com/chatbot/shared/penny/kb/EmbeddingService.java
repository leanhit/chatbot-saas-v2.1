package com.chatbot.shared.penny.kb;

import com.chatbot.shared.penny.security.ApiKeyManager;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.embeddings.EmbeddingCreateParams;
import com.openai.models.embeddings.EmbeddingModel;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * EmbeddingService — Generate and cache text embeddings using OpenAI API
 *
 * Uses text-embedding-3-small model (1536 dimensions) for vector similarity search.
 * Caches embeddings in Redis with 7-day TTL to reduce API calls.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    @Value("${penny.rag.embedding-model:text-embedding-3-small}")
    private String embeddingModel;

    @Value("${penny.rag.cache.ttl-days:7}")
    private int cacheTtlDays;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ApiKeyManager apiKeyManager;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    private OpenAIClient openAIClient;
    private boolean enabled = false;
    private CircuitBreaker circuitBreaker;
    private Retry retry;

    @PostConstruct
    public void init() {
        String apiKey = apiKeyManager.getOpenAiApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("⚠️ EmbeddingService: OPENAI_API_KEY is not set. Embedding generation disabled.");
            return;
        }
        this.openAIClient = OpenAIOkHttpClient.builder()
            .apiKey(apiKey)
            .build();
        this.enabled = true;
        
        // Initialize circuit breaker and retry
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("embeddingService");
        this.retry = retryRegistry.retry("embeddingService");
        
        log.info("✅ EmbeddingService initialized with model: {}", embeddingModel);
    }

    /**
     * Generate embedding for a single text
     *
     * Key is cached in Redis with format: "embedding:{hash(text)}"
     */
    @Cacheable(value = "embeddings", key = "#text", unless = "#result == null")
    public float[] generateEmbedding(String text) {
        if (!enabled) {
            log.warn("⚠️ EmbeddingService is disabled, returning null embedding");
            return null;
        }

        if (text == null || text.isBlank()) {
            log.warn("⚠️ Cannot generate embedding for empty text");
            return null;
        }

        try {
            String cacheKey = getCacheKey(text);
            
            // Try to get from Redis cache first
            @SuppressWarnings("unchecked")
            List<Double> cached = (List<Double>) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null && !cached.isEmpty()) {
                log.debug("📦 Embedding cache hit for text ({} chars)", text.length());
                // Convert List<Double> to float[]
                float[] embedding = new float[cached.size()];
                for (int i = 0; i < cached.size(); i++) {
                    embedding[i] = cached.get(i).floatValue();
                }
                return embedding;
            }

            // Generate embedding via OpenAI API with circuit breaker and retry
            Supplier<float[]> embeddingSupplier = CircuitBreaker.decorateSupplier(
                circuitBreaker,
                Retry.decorateSupplier(
                    retry,
                    () -> {
                        EmbeddingCreateParams params = EmbeddingCreateParams.builder()
                            .model(EmbeddingModel.of(embeddingModel))
                            .input(text)
                            .build();

                        var response = openAIClient.embeddings().create(params);
                        List<Float> embeddingList = response.data().get(0).embedding();
                        
                        // Convert List<Float> to float[]
                        float[] embedding = new float[embeddingList.size()];
                        for (int i = 0; i < embeddingList.size(); i++) {
                            embedding[i] = embeddingList.get(i);
                        }
                        
                        return embedding;
                    }
                )
            );

            float[] embedding = embeddingSupplier.get();

            log.info("✅ Generated embedding for text ({} chars), dimensions: {}", text.length(), embedding.length);

            // Cache in Redis (store as List<Double> for Redis serialization)
            List<Double> redisEmbeddingList = new ArrayList<>();
            for (float f : embedding) {
                redisEmbeddingList.add((double) f);
            }
            redisTemplate.opsForValue().set(
                cacheKey,
                redisEmbeddingList,
                Duration.ofDays(cacheTtlDays)
            );

            return embedding;

        } catch (Exception e) {
            log.error("❌ Error generating embedding: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate embeddings for multiple texts (batch processing)
     * For now, calls generateEmbedding() for each text individually
     * TODO: Optimize with true batch API when OpenAI SDK supports it properly
     */
    public List<float[]> generateEmbeddingsBatch(List<String> texts) {
        if (!enabled) {
            log.warn("⚠️ EmbeddingService is disabled, returning empty list");
            return List.of();
        }

        if (texts == null || texts.isEmpty()) {
            return List.of();
        }

        List<float[]> embeddings = new ArrayList<>();
        
        for (String text : texts) {
            float[] embedding = generateEmbedding(text);
            embeddings.add(embedding);
        }
        
        log.info("✅ Generated {} embeddings (batch)", embeddings.size());
        return embeddings;
    }

    /**
     * Check if embedding service is enabled and configured
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the embedding model being used
     */
    public String getEmbeddingModel() {
        return embeddingModel;
    }

    /**
     * Get embedding dimensions for the current model
     */
    public int getEmbeddingDimensions() {
        return switch (embeddingModel) {
            case "text-embedding-3-small" -> 1536;
            case "text-embedding-3-large" -> 3072;
            case "text-embedding-ada-002" -> 1536;
            default -> 1536; // Default to 1536
        };
    }

    /**
     * Clear cache for a specific text
     */
    public void clearCache(String text) {
        String cacheKey = getCacheKey(text);
        redisTemplate.delete(cacheKey);
        log.debug("🗑️ Cleared embedding cache for text ({} chars)", text.length());
    }

    /**
     * Clear all embedding cache
     */
    public void clearAllCache() {
        redisTemplate.delete(redisTemplate.keys("embedding:*"));
        log.info("🗑️ Cleared all embedding cache");
    }

    // ─── Private helpers ───────────────────────────────────────────────────

    private String getCacheKey(String text) {
        // Use hash of text as cache key to avoid storing full text in Redis key
        int hash = text.hashCode();
        return "embedding:" + Math.abs(hash);
    }
}
