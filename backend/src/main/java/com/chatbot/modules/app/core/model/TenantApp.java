package com.chatbot.modules.app.core.model;

import com.chatbot.modules.app.core.model.AppCode;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TenantApp entity - App enabled/disabled status per tenant
 * 
 * v0.1: Simple app management model
 * - Links tenant to available applications
 * - Tracks enable/disable status and timestamps
 * - No billing, permissions, or configuration
 */
@Entity
@Table(
    name = "tenant_apps",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"tenant_id", "app_code"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantApp {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "app_code", nullable = false, length = 50)
    private AppCode appCode;

    @Column(name = "enabled", nullable = false)
    @Builder.Default
    private Boolean enabled = false;

    @Column(name = "enabled_at")
    private LocalDateTime enabledAt;

    @Column(name = "created_at", updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Enable the app and set enabled timestamp
     */
    public void enable() {
        this.enabled = true;
        this.enabledAt = LocalDateTime.now();
    }

    /**
     * Disable the app and clear enabled timestamp
     */
    public void disable() {
        this.enabled = false;
        this.enabledAt = null;
    }

    // Business logic helpers
    public boolean isEnabled() {
        return Boolean.TRUE.equals(enabled);
    }
}
