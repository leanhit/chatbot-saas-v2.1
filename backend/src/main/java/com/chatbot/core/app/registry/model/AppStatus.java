package com.chatbot.core.app.registry.model;

public enum AppStatus {
    DRAFT("Draft"),
    PENDING("Pending"),
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    DEPRECATED("Deprecated"),
    ARCHIVED("Archived");
    
    private final String displayName;
    
    AppStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
