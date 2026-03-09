package com.chatbot.spokes.facebook.webhook.dto;

import lombok.Data;
import java.util.List;

/**
 * Facebook Webhook Request DTO
 * Matches traloitudongV2 structure
 */
@Data
public class WebhookRequest {
    private String object;
    private List<Entry> entry;

    @Data
    public static class Entry {
        private String id;
        private Long time;
        private List<Messaging> messaging;
    }

    @Data
    public static class Messaging {
        private Sender sender;
        private Recipient recipient;
        private Long timestamp;
        private Message message;
        private Postback postback;
        private Delivery delivery;
        private Read read;
        private Reaction reaction;
        private QuickReply quickReply;
    }

    @Data
    public static class Sender {
        private String id;
    }

    @Data
    public static class Recipient {
        private String id;
    }

    @Data
    public static class Message {
        private String mid;
        private String text;
        private Boolean isEcho;
        private List<Attachment> attachments;
        private QuickReply quickReply;
    }

    @Data
    public static class Postback {
        private String payload;
        private String title;
    }

    @Data
    public static class Delivery {
        private List<String> mids;
        private Long watermark;
        private Long sequence_number;
    }

    @Data
    public static class Read {
        private Long watermark;
        private Long sequence_number;
    }

    @Data
    public static class Reaction {
        private String mid;
        private String reaction;
        private String emoji;
        private String action;
    }

    @Data
    public static class QuickReply {
        private String payload;
        private String title;
    }

    @Data
    public static class Attachment {
        private String type;
        private Payload payload;
    }

    @Data
    public static class Payload {
        private String url;
    }
}
