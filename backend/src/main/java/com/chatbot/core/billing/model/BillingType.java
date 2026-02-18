package com.chatbot.core.billing.model;

/**
 * Billing Type Enumeration
 * Defines different types of billing accounts
 */
public enum BillingType {
    PREPAID("Prepaid", "Thanh toán trước"),
    POSTPAID("Postpaid", "Thanh toán sau"),
    SUBSCRIPTION("Subscription", "Gói đăng ký"),
    USAGE_BASED("Usage Based", "Theo mức sử dụng");

    private final String code;
    private final String description;

    BillingType(String code, String description) {
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
