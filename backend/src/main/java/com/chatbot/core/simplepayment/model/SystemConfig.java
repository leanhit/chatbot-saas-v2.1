package com.chatbot.core.simplepayment.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "system_configs", indexes = {
    @Index(name = "idx_config_key", columnList = "config_key"),
    @Index(name = "idx_config_category", columnList = "config_category")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SystemConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "config_key", nullable = false, unique = true, length = 100)
    private String configKey;

    @Column(name = "config_value", nullable = false, columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "config_category", nullable = false, length = 50)
    private String configCategory;

    @Column(name = "config_type", nullable = false, length = 20)
    @Builder.Default
    private String configType = "STRING";

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "is_sensitive", nullable = false)
    @Builder.Default
    private Boolean isSensitive = false;

    @Column(name = "is_encrypted", nullable = false)
    @Builder.Default
    private Boolean isEncrypted = false;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (configCategory == null) {
            configCategory = "GENERAL";
        }
        if (configType == null) {
            configType = "STRING";
        }
        if (isSensitive == null) {
            isSensitive = false;
        }
        if (isEncrypted == null) {
            isEncrypted = false;
        }
    }
}
