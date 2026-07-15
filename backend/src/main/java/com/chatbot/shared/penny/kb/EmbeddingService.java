package com.chatbot.shared.penny.kb;

import com.chatbot.shared.penny.core.config.PennyProperties;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * EmbeddingService — Generate and cache text embeddings using OpenAI API
 *
 * Uses text-embedding-3-small model (1536 dimensions) for vector similarity search.
 * Caches embeddings in Redis with configurable TTL to reduce API calls.
 * Supports batch processing with parallel calls when enabled.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    @Value("${penny.rag.embedding-model:text-embedding-3-small}")
    private String embeddingModel;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ApiKeyManager apiKeyManager;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final PennyProperties pennyProperties;

    private OpenAIClient openAIClient;
    private boolean enabled = false;
    private CircuitBreaker circuitBreaker;
    private Retry retry;
    private ExecutorService batchExecutor;

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
        
        // Initialize batch executor for parallel processing
        if (pennyProperties.getRag().isBatchEnabled()) {
            this.batchExecutor = Executors.newFixedThreadPool(
                Math.min(pennyProperties.getRag().getBatchSize(), 10)
            );
            log.info("✅ Batch embedding enabled with pool size: {}", pennyProperties.getRag().getBatchSize());
        }
        
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

            float[] embedding;
            try {
                embedding = embeddingSupplier.get();
            } catch (Exception e) {
                // Circuit breaker fallback: try to return cached embedding
                if (pennyProperties.getRag().isCircuitBreakerFallbackEnabled()) {
                    log.warn("⚠️ Circuit breaker open, attempting fallback to cached embedding");
                    @SuppressWarnings("unchecked")
                    List<Double> cachedFallback = (List<Double>) redisTemplate.opsForValue().get(cacheKey);
                    if (cachedFallback != null && !cachedFallback.isEmpty()) {
                        log.info("✅ Fallback to cached embedding successful");
                        float[] cachedEmbedding = new float[cachedFallback.size()];
                        for (int i = 0; i < cachedFallback.size(); i++) {
                            cachedEmbedding[i] = cachedFallback.get(i).floatValue();
                        }
                        return cachedEmbedding;
                    }
                    log.warn("⚠️ No cached embedding available for fallback");
                }
                throw e;
            }

            log.info("✅ Generated embedding for text ({} chars), dimensions: {}", text.length(), embedding.length);

            // Cache in Redis (store as List<Double> for Redis serialization)
            List<Double> redisEmbeddingList = new ArrayList<>();
            for (float f : embedding) {
                redisEmbeddingList.add((double) f);
            }
            redisTemplate.opsForValue().set(
                cacheKey,
                redisEmbeddingList,
                pennyProperties.getRag().getCacheTtl()
            );

            return embedding;

        } catch (Exception e) {
            log.error("❌ Error generating embedding: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Generate embeddings for multiple texts (batch processing)
     * Uses parallel processing when batch mode is enabled
     * Falls back to sequential processing when batch mode is disabled
     */
    public List<float[]> generateEmbeddingsBatch(List<String> texts) {
        if (!enabled) {
            log.warn("⚠️ EmbeddingService is disabled, returning empty list");
            return List.of();
        }

        if (texts == null || texts.isEmpty()) {
            return List.of();
        }

        // Use parallel processing if batch mode is enabled
        if (pennyProperties.getRag().isBatchEnabled() && batchExecutor != null) {
            return generateEmbeddingsBatchParallel(texts);
        }

        // Sequential processing (fallback)
        List<float[]> embeddings = new ArrayList<>();
        for (String text : texts) {
            float[] embedding = generateEmbedding(text);
            embeddings.add(embedding);
        }
        
        log.info("✅ Generated {} embeddings (sequential batch)", embeddings.size());
        return embeddings;
    }

    /**
     * Generate embeddings in parallel using thread pool
     */
    private List<float[]> generateEmbeddingsBatchParallel(List<String> texts) {
        List<CompletableFuture<float[]>> futures = new ArrayList<>();
        
        for (String text : texts) {
            CompletableFuture<float[]> future = CompletableFuture.supplyAsync(
                () -> generateEmbedding(text),
                batchExecutor
            );
            futures.add(future);
        }
        
        // Wait for all futures to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );
        
        try {
            allFutures.join();
            
            List<float[]> embeddings = new ArrayList<>();
            for (CompletableFuture<float[]> future : futures) {
                embeddings.add(future.get());
            }
            
            log.info("✅ Generated {} embeddings (parallel batch)", embeddings.size());
            return embeddings;
            
        } catch (Exception e) {
            log.error("❌ Error in parallel batch embedding generation: {}", e.getMessage(), e);
            // Fallback to sequential processing
            return generateEmbeddingsBatchSequential(texts);
        }
    }

    /**
     * Sequential batch processing (fallback)
     */
    private List<float[]> generateEmbeddingsBatchSequential(List<String> texts) {
        List<float[]> embeddings = new ArrayList<>();
        for (String text : texts) {
            float[] embedding = generateEmbedding(text);
            embeddings.add(embedding);
        }
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
