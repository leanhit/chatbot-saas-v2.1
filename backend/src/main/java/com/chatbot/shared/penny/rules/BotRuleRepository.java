package com.chatbot.shared.penny.rules;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for BotRule entities
 */
@Repository
public interface BotRuleRepository extends JpaRepository<BotRule, UUID> {

    /**
     * Find active rules by bot ID
     */
    List<BotRule> findByBotIdAndIsActiveTrueOrderByPriorityDesc(UUID botId);

    /**
     * Find rules by bot ID and trigger type
     */
    List<BotRule> findByBotIdAndTriggerTypeAndIsActiveTrueOrderByPriorityDesc(
            UUID botId, BotRule.TriggerType triggerType);

    /**
     * Find rules by bot ID and trigger value
     */
    List<BotRule> findByBotIdAndTriggerValueAndIsActiveTrueOrderByPriorityDesc(
            UUID botId, String triggerValue);

    /**
     * Find rules by bot ID and intent
     */
    @Query("SELECT r FROM BotRule r WHERE r.botId = :botId AND r.triggerType = 'INTENT' AND r.triggerValue = :intent AND r.isActive = true ORDER BY r.priority DESC")
    List<BotRule> findByBotIdAndIntent(@Param("botId") UUID botId, @Param("intent") String intent);

    /**
     * Find keyword rules for a message
     */
    @Query("SELECT r FROM BotRule r WHERE r.botId = :botId AND r.triggerType = 'KEYWORD' AND r.isActive = true ORDER BY r.priority DESC")
    List<BotRule> findKeywordRulesByBotId(@Param("botId") UUID botId);

    /**
     * Find rules by creator
     */
    List<BotRule> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    /**
     * Count active rules by bot ID
     */
    long countByBotIdAndIsActiveTrue(UUID botId);

    /**
     * Check if rule name exists for bot
     */
    boolean existsByBotIdAndNameAndIsActiveTrue(UUID botId, String name);

    /**
     * Soft delete rule by setting isActive to false
     */
    @Query("UPDATE BotRule r SET r.isActive = false, r.updatedAt = CURRENT_TIMESTAMP WHERE r.id = :ruleId")
    void softDeleteRule(@Param("ruleId") UUID ruleId);
}
