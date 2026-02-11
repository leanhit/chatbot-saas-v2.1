package com.chatbot.modules.penny.rules;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ResponseTemplate entities
 */
@Repository
public interface ResponseTemplateRepository extends JpaRepository<ResponseTemplate, UUID> {

    /**
     * Find active templates by bot ID
     */
    List<ResponseTemplate> findByBotIdAndIsActiveTrueOrderByPriorityDesc(UUID botId);

    /**
     * Find templates by bot ID and intent
     */
    List<ResponseTemplate> findByBotIdAndIntentAndIsActiveTrueOrderByPriorityDesc(
            UUID botId, String intent);

    /**
     * Find templates by bot ID and language
     */
    List<ResponseTemplate> findByBotIdAndLanguageAndIsActiveTrueOrderByPriorityDesc(
            UUID botId, String language);

    /**
     * Find templates by bot ID, intent, and language
     */
    List<ResponseTemplate> findByBotIdAndIntentAndLanguageAndIsActiveTrueOrderByPriorityDesc(
            UUID botId, String intent, String language);

    /**
     * Find templates by creator
     */
    List<ResponseTemplate> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    /**
     * Count active templates by bot ID
     */
    long countByBotIdAndIsActiveTrue(UUID botId);

    /**
     * Check if template name exists for bot
     */
    boolean existsByBotIdAndNameAndIsActiveTrue(UUID botId, String name);

    /**
     * Find most used templates
     */
    @Query("SELECT t FROM ResponseTemplate t WHERE t.botId = :botId AND t.isActive = true ORDER BY t.usageCount DESC")
    List<ResponseTemplate> findMostUsedTemplates(@Param("botId") UUID botId);

    /**
     * Soft delete template by setting isActive to false
     */
    @Query("UPDATE ResponseTemplate t SET t.isActive = false, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :templateId")
    void softDeleteTemplate(@Param("templateId") UUID templateId);
}
