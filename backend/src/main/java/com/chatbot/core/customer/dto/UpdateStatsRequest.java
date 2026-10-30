package com.chatbot.core.customer.dto;

import lombok.Data;

@Data
public class UpdateStatsRequest {
    private Integer messageCount;
    private Integer responseTimeSeconds;
}
