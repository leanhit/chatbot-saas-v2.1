package com.chatbot.core.config.grpc;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Config Service gRPC Implementation - Placeholder
 */
@Service
@Slf4j
public class ConfigServiceGrpcImpl {
    
    public void handleConfigRequest(String configKey) {
        log.info("Handling gRPC config request: {}", configKey);
        // Implementation will be added
    }
}
