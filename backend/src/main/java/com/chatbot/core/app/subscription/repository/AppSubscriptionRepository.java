package com.chatbot.core.app.subscription.repository;

import com.chatbot.core.app.subscription.model.AppSubscription;
import com.chatbot.core.app.subscription.model.SubscriptionPlan;
import com.chatbot.core.app.subscription.model.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AppSubscriptionRepository extends JpaRepository<AppSubscription, Long> {
    
    Optional<AppSubscription> findByAppIdAndTenantId(Long appId, Long tenantId);
    
    List<AppSubscription> findByTenantId(Long tenantId);
    
    List<AppSubscription> findByUserId(Long userId);
    
    List<AppSubscription> findByAppId(Long appId);
    
    List<AppSubscription> findBySubscriptionStatus(SubscriptionStatus status);
    
    List<AppSubscription> findBySubscriptionPlan(SubscriptionPlan plan);
    
    List<AppSubscription> findByTenantIdAndSubscriptionStatus(Long tenantId, SubscriptionStatus status);
    
    List<AppSubscription> findByAppIdAndSubscriptionStatus(Long appId, SubscriptionStatus status);
    
    @Query("SELECT s FROM AppSubscription s WHERE s.subscriptionEnd <= :now AND s.subscriptionStatus = :status")
    List<AppSubscription> findExpiredSubscriptions(@Param("now") LocalDateTime now, @Param("status") SubscriptionStatus status);
    
    @Query("SELECT s FROM AppSubscription s WHERE s.trialEnd <= :now AND s.subscriptionStatus = :status")
    List<AppSubscription> findExpiredTrials(@Param("now") LocalDateTime now, @Param("status") SubscriptionStatus status);
    
    @Query("SELECT COUNT(s) FROM AppSubscription s WHERE s.appId = :appId AND s.subscriptionStatus = :status")
    long countByAppIdAndSubscriptionStatus(@Param("appId") Long appId, @Param("status") SubscriptionStatus status);
    
    @Query("SELECT COUNT(s) FROM AppSubscription s WHERE s.tenantId = :tenantId AND s.subscriptionStatus = :status")
    long countByTenantIdAndSubscriptionStatus(@Param("tenantId") Long tenantId, @Param("status") SubscriptionStatus status);
    
    boolean existsByAppIdAndTenantId(Long appId, Long tenantId);
    
    @Query("SELECT s FROM AppSubscription s WHERE s.autoRenew = true AND s.subscriptionEnd <= :now AND s.subscriptionStatus = :status")
    List<AppSubscription> findSubscriptionsForAutoRenewal(@Param("now") LocalDateTime now, @Param("status") SubscriptionStatus status);
}
