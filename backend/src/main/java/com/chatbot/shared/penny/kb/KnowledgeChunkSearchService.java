package com.chatbot.shared.penny.kb;

import com.chatbot.shared.penny.model.PennyKnowledgeChunk;
import com.chatbot.shared.penny.repository.PennyKnowledgeChunkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * KnowledgeChunkSearchService — RAG search service for retrieving relevant document chunks
 *
 * Performs vector similarity search using pgvector HNSW index for document chunks.
 * Formats retrieved chunks as context snippets for LLM prompt injection.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class KnowledgeChunkSearchService {

    @Value("${penny.rag.top-k:3}")
    private int topK;

    @Value("${penny.rag.similarity-threshold:0.72}")
    private double similarityThreshold;

    @Value("${penny.rag.max-context-tokens:1500}")
    private int maxContextTokens;

    private final PennyKnowledgeChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;

    /**
     * Search for relevant document chunks based on query text
     * 
     * @param botId UUID of the bot
     * @param tenantId Tenant ID for multi-tenant isolation
     * @param queryText User query to search for
     * @return List of relevant chunks sorted by similarity
     */
    public List<PennyKnowledgeChunk> search(UUID botId, Long tenantId, String queryText) {
        if (!embeddingService.isEnabled()) {
            log.warn("⚠️ EmbeddingService is disabled, cannot perform vector search on chunks");
            return List.of();
        }

        try {
            // Generate embedding for query
            float[] queryEmbedding = embeddingService.generateEmbedding(queryText);
            if (queryEmbedding == null) {
                log.warn("⚠️ Failed to generate embedding for query");
                return List.of();
            }

            // Convert float[] to string format for pgvector
            String embeddingString = floatArrayToString(queryEmbedding);

            // Perform vector similarity search with threshold
            List<PennyKnowledgeChunk> chunks = chunkRepository.findSimilarByVectorWithThreshold(
                botId, tenantId, embeddingString, similarityThreshold, topK);

            log.info("🔍 Vector search returned {} chunks for bot {} (threshold: {})", 
                chunks.size(), botId, similarityThreshold);
            return chunks;

        } catch (Exception e) {
            log.error("❌ Error in vector search for chunks: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Search without similarity threshold (returns top-K regardless of score)
     */
    public List<PennyKnowledgeChunk> searchWithoutThreshold(UUID botId, Long tenantId, String queryText) {
        if (!embeddingService.isEnabled()) {
            log.warn("⚠️ EmbeddingService is disabled");
            return List.of();
        }

        try {
            float[] queryEmbedding = embeddingService.generateEmbedding(queryText);
            if (queryEmbedding == null) {
                return List.of();
            }

            String embeddingString = floatArrayToString(queryEmbedding);
            List<PennyKnowledgeChunk> chunks = chunkRepository.findSimilarByVector(
                botId, tenantId, embeddingString, topK);

            log.info("🔍 Vector search (no threshold) returned {} chunks for bot {}", chunks.size(), botId);
            return chunks;

        } catch (Exception e) {
            log.error("❌ Error in vector search: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Format retrieved chunks as context snippets for LLM prompt
     * 
     * @param chunks List of chunks to format
     * @return Formatted context string
     */
    public String formatContextForPrompt(List<PennyKnowledgeChunk> chunks) {
        if (chunks == null || chunks.isEmpty()) {
            return "";
        }

        StringBuilder context = new StringBuilder();
        int totalChars = 0;

        for (PennyKnowledgeChunk chunk : chunks) {
            String snippet = chunkToContextSnippet(chunk);
            if (totalChars + snippet.length() > maxContextTokens * 4) { // Approximate 4 chars per token
                log.debug("Context truncated at {} chars to stay within token limit", totalChars);
                break;
            }
            context.append(snippet).append("\n\n---\n\n");
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
        List<PennyKnowledgeChunk> chunks = search(botId, tenantId, queryText);
        return formatContextForPrompt(chunks);
    }

    /**
     * Convert chunk to context snippet format
     */
    private String chunkToContextSnippet(PennyKnowledgeChunk chunk) {
        StringBuilder sb = new StringBuilder();
        sb.append("[Document Chunk #").append(chunk.getChunkIndex()).append("]");
        
        if (chunk.getPageNumber() != null) {
            sb.append(" (Page ").append(chunk.getPageNumber()).append(")");
        }
        if (chunk.getSheetName() != null) {
            sb.append(" (Sheet: ").append(chunk.getSheetName()).append(")");
        }
        
        sb.append("\n").append(chunk.getChunkText());
        return sb.toString();
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
