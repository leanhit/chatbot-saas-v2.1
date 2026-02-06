package com.chatbot.modules.billing.core.model;

/**
 * Plan code enum for v0.1
 * Static list of available billing plans
 */
public enum PlanCode {
    FREE("Free", "Basic features with limited apps"),
    PRO("Pro", "Advanced features with more apps"),
    ENTERPRISE("Enterprise", "All features and unlimited apps");

    private final String displayName;
    private final String description;

    PlanCode(String displayName, String description) {
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
