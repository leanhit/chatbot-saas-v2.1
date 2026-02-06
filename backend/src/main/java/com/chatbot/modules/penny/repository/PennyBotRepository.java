package com.chatbot.modules.penny.repository;

import com.chatbot.modules.penny.model.PennyBot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Penny Bot Repository
 * Data access layer cho PennyBot entities
 */
@Repository
public interface PennyBotRepository extends JpaRepository<PennyBot, UUID> {
    
    /**
     * Find all bots for a specific tenant
     */
    List<PennyBot> findByTenantId(UUID tenantId);
    
    /**
     * Find all bots for a specific user
     */
    List<PennyBot> findByUserId(Long userId);
    
    /**
     * Find bots for tenant and user
     */
    List<PennyBot> findByTenantIdAndUserId(UUID tenantId, Long userId);
    
    /**
     * Find active bots for tenant
     */
    List<PennyBot> findByTenantIdAndIsActive(UUID tenantId, Boolean isActive);
    
    /**
     * Find enabled bots for tenant
     */
    List<PennyBot> findByTenantIdAndIsEnabled(UUID tenantId, Boolean isEnabled);
    
    /**
     * Find active and enabled bots for tenant
     */
    List<PennyBot> findByTenantIdAndIsActiveAndIsEnabled(UUID tenantId, Boolean isActive, Boolean isEnabled);
    
    /**
     * Find bot by ID and tenant (security check)
     */
    Optional<PennyBot> findByIdAndTenantId(UUID id, UUID tenantId);
    
    /**
     * Check if bot exists for tenant
     */
    boolean existsByTenantIdAndBotName(UUID tenantId, String botName);
    
    /**
     * Find bot by Botpress bot ID
     */
    Optional<PennyBot> findByBotpressBotId(String botpressBotId);
    
    /**
     * Count bots for tenant
     */
    long countByTenantId(UUID tenantId);
    
    /**
     * Count active bots for tenant
     */
    @Query("SELECT COUNT(pb) FROM PennyBot pb WHERE pb.tenantId = :tenantId AND pb.isActive = true")
    long countActiveByTenantId(@Param("tenantId") UUID tenantId);
    
    /**
     * Find bots by type for tenant
     */
    List<PennyBot> findByTenantIdAndBotType(UUID tenantId, String botType);
    
    /**
     * Update last used timestamp
     */
    @Query("UPDATE PennyBot pb SET pb.lastUsedAt = CURRENT_TIMESTAMP WHERE pb.id = :botId")
    void updateLastUsedAt(@Param("botId") UUID botId);
}
