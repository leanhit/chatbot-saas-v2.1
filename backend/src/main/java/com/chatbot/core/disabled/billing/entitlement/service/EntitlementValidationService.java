package com.chatbot.core.billing.entitlement.service;

import com.chatbot.core.billing.entitlement.model.Entitlement;
import com.chatbot.core.billing.entitlement.model.Feature;
import com.chatbot.core.billing.entitlement.model.UsageLimit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for validating entitlements and usage limits
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EntitlementValidationService {

    private final EntitlementService entitlementService;

    /**
     * Validate if tenant can perform action based on feature
     */
    public ValidationResult validateFeatureAccess(Long tenantId, Feature feature) {
        try {
            if (!entitlementService.hasFeature(tenantId, feature)) {
                return ValidationResult.denied("Feature not available for tenant");
            }

            Entitlement entitlement = entitlementService.getEntitlementByTenantAndFeature(tenantId, feature);
            
            if (!entitlement.getIsEnabled()) {
                return ValidationResult.denied("Feature is disabled");
            }

            if (entitlement.isOverLimit()) {
                return ValidationResult.denied("Usage limit exceeded");
            }

            return ValidationResult.granted();

        } catch (Exception e) {
            log.error("Error validating feature access for tenant: {}, feature: {}", tenantId, feature, e);
            return ValidationResult.denied("Validation error occurred");
        }
    }

    /**
     * Validate and consume usage
     */
    public ValidationResult validateAndConsumeUsage(Long tenantId, Feature feature, long amount) {
        try {
            ValidationResult accessResult = validateFeatureAccess(tenantId, feature);
            if (!accessResult.isAllowed()) {
                return accessResult;
            }

            boolean consumed = entitlementService.consumeUsage(tenantId, feature, amount);
            if (!consumed) {
                return ValidationResult.denied("Unable to consume usage - limit reached");
            }

            return ValidationResult.granted();

        } catch (Exception e) {
            log.error("Error validating and consuming usage for tenant: {}, feature: {}, amount: {}", 
                    tenantId, feature, amount, e);
            return ValidationResult.denied("Validation error occurred");
        }
    }

    /**
     * Check usage limits for tenant
     */
    public UsageCheckResult checkUsageLimits(Long tenantId) {
        try {
            List<Entitlement> atOrNearLimit = entitlementService.getEntitlementsAtOrNearLimit(tenantId);
            List<Entitlement> overLimit = entitlementService.getEntitlementsOverLimit(tenantId);

            Map<Feature, LimitStatus> limitStatuses = new HashMap<>();

            // Process at/near limit entitlements
            for (Entitlement entitlement : atOrNearLimit) {
                if (entitlement.isAtLimit()) {
                    limitStatuses.put(entitlement.getFeature(), LimitStatus.AT_LIMIT);
                } else if (entitlement.isNearLimit()) {
                    limitStatuses.put(entitlement.getFeature(), LimitStatus.NEAR_LIMIT);
                }
            }

            // Process over limit entitlements
            for (Entitlement entitlement : overLimit) {
                limitStatuses.put(entitlement.getFeature(), LimitStatus.OVER_LIMIT);
            }

            boolean hasIssues = !overLimit.isEmpty() || !atOrNearLimit.isEmpty();
            String message = hasIssues ? "Usage limits require attention" : "All usage within limits";

            return new UsageCheckResult(hasIssues, message, limitStatuses);

        } catch (Exception e) {
            log.error("Error checking usage limits for tenant: {}", tenantId, e);
            return new UsageCheckResult(true, "Error checking usage limits", new HashMap<>());
        }
    }

    /**
     * Get usage limit for specific type
     */
    public Long getUsageLimit(Long tenantId, UsageLimit limitType) {
        try {
            return entitlementService.getUsageLimit(tenantId, limitType);
        } catch (Exception e) {
            log.error("Error getting usage limit for tenant: {}, limit: {}", tenantId, limitType, e);
            return null;
        }
    }

    /**
     * Check if tenant can create more users
     */
    public ValidationResult canCreateUser(Long tenantId) {
        Long currentLimit = getUsageLimit(tenantId, UsageLimit.MAX_USERS);
        if (currentLimit == null) {
            return ValidationResult.granted(); // No limit set
        }

        // TODO: Get current user count from user service
        // For now, assume unlimited
        return ValidationResult.granted();
    }

    /**
     * Check if tenant can create more bots
     */
    public ValidationResult canCreateBot(Long tenantId) {
        return validateFeatureAccess(tenantId, Feature.CHATBOT_CREATION);
    }

    /**
     * Check if tenant can upload file
     */
    public ValidationResult canUploadFile(Long tenantId, long fileSize) {
        // Check file size limit
        Long maxFileSize = getUsageLimit(tenantId, UsageLimit.MAX_FILE_SIZE_MB);
        if (maxFileSize != null && fileSize > maxFileSize * 1024 * 1024) {
            return ValidationResult.denied("File size exceeds limit");
        }

        // Check storage limit
        ValidationResult storageResult = validateFeatureAccess(tenantId, Feature.CLOUD_STORAGE);
        if (!storageResult.isAllowed()) {
            return storageResult;
        }

        return ValidationResult.granted();
    }

    /**
     * Check if tenant can make API call
     */
    public ValidationResult canMakeApiCall(Long tenantId) {
        return validateFeatureAccess(tenantId, Feature.API_ACCESS);
    }

    /**
     * Get comprehensive entitlement status
     */
    public EntitlementStatusResult getEntitlementStatus(Long tenantId) {
        try {
            Map<Feature, EntitlementService.EntitlementSummary> summary = 
                    entitlementService.getEntitlementSummary(tenantId);

            List<Entitlement> needingWarning = entitlementService.getEntitlementsNeedingWarning(tenantId);

            return new EntitlementStatusResult(summary, needingWarning);

        } catch (Exception e) {
            log.error("Error getting entitlement status for tenant: {}", tenantId, e);
            return new EntitlementStatusResult(new HashMap<>(), List.of());
        }
    }

    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final boolean allowed;
        private final String message;

        private ValidationResult(boolean allowed, String message) {
            this.allowed = allowed;
            this.message = message;
        }

        public static ValidationResult granted() {
            return new ValidationResult(true, "Access granted");
        }

        public static ValidationResult denied(String message) {
            return new ValidationResult(false, message);
        }

        public boolean isAllowed() { return allowed; }
        public String getMessage() { return message; }
    }

    /**
     * Usage check result class
     */
    public static class UsageCheckResult {
        private final boolean hasIssues;
        private final String message;
        private final Map<Feature, LimitStatus> limitStatuses;

        public UsageCheckResult(boolean hasIssues, String message, Map<Feature, LimitStatus> limitStatuses) {
            this.hasIssues = hasIssues;
            this.message = message;
            this.limitStatuses = limitStatuses;
        }

        public boolean hasIssues() { return hasIssues; }
        public String getMessage() { return message; }
        public Map<Feature, LimitStatus> getLimitStatuses() { return limitStatuses; }
    }

    /**
     * Entitlement status result class
     */
    public static class EntitlementStatusResult {
        private final Map<Feature, EntitlementService.EntitlementSummary> summary;
        private final List<Entitlement> needingWarning;

        public EntitlementStatusResult(Map<Feature, EntitlementService.EntitlementSummary> summary, 
                                   List<Entitlement> needingWarning) {
            this.summary = summary;
            this.needingWarning = needingWarning;
        }

        public Map<Feature, EntitlementService.EntitlementSummary> getSummary() { return summary; }
        public List<Entitlement> getNeedingWarning() { return needingWarning; }
    }

    /**
     * Limit status enumeration
     */
    public enum LimitStatus {
        WITHIN_LIMIT, NEAR_LIMIT, AT_LIMIT, OVER_LIMIT
    }
}
