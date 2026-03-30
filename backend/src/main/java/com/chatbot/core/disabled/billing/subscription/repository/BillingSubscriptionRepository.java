package com.chatbot.core.billing.subscription.repository;

import com.chatbot.core.billing.subscription.model.BillingSubscription;
import com.chatbot.core.billing.subscription.model.SubscriptionPlan;
import com.chatbot.core.billing.subscription.model.SubscriptionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BillingSubscriptionRepository extends JpaRepository<BillingSubscription, Long> {

    /**
     * Find subscription by tenant ID
     */
    Optional<BillingSubscription> findByTenantId(Long tenantId);

    /**
     * Find active subscription by tenant ID
     */
    Optional<BillingSubscription> findByTenantIdAndStatus(Long tenantId, SubscriptionStatus status);

    /**
     * Find subscription by subscription number
     */
    Optional<BillingSubscription> findBySubscriptionNumber(String subscriptionNumber);

    /**
     * Check if subscription exists for tenant
     */
    boolean existsByTenantId(Long tenantId);

    /**
     * Find subscriptions by plan
     */
    List<BillingSubscription> findByPlan(SubscriptionPlan plan);

    /**
     * Find subscriptions by status
     */
    List<BillingSubscription> findByStatus(SubscriptionStatus status);

    /**
     * Find active subscriptions
     */
    @Query("SELECT s FROM BillingSubscription s WHERE s.status IN ('ACTIVE', 'TRIAL')")
    List<BillingSubscription> findActiveSubscriptions();

    /**
     * Find expired subscriptions
     */
    @Query("SELECT s FROM BillingSubscription s WHERE s.endsAt < :now AND s.status NOT IN ('CANCELLED', 'EXPIRED')")
    List<BillingSubscription> findExpiredSubscriptions(@Param("now") LocalDateTime now);

    /**
     * Find subscriptions ending soon (within 7 days)
     */
    @Query("SELECT s FROM BillingSubscription s WHERE s.endsAt BETWEEN :now AND :weekLater AND s.status IN ('ACTIVE', 'TRIAL')")
    List<BillingSubscription> findSubscriptionsEndingSoon(@Param("now") LocalDateTime now, @Param("weekLater") LocalDateTime weekLater);

    /**
     * Find subscriptions needing billing
     */
    @Query("SELECT s FROM BillingSubscription s WHERE s.nextBillingDate <= :now AND s.autoRenew = true AND s.status IN ('ACTIVE', 'TRIAL')")
    List<BillingSubscription> findSubscriptionsNeedingBilling(@Param("now") LocalDateTime now);

    /**
     * Find trial subscriptions ending soon
     */
    @Query("SELECT s FROM BillingSubscription s WHERE s.trialEnd BETWEEN :now AND :weekLater AND s.status = 'TRIAL'")
    List<BillingSubscription> findTrialsEndingSoon(@Param("now") LocalDateTime now, @Param("weekLater") LocalDateTime weekLater);

    /**
     * Count subscriptions by plan
     */
    @Query("SELECT COUNT(s) FROM BillingSubscription s WHERE s.plan = :plan")
    long countByPlan(@Param("plan") SubscriptionPlan plan);

    /**
     * Count subscriptions by status
     */
    @Query("SELECT COUNT(s) FROM BillingSubscription s WHERE s.status = :status")
    long countByStatus(@Param("status") SubscriptionStatus status);

    /**
     * Get monthly recurring revenue (MRR)
     */
    @Query("SELECT COALESCE(SUM(s.price), 0) FROM BillingSubscription s WHERE s.status IN ('ACTIVE', 'TRIAL') AND s.billingCycle = 'MONTHLY'")
    java.math.BigDecimal getMonthlyRecurringRevenue();

    /**
     * Get yearly recurring revenue (ARR)
     */
    @Query("SELECT COALESCE(SUM(s.price), 0) FROM BillingSubscription s WHERE s.status IN ('ACTIVE', 'TRIAL') AND s.billingCycle = 'YEARLY'")
    java.math.BigDecimal getYearlyRecurringRevenue();

    /**
     * Search subscriptions by plan name or tenant
     */
    @Query("SELECT s FROM BillingSubscription s WHERE " +
           "LOWER(s.planName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.subscriptionNumber) LIKE CONCAT('%', :keyword, '%')")
    Page<BillingSubscription> searchSubscriptions(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find subscriptions by billing account
     */
    List<BillingSubscription> findByBillingAccountId(Long billingAccountId);
}
