package com.chatbot.core.training.controller;

import com.chatbot.core.training.dto.AgentTrainingSummary;
import com.chatbot.core.training.dto.TrainingDashboardStats;
import com.chatbot.core.training.model.*;
import com.chatbot.core.training.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;
    private final SkillAssessmentService skillAssessmentService;
    private final CertificationService certificationService;
    private final OnboardingService onboardingService;

    // Training Materials
    @GetMapping("/materials")
    public ResponseEntity<List<TrainingMaterial>> getAllMaterials(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long categoryId) {
        if (categoryId != null) {
            return ResponseEntity.ok(trainingService.getMaterialsByCategory(categoryId));
        }
        return ResponseEntity.ok(trainingService.getAllMaterials());
    }

    @GetMapping("/materials/{id}")
    public ResponseEntity<TrainingMaterial> getMaterialById(@PathVariable Long id) {
        return trainingService.getMaterialById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/materials")
    public ResponseEntity<TrainingMaterial> createTrainingMaterial(@RequestBody TrainingMaterial trainingMaterial) {
        TrainingMaterial created = trainingService.createTrainingMaterial(trainingMaterial);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/materials/{id}")
    public ResponseEntity<TrainingMaterial> updateMaterial(@PathVariable Long id, @RequestBody TrainingMaterial trainingMaterial) {
        TrainingMaterial existing = trainingService.getMaterialById(id)
            .orElseThrow(() -> new IllegalArgumentException("Training material not found"));
        
        // Update fields from request
        existing.setTitle(trainingMaterial.getTitle());
        existing.setDescription(trainingMaterial.getDescription());
        existing.setContent(trainingMaterial.getContent());
        existing.setType(trainingMaterial.getType());
        existing.setCategoryId(trainingMaterial.getCategoryId());
        existing.setStatus(trainingMaterial.getStatus());
        existing.setEstimatedDurationMinutes(trainingMaterial.getEstimatedDurationMinutes());
        existing.setRequiredSkills(trainingMaterial.getRequiredSkills());
        existing.setDifficultyLevel(trainingMaterial.getDifficultyLevel());
        existing.setIsMandatory(trainingMaterial.getIsMandatory());
        existing.setSequenceOrder(trainingMaterial.getSequenceOrder());
        existing.setPassScoreThreshold(trainingMaterial.getPassScoreThreshold());
        existing.setQuizQuestions(trainingMaterial.getQuizQuestions());
        existing.setExternalUrl(trainingMaterial.getExternalUrl());
        existing.setFileUrl(trainingMaterial.getFileUrl());
        
        TrainingMaterial updated = trainingService.createTrainingMaterial(existing);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/materials/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id) {
        trainingService.deleteMaterial(id);
        return ResponseEntity.ok().build();
    }

    // Training Categories
    @GetMapping("/categories")
    public ResponseEntity<List<TrainingCategory>> getAllCategories() {
        return ResponseEntity.ok(trainingService.getAllCategories());
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<TrainingCategory> getCategoryById(@PathVariable Long id) {
        return trainingService.getCategoryById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/categories")
    public ResponseEntity<TrainingCategory> createCategory(@RequestBody TrainingCategory category) {
        TrainingCategory created = trainingService.createCategory(category);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<TrainingCategory> updateCategory(@PathVariable Long id, @RequestBody TrainingCategory category) {
        TrainingCategory existing = trainingService.getCategoryById(id)
            .orElseThrow(() -> new IllegalArgumentException("Training category not found"));
        
        // Update fields from request
        existing.setName(category.getName());
        existing.setDescription(category.getDescription());
        
        TrainingCategory updated = trainingService.createCategory(existing);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        trainingService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

    // Agent Training Progress
    @GetMapping("/progress/agent/{agentId}")
    public ResponseEntity<List<AgentTrainingProgress>> getAgentProgress(@PathVariable Long agentId) {
        return ResponseEntity.ok(skillAssessmentService.getAgentTrainingProgress(agentId));
    }

    @GetMapping("/progress/material/{materialId}")
    public ResponseEntity<List<AgentTrainingProgress>> getMaterialProgress(@PathVariable Long materialId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        return ResponseEntity.ok(trainingService.getMaterialProgress(materialId, tenantId));
    }

    @PutMapping("/progress/{progressId}")
    public ResponseEntity<AgentTrainingProgress> updateProgress(
            @PathVariable Long progressId,
            @RequestBody AgentTrainingProgress progress) {
        progress.setId(progressId);
        return ResponseEntity.ok(skillAssessmentService.updateTrainingProgress(progress));
    }

    // Agent Onboarding
    @GetMapping("/onboarding/tasks")
    public ResponseEntity<List<AgentOnboardingTask>> getAllOnboardingTasks(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        return ResponseEntity.ok(onboardingService.getAllOnboardingTasks(tenantId));
    }

    @GetMapping("/onboarding/progress/agent/{agentId}")
    public ResponseEntity<List<AgentOnboardingProgress>> getAgentOnboardingProgress(
            @PathVariable Long agentId,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        return ResponseEntity.ok(onboardingService.getAgentOnboardingProgress(agentId, tenantId));
    }

    @PostMapping("/onboarding/tasks/{taskId}/start")
    public ResponseEntity<AgentOnboardingProgress> startOnboardingTask(
            @PathVariable Long taskId,
            @RequestBody StartTaskRequest request,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        return ResponseEntity.ok(onboardingService.startTask(request.agentId, taskId, tenantId));
    }

    // Agent Skills
    @GetMapping("/skills/agent/{agentId}")
    public ResponseEntity<List<AgentSkill>> getAgentSkills(@PathVariable Long agentId) {
        return ResponseEntity.ok(skillAssessmentService.getAgentSkills(agentId));
    }

    @PutMapping("/skills/{skillId}")
    public ResponseEntity<AgentSkill> updateAgentSkill(
            @PathVariable Long skillId,
            @RequestBody AgentSkill skill) {
        skill.setId(skillId);
        return ResponseEntity.ok(skillAssessmentService.updateAgentSkill(skill));
    }

    // Certifications
    @GetMapping("/certifications/levels")
    public ResponseEntity<List<CertificationLevel>> getAllCertificationLevels(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        return ResponseEntity.ok(certificationService.getAllCertificationLevels(tenantId));
    }

    @GetMapping("/certifications/agent/{agentId}")
    public ResponseEntity<List<AgentCertification>> getAgentCertifications(
            @PathVariable Long agentId,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        return ResponseEntity.ok(certificationService.getAgentCertifications(agentId, tenantId));
    }

    @PostMapping("/certifications/award")
    public ResponseEntity<AgentCertification> awardCertification(
            @RequestBody AwardCertificationRequest request,
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        return ResponseEntity.ok(certificationService.awardCertification(
                request.agentId,
                request.certificationLevelId,
                request.issuedBy,
                tenantId
        ));
    }

    // Training Dashboard Statistics
    @GetMapping("/dashboard/stats")
    public ResponseEntity<TrainingDashboardStats> getTrainingDashboardStats(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        return ResponseEntity.ok(trainingService.getTrainingDashboardStats(tenantId));
    }

    @GetMapping("/dashboard/agents")
    public ResponseEntity<List<AgentTrainingSummary>> getAgentsWithTrainingStats(
            @RequestHeader("X-Tenant-Id") Long tenantId) {
        return ResponseEntity.ok(trainingService.getAgentsWithTrainingStats(tenantId));
    }

    // DTOs
    public static class StartTaskRequest {
        public Long agentId;
    }

    public static class AwardCertificationRequest {
        public Long agentId;
        public Long certificationLevelId;
        public String issuedBy;
    }
}
