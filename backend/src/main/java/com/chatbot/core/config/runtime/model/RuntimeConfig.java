package com.chatbot.core.config.runtime.model;

import com.chatbot.shared.infrastructure.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "runtime_configs")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuntimeConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", unique = true, nullable = false, length = 200)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "default_value", columnDefinition = "TEXT")
    private String defaultValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "config_type", nullable = false)
    private ConfigType configType;

    @Enumerated(EnumType.STRING)
    @Column(name = "config_scope", nullable = false)
    private ConfigScope configScope;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "is_encrypted", nullable = false)
    private Boolean isEncrypted;

    @Column(name = "is_readonly", nullable = false)
    private Boolean isReadonly;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (version == null) {
            version = 1;
        }
        if (isEncrypted == null) {
            isEncrypted = false;
        }
        if (isReadonly == null) {
            isReadonly = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        version++;
    }
}
