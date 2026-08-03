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
 * Skill entity for skills-based routing
 * Implements Phase 3.2: Skills-based Routing
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "skills",
    indexes = {
        @Index(name = "idx_skill_tenant", columnList = "tenant_id"),
        @Index(name = "idx_skill_category", columnList = "category")
    }
)
@EqualsAndHashCode(callSuper = true)
public class Skill extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Skill name (e.g., "billing", "technical", "sales", "vietnamese", "english")
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Skill category (TECHNICAL, LANGUAGE, PRODUCT, SOFT_SKILL)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillCategory category;

    /**
     * Skill description
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Whether skill is active
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    /**
     * Skill level (BEGINNER, INTERMEDIATE, ADVANCED, EXPERT)
     */
    @Enumerated(EnumType.STRING)
    private SkillLevel level;

    // Timestamp
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
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

    /**
     * Skill Category Enum
     */
    public enum SkillCategory {
        TECHNICAL,
        LANGUAGE,
        PRODUCT,
        SOFT_SKILL
    }

    /**
     * Skill Level Enum
     */
    public enum SkillLevel {
        BEGINNER,
        INTERMEDIATE,
        ADVANCED,
        EXPERT
    }
}
