package com.chatbot.core.penny.kb.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class KnowledgeArticleRequest {
    @NotBlank(message = "title is required")
    private String title;
    @NotBlank(message = "content is required")
    private String content;
    private String category;
    private String tags;
    private String sourceUrl;
    private Boolean isActive;
    private Integer priority;
}
