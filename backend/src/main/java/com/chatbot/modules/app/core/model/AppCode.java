package com.chatbot.modules.app.core.model;

/**
 * App code enum for v0.1
 * Static list of available applications
 */
public enum AppCode {
    CHATBOT("Chatbot", "AI-powered chatbot application"),
    ERP("ERP", "Enterprise Resource Planning system"),
    CRM("CRM", "Customer Relationship Management system"),
    SEND_MESSAGE("SEND_MESSAGE", "Send message application");

    private final String displayName;
    private final String description;

    AppCode(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
