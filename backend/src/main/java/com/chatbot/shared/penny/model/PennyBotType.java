package com.chatbot.shared.penny.model;

/**
 * Enum định nghĩa các loại bot trong Penny Middleware
 */
public enum PennyBotType {
    CUSTOMER_SERVICE("Customer Service", "penny-customer-service-001"),
    SALES("Sales", "penny-sales-001"),
    SUPPORT("Technical Support", "penny-support-001"),
    MARKETING("Marketing", "penny-marketing-001"),
    HR("Human Resources", "penny-hr-001"),
    FINANCE("Finance", "penny-finance-001"),
    GENERAL("General Purpose", "penny-general-001"),
    BOTPRESS("Botpress Integration", null); // Botpress type with dynamic botId

    private final String displayName;
    private final String pennyBotId;

    PennyBotType(String displayName, String pennyBotId) {
        this.displayName = displayName;
        this.pennyBotId = pennyBotId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPennyBotId() {
        return pennyBotId;
    }

    // Keep for backward compatibility
    @Deprecated
    public String getBotpressBotId() {
        return pennyBotId;
    }

    public static PennyBotType fromString(String type) {
        try {
            return PennyBotType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return GENERAL; // Default fallback
        }
    }
}
