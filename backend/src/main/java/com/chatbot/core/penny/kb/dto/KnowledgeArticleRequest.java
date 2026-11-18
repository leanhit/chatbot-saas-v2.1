package com.chatbot.core.penny.kb.dto;

import lombok.Data;

@Data
public class KnowledgeArticleRequest {
    private String title;
    private String content;
    private String category;
    private String tags;
    private String sourceUrl;
    private Boolean isActive;
    private Integer priority;
}
