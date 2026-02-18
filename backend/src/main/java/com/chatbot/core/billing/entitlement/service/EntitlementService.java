package com.chatbot.core.billing.entitlement.service;

import com.chatbot.core.billing.entitlement.model.Entitlement;
import com.chatbot.core.billing.entitlement.model.Feature;
import com.chatbot.core.billing.entitlement.model.UsageLimit;
import com.chatbot.core.billing.entitlement.repository.EntitlementRepository;
import com.chatbot.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EntitlementService {

    private final EntitlementRepository entitlementRepository;

    /**
     * Create new entitlement
     */
    @Transactional
    public Entitlement createEntitlement(Entitlement entitlement) {
        log.info("Creating entitlement for tenant: {}, feature: {}", 
                entitlement.getTenantId(), entitlement.getFeature());

        Entitlement saved = entitlementRepository.save(entitlement);
        log.info("Created entitlement: {}", saved.getId());
        return saved;
    }

    /**
     * Get entitlements by tenant ID
     */
    public List<Entitlement> getEntitlementsByTenant(Long tenantId) {
        return entitlementRepository.findByTenantId(tenantId);
    }

    /**
     * Get enabled entitlements by tenant ID
     */
    public List<Entitlement> getEnabledEntitlementsByTenant(Long tenantId) {
        return entitlementRepository.findEnabledByTenantId(tenantId);
    }

    /**
     * Get entitlement by tenant and feature
     */
    public Entitlement getEntitlementByTenantAndFeature(Long tenantId, Feature feature) {
        return entitlementRepository.findByTenantIdAndFeature(tenantId, feature)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Entitlement not found for tenant: " + tenantId + ", feature: " + feature));
    }

    /**
     * Update entitlement
     */
    @Transactional
    public Entitlement updateEntitlement(Long id, Entitlement entitlement) {
        Entitlement existing = entitlementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entitlement not found: " + id));

        existing.setFeature(entitlement.getFeature());
        existing.setUsageLimitType(entitlement.getUsageLimitType());
        existing.setLimitValue(entitlement.getLimitValue());
        existing.setCurrentUsage(entitlement.getCurrentUsage());
        existing.setResetPeriod(entitlement.getResetPeriod());
        existing.setIsEnabled(entitlement.getIsEnabled());
        existing.setIsUnlimited(entitlement.getIsUnlimited());
        existing.setOverageAllowed(entitlement.getOverageAllowed());
        existing.setOverageRate(entitlement.getOverageRate());
        existing.setSoftLimit(entitlement.getSoftLimit());
        existing.setWarningThresholdPercentage(entitlement.getWarningThresholdPercentage());

        Entitlement updated = entitlementRepository.save(existing);
        log.info("Updated entitlement: {}", updated.getId());
        return updated;
    }

    /**
     * Delete entitlement
     */
    @Transactional
    public void deleteEntitlement(Long id) {
        Entitlement entitlement = entitlementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Entitlement not found: " + id));

        entitlementRepository.delete(entitlement);
        log.info("Deleted entitlement: {}", id);
    }

    /**
     * Check if tenant has feature enabled
     */
    public boolean hasFeature(Long tenantId, Feature feature) {
        return entitlementRepository.hasFeature(tenantId, feature);
    }

    /**
     * Get usage limit for tenant and limit type
     */
    public Long getUsageLimit(Long tenantId, UsageLimit limitType) {
        return entitlementRepository.getUsageLimit(tenantId, limitType);
    }

    /**
     * Check and consume usage
     */
    @Transactional
    public boolean consumeUsage(Long tenantId, Feature feature, long amount) {
        Entitlement entitlement = entitlementRepository.findByTenantIdAndFeature(tenantId, feature)
                .orElse(null);

        if (entitlement == null || !entitlement.getIsEnabled()) {
            return false;
        }

        boolean success = entitlement.addUsage(amount);
        entitlementRepository.save(entitlement);

        if (!success) {
            log.warn("Usage blocked for tenant: {}, feature: {}, amount: {}", 
                    tenantId, feature, amount);
        }

        return success;
    }

    /**
     * Get current usage for feature
     */
    public Long getCurrentUsage(Long tenantId, Feature feature) {
        return entitlementRepository.getTotalUsageByFeature(tenantId, feature);
    }

    /**
     * Get entitlement summary for tenant
     */
    public Map<Feature, EntitlementSummary> getEntitlementSummary(Long tenantId) {
        List<Object[]> results = entitlementRepository.getEntitlementSummary(tenantId);
        
        return results.stream()
                .collect(Collectors.toMap(
                        row -> (Feature) row[0],
                        row -> new EntitlementSummary(
                                (Feature) row[0],
                                (Long) row[1],
                                (Long) row[2],
                                (Boolean) row[3],
                                (Boolean) row[4]
                        )
                ));
    }

    /**
     * Get entitlements at or near limit
     */
    public List<Entitlement> getEntitlementsAtOrNearLimit(Long tenantId) {
        return entitlementRepository.findEntitlementsAtOrNearLimit(tenantId, 80.0);
    }

    /**
     * Get entitlements over limit
     */
    public List<Entitlement> getEntitlementsOverLimit(Long tenantId) {
        return entitlementRepository.findEntitlementsOverLimit(tenantId);
    }

    /**
     * Process usage resets
     */
    @Transactional
    public void processUsageResets() {
        List<Entitlement> entitlements = entitlementRepository.findEntitlementsNeedingReset(LocalDateTime.now());
        
        for (Entitlement entitlement : entitlements) {
            entitlement.resetUsage();
            entitlementRepository.save(entitlement);
            log.info("Reset usage for entitlement: {}", entitlement.getId());
        }
    }

    /**
     * Get entitlements needing warning
     */
    public List<Entitlement> getEntitlementsNeedingWarning(Long tenantId) {
        LocalDateTime dayAgo = LocalDateTime.now().minusDays(1);
        return entitlementRepository.findEntitlementsNeedingWarning(tenantId, dayAgo);
    }

    /**
     * Send usage warning (placeholder for notification service)
     */
    @Transactional
    public void sendUsageWarning(Long entitlementId) {
        Entitlement entitlement = entitlementRepository.findById(entitlementId)
                .orElseThrow(() -> new ResourceNotFoundException("Entitlement not found: " + entitlementId));

        entitlement.setLastWarningAt(LocalDateTime.now());
        entitlementRepository.save(entitlement);

        // TODO: Integrate with notification service
        log.warn("Usage warning sent for entitlement: {}, usage: {}/{} ({}%)", 
                entitlementId, 
                entitlement.getCurrentUsage(), 
                entitlement.getLimitValue(),
                entitlement.getUsagePercentage());
    }

    /**
     * Entitlement summary data class
     */
    public static class EntitlementSummary {
        private final Feature feature;
        private final Long limit;
        private final Long currentUsage;
        private final Boolean isUnlimited;
        private final Boolean isEnabled;

        public EntitlementSummary(Feature feature, Long limit, Long currentUsage, 
                              Boolean isUnlimited, Boolean isEnabled) {
            this.feature = feature;
            this.limit = limit;
            this.currentUsage = currentUsage;
            this.isUnlimited = isUnlimited;
            this.isEnabled = isEnabled;
        }

        public Feature getFeature() { return feature; }
        public Long getLimit() { return limit; }
        public Long getCurrentUsage() { return currentUsage; }
        public Boolean getIsUnlimited() { return isUnlimited; }
        public Boolean getIsEnabled() { return isEnabled; }
        public Double getUsagePercentage() {
            if (isUnlimited || limit == null || limit == 0) return 0.0;
            return (double) currentUsage / limit * 100.0;
        }
        public Long getRemaining() {
            if (isUnlimited || limit == null) return Long.MAX_VALUE;
            return Math.max(0, limit - currentUsage);
        }
    }
}
