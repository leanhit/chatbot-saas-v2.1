package com.chatbot.core.tenant.model;

import com.chatbot.core.tenant.profile.model.TenantProfile;
import com.chatbot.core.tenant.professional.model.TenantProfessional;
import com.chatbot.shared.utils.DateUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String tenantKey; // UUID for frontend

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TenantStatus status = TenantStatus.ACTIVE;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private TenantVisibility visibility = TenantVisibility.PUBLIC;

    @Column(name = "expires_at")
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    private LocalDateTime expiresAt;

    @Column(name = "current_package_id")
    private String currentPackageId; // e.g., 'free', 'pro', 'business', 'enterprise'

    @Column(name = "package_activated_at")
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    private LocalDateTime packageActivatedAt;

    // --- audit ---
    @Column
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    private LocalDateTime createdAt;
    
    @Column
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
        // Generate tenantKey if not set
        if (tenantKey == null || tenantKey.isBlank()) {
            tenantKey = UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @OneToOne(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private TenantProfile profile;

    @OneToOne(mappedBy = "tenant", cascade = CascadeType.ALL, orphanRemoval = true)
    private TenantProfessional professional;

    // Các phương thức prePersist và preUpdate vẫn giữ nguyên vì cần xử lý logic đặc biệt
}
