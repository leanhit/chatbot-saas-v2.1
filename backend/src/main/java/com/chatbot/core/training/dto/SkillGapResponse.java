package com.chatbot.core.training.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SkillGapResponse {
    private String category;
    private Integer currentScore;
    private Integer targetScore;
    private Integer gap;
    private String currentLevel;
}
