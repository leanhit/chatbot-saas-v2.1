package com.chatbot.core.message.router.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Destination model for message routing
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Destination {
    
    private Long id;
    private String name;
    private String endpoint;
    private String type;
    private String description;
    private Boolean isActive;
    private Integer timeout;
    private Integer retryCount;
    
    // Destination types
    public enum DestinationType {
        HUB,         // Internal hub
        SERVICE,     // Microservice
        EXTERNAL,    // External API
        WEBHOOK,     // Webhook endpoint
        QUEUE        // Message queue
    }
    
    private DestinationType destinationType;
    
    // Connection methods
    public enum ConnectionMethod {
        HTTP,        // HTTP/HTTPS
        GRPC,        // gRPC
        WEBSOCKET,   // WebSocket
        AMQP,        // Message queue
        KAFKA        // Kafka topic
    }
    
    private ConnectionMethod connectionMethod;
}
