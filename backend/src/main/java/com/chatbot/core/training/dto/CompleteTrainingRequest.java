package com.chatbot.core.training.dto;

import lombok.Data;

@Data
public class CompleteTrainingRequest {
    private Integer quizScore;
    private String quizAnswers;
}
