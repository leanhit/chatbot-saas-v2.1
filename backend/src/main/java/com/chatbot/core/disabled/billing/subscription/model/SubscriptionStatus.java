package com.chatbot.core.billing.subscription.model;

/**
 * Subscription Status Enumeration
 */
public enum SubscriptionStatus {
    PENDING("Pending", "Chờ kích hoạt"),
    ACTIVE("Active", "Đang hoạt động"),
    SUSPENDED("Suspended", "Tạm dừng"),
    CANCELLED("Cancelled", "Đã hủy"),
    EXPIRED("Expired", "Đã hết hạn"),
    TRIAL("Trial", "Dùng thử");

    private final String code;
    private final String description;

    SubscriptionStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isActive() {
        return this == ACTIVE || this == TRIAL;
    }

    public boolean isTerminated() {
        return this == CANCELLED || this == EXPIRED;
    }
}
