package com.chatbot.spokes.facebook.webhook.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageDto {
    private String mid;
    private String text;
    private List<AttachmentDto> attachments;
    private QuickReplyDto quick_reply;
    private List<EntityDto> entities;
    private Boolean is_echo;
    private String app_id;
    private Map<String, Object> metadata;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntityDto {
        private String type;
        private String text;
        private Double offset;
        private Double length;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuickReplyDto {
        private String title;
        private String payload;
        private String content_type;
        private Map<String, Object> image_url;
    }
}
