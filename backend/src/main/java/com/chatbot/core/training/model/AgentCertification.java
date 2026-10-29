package com.chatbot.core.training.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Agent Certification entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "agent_certifications")
@EqualsAndHashCode(callSuper = true)
public class AgentCertification extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Column(name = "certification_level_id", nullable = false)
    private Long certificationLevelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CertificationStatus status;

    @Column(name = "issued_at")
    private String issuedAt;

    @Column(name = "expires_at")
    private String expiresAt;

    @Column(nullable = false, unique = true)
    private String certificateNumber;

    @Column(name = "issued_by")
    private String issuedBy;

    @Column(name = "revoked_at")
    private String revokedAt;

    @Column(name = "revocation_reason", columnDefinition = "TEXT")
    private String revocationReason;

    @Column(name = "revoked_by")
    private String revokedBy;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Override
    public Long getId() {
        return id;
    }

    public enum CertificationStatus {
        ACTIVE, EXPIRED, REVOKED
    }
}
