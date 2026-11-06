package com.chatbot.core.training.repository;

import com.chatbot.core.training.model.TrainingMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrainingMaterialRepository extends JpaRepository<TrainingMaterial, Long> {

    List<TrainingMaterial> findByTenantId(Long tenantId);

    Optional<TrainingMaterial> findByIdAndTenantId(Long id, Long tenantId);

    List<TrainingMaterial> findByTenantIdAndCategoryId(Long tenantId, Long categoryId);

    List<TrainingMaterial> findByCategoryId(Long categoryId);
}
