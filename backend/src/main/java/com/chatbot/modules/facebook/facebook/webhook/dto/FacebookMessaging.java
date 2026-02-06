package com.chatbot.modules.facebook.facebook.webhook.dto;

import lombok.*;

/**
 * Facebook Messaging DTO
 * Represents individual message from Facebook webhook
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacebookMessaging {

    private FacebookSender sender;
    private FacebookRecipient recipient;
    private Long timestamp;
    private FacebookMessage message;
    private FacebookPostback postback;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FacebookSender {
        private String id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FacebookRecipient {
        private String id;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FacebookMessage {
        private String text;
        private String mid;
        private Long seq;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FacebookPostback {
        private String payload;
        private String title;
    }
}
