package com.chatbot.core.training.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TrainingResponse {
    private Long id;
    private String title;
    private String status;
    private String message;
}
