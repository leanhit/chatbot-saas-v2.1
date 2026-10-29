package com.chatbot.core.training.dto;

import lombok.Data;

@Data
public class AwardCertificationRequest {
    private Long agentId;
    private Long certificationLevelId;
    private String issuedBy;
}
