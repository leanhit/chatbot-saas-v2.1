package com.chatbot.core.training.dto;

import com.chatbot.core.training.model.AgentCertification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentCertificationResponse {
    private Long id;
    private Long agentId;
    private Long certificationLevelId;
    private String status;
    private String issuedAt;
    private String expiresAt;
    private String certificateNumber;
    private String issuedBy;
    private String revokedAt;
    private String revocationReason;
    private String revokedBy;
    private String notes;

    public static AgentCertificationResponse from(AgentCertification c) {
        if (c == null) return null;
        return AgentCertificationResponse.builder()
                .id(c.getId()).agentId(c.getAgentId()).certificationLevelId(c.getCertificationLevelId())
                .status(c.getStatus() != null ? c.getStatus().name() : null)
                .issuedAt(c.getIssuedAt()).expiresAt(c.getExpiresAt())
                .certificateNumber(c.getCertificateNumber()).issuedBy(c.getIssuedBy())
                .revokedAt(c.getRevokedAt()).revocationReason(c.getRevocationReason())
                .revokedBy(c.getRevokedBy()).notes(c.getNotes())
                .build();
    }
}
