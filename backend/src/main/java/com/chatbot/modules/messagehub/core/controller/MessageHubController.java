package com.chatbot.modules.messagehub.core.controller;

import com.chatbot.modules.messagehub.core.dto.MessageRequest;
import com.chatbot.modules.messagehub.core.dto.MessageResponse;
import com.chatbot.modules.messagehub.core.gateway.MessageGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Message Hub controller for Message Hub MVP
 * REST API endpoints for Message Gateway
 */
@RestController
@RequestMapping("/api/message-hub")
@RequiredArgsConstructor
@Slf4j
public class MessageHubController {

    private final MessageGateway messageGateway;

    /**
     * Process incoming message
     * This is the main endpoint for all adapters
     */
    @PostMapping("/message")
    public ResponseEntity<MessageResponse> processMessage(@RequestBody MessageRequest request) {
        log.info("Received message request for conversation: {}", request.getConversationId());
        
        MessageResponse response = messageGateway.processMessage(request);
        
        log.info("Message processed with decision: {} for conversation: {}", 
                response.getDecision(), request.getConversationId());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get conversation context
     */
    @GetMapping("/context/{conversationId}")
    public ResponseEntity<?> getContext(@PathVariable String conversationId) {
        var context = messageGateway.getContext(conversationId);
        
        if (context == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(context);
    }

    /**
     * Health check for Message Hub
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Message Hub MVP is running");
    }
}
