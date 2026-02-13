package com.chatbot.core.app.registry.model;

public enum AppType {
    CHATBOT("Chatbot"),
    INTEGRATION("Integration"),
    ANALYTICS("Analytics"),
    AUTOMATION("Automation"),
    NOTIFICATION("Notification"),
    PAYMENT("Payment"),
    STORAGE("Storage"),
    SECURITY("Security"),
    CUSTOM("Custom");
    
    private final String displayName;
    
    AppType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
