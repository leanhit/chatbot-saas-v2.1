package com.chatbot.core.message.store.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.chatbot.shared.utils.DateUtils;
import java.time.LocalDateTime;

/**
 * Auto-assign configuration entity for tenant-specific auto-assignment settings
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "auto_assign_configs",
    indexes = {
        @Index(name = "idx_auto_assign_tenant", columnList = "tenant_id")
    }
)
@EqualsAndHashCode(callSuper = true)
public class AutoAssignConfig extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Whether auto-assignment is enabled for this tenant
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * Auto-assignment interval in seconds
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer intervalSeconds = 30;

    /**
     * Maximum concurrent conversations per agent
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer maxConcurrentPerAgent = 5;

    /**
     * Configuration description
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    // Timestamp
    @CreationTimestamp
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime updatedAt;

    @Column(name = "tenant_id")
    private Long tenantId;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public Long getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
