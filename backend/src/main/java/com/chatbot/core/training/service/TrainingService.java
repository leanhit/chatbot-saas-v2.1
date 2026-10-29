package com.chatbot.core.training.service;

import com.chatbot.core.training.model.TrainingMaterial;
import com.chatbot.core.training.repository.TrainingMaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrainingService {

    private final TrainingMaterialRepository trainingMaterialRepository;

    public TrainingMaterial createTrainingMaterial(TrainingMaterial trainingMaterial) {
        return trainingMaterialRepository.save(trainingMaterial);
    }
}
