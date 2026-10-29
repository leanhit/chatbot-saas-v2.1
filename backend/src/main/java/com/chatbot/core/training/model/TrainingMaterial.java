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

import java.util.Set;

/**
 * Training Material entity for agent training system
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "training_materials")
@EqualsAndHashCode(callSuper = true)
public class TrainingMaterial extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainingType type;

    @Column(name = "category_id")
    private Long categoryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainingStatus status;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @JdbcTypeCode(SqlTypes.JSON)
    private Set<String> requiredSkills;

    @Enumerated(EnumType.STRING)
    private DifficultyLevel difficultyLevel;

    @Column(name = "is_mandatory")
    private Boolean isMandatory;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Column(name = "pass_score_threshold")
    private Integer passScoreThreshold;

    @JdbcTypeCode(SqlTypes.JSON)
    private String quizQuestions;

    @Column(name = "external_url")
    private String externalUrl;

    @Column(name = "file_url")
    private String fileUrl;

    @Override
    public Long getId() {
        return id;
    }

    public enum TrainingType {
        DOCUMENT, VIDEO, QUIZ, INTERACTIVE, EXTERNAL
    }

    public enum TrainingStatus {
        DRAFT, PUBLISHED, ARCHIVED
    }

    public enum DifficultyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
}
