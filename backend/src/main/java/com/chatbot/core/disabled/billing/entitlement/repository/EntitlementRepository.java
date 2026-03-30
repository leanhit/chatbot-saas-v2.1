package com.chatbot.core.billing.entitlement.repository;

import com.chatbot.core.billing.entitlement.model.Entitlement;
import com.chatbot.core.billing.entitlement.model.Feature;
import com.chatbot.core.billing.entitlement.model.UsageLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EntitlementRepository extends JpaRepository<Entitlement, Long> {

    /**
     * Find entitlements by tenant ID
     */
    List<Entitlement> findByTenantId(Long tenantId);

    /**
     * Find entitlements by tenant ID and feature
     */
    Optional<Entitlement> findByTenantIdAndFeature(Long tenantId, Feature feature);

    /**
     * Find entitlements by subscription ID
     */
    List<Entitlement> findBySubscriptionId(Long subscriptionId);

    /**
     * Find enabled entitlements by tenant ID
     */
    @Query("SELECT e FROM Entitlement e WHERE e.tenantId = :tenantId AND e.isEnabled = true")
    List<Entitlement> findEnabledByTenantId(@Param("tenantId") Long tenantId);

    /**
     * Find entitlements by feature
     */
    List<Entitlement> findByFeature(Feature feature);

    /**
     * Find entitlements by usage limit type
     */
    List<Entitlement> findByUsageLimitType(UsageLimit usageLimitType);

    /**
     * Find entitlements at or near limit
     */
    @Query("SELECT e FROM Entitlement e WHERE e.tenantId = :tenantId AND " +
           "e.isEnabled = true AND e.isUnlimited = false AND " +
           "((e.currentUsage >= e.limitValue) OR " +
           "(e.currentUsage * 100.0 / NULLIF(e.limitValue, 0) >= :threshold))")
    List<Entitlement> findEntitlementsAtOrNearLimit(
            @Param("tenantId") Long tenantId, 
            @Param("threshold") double threshold);

    /**
     * Find entitlements over limit
     */
    @Query("SELECT e FROM Entitlement e WHERE e.tenantId = :tenantId AND " +
           "e.isEnabled = true AND e.isUnlimited = false AND " +
           "e.currentUsage > e.limitValue")
    List<Entitlement> findEntitlementsOverLimit(@Param("tenantId") Long tenantId);

    /**
     * Find entitlements needing reset
     */
    @Query("SELECT e FROM Entitlement e WHERE e.resetPeriod IS NOT NULL AND " +
           "e.nextResetAt <= :now")
    List<Entitlement> findEntitlementsNeedingReset(@Param("now") LocalDateTime now);

    /**
     * Get total usage by feature for tenant
     */
    @Query("SELECT COALESCE(SUM(e.currentUsage), 0) FROM Entitlement e WHERE " +
           "e.tenantId = :tenantId AND e.feature = :feature AND e.isEnabled = true")
    Long getTotalUsageByFeature(@Param("tenantId") Long tenantId, @Param("feature") Feature feature);

    /**
     * Get entitlement summary for tenant
     */
    @Query("SELECT e.feature, e.limitValue, e.currentUsage, e.isUnlimited, e.isEnabled FROM Entitlement e WHERE " +
           "e.tenantId = :tenantId AND e.isEnabled = true")
    List<Object[]> getEntitlementSummary(@Param("tenantId") Long tenantId);

    /**
     * Check if tenant has feature enabled
     */
    @Query("SELECT COUNT(e) > 0 FROM Entitlement e WHERE " +
           "e.tenantId = :tenantId AND e.feature = :feature AND e.isEnabled = true")
    boolean hasFeature(@Param("tenantId") Long tenantId, @Param("feature") Feature feature);

    /**
     * Get usage limit for tenant and feature
     */
    @Query("SELECT e.limitValue FROM Entitlement e WHERE " +
           "e.tenantId = :tenantId AND e.usageLimitType = :limitType AND e.isEnabled = true")
    Long getUsageLimit(@Param("tenantId") Long tenantId, @Param("limitType") UsageLimit limitType);

    /**
     * Find entitlements that need warning
     */
    @Query("SELECT e FROM Entitlement e WHERE e.tenantId = :tenantId AND " +
           "e.isEnabled = true AND e.isUnlimited = false AND " +
           "e.currentUsage * 100.0 / NULLIF(e.limitValue, 0) >= e.warningThresholdPercentage AND " +
           "(e.lastWarningAt IS NULL OR e.lastWarningAt < :dayAgo)")
    List<Entitlement> findEntitlementsNeedingWarning(
            @Param("tenantId") Long tenantId, 
            @Param("dayAgo") LocalDateTime dayAgo);
}
