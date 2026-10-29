package com.chatbot.core.training.controller;

import com.chatbot.core.training.model.TrainingMaterial;
import com.chatbot.core.training.service.TrainingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;

    @PostMapping("/materials")
    public ResponseEntity<TrainingMaterial> createTrainingMaterial(@RequestBody TrainingMaterial trainingMaterial) {
        TrainingMaterial created = trainingService.createTrainingMaterial(trainingMaterial);
        return ResponseEntity.ok(created);
    }
}
