package com.chatbot.shared.penny.repository;

import com.chatbot.shared.penny.model.PennyBot;
import com.chatbot.shared.penny.model.PennyBotType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for PennyBot entities
 */
@Repository
public interface PennyBotRepository extends JpaRepository<PennyBot, UUID> {

    /**
     * Find active bots by tenant ID
     */
    List<PennyBot> findByTenantIdAndIsActiveTrue(Long tenantId);

    /**
     * Find active bots by owner ID
     */
    List<PennyBot> findByOwnerIdAndIsActiveTrue(String ownerId);

    /**
     * Find bot by tenant ID and bot type
     */
    Optional<PennyBot> findByTenantIdAndBotTypeAndIsActiveTrue(Long tenantId, PennyBotType botType);

    /**
     * Check if tenant already has a bot of specific type
     */
    boolean existsByTenantIdAndBotTypeAndIsActiveTrue(Long tenantId, PennyBotType botType);

    /**
     * Find all active bots for a specific owner
     */
    @Query("SELECT b FROM PennyBot b WHERE b.ownerId = :ownerId AND b.isActive = true ORDER BY b.createdAt DESC")
    List<PennyBot> findActiveBotsByOwner(@Param("ownerId") String ownerId);

    /**
     * Find bot by tenant ID and owner ID
     */
    Optional<PennyBot> findByTenantIdAndOwnerIdAndIsActiveTrue(Long tenantId, String ownerId);

    /**
     * Count active bots by tenant
     */
    @Query("SELECT COUNT(b) FROM PennyBot b WHERE b.tenantId = :tenantId AND b.isActive = true")
    long countActiveBotsByTenant(@Param("tenantId") Long tenantId);

    /**
     * Find bots by botpress bot ID
     */
    List<PennyBot> findByBotpressBotIdAndIsActiveTrue(String botpressBotId);

    /**
     * Hard delete bot (xóa vĩnh viễn)
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM PennyBot b WHERE b.id = :botId")
    void hardDeleteBot(@Param("botId") UUID botId);
}
