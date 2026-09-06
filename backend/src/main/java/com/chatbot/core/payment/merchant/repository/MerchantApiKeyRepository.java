package com.chatbot.core.payment.merchant.repository;

import com.chatbot.core.payment.merchant.model.MerchantApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MerchantApiKeyRepository extends JpaRepository<MerchantApiKey, Long> {

    Optional<MerchantApiKey> findByApiKey(String apiKey);

    List<MerchantApiKey> findByTenantId(Long tenantId);

    @Query("SELECT m FROM MerchantApiKey m WHERE m.isActive = true AND (m.expiresAt IS NULL OR m.expiresAt > :now)")
    List<MerchantApiKey> findActiveApiKeys(@Param("now") LocalDateTime now);

    @Query("SELECT m FROM MerchantApiKey m WHERE m.tenantId = :tenantId AND m.isActive = true AND (m.expiresAt IS NULL OR m.expiresAt > :now)")
    Optional<MerchantApiKey> findActiveApiKeyByTenantId(@Param("tenantId") Long tenantId, @Param("now") LocalDateTime now);

    boolean existsByApiKey(String apiKey);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional(value = "sharedTransactionManager", rollbackFor = Exception.class)
    void deleteByApiKey(String apiKey);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional(value = "sharedTransactionManager", rollbackFor = Exception.class)
    void deleteByTenantId(Long tenantId);
}
