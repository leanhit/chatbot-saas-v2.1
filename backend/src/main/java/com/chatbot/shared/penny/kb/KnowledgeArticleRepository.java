package com.chatbot.shared.penny.kb;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for KnowledgeArticle — supports tenant-scoped queries
 * and keyword-based search (Phase 1: text search, Phase 2: vector search).
 */
@Repository
public interface KnowledgeArticleRepository extends JpaRepository<KnowledgeArticle, UUID> {

    /**
     * Find all active articles for a bot, tenant-scoped, pageable.
     */
    Page<KnowledgeArticle> findByBotIdAndTenantIdAndIsActiveTrue(
        UUID botId, Long tenantId, Pageable pageable);

    /**
     * Find all active articles for a bot by category.
     */
    List<KnowledgeArticle> findByBotIdAndTenantIdAndCategoryAndIsActiveTrue(
        UUID botId, Long tenantId, String category);

    /**
     * Full-text keyword search in title and content, tenant-scoped.
     * Uses ILIKE for PostgreSQL case-insensitive search.
     */
    @Query("SELECT a FROM KnowledgeArticle a " +
           "WHERE a.botId = :botId AND a.tenantId = :tenantId AND a.isActive = true " +
           "AND (LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "  OR LOWER(a.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY a.priority DESC, a.updatedAt DESC")
    List<KnowledgeArticle> searchByKeyword(
        @Param("botId") UUID botId,
        @Param("tenantId") Long tenantId,
        @Param("keyword") String keyword);

    /**
     * Find top-K articles by priority for a bot (simple RAG without vector search).
     * Used as fallback when pgvector is not available.
     */
    @Query("SELECT a FROM KnowledgeArticle a " +
           "WHERE a.botId = :botId AND a.tenantId = :tenantId AND a.isActive = true " +
           "ORDER BY a.priority DESC, a.updatedAt DESC")
    List<KnowledgeArticle> findTopArticles(
        @Param("botId") UUID botId,
        @Param("tenantId") Long tenantId,
        Pageable pageable);

    /**
     * Count articles for a bot (tenant-scoped).
     */
    long countByBotIdAndTenantIdAndIsActiveTrue(UUID botId, Long tenantId);

    /**
     * Delete all articles for a bot.
     */
    void deleteByBotIdAndTenantId(UUID botId, Long tenantId);

    /**
     * Vector similarity search using pgvector cosine distance
     * Returns articles sorted by similarity (most similar first)
     * Only works if pgvector extension is enabled in PostgreSQL
     */
    @Query(value =
        "SELECT *, 1 - (embedding <=> CAST(:queryEmbedding AS vector)) as similarity " +
        "FROM penny_knowledge_articles " +
        "WHERE bot_id = :botId AND tenant_id = :tenantId AND is_active = true " +
        "  AND embedding IS NOT NULL " +
        "ORDER BY embedding <=> CAST(:queryEmbedding AS vector) " +
        "LIMIT :topK",
        nativeQuery = true)
    List<KnowledgeArticle> findSimilarByVector(
        @Param("botId") UUID botId,
        @Param("tenantId") Long tenantId,
        @Param("queryEmbedding") String queryEmbedding,
        @Param("topK") int topK);

    /**
     * Fallback: Find top-K articles by priority when vector search is not available
     */
    @Query(value =
        "SELECT * FROM penny_knowledge_articles " +
        "WHERE bot_id = :botId AND tenant_id = :tenantId AND is_active = true " +
        "ORDER BY priority DESC, updated_at DESC " +
        "LIMIT :topK",
        nativeQuery = true)
    List<KnowledgeArticle> findTopKByPriority(
        @Param("botId") UUID botId,
        @Param("tenantId") Long tenantId,
        @Param("topK") int topK);

    /**
     * Sum total content length for all active knowledge articles of a tenant.
     * Used to estimate storage consumed by the knowledge base.
     */
    @Query("SELECT COALESCE(SUM(LENGTH(ka.content)), 0) FROM KnowledgeArticle ka " +
           "WHERE ka.tenantId = :tenantId AND ka.isActive = true")
    long sumContentLengthByTenantId(@Param("tenantId") Long tenantId);
}
