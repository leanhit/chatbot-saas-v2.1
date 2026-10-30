package com.chatbot.core.customer.dto;

import lombok.Data;

@Data
public class CreateFeedbackRequest {
    private Long customerProfileId;
    private Long conversationId;
    private String type;
    private String source;
    private Integer rating;
    private String comment;
    private String category;
    private Boolean isAnonymous;
}
