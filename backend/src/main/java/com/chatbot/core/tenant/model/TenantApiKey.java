package com.chatbot.core.tenant.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.chatbot.shared.utils.DateUtils;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Tenant API key for programmatic access to /api/tenant-api/**.
 * Only the SHA-256 hash of the key is stored; the raw key is shown once at creation.
 */
@Entity
@Table(name = "tenant_api_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantApiKey {

    public enum KeyRole { OWNER, EDITOR, MEMBER }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "key_prefix", nullable = false, length = 20)
    private String keyPrefix;

    @Column(name = "key_hash", nullable = false, length = 128)
    private String keyHash;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private KeyRole role = KeyRole.MEMBER;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Builder.Default
    @Column(name = "usage_count", nullable = false)
    private Long usageCount = 0L;

    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_by")
    private String createdBy;

    @CreationTimestamp
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonFormat(pattern = DateUtils.API_DATETIME_FORMAT, timezone = DateUtils.API_TIMEZONE)
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public boolean isValid() {
        if (!Boolean.TRUE.equals(isActive)) {
            return false;
        }
        return expiresAt == null || expiresAt.isAfter(LocalDateTime.now());
    }

    public void recordUsage() {
        this.lastUsedAt = LocalDateTime.now();
        this.usageCount = (this.usageCount == null ? 0 : this.usageCount) + 1;
    }
}
