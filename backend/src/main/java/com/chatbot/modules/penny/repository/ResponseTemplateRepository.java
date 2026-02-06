package com.chatbot.modules.penny.repository;

import com.chatbot.modules.penny.rules.ResponseTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Response Template Repository
 * Data access layer cho ResponseTemplate entities
 */
@Repository
public interface ResponseTemplateRepository extends JpaRepository<ResponseTemplate, UUID> {
    
    /**
     * Find all templates for a specific bot
     */
    List<ResponseTemplate> findByBotId(UUID botId);
    
    /**
     * Find active templates for a bot
     */
    List<ResponseTemplate> findByBotIdAndIsActive(UUID botId, Boolean isActive);
    
    /**
     * Find templates by intent for a bot
     */
    List<ResponseTemplate> findByBotIdAndIntent(UUID botId, String intent);
    
    /**
     * Find active templates by intent for a bot
     */
    List<ResponseTemplate> findByBotIdAndIntentAndIsActive(UUID botId, String intent, Boolean isActive);
    
    /**
     * Find templates by type for a bot
     */
    List<ResponseTemplate> findByBotIdAndTemplateType(UUID botId, ResponseTemplate.TemplateType templateType);
    
    /**
     * Find templates by language for a bot
     */
    List<ResponseTemplate> findByBotIdAndLanguage(UUID botId, String language);
    
    /**
     * Find templates by priority for a bot
     */
    List<ResponseTemplate> findByBotIdOrderByPriorityDesc(UUID botId);
    
    /**
     * Find active templates by priority for a bot
     */
    List<ResponseTemplate> findByBotIdAndIsActiveOrderByPriorityDesc(UUID botId, Boolean isActive);
    
    /**
     * Find template by ID and bot (security check)
     */
    Optional<ResponseTemplate> findByIdAndBotId(UUID id, UUID botId);
    
    /**
     * Find best template for intent and language
     */
    @Query("SELECT rt FROM ResponseTemplate rt WHERE rt.botId = :botId AND rt.intent = :intent " +
           "AND rt.language = :language AND rt.isActive = true ORDER BY rt.priority DESC")
    Optional<ResponseTemplate> findBestTemplate(@Param("botId") UUID botId, 
                                           @Param("intent") String intent, 
                                           @Param("language") String language);
    
    /**
     * Find templates by creator
     */
    List<ResponseTemplate> findByCreatedBy(String createdBy);
    
    /**
     * Find templates by bot, intent, active status, ordered by priority
     */
    List<ResponseTemplate> findByBotIdAndIntentAndIsActiveOrderByPriorityDesc(UUID botId, String intent, Boolean isActive);
    
    /**
     * Count templates for bot
     */
    long countByBotId(UUID botId);
    
    /**
     * Count active templates for bot
     */
    @Query("SELECT COUNT(rt) FROM ResponseTemplate rt WHERE rt.botId = :botId AND rt.isActive = true")
    long countActiveByBotId(@Param("botId") UUID botId);
    
    /**
     * Update usage count
     */
    @Query("UPDATE ResponseTemplate rt SET rt.usageCount = rt.usageCount + 1, rt.lastUsedAt = CURRENT_TIMESTAMP WHERE rt.id = :templateId")
    void incrementUsageCount(@Param("templateId") UUID templateId);
    
    /**
     * Find templates with quick replies
     */
    List<ResponseTemplate> findByBotIdAndQuickRepliesIsNotNull(UUID botId);
}
