package com.chatbot.core.app.subscription.model;

public enum SubscriptionStatus {
    PENDING("Pending"),
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended"),
    CANCELLED("Cancelled"),
    EXPIRED("Expired"),
    TRIAL("Trial");
    
    private final String displayName;
    
    SubscriptionStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
