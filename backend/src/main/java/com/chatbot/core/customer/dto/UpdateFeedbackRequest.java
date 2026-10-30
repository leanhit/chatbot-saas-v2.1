package com.chatbot.core.customer.dto;

import lombok.Data;

@Data
public class UpdateFeedbackRequest {
    private String status;
    private String adminResponse;
}
