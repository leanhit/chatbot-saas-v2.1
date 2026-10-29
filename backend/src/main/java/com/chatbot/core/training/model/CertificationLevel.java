package com.chatbot.core.training.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * Certification Level entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "certification_levels")
@EqualsAndHashCode(callSuper = true)
public class CertificationLevel extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String levelName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JdbcTypeCode(SqlTypes.JSON)
    private String requiredTrainingIds;

    @JdbcTypeCode(SqlTypes.JSON)
    private String requiredSkillScores;

    @JdbcTypeCode(SqlTypes.JSON)
    private String requiredStats;

    @Column(name = "badge_icon")
    private String badgeIcon;

    @Column(name = "badge_color")
    private String badgeColor;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(nullable = false)
    private Boolean active;

    @Column(name = "validity_days")
    private Integer validityDays;

    @Override
    public Long getId() {
        return id;
    }
}
