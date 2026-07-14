package com.chatbot.shared.penny.controller;

import com.chatbot.shared.penny.kb.EmbeddingService;
import com.chatbot.shared.penny.kb.KnowledgeArticle;
import com.chatbot.shared.penny.kb.KnowledgeArticleRepository;
import com.chatbot.shared.penny.kb.KnowledgeBaseSearchService;
import com.chatbot.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * KnowledgeBaseController — Admin REST API for managing knowledge base articles
 *
 * Provides CRUD operations for knowledge articles used in RAG pipeline.
 * Requires ADMIN or TENANT_ADMIN role.
 */
@RestController
@RequestMapping("/api/penny/bots/{botId}/kb")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'TENANT_ADMIN')")
public class KnowledgeBaseController {

    private final KnowledgeArticleRepository knowledgeArticleRepository;
    private final EmbeddingService embeddingService;
    private final KnowledgeBaseSearchService knowledgeBaseSearchService;

    /**
     * Create a new knowledge article
     * POST /api/penny/bots/{botId}/kb/articles
     */
    @PostMapping("/articles")
    public ResponseEntity<KnowledgeArticle> createArticle(
            @PathVariable UUID botId,
            @Valid @RequestBody KnowledgeArticle article) {
        
        log.info("📝 Creating knowledge article for bot: {}", botId);
        
        article.setBotId(botId);
        article.setId(UUID.randomUUID()); // Generate new ID
        
        // Generate embedding if service is enabled
        if (embeddingService != null && embeddingService.isEnabled()) {
            try {
                String textToEmbed = article.getTitle() + " " + article.getContent();
                float[] embedding = embeddingService.generateEmbedding(textToEmbed);
                if (embedding != null) {
                    article.setEmbedding(embedding);
                    article.setEmbeddingModel(embeddingService.getEmbeddingModel());
                    log.debug("✅ Generated embedding for article");
                }
            } catch (Exception e) {
                log.warn("⚠️ Failed to generate embedding for article: {}", e.getMessage());
            }
        }
        
        KnowledgeArticle saved = knowledgeArticleRepository.save(article);
        log.info("✅ Created knowledge article: {}", saved.getId());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * Get all knowledge articles for a bot (paginated)
     * GET /api/penny/bots/{botId}/kb/articles?page=0&size=20
     */
    @GetMapping("/articles")
    public ResponseEntity<Page<KnowledgeArticle>> getArticles(
            @PathVariable UUID botId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        log.debug("📚 Fetching knowledge articles for bot: {} (page: {}, size: {})", botId, page, size);
        
        Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        // Get tenant ID from security context
        Long tenantId = SecurityUtils.getCurrentTenantId()
            .orElseThrow(() -> new IllegalStateException("Tenant ID not found in security context"));
        
        Page<KnowledgeArticle> articles = knowledgeArticleRepository
            .findByBotIdAndTenantIdAndIsActiveTrue(botId, tenantId, pageable);
        
        return ResponseEntity.ok(articles);
    }

    /**
     * Get a specific knowledge article by ID
     * GET /api/penny/bots/{botId}/kb/articles/{id}
     */
    @GetMapping("/articles/{id}")
    public ResponseEntity<KnowledgeArticle> getArticle(
            @PathVariable UUID botId,
            @PathVariable UUID id) {
        
        log.debug("📖 Fetching knowledge article: {} for bot: {}", id, botId);
        
        return knowledgeArticleRepository.findById(id)
            .filter(article -> article.getBotId().equals(botId))
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Update a knowledge article
     * PUT /api/penny/bots/{botId}/kb/articles/{id}
     */
    @PutMapping("/articles/{id}")
    public ResponseEntity<KnowledgeArticle> updateArticle(
            @PathVariable UUID botId,
            @PathVariable UUID id,
            @Valid @RequestBody KnowledgeArticle updatedArticle) {
        
        log.info("✏️ Updating knowledge article: {} for bot: {}", id, botId);
        
        return knowledgeArticleRepository.findById(id)
            .filter(article -> article.getBotId().equals(botId))
            .map(existingArticle -> {
                // Update fields
                existingArticle.setTitle(updatedArticle.getTitle());
                existingArticle.setContent(updatedArticle.getContent());
                existingArticle.setCategory(updatedArticle.getCategory());
                existingArticle.setTags(updatedArticle.getTags());
                existingArticle.setSourceUrl(updatedArticle.getSourceUrl());
                existingArticle.setActive(updatedArticle.isActive());
                existingArticle.setPriority(updatedArticle.getPriority());
                
                // Re-generate embedding if content changed
                if (embeddingService != null && embeddingService.isEnabled()) {
                    try {
                        String textToEmbed = existingArticle.getTitle() + " " + existingArticle.getContent();
                        float[] embedding = embeddingService.generateEmbedding(textToEmbed);
                        if (embedding != null) {
                            existingArticle.setEmbedding(embedding);
                            existingArticle.setEmbeddingModel(embeddingService.getEmbeddingModel());
                            log.debug("✅ Re-generated embedding for updated article");
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ Failed to re-generate embedding: {}", e.getMessage());
                    }
                }
                
                KnowledgeArticle saved = knowledgeArticleRepository.save(existingArticle);
                log.info("✅ Updated knowledge article: {}", saved.getId());
                return ResponseEntity.ok(saved);
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Delete a knowledge article
     * DELETE /api/penny/bots/{botId}/kb/articles/{id}
     */
    @DeleteMapping("/articles/{id}")
    public ResponseEntity<Void> deleteArticle(
            @PathVariable UUID botId,
            @PathVariable UUID id) {
        
        log.info("🗑️ Deleting knowledge article: {} for bot: {}", id, botId);
        
        return knowledgeArticleRepository.findById(id)
            .filter(article -> article.getBotId().equals(botId))
            .map(article -> {
                knowledgeArticleRepository.deleteById(id);
                log.info("✅ Deleted knowledge article: {}", id);
                return ResponseEntity.noContent().<Void>build();
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Import multiple knowledge articles in bulk
     * POST /api/penny/bots/{botId}/kb/import
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importArticles(
            @PathVariable UUID botId,
            @RequestBody List<@Valid KnowledgeArticle> articles) {
        
        log.info("📥 Importing {} knowledge articles for bot: {}", articles.size(), botId);

        Long tenantId = SecurityUtils.getCurrentTenantId()
            .orElseThrow(() -> new IllegalStateException("Tenant ID not found in security context"));
        int successCount = 0;
        int failureCount = 0;
        
        for (KnowledgeArticle article : articles) {
            try {
                article.setBotId(botId);
                article.setTenantId(tenantId);
                article.setId(UUID.randomUUID());
                
                // Generate embedding
                if (embeddingService != null && embeddingService.isEnabled()) {
                    String textToEmbed = article.getTitle() + " " + article.getContent();
                    float[] embedding = embeddingService.generateEmbedding(textToEmbed);
                    if (embedding != null) {
                        article.setEmbedding(embedding);
                        article.setEmbeddingModel(embeddingService.getEmbeddingModel());
                    }
                }
                
                knowledgeArticleRepository.save(article);
                successCount++;
            } catch (Exception e) {
                log.error("❌ Failed to import article: {}", e.getMessage());
                failureCount++;
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("total", articles.size());
        result.put("success", successCount);
        result.put("failed", failureCount);
        
        log.info("✅ Import completed: {} success, {} failed", successCount, failureCount);
        return ResponseEntity.ok(result);
    }

    /**
     * Re-generate embedding for a specific article
     * POST /api/penny/bots/{botId}/kb/articles/{id}/reembed
     */
    @PostMapping("/articles/{id}/reembed")
    public ResponseEntity<KnowledgeArticle> reembedArticle(
            @PathVariable UUID botId,
            @PathVariable UUID id) {
        
        log.info("🔄 Re-generating embedding for article: {} for bot: {}", id, botId);
        
        if (embeddingService == null || !embeddingService.isEnabled()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }
        
        return knowledgeArticleRepository.findById(id)
            .filter(article -> article.getBotId().equals(botId))
            .map(article -> {
                try {
                    String textToEmbed = article.getTitle() + " " + article.getContent();
                    float[] embedding = embeddingService.generateEmbedding(textToEmbed);
                    if (embedding != null) {
                        article.setEmbedding(embedding);
                        article.setEmbeddingModel(embeddingService.getEmbeddingModel());
                        KnowledgeArticle saved = knowledgeArticleRepository.save(article);
                        log.info("✅ Re-generated embedding for article: {}", id);
                        return ResponseEntity.ok(saved);
                    }
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<KnowledgeArticle>build();
                } catch (Exception e) {
                    log.error("❌ Failed to re-generate embedding: {}", e.getMessage());
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).<KnowledgeArticle>build();
                }
            })
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Test search endpoint (admin only)
     * GET /api/penny/bots/{botId}/kb/search?q=...
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> testSearch(
            @PathVariable UUID botId,
            @RequestParam String q) {
        
        log.debug("🔍 Testing KB search for bot: {} with query: {}", botId, q);

        if (knowledgeBaseSearchService == null || !knowledgeBaseSearchService.isEnabled()) {
            Map<String, Object> result = new HashMap<>();
            result.put("error", "Knowledge base search is not enabled");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(result);
        }

        Long tenantId = SecurityUtils.getCurrentTenantId()
            .orElseThrow(() -> new IllegalStateException("Tenant ID not found in security context"));
        List<KnowledgeArticle> articles = knowledgeBaseSearchService.search(botId, tenantId, q);
        
        Map<String, Object> result = new HashMap<>();
        result.put("query", q);
        result.put("count", articles.size());
        result.put("articles", articles);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Get statistics for knowledge base
     * GET /api/penny/bots/{botId}/kb/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(
            @PathVariable UUID botId) {
        
        log.debug("📊 Fetching KB stats for bot: {}", botId);

        Long tenantId = SecurityUtils.getCurrentTenantId()
            .orElseThrow(() -> new IllegalStateException("Tenant ID not found in security context"));
        long totalArticles = knowledgeArticleRepository.countByBotIdAndTenantIdAndIsActiveTrue(botId, tenantId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalArticles", totalArticles);
        stats.put("ragEnabled", knowledgeBaseSearchService != null && knowledgeBaseSearchService.isEnabled());
        stats.put("embeddingEnabled", embeddingService != null && embeddingService.isEnabled());
        
        if (embeddingService != null && embeddingService.isEnabled()) {
            stats.put("embeddingModel", embeddingService.getEmbeddingModel());
            stats.put("embeddingDimensions", embeddingService.getEmbeddingDimensions());
        }
        
        return ResponseEntity.ok(stats);
    }
}
