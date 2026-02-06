package com.chatbot.modules.penny.model;

/**
 * Penny Bot Types enum
 * Định nghĩa các loại bot có thể tạo
 */
public enum PennyBotType {
    CUSTOMER_SERVICE("customer_service_bot", "Customer Service Bot"),
    SALES_ASSISTANT("sales_assistant_bot", "Sales Assistant Bot"),
    SUPPORT_BOT("support_bot", "Support Bot"),
    FAQ_BOT("faq_bot", "FAQ Bot"),
    NOTIFICATION_BOT("notification_bot", "Notification Bot"),
    CUSTOM("custom_bot", "Custom Bot");

    private final String botpressBotId;
    private final String description;

    PennyBotType(String botpressBotId, String description) {
        this.botpressBotId = botpressBotId;
        this.description = description;
    }

    public String getBotpressBotId() {
        return botpressBotId;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Convert string to PennyBotType
     */
    public static PennyBotType fromString(String type) {
        try {
            return PennyBotType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CUSTOMER_SERVICE; // Default fallback
        }
    }
}
