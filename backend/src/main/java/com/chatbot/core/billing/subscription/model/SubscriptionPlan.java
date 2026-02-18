package com.chatbot.core.billing.subscription.model;

/**
 * Subscription Plan Enumeration
 */
public enum SubscriptionPlan {
    FREE("Free", "Miễn phí"),
    STARTER("Starter", "Khởi đầu"),
    PROFESSIONAL("Professional", "Chuyên nghiệp"),
    ENTERPRISE("Enterprise", "Doanh nghiệp"),
    CUSTOM("Custom", "Tùy chỉnh");

    private final String code;
    private final String description;

    SubscriptionPlan(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
