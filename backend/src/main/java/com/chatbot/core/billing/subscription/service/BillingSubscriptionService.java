package com.chatbot.core.billing.subscription.service;

import com.chatbot.core.billing.account.model.BillingAccount;
import com.chatbot.core.billing.account.service.BillingAccountService;
import com.chatbot.core.billing.subscription.dto.SubscriptionRequest;
import com.chatbot.core.billing.subscription.dto.SubscriptionResponse;
import com.chatbot.core.billing.subscription.model.BillingSubscription;
import com.chatbot.core.billing.subscription.model.SubscriptionPlan;
import com.chatbot.core.billing.subscription.model.SubscriptionStatus;
import com.chatbot.core.billing.subscription.repository.BillingSubscriptionRepository;
import com.chatbot.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingSubscriptionService {

    private final BillingSubscriptionRepository subscriptionRepository;
    private final BillingAccountService billingAccountService;

    /**
     * Create new subscription for tenant
     */
    @Transactional
    public SubscriptionResponse createSubscription(Long tenantId, SubscriptionRequest request) {
        log.info("Creating subscription for tenant: {} with plan: {}", tenantId, request.getPlan());

        // Get or create billing account
        BillingAccount billingAccount;
        try {
            billingAccountService.getBillingAccountByTenant(tenantId);
            billingAccount = null; // Will be set properly below
        } catch (ResourceNotFoundException e) {
            // Create default billing account
            billingAccount = null;
        }

        BillingSubscription subscription = BillingSubscription.builder()
                .plan(request.getPlan())
                .planName(request.getPlanName())
                .planDescription(request.getPlanDescription())
                .billingCycle(request.getBillingCycle())
                .price(request.getPrice())
                .currency(request.getCurrency())
                .autoRenew(request.getAutoRenew())
                .maxUsers(request.getMaxUsers())
                .maxStorageMb(request.getMaxStorageMb())
                .maxApiCallsPerMonth(request.getMaxApiCallsPerMonth())
                .build();

        // Set tenantId separately since it's inherited from BaseTenantEntity
        subscription.setTenantId(tenantId);

        // Set trial period if enabled
        if (request.getEnableTrial() && request.getTrialDays() != null && request.getTrialDays() > 0) {
            LocalDateTime now = LocalDateTime.now();
            subscription.setTrialStart(now);
            subscription.setTrialEnd(now.plusDays(request.getTrialDays()));
            subscription.setStatus(SubscriptionStatus.TRIAL);
            subscription.setStartsAt(now);
        } else {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setStartsAt(LocalDateTime.now());
        }

        // Set end date based on billing cycle
        LocalDateTime startDate = subscription.getStartsAt();
        if ("MONTHLY".equals(request.getBillingCycle())) {
            subscription.setEndsAt(startDate.plusMonths(1));
        } else if ("YEARLY".equals(request.getBillingCycle())) {
            subscription.setEndsAt(startDate.plusYears(1));
        }

        // Set next billing date
        subscription.setNextBillingDate(subscription.getEndsAt());

        BillingSubscription saved = subscriptionRepository.save(subscription);
        log.info("Created subscription: {} for tenant: {}", saved.getSubscriptionNumber(), tenantId);

        return mapToResponse(saved);
    }

    /**
     * Get subscription by tenant ID
     */
    public SubscriptionResponse getSubscriptionByTenant(Long tenantId) {
        BillingSubscription subscription = subscriptionRepository.findByTenantId(tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found for tenant: " + tenantId));
        return mapToResponse(subscription);
    }

    /**
     * Get subscription by ID
     */
    public SubscriptionResponse getSubscriptionById(Long id) {
        BillingSubscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));
        return mapToResponse(subscription);
    }

    /**
     * Update subscription
     */
    @Transactional
    public SubscriptionResponse updateSubscription(Long id, SubscriptionRequest request) {
        BillingSubscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));

        subscription.setPlan(request.getPlan());
        subscription.setPlanName(request.getPlanName());
        subscription.setPlanDescription(request.getPlanDescription());
        subscription.setBillingCycle(request.getBillingCycle());
        subscription.setPrice(request.getPrice());
        subscription.setCurrency(request.getCurrency());
        subscription.setAutoRenew(request.getAutoRenew());
        subscription.setMaxUsers(request.getMaxUsers());
        subscription.setMaxStorageMb(request.getMaxStorageMb());
        subscription.setMaxApiCallsPerMonth(request.getMaxApiCallsPerMonth());

        BillingSubscription updated = subscriptionRepository.save(subscription);
        log.info("Updated subscription: {}", updated.getSubscriptionNumber());

        return mapToResponse(updated);
    }

    /**
     * Cancel subscription
     */
    @Transactional
    public void cancelSubscription(Long id, String reason) {
        BillingSubscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(LocalDateTime.now());
        subscription.setCancellationReason(reason);
        subscription.setAutoRenew(false);

        subscriptionRepository.save(subscription);
        log.info("Cancelled subscription: {} for reason: {}", subscription.getSubscriptionNumber(), reason);
    }

    /**
     * Suspend subscription
     */
    @Transactional
    public void suspendSubscription(Long id, String reason) {
        BillingSubscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));

        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        subscription.setSuspendedAt(LocalDateTime.now());
        subscription.setSuspensionReason(reason);

        subscriptionRepository.save(subscription);
        log.info("Suspended subscription: {} for reason: {}", subscription.getSubscriptionNumber(), reason);
    }

    /**
     * Reactivate subscription
     */
    @Transactional
    public void reactivateSubscription(Long id) {
        BillingSubscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setSuspendedAt(null);
        subscription.setSuspensionReason(null);

        subscriptionRepository.save(subscription);
        log.info("Reactivated subscription: {}", subscription.getSubscriptionNumber());
    }

    /**
     * Process subscription renewal
     */
    @Transactional
    public void processRenewal(Long id) {
        BillingSubscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found: " + id));

        LocalDateTime newEndDate;
        if ("MONTHLY".equals(subscription.getBillingCycle())) {
            newEndDate = subscription.getEndsAt().plusMonths(1);
        } else if ("YEARLY".equals(subscription.getBillingCycle())) {
            newEndDate = subscription.getEndsAt().plusYears(1);
        } else {
            throw new IllegalArgumentException("Invalid billing cycle: " + subscription.getBillingCycle());
        }

        subscription.setEndsAt(newEndDate);
        subscription.setNextBillingDate(newEndDate);
        subscription.setLastBillingDate(LocalDateTime.now());

        subscriptionRepository.save(subscription);
        log.info("Renewed subscription: {} until {}", subscription.getSubscriptionNumber(), newEndDate);
    }

    /**
     * Search subscriptions
     */
    public Page<SubscriptionResponse> searchSubscriptions(String keyword, Pageable pageable) {
        return subscriptionRepository.searchSubscriptions(keyword, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Get active subscriptions
     */
    public List<SubscriptionResponse> getActiveSubscriptions() {
        return subscriptionRepository.findActiveSubscriptions().stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get expired subscriptions
     */
    public List<SubscriptionResponse> getExpiredSubscriptions() {
        return subscriptionRepository.findExpiredSubscriptions(LocalDateTime.now()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get subscriptions ending soon
     */
    public List<SubscriptionResponse> getSubscriptionsEndingSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekLater = now.plusDays(7);
        return subscriptionRepository.findSubscriptionsEndingSoon(now, weekLater).stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get subscriptions needing billing
     */
    public List<SubscriptionResponse> getSubscriptionsNeedingBilling() {
        return subscriptionRepository.findSubscriptionsNeedingBilling(LocalDateTime.now()).stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get trials ending soon
     */
    public List<SubscriptionResponse> getTrialsEndingSoon() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime weekLater = now.plusDays(7);
        return subscriptionRepository.findTrialsEndingSoon(now, weekLater).stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get monthly recurring revenue
     */
    public BigDecimal getMonthlyRecurringRevenue() {
        return subscriptionRepository.getMonthlyRecurringRevenue();
    }

    /**
     * Get yearly recurring revenue
     */
    public BigDecimal getYearlyRecurringRevenue() {
        return subscriptionRepository.getYearlyRecurringRevenue();
    }

    private SubscriptionResponse mapToResponse(BillingSubscription subscription) {
        return SubscriptionResponse.builder()
                .id((Long) subscription.getId())
                .subscriptionNumber(subscription.getSubscriptionNumber())
                .billingAccountId(subscription.getBillingAccount() != null ? (Long) subscription.getBillingAccount().getId() : null)
                .plan(subscription.getPlan())
                .status(subscription.getStatus())
                .planName(subscription.getPlanName())
                .planDescription(subscription.getPlanDescription())
                .billingCycle(subscription.getBillingCycle())
                .price(subscription.getPrice())
                .currency(subscription.getCurrency())
                .trialStart(subscription.getTrialStart())
                .trialEnd(subscription.getTrialEnd())
                .startsAt(subscription.getStartsAt())
                .endsAt(subscription.getEndsAt())
                .autoRenew(subscription.getAutoRenew())
                .cancelledAt(subscription.getCancelledAt())
                .cancellationReason(subscription.getCancellationReason())
                .suspendedAt(subscription.getSuspendedAt())
                .suspensionReason(subscription.getSuspensionReason())
                .lastBillingDate(subscription.getLastBillingDate())
                .nextBillingDate(subscription.getNextBillingDate())
                .maxUsers(subscription.getMaxUsers())
                .maxStorageMb(subscription.getMaxStorageMb())
                .maxApiCallsPerMonth(subscription.getMaxApiCallsPerMonth())
                .isTrial(subscription.isTrial())
                .isActive(subscription.isActive())
                .isExpired(subscription.isExpired())
                .trialDaysRemaining(subscription.getTrialDaysRemaining())
                .daysUntilExpiry(subscription.getDaysUntilExpiry())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .tenantId(subscription.getTenantId())
                .build();
    }
}
