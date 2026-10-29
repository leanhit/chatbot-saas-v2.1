package com.chatbot.core.training.repository;

import com.chatbot.core.training.model.TrainingMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingMaterialRepository extends JpaRepository<TrainingMaterial, Long> {
}
