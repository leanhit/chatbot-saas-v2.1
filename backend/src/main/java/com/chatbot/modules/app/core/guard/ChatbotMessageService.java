package com.chatbot.modules.app.core.guard;

import com.chatbot.modules.app.core.model.AppCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Example usage service demonstrating how to use AppServiceGuard
 * Business logic never checks permissions - guard throws on failure
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ChatbotMessageService {
    
    private final AppServiceGuard appServiceGuard;
    
    /**
     * Example method that sends a message using the guard
     * Guard throws exception on failure, business logic continues unchanged
     */
    public void sendMessage(UUID tenantId, UUID userId, String message) {
        // Create guard request
        GuardRequest guardRequest = GuardRequest.of(
                tenantId,
                userId,
                AppCode.CHATBOT,
                "SEND_MESSAGE"
        );
        
        // Guard check - throws exception on failure, returns context on success
        GuardPassContext passContext = appServiceGuard.check(guardRequest);
        
        // Continue with business logic (unchanged)
        log.info("Sending message for tenant: {}, user: {}, cost: {}", 
                tenantId, userId, passContext.getCost());
        
        // TODO: Existing message sending logic here
        // Business logic remains completely unchanged
    }
}
