package com.chatbot.spokes.facebook.webhook.model;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Facebook Webhook Event Model
 */
@Data
public class WebhookEvent {
    
    @JsonProperty("object")
    private String object;
    
    @JsonProperty("entry")
    private Entry[] entry;
    
    @Data
    public static class Entry {
        private String id;
        private Long time;
        private Messaging[] messaging;
    }
    
    @Data
    public static class Messaging {
        private Sender sender;
        private Recipient recipient;
        private Long timestamp;
        private Message message;
        private Postback postback;
        private Optin optin;
        private Referral referral;
    }
    
    @Data
    public static class Sender {
        @JsonProperty("user_id")
        private String userId;
    }
    
    @Data
    public static class Recipient {
        @JsonProperty("page_id")
        private String pageId;
    }
    
    @Data
    public static class Message {
        private String mid;
        private String text;
        private Attachment[] attachments;
        private QuickReply quickReply;
    }
    
    @Data
    public static class Postback {
        private String payload;
        private String title;
    }
    
    @Data
    public static class Optin {
        private String payload;
        private String type;
    }
    
    @Data
    public static class Referral {
        private String source;
        private String type;
        private String ref;
    }
    
    @Data
    public static class Attachment {
        private String type;
        private Payload payload;
    }
    
    @Data
    public static class Payload {
        private String url;
        private String sticker_id;
    }
    
    @Data
    public static class QuickReply {
        private String payload;
    }
}
