package com.chatbot.modules.tenant.core.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Tenant entity - Multi-tenant workspace
 * 
 * v0.1: Simplified core tenant model
 * - UUID primary key for distributed systems
 * - Basic workspace information only
 * - No expiry, profile, or advanced features
 */
@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private TenantStatus status = TenantStatus.ACTIVE;

    @Column(name = "default_locale", nullable = false, length = 10, columnDefinition = "varchar(10) default 'vi'")
    @Builder.Default
    private String defaultLocale = "vi";

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        // Generate UUID if not set
        if (this.id == null) {
            this.id = UUID.randomUUID();
        }
        
        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Business logic helpers
    public boolean isActive() {
        return TenantStatus.ACTIVE.equals(status);
    }
}
