package com.chatbot.shared.penny.model;

/**
 * Enum định nghĩa các loại bot trong Penny Middleware
 */
public enum PennyBotType {
    CUSTOMER_SERVICE("Customer Service", "botpress-customer-service-001"),
    SALES("Sales", "botpress-sales-001"),
    SUPPORT("Technical Support", "botpress-support-001"),
    MARKETING("Marketing", "botpress-marketing-001"),
    HR("Human Resources", "botpress-hr-001"),
    FINANCE("Finance", "botpress-finance-001"),
    GENERAL("General Purpose", "botpress-general-001");

    private final String displayName;
    private final String botpressBotId;

    PennyBotType(String displayName, String botpressBotId) {
        this.displayName = displayName;
        this.botpressBotId = botpressBotId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getBotpressBotId() {
        return botpressBotId;
    }

    public static PennyBotType fromString(String type) {
        try {
            return PennyBotType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return GENERAL; // Default fallback
        }
    }
}
