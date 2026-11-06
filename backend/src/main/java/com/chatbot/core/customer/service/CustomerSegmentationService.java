package com.chatbot.core.customer.service;

import com.chatbot.core.customer.model.CustomerProfile;
import com.chatbot.core.customer.repository.CustomerProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Customer segmentation / VIP tier rules (Phase 4).
 *
 * Business rules (based on real stats: totalConversations, satisfactionScore):
 * - ENTERPRISE: >= 50 conversations
 * - PREMIUM:    >= 10 conversations AND satisfactionScore >= 4.0
 * - BASIC:      >= 3 conversations
 * - FREE:       default
 *
 * Auto-recalculation only upgrades tiers (never downgrades a manually
 * assigned higher tier). Use force=true to apply exact computed tier.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerSegmentationService {

    private static final int BASIC_MIN_CONVERSATIONS = 3;
    private static final int PREMIUM_MIN_CONVERSATIONS = 10;
    private static final double PREMIUM_MIN_SATISFACTION = 4.0;
    private static final int ENTERPRISE_MIN_CONVERSATIONS = 50;

    private final CustomerProfileRepository customerProfileRepository;

    /**
     * Compute the tier from profile stats.
     */
    public CustomerProfile.CustomerTier computeTier(CustomerProfile profile) {
        int conversations = profile.getTotalConversations() != null ? profile.getTotalConversations() : 0;
        Double satisfaction = profile.getSatisfactionScore();

        if (conversations >= ENTERPRISE_MIN_CONVERSATIONS) {
            return CustomerProfile.CustomerTier.ENTERPRISE;
        }
        if (conversations >= PREMIUM_MIN_CONVERSATIONS
                && satisfaction != null && satisfaction >= PREMIUM_MIN_SATISFACTION) {
            return CustomerProfile.CustomerTier.PREMIUM;
        }
        if (conversations >= BASIC_MIN_CONVERSATIONS) {
            return CustomerProfile.CustomerTier.BASIC;
        }
        return CustomerProfile.CustomerTier.FREE;
    }

    /**
     * Recalculate tier for a single customer profile.
     *
     * @param force if true, apply computed tier exactly; if false, only upgrade
     */
    @Transactional(value = "appTransactionManager", rollbackFor = Exception.class)
    public CustomerProfile recalculateTier(Long customerProfileId, Long tenantId, boolean force) {
        CustomerProfile profile = customerProfileRepository.findById(customerProfileId)
            .orElseThrow(() -> new IllegalArgumentException("Customer profile not found: " + customerProfileId));
        if (!profile.getTenantId().equals(tenantId)) {
            throw new SecurityException("Customer profile belongs to another tenant");
        }

        CustomerProfile.CustomerTier computed = computeTier(profile);
        CustomerProfile.CustomerTier current = profile.getTier() != null
            ? profile.getTier() : CustomerProfile.CustomerTier.FREE;

        if (force || tierRank(computed) > tierRank(current)) {
            if (!computed.equals(current)) {
                log.info("[Segmentation] Upgrading customer {} tier {} -> {} (conversations={}, satisfaction={})",
                    customerProfileId, current, computed, profile.getTotalConversations(), profile.getSatisfactionScore());
                profile.setTier(computed);
                profile = customerProfileRepository.save(profile);
            }
        } else {
            log.debug("[Segmentation] Keeping tier {} for customer {} (computed {})",
                current, customerProfileId, computed);
        }
        return profile;
    }

    /**
     * Recalculate tiers for all customer profiles of a tenant.
     */
    @Transactional(value = "appTransactionManager", rollbackFor = Exception.class)
    public int recalculateAllForTenant(Long tenantId, boolean force) {
        List<CustomerProfile> profiles = customerProfileRepository.findByTenantId(tenantId);
        int changed = 0;
        for (CustomerProfile profile : profiles) {
            CustomerProfile.CustomerTier computed = computeTier(profile);
            CustomerProfile.CustomerTier current = profile.getTier() != null
                ? profile.getTier() : CustomerProfile.CustomerTier.FREE;
            if (force || tierRank(computed) > tierRank(current)) {
                if (!computed.equals(current)) {
                    profile.setTier(computed);
                    customerProfileRepository.save(profile);
                    changed++;
                }
            }
        }
        log.info("[Segmentation] Recalculated tiers for tenant {}: {} profiles changed", tenantId, changed);
        return changed;
    }

    private int tierRank(CustomerProfile.CustomerTier tier) {
        return switch (tier) {
            case FREE -> 0;
            case BASIC -> 1;
            case PREMIUM -> 2;
            case ENTERPRISE -> 3;
        };
    }
}
