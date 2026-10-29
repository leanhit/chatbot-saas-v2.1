package com.chatbot.core.training.dto;

import lombok.Data;

@Data
public class AgentCertificationDTO {
    private Long id;
    private Long agentId;
    private Long certificationLevelId;
    private String status;
    private String certificateNumber;
    private String issuedAt;
    private String expiresAt;
}
