package com.chatbot.core.training.dto;

import lombok.Data;

@Data
public class UpdateTrainingProgressRequest {
    private Integer progress;
    private String notes;
}
