package com.chatbot.core.training.repository;

import com.chatbot.core.training.model.TrainingMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingMaterialRepository extends JpaRepository<TrainingMaterial, Long> {

    List<TrainingMaterial> findByTenantId(Long tenantId);

    List<TrainingMaterial> findByTenantIdAndCategoryId(Long tenantId, Long categoryId);

    List<TrainingMaterial> findByCategoryId(Long categoryId);
}
