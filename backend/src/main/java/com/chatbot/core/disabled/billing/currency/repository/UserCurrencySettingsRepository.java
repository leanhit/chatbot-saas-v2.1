package com.chatbot.core.billing.currency.repository;

import com.chatbot.core.billing.currency.model.UserCurrencySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCurrencySettingsRepository extends JpaRepository<UserCurrencySettings, Long> {

    /**
     * Find user currency settings by user ID and tenant ID
     */
    Optional<UserCurrencySettings> findByUserIdAndTenantId(Long userId, Long tenantId);

    /**
     * Find all currency settings for a tenant
     */
    List<UserCurrencySettings> findByTenantId(Long tenantId);

    /**
     * Find all currency settings for a user across all tenants
     */
    List<UserCurrencySettings> findByUserId(Long userId);

    /**
     * Check if user has currency settings for a tenant
     */
    boolean existsByUserIdAndTenantId(Long userId, Long tenantId);

    /**
     * Delete user currency settings by user and tenant
     */
    void deleteByUserIdAndTenantId(Long userId, Long tenantId);

    /**
     * Count users by display currency within a tenant
     */
    @Query("SELECT COUNT(ucs) FROM UserCurrencySettings ucs WHERE ucs.tenantId = :tenantId AND ucs.displayCurrency = :currency")
    long countUsersByDisplayCurrency(@Param("tenantId") Long tenantId, @Param("currency") String currency);
}
