package com.chatbot.core.message.router.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Routing rule for message routing decisions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoutingRule {
    
    private Long id;
    private String name;
    private String pattern;
    private String source;
    private String destination;
    private String description;
    private Boolean isActive;
    private Integer priority;
    
    // Rule types
    public enum RuleType {
        REGEX,       // Pattern matching with regex
        KEYWORD,     // Keyword matching
        INTENT,       // Intent-based routing
        CONDITION,    // Conditional routing
        DEFAULT       // Default rule
    }
    
    private RuleType ruleType;
    
    // Rule actions
    public enum Action {
        ROUTE,       // Route to destination
        TRANSFORM,    // Transform message
        BLOCK,        // Block message
        LOG           // Log message only
    }
    
    private Action action;
}
