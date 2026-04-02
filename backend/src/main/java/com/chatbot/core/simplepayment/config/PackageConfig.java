package com.chatbot.core.simplepayment.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackageConfig {

    private Map<String, PackageDefinition> packages;
    private LimitsConfig limits;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PackageDefinition {
        private String name;
        private int price;
        private String currency;
        private String duration;
        private String description;
        private int messageLimit;
        private int chatbotLimit;
        private boolean hasPrioritySupport;
        private boolean hasAnalytics;
        private boolean hasAdvancedAnalytics;
        private boolean hasCustomIntegrations;
        private boolean hasDedicatedSupport;
        private boolean hasCustomFeatures;
        private boolean hasSlaGuarantee;
        private boolean isActive;
        private int sortOrder;
        private String badge;
    }

    @Data
    public static class LimitsConfig {
        private FreeLimits free;
        private PaidLimits paid;

        @Data
        public static class FreeLimits {
            private int maxMonthlyDeposits;
            private int maxMonthlyAmount;
            private int minDepositAmount;
        }

        @Data
        public static class PaidLimits {
            private int minDepositAmount;
            private int recentPurchaseWarningDays;
        }
    }
}
