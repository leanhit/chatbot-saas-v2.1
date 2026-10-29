package com.chatbot.core.training.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Agent Skill entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "agent_skills")
@EqualsAndHashCode(callSuper = true)
public class AgentSkill extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SkillCategory skillCategory;

    @Column(nullable = false)
    private Integer score;

    @Enumerated(EnumType.STRING)
    private ProficiencyLevel proficiencyLevel;

    @Column(name = "last_assessed_at")
    private String lastAssessedAt;

    @Enumerated(EnumType.STRING)
    private AssessmentMethod assessmentMethod;

    @Column(name = "training_id")
    private Long trainingId;

    @Column(name = "previous_score")
    private Integer previousScore;

    @Column(name = "assessment_count")
    private Integer assessmentCount;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Override
    public Long getId() {
        return id;
    }

    public enum SkillCategory {
        COMMUNICATION, TECHNICAL, PROBLEM_SOLVING, PRODUCT_KNOWLEDGE, 
        SALES, BILLING, LANGUAGE, CUSTOMER_SERVICE
    }

    public enum ProficiencyLevel {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }

    public enum AssessmentMethod {
        QUIZ, MANUAL, AUTO, CERTIFICATION
    }
}
