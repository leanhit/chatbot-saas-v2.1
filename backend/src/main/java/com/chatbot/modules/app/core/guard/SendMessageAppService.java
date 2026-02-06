package com.chatbot.modules.app.core.service;

import com.chatbot.modules.app.core.command.SendMessageCommand;
import com.chatbot.modules.app.core.guard.AppServiceGuard;
import com.chatbot.modules.app.core.guard.GuardPassContext;
import com.chatbot.modules.app.core.guard.GuardRegistry;
import com.chatbot.modules.app.core.model.AppCode;
import com.chatbot.modules.app.core.service.MessageDispatcher;
import com.chatbot.modules.app.core.guard.GuardRequest;
import com.chatbot.modules.app.core.guard.GuardException;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

/**
 * SendMessageAppService
 *
 * Entry point for SEND_MESSAGE AppCode
 * - Build GuardRequest
 * - Execute guards
 * - Call underlying message service (FacebookMessengerService)
 */
@Service
public class SendMessageAppService {

    private final GuardRegistry guardRegistry;
    private final MessageDispatcher messageDispatcher;

    public SendMessageAppService(
            GuardRegistry guardRegistry,
            MessageDispatcher messageDispatcher
    ) {
        this.guardRegistry = guardRegistry;
        this.messageDispatcher = messageDispatcher;
    }

    /**
     * Send message with guard validation
     */
    public void sendMessageWithGuard(
            String pageId,
            String recipientId,
            String message,
            String sender
    ) {
        // TODO: Get tenantId and userId from context - using stub UUIDs for now
        UUID tenantId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        
        SendMessageCommand command = new SendMessageCommand(tenantId, userId, pageId, recipientId, message, sender);

        GuardRequest guardRequest = command.getGuardRequest();

        // 1. Resolve guards by AppCode
        List<AppServiceGuard> guards =
                guardRegistry.getGuards(AppCode.SEND_MESSAGE);

        // 2. Execute guards (FAIL FAST)
        GuardPassContext passContext = null;
        for (AppServiceGuard guard : guards) {
            passContext = guard.check(guardRequest);
        }

        if (passContext == null) {
            throw new GuardException("GuardPassContext is null");
        }

        // 3. Dispatch message (no validation here)
        messageDispatcher.dispatchSendMessage(command);
    }
}
