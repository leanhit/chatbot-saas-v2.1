package com.chatbot.core.message.grpc;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Message Service gRPC Implementation
 */
@Service
@Slf4j
public class MessageServiceGrpcImpl {
    
    public void handleMessage(String messageId) {
        log.info("Handling gRPC message: {}", messageId);
        // Implementation will be added
    }
}
