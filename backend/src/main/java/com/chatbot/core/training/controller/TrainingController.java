package com.chatbot.core.training.controller;

import com.chatbot.core.training.dto.AgentTrainingSummary;
import com.chatbot.core.training.dto.TrainingDashboardStats;
import com.chatbot.core.training.model.*;
import com.chatbot.core.training.service.*;
import com.chatbot.core.training.repository.AgentTrainingProgressRepository;
import com.chatbot.core.message.store.model.Agent;
import com.chatbot.core.message.store.repository.AgentRepository;
import com.chatbot.core.user.model.User;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.service.TenantPermissionValidator;
import com.chatbot.core.tenant.exception.InsufficientPermissionException;
import com.chatbot.shared.exceptions.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.chatbot.core.training.dto.TrainingCategoryResponse;
import com.chatbot.core.training.dto.TrainingMaterialResponse;
import com.chatbot.core.training.dto.AgentSkillResponse;
import com.chatbot.core.training.dto.AgentTrainingProgressResponse;
import com.chatbot.core.training.dto.CertificationLevelResponse;
import com.chatbot.core.training.dto.AgentCertificationResponse;
import com.chatbot.core.training.dto.AgentOnboardingTaskResponse;
import com.chatbot.core.training.dto.AgentOnboardingProgressResponse;
import com.chatbot.core.training.dto.TrainingMaterialRequest;
import com.chatbot.core.training.dto.TrainingCategoryRequest;
import com.chatbot.core.training.dto.AgentTrainingProgressRequest;
import com.chatbot.core.training.dto.AgentSkillRequest;
import com.chatbot.core.training.repository.AgentSkillRepository;

@RestController
@RequestMapping("/api/training")
@RequiredArgsConstructor
public class TrainingController {

    private final TrainingService trainingService;
    private final SkillAssessmentService skillAssessmentService;
    private final CertificationService certificationService;
    private final OnboardingService onboardingService;
    private final TenantPermissionValidator permissionValidator;
    private final AgentTrainingProgressRepository trainingProgressRepository;
    private final AgentRepository agentRepository;
    private final AgentSkillRepository agentSkillRepository;

    private void requireOwnerOrEditor() {
        Long tenantId = TenantContext.getTenantId();
        String email = permissionValidator.getCurrentUserEmail();
        if (tenantId == null || email == null || !permissionValidator.isOwnerOrEditor(tenantId, email)) {
            throw new InsufficientPermissionException(ErrorCode.INSUFFICIENT_PERMISSION,
                    "Only tenant owner or editor can modify training configuration");
        }
    }

    private void requireSelfAgentOrOwnerOrEditor(Long agentId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        Agent agent = agentRepository.findByIdAndTenantId(agentId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Agent not found"));
        User current = permissionValidator.getCurrentUser();
        boolean isSelf = current != null && (
                (agent.getUserId() != null && agent.getUserId().equals(current.getId()))
                || (agent.getEmail() != null && current.getEmail() != null
                    && agent.getEmail().equalsIgnoreCase(current.getEmail())));
        if (!isSelf) {
            requireOwnerOrEditor();
        }
    }

    private Long tenantId() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        return tenantId;
    }

    // Training Materials
    @GetMapping("/materials")
    public ResponseEntity<List<TrainingMaterialResponse>> getAllMaterials(
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) Long categoryId) {
        if (categoryId != null) {
            return ResponseEntity.ok(trainingService.getMaterialsByCategory(categoryId).stream().map(TrainingMaterialResponse::from).toList());
        }
        return ResponseEntity.ok(trainingService.getAllMaterials().stream().map(TrainingMaterialResponse::from).toList());
    }

    @GetMapping("/materials/{id}")
    public ResponseEntity<TrainingMaterialResponse> getMaterialById(@PathVariable Long id) {
        return trainingService.getMaterialById(id)
                .map(m -> ResponseEntity.ok(TrainingMaterialResponse.from(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/materials")
    public ResponseEntity<TrainingMaterialResponse> createTrainingMaterial(@RequestBody TrainingMaterialRequest request) {
        requireOwnerOrEditor();
        TrainingMaterial created = trainingService.createTrainingMaterial(toEntity(request));
        return ResponseEntity.ok(TrainingMaterialResponse.from(created));
    }

    @PutMapping("/materials/{id}")
    public ResponseEntity<TrainingMaterialResponse> updateMaterial(@PathVariable Long id, @RequestBody TrainingMaterialRequest request) {
        requireOwnerOrEditor();
        TrainingMaterial existing = trainingService.getMaterialById(id)
            .orElseThrow(() -> new IllegalArgumentException("Training material not found"));
        applyTo(existing, request);
        TrainingMaterial updated = trainingService.createTrainingMaterial(existing);
        return ResponseEntity.ok(TrainingMaterialResponse.from(updated));
    }

    private TrainingMaterial toEntity(TrainingMaterialRequest r) {
        TrainingMaterial m = new TrainingMaterial();
        applyTo(m, r);
        return m;
    }

    private void applyTo(TrainingMaterial m, TrainingMaterialRequest r) {
        m.setTitle(r.getTitle());
        m.setDescription(r.getDescription());
        m.setContent(r.getContent());
        m.setType(r.getType());
        m.setCategoryId(r.getCategoryId());
        m.setStatus(r.getStatus());
        m.setEstimatedDurationMinutes(r.getEstimatedDurationMinutes());
        m.setRequiredSkills(r.getRequiredSkills());
        m.setDifficultyLevel(r.getDifficultyLevel());
        m.setIsMandatory(r.getIsMandatory());
        m.setSequenceOrder(r.getSequenceOrder());
        m.setPassScoreThreshold(r.getPassScoreThreshold());
        m.setQuizQuestions(r.getQuizQuestions());
        m.setExternalUrl(r.getExternalUrl());
        m.setFileUrl(r.getFileUrl());
    }

    @DeleteMapping("/materials/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long id) {
        requireOwnerOrEditor();
        trainingService.deleteMaterial(id);
        return ResponseEntity.ok().build();
    }

    // Training Categories
    @GetMapping("/categories")
    public ResponseEntity<List<TrainingCategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(trainingService.getAllCategories().stream().map(TrainingCategoryResponse::from).toList());
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<TrainingCategoryResponse> getCategoryById(@PathVariable Long id) {
        return trainingService.getCategoryById(id)
                .map(c -> ResponseEntity.ok(TrainingCategoryResponse.from(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/categories")
    public ResponseEntity<TrainingCategoryResponse> createCategory(@RequestBody TrainingCategoryRequest request) {
        requireOwnerOrEditor();
        TrainingCategory created = trainingService.createCategory(toEntity(request));
        return ResponseEntity.ok(TrainingCategoryResponse.from(created));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<TrainingCategoryResponse> updateCategory(@PathVariable Long id, @RequestBody TrainingCategoryRequest request) {
        requireOwnerOrEditor();
        TrainingCategory existing = trainingService.getCategoryById(id)
            .orElseThrow(() -> new IllegalArgumentException("Training category not found"));
        existing.setName(request.getName());
        existing.setDescription(request.getDescription());
        TrainingCategory updated = trainingService.createCategory(existing);
        return ResponseEntity.ok(TrainingCategoryResponse.from(updated));
    }

    private TrainingCategory toEntity(TrainingCategoryRequest r) {
        TrainingCategory c = new TrainingCategory();
        c.setName(r.getName());
        c.setDescription(r.getDescription());
        return c;
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        requireOwnerOrEditor();
        trainingService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

    // Agent Training Progress
    @GetMapping("/progress/agent/{agentId}")
    public ResponseEntity<List<AgentTrainingProgressResponse>> getAgentProgress(@PathVariable Long agentId) {
        return ResponseEntity.ok(skillAssessmentService.getAgentTrainingProgress(agentId).stream().map(AgentTrainingProgressResponse::from).toList());
    }

    @GetMapping("/progress/material/{materialId}")
    public ResponseEntity<List<AgentTrainingProgressResponse>> getMaterialProgress(@PathVariable Long materialId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        return ResponseEntity.ok(trainingService.getMaterialProgress(materialId, tenantId).stream().map(AgentTrainingProgressResponse::from).toList());
    }

    @PutMapping("/progress/{progressId}")
    public ResponseEntity<AgentTrainingProgressResponse> updateProgress(
            @PathVariable Long progressId,
            @RequestBody AgentTrainingProgressRequest request) {
        AgentTrainingProgress existing = trainingProgressRepository.findById(progressId)
                .orElseThrow(() -> new IllegalArgumentException("Training progress not found"));
        if (!tenantId().equals(existing.getTenantId())) {
            throw new InsufficientPermissionException(ErrorCode.CANNOT_ACCESS_TENANT,
                    "Training progress does not belong to current tenant");
        }
        requireSelfAgentOrOwnerOrEditor(existing.getAgentId());
        // Field whitelist: only mutable progress fields may be updated;
        // id/agentId/trainingId/tenantId always come from the persisted entity.
        existing.setStatus(request.getStatus());
        existing.setProgress(request.getProgress());
        existing.setQuizScore(request.getQuizScore());
        existing.setStartedAt(request.getStartedAt());
        existing.setCompletedAt(request.getCompletedAt());
        existing.setTimeSpentSeconds(request.getTimeSpentSeconds());
        existing.setNotes(request.getNotes());
        return ResponseEntity.ok(AgentTrainingProgressResponse.from(skillAssessmentService.updateTrainingProgress(existing)));
    }

    // Agent Onboarding
    @GetMapping("/onboarding/tasks")
    public ResponseEntity<List<AgentOnboardingTaskResponse>> getAllOnboardingTasks() {
        return ResponseEntity.ok(onboardingService.getAllOnboardingTasks(tenantId()).stream().map(AgentOnboardingTaskResponse::from).toList());
    }

    @GetMapping("/onboarding/progress/agent/{agentId}")
    public ResponseEntity<List<AgentOnboardingProgressResponse>> getAgentOnboardingProgress(
            @PathVariable Long agentId) {
        return ResponseEntity.ok(onboardingService.getAgentOnboardingProgress(agentId, tenantId()).stream().map(AgentOnboardingProgressResponse::from).toList());
    }

    @PostMapping("/onboarding/tasks/{taskId}/start")
    public ResponseEntity<AgentOnboardingProgressResponse> startOnboardingTask(
            @PathVariable Long taskId,
            @RequestBody StartTaskRequest request) {
        return ResponseEntity.ok(AgentOnboardingProgressResponse.from(onboardingService.startTask(request.agentId, taskId, tenantId())));
    }

    // Agent Skills
    @GetMapping("/skills/agent/{agentId}")
    public ResponseEntity<List<AgentSkillResponse>> getAgentSkills(@PathVariable Long agentId) {
        return ResponseEntity.ok(skillAssessmentService.getAgentSkills(agentId).stream().map(AgentSkillResponse::from).toList());
    }

    @PutMapping("/skills/{skillId}")
    public ResponseEntity<AgentSkillResponse> updateAgentSkill(
            @PathVariable Long skillId,
            @RequestBody AgentSkillRequest request) {
        requireOwnerOrEditor();
        AgentSkill existing = agentSkillRepository.findById(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Agent skill not found"));
        if (!tenantId().equals(existing.getTenantId())) {
            throw new InsufficientPermissionException(ErrorCode.CANNOT_ACCESS_TENANT,
                    "Agent skill does not belong to current tenant");
        }
        // Field whitelist: agentId/tenantId/id always come from the persisted entity.
        existing.setSkillCategory(request.getSkillCategory());
        existing.setScore(request.getScore());
        existing.setProficiencyLevel(request.getProficiencyLevel());
        existing.setLastAssessedAt(request.getLastAssessedAt());
        existing.setAssessmentMethod(request.getAssessmentMethod());
        existing.setTrainingId(request.getTrainingId());
        existing.setPreviousScore(request.getPreviousScore());
        existing.setAssessmentCount(request.getAssessmentCount());
        existing.setNotes(request.getNotes());
        return ResponseEntity.ok(AgentSkillResponse.from(skillAssessmentService.updateAgentSkill(existing)));
    }

    // Certifications
    @GetMapping("/certifications/levels")
    public ResponseEntity<List<CertificationLevelResponse>> getAllCertificationLevels() {
        return ResponseEntity.ok(certificationService.getAllCertificationLevels(tenantId()).stream().map(CertificationLevelResponse::from).toList());
    }

    @GetMapping("/certifications/agent/{agentId}")
    public ResponseEntity<List<AgentCertificationResponse>> getAgentCertifications(
            @PathVariable Long agentId) {
        return ResponseEntity.ok(certificationService.getAgentCertifications(agentId, tenantId()).stream().map(AgentCertificationResponse::from).toList());
    }

    @PostMapping("/certifications/award")
    public ResponseEntity<AgentCertificationResponse> awardCertification(
            @RequestBody AwardCertificationRequest request) {
        requireOwnerOrEditor();
        return ResponseEntity.ok(AgentCertificationResponse.from(certificationService.awardCertification(
                request.agentId,
                request.certificationLevelId,
                request.issuedBy,
                tenantId()
        )));
    }

    // Training Dashboard Statistics
    @GetMapping("/dashboard/stats")
    public ResponseEntity<TrainingDashboardStats> getTrainingDashboardStats() {
        return ResponseEntity.ok(trainingService.getTrainingDashboardStats(tenantId()));
    }

    @GetMapping("/dashboard/agents")
    public ResponseEntity<List<AgentTrainingSummary>> getAgentsWithTrainingStats() {
        return ResponseEntity.ok(trainingService.getAgentsWithTrainingStats(tenantId()));
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
