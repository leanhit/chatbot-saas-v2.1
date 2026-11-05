package com.chatbot.core.training.repository;

import com.chatbot.core.training.model.TrainingCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingCategoryRepository extends JpaRepository<TrainingCategory, Long> {
    List<TrainingCategory> findByTenantId(Long tenantId);
}
