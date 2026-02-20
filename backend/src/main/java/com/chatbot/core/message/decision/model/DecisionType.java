package com.chatbot.core.message.decision.model;

/**
 * Decision type enumeration
 */
public enum DecisionType {
    
    // Routing decisions
    ROUTE_TO_HUB("Route to specific hub"),
    ROUTE_TO_SERVICE("Route to specific service"),
    ROUTE_TO_EXTERNAL("Route to external service"),
    
    // Processing decisions
    TRANSFORM_MESSAGE("Transform message"),
    VALIDATE_MESSAGE("Validate message"),
    ENRICH_MESSAGE("Enrich message with data"),
    
    // Control flow decisions
    TAKEOVER_CONVERSATION("Agent takeover conversation"),
    RELEASE_CONVERSATION("Release conversation from agent"),
    ESCALATE_CONVERSATION("Escalate conversation"),
    
    // Quality decisions
    BLOCK_MESSAGE("Block inappropriate message"),
    RATE_LIMIT("Apply rate limiting"),
    FILTER_CONTENT("Filter content"),
    
    // System decisions
    LOG_MESSAGE("Log message for analytics"),
    CACHE_MESSAGE("Cache message for performance"),
    ARCHIVE_MESSAGE("Archive message"),
    
    // Default decisions
    DEFAULT_PROCESSING("Default processing flow"),
    FALLBACK_HANDLING("Fallback handling");
    
    private final String description;
    
    DecisionType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
