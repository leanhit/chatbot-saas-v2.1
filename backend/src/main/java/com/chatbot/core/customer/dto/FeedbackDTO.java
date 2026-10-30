package com.chatbot.core.customer.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeedbackDTO {
    private Long id;
    private Long customerProfileId;
    private Long conversationId;
    private String type;
    private String source;
    private Integer rating;
    private String comment;
    private String category;
    private Boolean isAnonymous;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String reviewedBy;
    private String status;
    private String adminResponse;
    private LocalDateTime responseAt;
}
