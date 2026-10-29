package com.chatbot.core.training.model;

import com.chatbot.core.tenant.infra.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Training Category entity for organizing training materials
 */
@Data
@Entity
@Table(name = "training_categories")
@EqualsAndHashCode(callSuper = true)
public class TrainingCategory extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Override
    public Long getId() {
        return id;
    }
}
