package com.chatbot.core.customer.service;

import com.chatbot.core.customer.model.CustomerProfile;
import com.chatbot.core.customer.model.VipConfig;
import com.chatbot.core.customer.repository.CustomerProfileRepository;
import com.chatbot.core.customer.repository.VipConfigRepository;
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
    private final VipConfigRepository vipConfigRepository;

    /**
     * Compute the tier from profile stats using built-in default thresholds.
     */
    public CustomerProfile.CustomerTier computeTier(CustomerProfile profile) {
        return computeTier(profile, null);
    }

    /**
     * Compute the tier from profile stats using tenant-specific thresholds
     * when a VipConfig is provided; falls back to defaults for null fields.
     */
    public CustomerProfile.CustomerTier computeTier(CustomerProfile profile, VipConfig config) {
        int conversations = profile.getTotalConversations() != null ? profile.getTotalConversations() : 0;
        Double satisfaction = profile.getSatisfactionScore();

        int basicMin = valueOrDefault(config != null ? config.getBasicMinConversations() : null, BASIC_MIN_CONVERSATIONS);
        int premiumMin = valueOrDefault(config != null ? config.getPremiumMinConversations() : null, PREMIUM_MIN_CONVERSATIONS);
        double premiumSat = config != null && config.getPremiumMinSatisfaction() != null
            ? config.getPremiumMinSatisfaction() : PREMIUM_MIN_SATISFACTION;
        int enterpriseMin = valueOrDefault(config != null ? config.getEnterpriseMinConversations() : null, ENTERPRISE_MIN_CONVERSATIONS);

        if (conversations >= enterpriseMin) {
            return CustomerProfile.CustomerTier.ENTERPRISE;
        }
        if (conversations >= premiumMin
                && satisfaction != null && satisfaction >= premiumSat) {
            return CustomerProfile.CustomerTier.PREMIUM;
        }
        if (conversations >= basicMin) {
            return CustomerProfile.CustomerTier.BASIC;
        }
        return CustomerProfile.CustomerTier.FREE;
    }

    private int valueOrDefault(Integer value, int defaultValue) {
        return value != null ? value : defaultValue;
    }

    private VipConfig loadConfig(Long tenantId) {
        try {
            return vipConfigRepository.findByTenantIdAndIsDeletedFalse(tenantId).orElse(null);
        } catch (Exception e) {
            log.warn("[Segmentation] Failed to load VipConfig for tenant {}: {}", tenantId, e.getMessage());
            return null;
        }
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

        VipConfig config = loadConfig(tenantId);
        CustomerProfile.CustomerTier computed = computeTier(profile, config);
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
        VipConfig config = loadConfig(tenantId);
        int changed = 0;
        for (CustomerProfile profile : profiles) {
            CustomerProfile.CustomerTier computed = computeTier(profile, config);
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
