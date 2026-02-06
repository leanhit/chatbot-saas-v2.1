package com.chatbot.modules.penny.repository;

import com.chatbot.modules.penny.rules.BotRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Bot Rule Repository
 * Data access layer cho BotRule entities
 */
@Repository
public interface BotRuleRepository extends JpaRepository<BotRule, UUID> {
    
    /**
     * Find all rules for a specific bot
     */
    List<BotRule> findByBotId(UUID botId);
    
    /**
     * Find active rules for a bot
     */
    List<BotRule> findByBotIdAndIsActive(UUID botId, Boolean isActive);
    
    /**
     * Find rules by type for a bot
     */
    List<BotRule> findByBotIdAndRuleType(UUID botId, BotRule.RuleType ruleType);
    
    /**
     * Find rules by trigger type for a bot
     */
    List<BotRule> findByBotIdAndTriggerType(UUID botId, BotRule.TriggerType triggerType);
    
    /**
     * Find rules by priority for a bot
     */
    List<BotRule> findByBotIdOrderByPriorityDesc(UUID botId);
    
    /**
     * Find active rules by priority for a bot
     */
    List<BotRule> findByBotIdAndIsActiveOrderByPriorityDesc(UUID botId, Boolean isActive);
    
    /**
     * Find rule by ID and bot (security check)
     */
    Optional<BotRule> findByIdAndBotId(UUID id, UUID botId);
    
    /**
     * Find rules matching specific trigger
     */
    @Query(value = "SELECT * FROM bot_rules br WHERE br.bot_id = :botId AND br.is_active = true " +
           "AND ((br.trigger_type = 'INTENT' AND br.trigger_value = :triggerValue) " +
           "OR (br.trigger_type = 'KEYWORD' AND :message LIKE '%' || br.trigger_value || '%') " +
           "OR (br.trigger_type = 'REGEX' AND :message ~ br.trigger_value) " +
           "OR (br.trigger_type = 'ALWAYS') " +
           "OR (br.trigger_type = 'CONDITION' AND br.condition IS NOT NULL))",
           nativeQuery = true)
    List<BotRule> findMatchingRules(@Param("botId") UUID botId, 
                                   @Param("triggerValue") String triggerValue, 
                                   @Param("message") String message);
    
    /**
     * Count rules for bot
     */
    long countByBotId(UUID botId);
    
    /**
     * Count active rules for bot
     */
    @Query("SELECT COUNT(br) FROM BotRule br WHERE br.botId = :botId AND br.isActive = true")
    long countActiveByBotId(@Param("botId") UUID botId);
    
    /**
     * Find rules by creator
     */
    List<BotRule> findByCreatedBy(String createdBy);
    
    /**
     * Update execution count
     */
    @Query("UPDATE BotRule br SET br.executionCount = br.executionCount + 1, br.lastExecutedAt = CURRENT_TIMESTAMP WHERE br.id = :ruleId")
    void incrementExecutionCount(@Param("ruleId") UUID ruleId);
}
