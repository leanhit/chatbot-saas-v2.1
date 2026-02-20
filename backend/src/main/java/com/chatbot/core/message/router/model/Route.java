package com.chatbot.core.message.router.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Route model for message routing
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {
    
    private Long id;
    private String name;
    private String destination;
    private String description;
    private Boolean isActive;
    private Integer priority;
    private String condition;
    
    // Route types
    public enum RouteType {
        INTERNAL,    // Internal hub routing
        EXTERNAL,    // External service routing
        FALLBACK     // Default fallback routing
    }
    
    private RouteType routeType;
}
