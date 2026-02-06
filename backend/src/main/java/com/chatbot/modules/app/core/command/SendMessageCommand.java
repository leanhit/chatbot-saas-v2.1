package com.chatbot.modules.app.core.command;

import com.chatbot.modules.app.core.guard.GuardRequest;
import com.chatbot.modules.app.core.model.AppCode;
import lombok.Data;

import java.util.UUID;

@Data
public class SendMessageCommand {
    
    private final GuardRequest guardRequest;
    private String pageId;
    private String recipientId;
    private String message;
    private String sender;
    
    public SendMessageCommand(UUID tenantId, UUID userId, String pageId, String recipientId, String message, String sender) {
        this.guardRequest = GuardRequest.of(tenantId, userId, AppCode.SEND_MESSAGE, "SEND_MESSAGE");
        this.pageId = pageId;
        this.recipientId = recipientId;
        this.message = message;
        this.sender = sender;
    }
    
    public GuardRequest getGuardRequest() {
        return guardRequest;
    }
    
    public String getPageId() {
        return pageId;
    }
    
    public String getRecipientId() {
        return recipientId;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getSender() {
        return sender;
    }
}
