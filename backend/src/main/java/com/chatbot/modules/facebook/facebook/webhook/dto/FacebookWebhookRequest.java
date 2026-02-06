package com.chatbot.modules.facebook.facebook.webhook.dto;

import lombok.*;

/**
 * Facebook Webhook Request DTO
 * Represents the complete Facebook webhook payload
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacebookWebhookRequest {

    private String object;
    private FacebookWebhookEntry[] entry;

    public boolean isPageEvent() {
        return "page".equals(object);
    }

    public boolean hasMessages() {
        return entry != null && entry.length > 0 
                && entry[0].getMessaging() != null 
                && entry[0].getMessaging().length > 0;
    }

    public FacebookMessaging getFirstMessage() {
        if (hasMessages()) {
            return entry[0].getMessaging()[0];
        }
        return null;
    }
}
