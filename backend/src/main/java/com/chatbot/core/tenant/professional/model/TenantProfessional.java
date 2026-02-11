package com.chatbot.core.tenant.professional.model;

import com.chatbot.core.tenant.core.model.Tenant;
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
    @Column(length = 100)
    private String jobTitle;

    @Column(length = 100)
    private String department;

    @Column(length = 200)
    private String company;

    @Column(length = 500)
    private String linkedinUrl;

    @Column(length = 500)
    private String website;

    @Column(length = 200)
    private String location;

    @Column(length = 1000)
    private String skills;

    @Column(length = 1000)
    private String experience;

    @Column(length = 500)
    private String education;

    @Column(length = 500)
    private String certifications;

    @Column(length = 200)
    private String languages;

    private String availability;

    @Column(length = 100)
    private String hourlyRate;

    @Column(length = 500)
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
