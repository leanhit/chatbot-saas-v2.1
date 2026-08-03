package com.chatbot.shared.penny.model;

/**
 * Enum định nghĩa các loại bot trong Penny Middleware
 * Simplified from 8 types to 3 core types for better UX
 */
public enum PennyBotType {
    GENERAL("General Purpose", "penny-general-001"),
    SUPPORT("Customer Support", "penny-support-001"),
    BUSINESS("Business & Sales", "penny-business-001"),
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

    public static PennyBotType fromString(String type) {
        try {
            return PennyBotType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return GENERAL; // Default fallback
        }
    }

    /**
     * Map old bot types to new simplified types for migration
     */
    public static PennyBotType migrateFromOldType(String oldType) {
        if (oldType == null) return GENERAL;

        return switch (oldType.toUpperCase()) {
            case "CUSTOMER_SERVICE", "SUPPORT" -> SUPPORT;
            case "SALES", "MARKETING" -> BUSINESS;
            case "HR", "FINANCE" -> GENERAL;
            case "GENERAL", "BOTPRESS" -> PennyBotType.valueOf(oldType.toUpperCase());
            default -> GENERAL;
        };
    }
}
