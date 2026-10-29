package com.chatbot.core.training.dto;

import lombok.Data;

@Data
public class CertificationLevelDTO {
    private Long id;
    private String levelName;
    private String description;
    private String badgeIcon;
    private String badgeColor;
    private Boolean active;
}
