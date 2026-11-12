package com.chatbot.core.penny.repository;

import com.chatbot.core.penny.model.PennyConversationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PennyConversationContextRepository extends JpaRepository<PennyConversationContext, UUID> {

    Optional<PennyConversationContext> findByContextKey(String contextKey);

    Optional<PennyConversationContext> findByTenantIdAndBotIdAndUserIdAndPlatform(
        Long tenantId, String botId, String userId, String platform);

    Optional<PennyConversationContext> findFirstByTenantIdAndBotIdOrderByLastUpdatedDesc(Long tenantId, String botId);

    List<PennyConversationContext> findByTenantIdAndBotId(Long tenantId, String botId);

    List<PennyConversationContext> findByTenantIdAndStatus(Long tenantId, String status);

    List<PennyConversationContext> findByTenantIdAndUserIdAndPlatform(Long tenantId, String userId, String platform);

    List<PennyConversationContext> findByExpiresAtBefore(Instant instant);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndStatus(Long tenantId, String status);

    @Query("SELECT COALESCE(SUM(c.messageCount), 0) FROM PennyConversationContext c WHERE c.tenantId = :tenantId")
    long sumMessageCountByTenantId(@Param("tenantId") Long tenantId);

    @Query("SELECT MAX(c.lastUpdated) FROM PennyConversationContext c WHERE c.tenantId = :tenantId")
    Optional<Instant> findLastUpdatedByTenantId(@Param("tenantId") Long tenantId);

    @Modifying
    @Transactional
    void deleteByContextKey(String contextKey);

    @Modifying
    @Transactional
    void deleteByTenantIdAndBotId(Long tenantId, String botId);
}
