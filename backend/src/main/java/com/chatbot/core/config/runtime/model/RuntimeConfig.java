package com.chatbot.core.config.runtime.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
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
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuntimeConfig extends BaseTenantEntity {

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

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public Long getTenantId() {
        return super.getTenantId();
    }

    @Override
    public void setTenantId(Long tenantId) {
        super.setTenantId(tenantId);
    }

    @PrePersist
    protected void onCreate() {
        super.onCreate();
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
        super.onUpdate();
        version++;
    }
}
