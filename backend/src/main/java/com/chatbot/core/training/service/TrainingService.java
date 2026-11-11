package com.chatbot.core.training.service;

import com.chatbot.core.message.store.model.Agent;
import com.chatbot.core.message.store.repository.AgentRepository;
import com.chatbot.core.message.store.service.AgentAssignmentService;
import com.chatbot.core.training.dto.AgentTrainingSummary;
import com.chatbot.core.training.dto.TrainingDashboardStats;
import com.chatbot.core.training.model.AgentCertification;
import com.chatbot.core.training.model.AgentOnboardingProgress;
import com.chatbot.core.training.model.AgentSkill;
import com.chatbot.core.training.model.AgentTrainingProgress;
import com.chatbot.core.training.model.CertificationLevel;
import com.chatbot.core.training.model.TrainingCategory;
import com.chatbot.core.training.model.TrainingMaterial;
import com.chatbot.core.training.repository.AgentCertificationRepository;
import com.chatbot.core.training.repository.AgentOnboardingProgressRepository;
import com.chatbot.core.training.repository.AgentSkillRepository;
import com.chatbot.core.training.repository.AgentTrainingProgressRepository;
import com.chatbot.core.training.repository.CertificationLevelRepository;
import com.chatbot.core.training.repository.TrainingCategoryRepository;
import com.chatbot.core.training.repository.TrainingMaterialRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrainingService {

    private final TrainingMaterialRepository trainingMaterialRepository;
    private final TrainingCategoryRepository trainingCategoryRepository;
    private final AgentTrainingProgressRepository trainingProgressRepository;
    private final AgentSkillRepository agentSkillRepository;
    private final AgentRepository agentRepository;
    private final AgentCertificationRepository certificationRepository;
    private final AgentOnboardingProgressRepository onboardingProgressRepository;
    private final CertificationLevelRepository certificationLevelRepository;
    private final CertificationService certificationService;
    private final AgentAssignmentService agentAssignmentService;

    // Training Materials
    public List<TrainingMaterial> getAllMaterials() {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        return trainingMaterialRepository.findByTenantId(tenantId);
    }

    public List<TrainingMaterial> getMaterialsByCategory(Long categoryId) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        return trainingMaterialRepository.findByTenantIdAndCategoryId(tenantId, categoryId);
    }

    public Optional<TrainingMaterial> getMaterialById(Long id) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        return trainingMaterialRepository.findById(id)
            .filter(material -> material.getTenantId().equals(tenantId));
    }

    public TrainingMaterial createTrainingMaterial(TrainingMaterial trainingMaterial) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        trainingMaterial.setTenantId(tenantId);
        return trainingMaterialRepository.save(trainingMaterial);
    }

    public void deleteMaterial(Long id) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        TrainingMaterial material = trainingMaterialRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Training material not found"));
        if (!material.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Training material does not belong to this tenant");
        }
        trainingMaterialRepository.deleteById(id);
    }

    // Training Categories
    public List<TrainingCategory> getAllCategories() {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        return trainingCategoryRepository.findByTenantId(tenantId);
    }

    public Optional<TrainingCategory> getCategoryById(Long id) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        return trainingCategoryRepository.findById(id)
            .filter(category -> category.getTenantId().equals(tenantId));
    }

    public TrainingCategory createCategory(TrainingCategory category) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        category.setTenantId(tenantId);
        return trainingCategoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        TrainingCategory category = trainingCategoryRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Training category not found"));
        if (!category.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Training category does not belong to this tenant");
        }
        trainingCategoryRepository.deleteById(id);
    }

    @Transactional
    public AgentTrainingProgress completeTraining(Long agentId, Long trainingId, Integer quizScore, Long tenantId) {
        // Update training progress
        AgentTrainingProgress progress = trainingProgressRepository
            .findByAgentIdAndTrainingIdAndTenantId(agentId, trainingId, tenantId)
            .orElse(AgentTrainingProgress.builder()
                .agentId(agentId)
                .trainingId(trainingId)
                .build());
        progress.setTenantId(tenantId);

        progress.setStatus(AgentTrainingProgress.ProgressStatus.COMPLETED);
        progress.setProgress(100);
        progress.setQuizScore(quizScore);
        progress.setCompletedAt(java.time.LocalDateTime.now().toString());
        trainingProgressRepository.save(progress);

        // Update skill score based on training completion
        // (tenant-scoped lookup: material must belong to same tenant)
        TrainingMaterial training = trainingMaterialRepository.findByIdAndTenantId(trainingId, tenantId).orElse(null);
        if (training != null && training.getRequiredSkills() != null && !training.getRequiredSkills().isEmpty()) {
            // Use first skill category from required skills
            String firstSkill = training.getRequiredSkills().iterator().next();
            AgentSkill.SkillCategory skillCategory = AgentSkill.SkillCategory.valueOf(firstSkill);
            updateSkillScore(agentId, skillCategory, quizScore, tenantId);
        }

        // Phase 3: auto-award certifications if agent now meets requirements
        try {
            certificationService.checkAndAutoAwardCertifications(agentId, tenantId);
        } catch (Exception e) {
            log.warn("[Certification] Auto-award check failed for agent {}: {}", agentId, e.getMessage());
        }

        return progress;
    }

    public List<AgentTrainingProgress> getMaterialProgress(Long materialId, Long tenantId) {
        return trainingProgressRepository.findByTrainingIdAndTenantId(materialId, tenantId);
    }

    public TrainingDashboardStats getTrainingDashboardStats(Long tenantId) {
        // Get all agents for tenant
        List<com.chatbot.core.message.store.model.Agent> agents = agentRepository.findByTenantId(tenantId);
        
        TrainingDashboardStats stats = new TrainingDashboardStats();
        stats.setTotalAgents((long) agents.size());
        
        // Count agents with active training
        long activeTraining = 0;
        long completedTraining = 0;
        long certifiedAgents = 0;
        
        for (com.chatbot.core.message.store.model.Agent agent : agents) {
            // Check training progress
            List<AgentTrainingProgress> progress = trainingProgressRepository.findByAgentIdAndTenantId(agent.getId(), tenantId);
            boolean hasActive = progress.stream().anyMatch(p -> p.getStatus() == AgentTrainingProgress.ProgressStatus.IN_PROGRESS);
            boolean hasCompleted = progress.stream().anyMatch(p -> p.getStatus() == AgentTrainingProgress.ProgressStatus.COMPLETED);
            
            if (hasActive) activeTraining++;
            if (hasCompleted) completedTraining++;
            
            // Check certifications
            List<AgentCertification> certifications = certificationRepository.findByAgentIdAndTenantId(agent.getId(), tenantId);
            if (!certifications.isEmpty()) {
                certifiedAgents++;
            }
        }
        
        stats.setActiveTraining(activeTraining);
        stats.setCompletedTraining(completedTraining);
        stats.setCertifiedAgents(certifiedAgents);
        
        return stats;
    }

    public List<AgentTrainingSummary> getAgentsWithTrainingStats(Long tenantId) {
        // Get all agents for tenant
        List<com.chatbot.core.message.store.model.Agent> agents = agentRepository.findByTenantId(tenantId);
        
        List<AgentTrainingSummary> summaries = new java.util.ArrayList<>();
        
        for (com.chatbot.core.message.store.model.Agent agent : agents) {
            AgentTrainingSummary summary = new AgentTrainingSummary();
            summary.setAgentId(agent.getId());
            summary.setAgentName(agent.getName());
            summary.setAgentEmail(agent.getEmail());
            
            // Get training progress
            List<AgentTrainingProgress> trainingProgress = trainingProgressRepository.findByAgentIdAndTenantId(agent.getId(), tenantId);
            if (!trainingProgress.isEmpty()) {
                int totalProgress = trainingProgress.stream().mapToInt(AgentTrainingProgress::getProgress).sum();
                int avgProgress = totalProgress / trainingProgress.size();
                summary.setTrainingProgress(avgProgress);
                
                // Determine training status
                boolean hasCompleted = trainingProgress.stream().anyMatch(p -> p.getStatus() == AgentTrainingProgress.ProgressStatus.COMPLETED);
                boolean hasInProgress = trainingProgress.stream().anyMatch(p -> p.getStatus() == AgentTrainingProgress.ProgressStatus.IN_PROGRESS);
                
                if (hasCompleted) {
                    summary.setTrainingStatus("COMPLETED");
                } else if (hasInProgress) {
                    summary.setTrainingStatus("IN_PROGRESS");
                } else {
                    summary.setTrainingStatus("NOT_STARTED");
                }
            } else {
                summary.setTrainingProgress(0);
                summary.setTrainingStatus("NOT_STARTED");
            }
            
            // Get onboarding progress
            List<AgentOnboardingProgress> onboardingProgress = onboardingProgressRepository.findByAgentIdAndTenantId(agent.getId(), tenantId);
            if (!onboardingProgress.isEmpty()) {
                int completedTasks = (int) onboardingProgress.stream()
                    .filter(p -> p.getStatus() == AgentOnboardingProgress.CompletionStatus.COMPLETED)
                    .count();
                int totalTasks = onboardingProgress.size();
                summary.setOnboardingProgress(totalTasks > 0 ? (completedTasks * 100) / totalTasks : 0);
            } else {
                summary.setOnboardingProgress(0);
            }
            
            // Get average skill score
            List<AgentSkill> skills = agentSkillRepository.findByAgentIdAndTenantId(agent.getId(), tenantId);
            if (!skills.isEmpty()) {
                int totalScore = skills.stream().mapToInt(AgentSkill::getScore).sum();
                summary.setAverageScore(totalScore / skills.size());
            } else {
                summary.setAverageScore(0);
            }
            
            // Get certification
            List<AgentCertification> certifications = certificationRepository.findByAgentIdAndTenantId(agent.getId(), tenantId);
            if (!certifications.isEmpty()) {
                // Get the highest level certification
                AgentCertification highestCert = certifications.stream()
                    .max((c1, c2) -> {
                        CertificationLevel level1 = certificationLevelRepository.findByIdAndTenantId(c1.getCertificationLevelId(), tenantId).orElse(null);
                        CertificationLevel level2 = certificationLevelRepository.findByIdAndTenantId(c2.getCertificationLevelId(), tenantId).orElse(null);
                        if (level1 == null || level2 == null) return 0;
                        return Integer.compare(level1.getDisplayOrder(), level2.getDisplayOrder());
                    })
                    .orElse(null);
                
                if (highestCert != null) {
                    CertificationLevel level = certificationLevelRepository.findByIdAndTenantId(highestCert.getCertificationLevelId(), tenantId).orElse(null);
                    summary.setCertification(level != null ? level.getLevelName() : null);
                }
            }
            
            summaries.add(summary);
        }
        
        return summaries;
    }

    private void updateSkillScore(Long agentId, AgentSkill.SkillCategory skillCategory, Integer quizScore, Long tenantId) {
        AgentSkill skill = agentSkillRepository
            .findByAgentIdAndSkillCategoryAndTenantId(agentId, skillCategory, tenantId)
            .orElse(AgentSkill.builder()
                .agentId(agentId)
                .skillCategory(skillCategory)
                .score(0)
                .proficiencyLevel(AgentSkill.ProficiencyLevel.BEGINNER)
                .build());
        skill.setTenantId(tenantId);

        // Update score based on quiz result
        int currentScore = skill.getScore() != null ? skill.getScore() : 0;
        int newScore = (currentScore + quizScore) / 2; // Average of current and new score
        skill.setScore(newScore);

        // Update proficiency level based on score
        if (newScore >= 80) {
            skill.setProficiencyLevel(AgentSkill.ProficiencyLevel.EXPERT);
        } else if (newScore >= 60) {
            skill.setProficiencyLevel(AgentSkill.ProficiencyLevel.INTERMEDIATE);
        } else {
            skill.setProficiencyLevel(AgentSkill.ProficiencyLevel.BEGINNER);
        }

        agentSkillRepository.save(skill);

        // Bridge to routing: expose skill on Agent.skills when INTERMEDIATE+
        if (newScore >= 60) {
            try {
                agentAssignmentService.addSkillToAgent(agentId, skillCategory.name().toLowerCase());
            } catch (Exception e) {
                log.warn("[SkillBridge] Failed to sync skill {} to agent {}: {}", skillCategory, agentId, e.getMessage());
            }
        }
    }
}
