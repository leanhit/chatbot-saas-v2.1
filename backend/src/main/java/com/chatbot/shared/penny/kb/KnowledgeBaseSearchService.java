package com.chatbot.shared.penny.kb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * KnowledgeBaseSearchService — RAG search service for retrieving relevant knowledge articles
 *
 * Performs vector similarity search using pgvector or falls back to priority-based search.
 * Formats retrieved articles as context snippets for LLM prompt injection.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeBaseSearchService {

    @Value("${penny.rag.top-k:3}")
    private int topK;

    @Value("${penny.rag.similarity-threshold:0.7}")
    private double similarityThreshold;

    @Value("${penny.rag.max-context-tokens:1500}")
    private int maxContextTokens;

    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final EmbeddingService embeddingService;

    /**
     * Search for relevant knowledge articles based on query text
     * 
     * @param botId UUID of the bot
     * @param tenantId Tenant ID for multi-tenant isolation
     * @param queryText User query to search for
     * @return List of relevant articles sorted by similarity
     */
    public List<KnowledgeArticle> search(UUID botId, Long tenantId, String queryText) {
        if (!embeddingService.isEnabled()) {
            log.warn("⚠️ EmbeddingService is disabled, using priority-based fallback search");
            return searchByPriority(botId, tenantId);
        }

        try {
            // Generate embedding for query
            float[] queryEmbedding = embeddingService.generateEmbedding(queryText);
            if (queryEmbedding == null) {
                log.warn("⚠️ Failed to generate embedding for query, using priority-based fallback");
                return searchByPriority(botId, tenantId);
            }

            // Convert float[] to string format for pgvector
            String embeddingString = floatArrayToString(queryEmbedding);

            // Perform vector similarity search
            List<KnowledgeArticle> articles = knowledgeArticleRepository.findSimilarByVector(
                botId, tenantId, embeddingString, topK);

            log.info("🔍 Vector search returned {} articles for bot {}", articles.size(), botId);
            return articles;

        } catch (Exception e) {
            log.error("❌ Error in vector search, falling back to priority-based search: {}", e.getMessage(), e);
            return searchByPriority(botId, tenantId);
        }
    }

    /**
     * Fallback search by priority when vector search is not available
     */
    private List<KnowledgeArticle> searchByPriority(UUID botId, Long tenantId) {
        List<KnowledgeArticle> articles = knowledgeArticleRepository.findTopKByPriority(
            botId, tenantId, topK);
        log.info("🔍 Priority-based search returned {} articles for bot {}", articles.size(), botId);
        return articles;
    }

    /**
     * Format retrieved articles as context snippets for LLM prompt
     * 
     * @param articles List of articles to format
     * @return Formatted context string
     */
    public String formatContextForPrompt(List<KnowledgeArticle> articles) {
        if (articles == null || articles.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        int totalChars = 0;

        for (KnowledgeArticle article : articles) {
            String snippet = article.toContextSnippet();
            if (totalChars + snippet.length() > maxContextTokens * 4) { // Approximate 4 chars per token
                log.debug("Context truncated at {} chars to stay within token limit", totalChars);
                break;
            }
            context.append(snippet).append("\n\n");
            totalChars += snippet.length();
        }

        return context.toString();
    }

    /**
     * Search and format context in one call
     * 
     * @param botId UUID of the bot
     * @param tenantId Tenant ID
     * @param queryText User query
     * @return Formatted context string ready for LLM prompt
     */
    public String searchAndFormatContext(UUID botId, Long tenantId, String queryText) {
        List<KnowledgeArticle> articles = search(botId, tenantId, queryText);
        return formatContextForPrompt(articles);
    }

    /**
     * Convert float array to string format for pgvector
     * Format: "[0.1,0.2,0.3,...]"
     */
    private String floatArrayToString(float[] array) {
        if (array == null || array.length == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Check if RAG search is enabled
     */
    public boolean isEnabled() {
        return embeddingService.isEnabled();
    }

    /**
     * Get current top-K setting
     */
    public int getTopK() {
        return topK;
    }

    /**
     * Get current similarity threshold
     */
    public double getSimilarityThreshold() {
        return similarityThreshold;
    }
}
