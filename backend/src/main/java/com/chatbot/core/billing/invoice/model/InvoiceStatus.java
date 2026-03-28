package com.chatbot.core.billing.invoice.model;

/**
 * Invoice Status Enumeration
 */
public enum InvoiceStatus {
    DRAFT("Draft", "Nháp"),
    SENT("Sent", "Đã gửi"),
    PARTIALLY_PAID("Partially Paid", "Thanh toán một phần"),
    PAID("Paid", "Đã thanh toán"),
    OVERDUE("Overdue", "Quá hạn"),
    CANCELLED("Cancelled", "Đã hủy"),
    REFUNDED("Refunded", "Đã hoàn tiền");

    private final String code;
    private final String description;

    InvoiceStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPaid() {
        return this == PAID || this == REFUNDED;
    }

    public boolean isActive() {
        return this == DRAFT || this == SENT || this == PARTIALLY_PAID || this == OVERDUE;
    }

    public boolean canBePaid() {
        return this == SENT || this == PARTIALLY_PAID || this == OVERDUE;
    }
}
