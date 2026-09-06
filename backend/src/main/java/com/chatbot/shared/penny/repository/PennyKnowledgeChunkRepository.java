package com.chatbot.shared.penny.repository;

import com.chatbot.shared.penny.model.PennyKnowledgeChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PennyKnowledgeChunkRepository extends JpaRepository<PennyKnowledgeChunk, UUID> {

    List<PennyKnowledgeChunk> findByDocumentId(UUID documentId);

    List<PennyKnowledgeChunk> findByBotId(UUID botId);

    List<PennyKnowledgeChunk> findByBotIdAndTenantId(UUID botId, Long tenantId);

    List<PennyKnowledgeChunk> findByDocumentIdOrderByChunkIndex(UUID documentId);

    @Query("SELECT c FROM PennyKnowledgeChunk c WHERE c.botId = :botId AND c.tenantId = :tenantId ORDER BY c.chunkIndex")
    List<PennyKnowledgeChunk> findByBotIdAndTenantIdOrdered(
        @Param("botId") UUID botId,
        @Param("tenantId") Long tenantId
    );

    @Query("SELECT COUNT(c) FROM PennyKnowledgeChunk c WHERE c.documentId = :documentId")
    long countByDocumentId(@Param("documentId") UUID documentId);

    @Query("SELECT COUNT(c) FROM PennyKnowledgeChunk c WHERE c.botId = :botId AND c.tenantId = :tenantId")
    long countByBotIdAndTenantId(@Param("botId") UUID botId, @Param("tenantId") Long tenantId);

    void deleteByDocumentId(UUID documentId);

    void deleteByBotId(UUID botId);

    /**
     * Vector similarity search using pgvector HNSW cosine distance
     * Returns chunks sorted by similarity (most similar first)
     * Only works if pgvector extension is enabled in PostgreSQL
     */
    @Query(value =
        "SELECT *, 1 - (embedding <=> CAST(:queryEmbedding AS vector)) as similarity " +
        "FROM penny_knowledge_chunks " +
        "WHERE bot_id = :botId AND tenant_id = :tenantId " +
        "  AND embedding IS NOT NULL " +
        "ORDER BY embedding <=> CAST(:queryEmbedding AS vector) " +
        "LIMIT :topK",
        nativeQuery = true)
    List<PennyKnowledgeChunk> findSimilarByVector(
        @Param("botId") UUID botId,
        @Param("tenantId") Long tenantId,
        @Param("queryEmbedding") String queryEmbedding,
        @Param("topK") int topK);

    /**
     * Vector similarity search with similarity threshold filter
     * Only returns chunks with similarity >= threshold
     */
    @Query(value =
        "SELECT *, 1 - (embedding <=> CAST(:queryEmbedding AS vector)) as similarity " +
        "FROM penny_knowledge_chunks " +
        "WHERE bot_id = :botId AND tenant_id = :tenantId " +
        "  AND embedding IS NOT NULL " +
        "  AND (1 - (embedding <=> CAST(:queryEmbedding AS vector))) >= :threshold " +
        "ORDER BY embedding <=> CAST(:queryEmbedding AS vector) " +
        "LIMIT :topK",
        nativeQuery = true)
    List<PennyKnowledgeChunk> findSimilarByVectorWithThreshold(
        @Param("botId") UUID botId,
        @Param("tenantId") Long tenantId,
        @Param("queryEmbedding") String queryEmbedding,
        @Param("threshold") double threshold,
        @Param("topK") int topK);
}
