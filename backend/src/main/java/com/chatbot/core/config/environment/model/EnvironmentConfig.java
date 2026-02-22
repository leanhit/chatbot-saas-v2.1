package com.chatbot.core.config.environment.model;

import com.chatbot.shared.infrastructure.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "environment_configs")
@Data
@EqualsAndHashCode(callSuper = false)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentConfig extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "environment", nullable = false, length = 50)
    private String environment;

    @Column(name = "config_key", nullable = false, length = 200)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "is_encrypted", nullable = false)
    private Boolean isEncrypted;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isEncrypted == null) {
            isEncrypted = false;
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
