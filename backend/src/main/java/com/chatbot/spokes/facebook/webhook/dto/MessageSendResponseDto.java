package com.chatbot.spokes.facebook.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageSendResponseDto {
    
    @JsonProperty("message_id")
    private String messageId;
    
    @JsonProperty("recipient_id")
    private String recipientId;
    
    private String status;
    
    @JsonProperty("thread_id")
    private String threadId;
    
    @JsonProperty("attachment_id")
    private String attachmentId;
    
    private List<String> errors;
    
    @JsonProperty("sent_at")
    private Long sentAt;
    
    @JsonProperty("messaging_type")
    private String messagingType;
    
    @JsonProperty("notification_type")
    private String notificationType;
    
    @JsonProperty("message_tag")
    private String messageTag;
    
    @JsonProperty("persona_id")
    private String personaId;
    
    @JsonProperty("custom_thread_id")
    private String customThreadId;
    
    @JsonProperty("sender_action_id")
    private String senderActionId;
    
    @JsonProperty("custom_payload")
    private Object customPayload;
    
    /**
     * Check if the message was sent successfully
     */
    public boolean isSuccess() {
        return messageId != null && (errors == null || errors.isEmpty());
    }
    
    /**
     * Check if there are any errors
     */
    public boolean hasErrors() {
        return errors != null && !errors.isEmpty();
    }
    
    /**
     * Get the first error message if any
     */
    public String getFirstError() {
        if (hasErrors()) {
            return errors.get(0);
        }
        return null;
    }
}
