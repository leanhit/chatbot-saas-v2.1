package com.chatbot.core.message.store.model;

/**
 * Conversation status enum to avoid magic strings
 */
public enum ConversationStatus {
    OPEN("open"),
    CLOSED("closed"),
    QUEUED("queued"),
    ACTIVE_AGENT("active_agent");

    private final String value;

    ConversationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static ConversationStatus fromValue(String value) {
        for (ConversationStatus status : values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown conversation status: " + value);
    }
}
