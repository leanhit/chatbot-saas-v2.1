package com.chatbot.core.tenant.professional.model;

import com.chatbot.core.tenant.model.Tenant;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "tenant_professionals",
    indexes = {
        @Index(name = "idx_tenant_professional_tenant", columnList = "tenant_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantProfessional {

    /**
     * Dùng chung ID với Tenant (giống UserInfo ↔ Auth)
     */
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    // ===== Professional Information =====
    @Column(name = "job_title")
    private String jobTitle;

    @Column(name = "department")
    private String department;

    @Column(length = 200)
    private String company;

    @Column(name = "linkedin_url")
    private String linkedinUrl;

    @Column(name = "website")
    private String website;

    @Column(name = "location")
    private String location;

    @Column(name = "skills")
    private String skills;

    @Column(name = "experience")
    private String experience;

    @Column(name = "education")
    private String education;

    @Column(name = "certifications")
    private String certifications;

    @Column(name = "languages")
    private String languages;

    @Column(name = "availability")
    private String availability;

    @Column(name = "hourly_rate")
    private String hourlyRate;

    @Column(name = "portfolio_url")
    private String portfolioUrl;

    // ===== audit =====
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
