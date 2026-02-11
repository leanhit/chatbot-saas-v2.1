package com.chatbot.modules.tenant.profile.model;

import com.chatbot.modules.tenant.core.model.Tenant;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "tenant_profiles",
    indexes = {
        @Index(name = "idx_tenant_profile_tenant", columnList = "tenant_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantProfile {

    /**
     * Dùng chung ID với Tenant (giống UserInfo ↔ Auth)
     */
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    // ===== Mô tả / hiển thị =====
    @Column(length = 1000)
    private String description;

    // ===== Business Information =====
    @Column(length = 100)
    private String industry;

    @Column(length = 50)
    private String plan;

    @Column(length = 50)
    private String companySize;

    // ===== Thông tin pháp lý =====
    private String legalName;

    @Column(length = 50)
    private String taxCode;

    // ===== Thông tin liên hệ =====
    @Column(length = 100)
    private String contactEmail;

    @Column(length = 20)
    private String contactPhone;

    // ===== Branding / White-label =====
    private String logoUrl;
    private String faviconUrl;

    @Column(length = 7)
    private String primaryColor; // #RRGGBB

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
