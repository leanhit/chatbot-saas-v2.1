package com.chatbot.core.app.guard.model;

public enum GuardType {
    RATE_LIMIT("Rate Limit"),
    AUTHENTICATION("Authentication"),
    AUTHORIZATION("Authorization"),
    INPUT_VALIDATION("Input Validation"),
    OUTPUT_FILTERING("Output Filtering"),
    DATA_ACCESS("Data Access"),
    API_THROTTLING("API Throttling"),
    IP_WHITELIST("IP Whitelist"),
    TIME_BASED("Time Based"),
    CUSTOM("Custom");
    
    private final String displayName;
    
    GuardType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
