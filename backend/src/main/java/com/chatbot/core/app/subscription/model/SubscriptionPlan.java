package com.chatbot.core.app.subscription.model;

public enum SubscriptionPlan {
    FREE("Free"),
    BASIC("Basic"),
    PROFESSIONAL("Professional"),
    ENTERPRISE("Enterprise"),
    CUSTOM("Custom");
    
    private final String displayName;
    
    SubscriptionPlan(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
