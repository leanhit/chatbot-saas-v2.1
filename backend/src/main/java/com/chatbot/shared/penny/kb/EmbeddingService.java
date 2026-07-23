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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    /**
     * Calculate cosine similarity between two embeddings
     * Returns value between -1 and 1, where 1 means identical
     */
    public double cosineSimilarity(float[] embedding1, float[] embedding2) {
        if (embedding1 == null || embedding2 == null) {
            return 0.0;
        }
        if (embedding1.length != embedding2.length) {
            log.warn("⚠️ Embedding dimensions mismatch: {} vs {}", embedding1.length, embedding2.length);
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < embedding1.length; i++) {
            dotProduct += embedding1[i] * embedding2[i];
            norm1 += embedding1[i] * embedding1[i];
            norm2 += embedding2[i] * embedding2[i];
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * Find most similar embeddings from a list of candidates
     * Optimized with early termination and batch processing
     */
    public List<SimilarityResult> findMostSimilar(float[] queryEmbedding, 
                                                   List<float[]> candidateEmbeddings,
                                                   List<String> candidateIds,
                                                   int topK,
                                                   double minSimilarity) {
        if (queryEmbedding == null || candidateEmbeddings == null || candidateEmbeddings.isEmpty()) {
            return List.of();
        }

        List<SimilarityResult> results = new ArrayList<>();
        
        // Calculate similarities in parallel if batch mode is enabled
        if (pennyProperties.getRag().isBatchEnabled() && batchExecutor != null) {
            results = calculateSimilaritiesParallel(queryEmbedding, candidateEmbeddings, candidateIds);
        } else {
            results = calculateSimilaritiesSequential(queryEmbedding, candidateEmbeddings, candidateIds);
        }

        // Filter by minimum similarity threshold
        results = results.stream()
            .filter(r -> r.getSimilarity() >= minSimilarity)
            .collect(Collectors.toList());

        // Sort by similarity (descending) and take top K
        results = results.stream()
            .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
            .limit(topK)
            .collect(Collectors.toList());

        log.debug("🔍 Found {} similar embeddings (topK: {}, minSimilarity: {})", 
            results.size(), topK, minSimilarity);
        
        return results;
    }

    /**
     * Calculate similarities in parallel
     */
    private List<SimilarityResult> calculateSimilaritiesParallel(float[] queryEmbedding,
                                                                   List<float[]> candidateEmbeddings,
                                                                   List<String> candidateIds) {
        List<CompletableFuture<SimilarityResult>> futures = new ArrayList<>();

        for (int i = 0; i < candidateEmbeddings.size(); i++) {
            final int index = i;
            CompletableFuture<SimilarityResult> future = CompletableFuture.supplyAsync(
                () -> {
                    float[] candidate = candidateEmbeddings.get(index);
                    String id = candidateIds != null && index < candidateIds.size() 
                        ? candidateIds.get(index) 
                        : String.valueOf(index);
                    double similarity = cosineSimilarity(queryEmbedding, candidate);
                    return new SimilarityResult(id, similarity);
                },
                batchExecutor
            );
            futures.add(future);
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("❌ Error in parallel similarity calculation: {}", e.getMessage(), e);
            return calculateSimilaritiesSequential(queryEmbedding, candidateEmbeddings, candidateIds);
        }
    }

    /**
     * Calculate similarities sequentially (fallback)
     */
    private List<SimilarityResult> calculateSimilaritiesSequential(float[] queryEmbedding,
                                                                     List<float[]> candidateEmbeddings,
                                                                     List<String> candidateIds) {
        List<SimilarityResult> results = new ArrayList<>();

        for (int i = 0; i < candidateEmbeddings.size(); i++) {
            float[] candidate = candidateEmbeddings.get(i);
            String id = candidateIds != null && i < candidateIds.size() 
                ? candidateIds.get(i) 
                : String.valueOf(i);
            double similarity = cosineSimilarity(queryEmbedding, candidate);
            results.add(new SimilarityResult(id, similarity));
        }

        return results;
    }

    /**
     * Hybrid search: combine semantic similarity with keyword matching
     * Uses weighted scoring to balance both approaches
     */
    public List<HybridSearchResult> hybridSearch(float[] queryEmbedding,
                                                  String queryText,
                                                  List<float[]> candidateEmbeddings,
                                                  List<String> candidateIds,
                                                  List<String> candidateTexts,
                                                  int topK,
                                                  double semanticWeight) {
        if (queryEmbedding == null || candidateEmbeddings == null || candidateEmbeddings.isEmpty()) {
            return List.of();
        }

        double keywordWeight = 1.0 - semanticWeight;
        List<HybridSearchResult> results = new ArrayList<>();

        for (int i = 0; i < candidateEmbeddings.size(); i++) {
            float[] candidateEmbedding = candidateEmbeddings.get(i);
            String id = candidateIds != null && i < candidateIds.size() 
                ? candidateIds.get(i) 
                : String.valueOf(i);
            String candidateText = candidateTexts != null && i < candidateTexts.size() 
                ? candidateTexts.get(i) 
                : "";

            // Calculate semantic similarity
            double semanticScore = cosineSimilarity(queryEmbedding, candidateEmbedding);

            // Calculate keyword similarity (simple overlap)
            double keywordScore = calculateKeywordSimilarity(queryText, candidateText);

            // Combine scores with weights
            double combinedScore = (semanticScore * semanticWeight) + (keywordScore * keywordWeight);

            results.add(new HybridSearchResult(id, semanticScore, keywordScore, combinedScore));
        }

        // Sort by combined score (descending) and take top K
        results = results.stream()
            .sorted((a, b) -> Double.compare(b.getCombinedScore(), a.getCombinedScore()))
            .limit(topK)
            .collect(Collectors.toList());

        log.debug("🔍 Hybrid search completed: {} results (semanticWeight: {})", 
            results.size(), semanticWeight);

        return results;
    }

    /**
     * Calculate keyword similarity using simple token overlap
     */
    private double calculateKeywordSimilarity(String query, String candidate) {
        if (query == null || candidate == null) {
            return 0.0;
        }

        Set<String> queryTokens = Arrays.stream(query.toLowerCase().split("\\s+"))
            .collect(Collectors.toSet());
        Set<String> candidateTokens = Arrays.stream(candidate.toLowerCase().split("\\s+"))
            .collect(Collectors.toSet());

        if (queryTokens.isEmpty() || candidateTokens.isEmpty()) {
            return 0.0;
        }

        // Jaccard similarity
        Set<String> intersection = new HashSet<>(queryTokens);
        intersection.retainAll(candidateTokens);
        Set<String> union = new HashSet<>(queryTokens);
        union.addAll(candidateTokens);

        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    /**
     * Re-rank results based on additional criteria (recency, popularity, etc.)
     */
    public List<SimilarityResult> reRankResults(List<SimilarityResult> results,
                                                 Map<String, Double> boostFactors) {
        if (results == null || results.isEmpty()) {
            return List.of();
        }

        return results.stream()
            .map(result -> {
                double boost = boostFactors.getOrDefault(result.getId(), 1.0);
                double boostedScore = result.getSimilarity() * boost;
                return new SimilarityResult(result.getId(), Math.min(boostedScore, 1.0));
            })
            .sorted((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()))
            .collect(Collectors.toList());
    }

    // ─── Inner classes ─────────────────────────────────────────────────────

    /**
     * Similarity result with ID and score
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class SimilarityResult {
        private String id;
        private double similarity;
    }

    /**
     * Hybrid search result with semantic, keyword, and combined scores
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class HybridSearchResult {
        private String id;
        private double semanticScore;
        private double keywordScore;
        private double combinedScore;
    }
}
