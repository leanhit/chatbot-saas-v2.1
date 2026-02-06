package com.chatbot.modules.facebook.facebook.webhook.dto;

import lombok.*;

/**
 * Facebook Webhook Entry DTO
 * Represents entry from Facebook webhook payload
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacebookWebhookEntry {

    private String id;
    private Long time;
    private FacebookMessaging[] messaging;
}
