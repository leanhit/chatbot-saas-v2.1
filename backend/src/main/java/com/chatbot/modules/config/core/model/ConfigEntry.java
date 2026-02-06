package com.chatbot.modules.config.core.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Config Entry entity for SaaS Ecosystem Config Hub
 * Stores configuration values with different scopes
 */
@Entity
@Table(name = "config_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", nullable = false, length = 100)
    private String key;

    @Column(name = "config_value", nullable = false, length = 1000)
    private String value;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 20)
    private ConfigScope scope;

    @Column(name = "scope_id")
    private UUID scopeId;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_at", nullable = false)
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
     * Configuration scope enumeration
     */
    public enum ConfigScope {
        SYSTEM,      // System-wide configuration
        TENANT,      // Tenant-specific configuration
        APP,         // App-specific configuration
        TENANT_APP   // Tenant-specific app configuration
    }
}
