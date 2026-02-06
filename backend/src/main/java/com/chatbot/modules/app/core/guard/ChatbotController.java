package com.chatbot.modules.app.core.guard;

import com.chatbot.modules.app.core.model.AppCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Example controller showing exception handling pattern
 * Controller maps GuardException to HTTP responses
 */
@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
@Slf4j
public class ChatbotController {
    
    private final ChatbotMessageService messageService;
    
    @PostMapping("/message")
    public ResponseEntity<String> sendMessage(@RequestBody SendMessageRequest request) {
        try {
            messageService.sendMessage(
                    request.getTenantId(),
                    request.getUserId(),
                    request.getMessage()
            );
            return ResponseEntity.ok("Message sent successfully");
        } catch (GuardException e) {
            log.warn("Guard blocked request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Access denied: " + e.getMessage());
        }
    }
    
    public static class SendMessageRequest {
        private UUID tenantId;
        private UUID userId;
        private String message;
        
        public UUID getTenantId() { return tenantId; }
        public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
        
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
