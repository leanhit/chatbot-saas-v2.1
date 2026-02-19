package com.chatbot.core.message.processor.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Message Processor Service - Processes and transforms messages
 */
@Service
@Slf4j
public class MessageProcessorService {
    
    public String processMessage(String rawMessage) {
        log.info("Processing message: {}", rawMessage);
        // Implementation will be added
        return rawMessage;
    }
}
