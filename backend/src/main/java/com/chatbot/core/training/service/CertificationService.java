package com.chatbot.core.training.service;

import com.chatbot.core.training.model.AgentCertification;
import com.chatbot.core.training.model.AgentSkill;
import com.chatbot.core.training.model.AgentTrainingProgress;
import com.chatbot.core.training.model.CertificationLevel;
import com.chatbot.core.training.repository.AgentCertificationRepository;
import com.chatbot.core.training.repository.AgentSkillRepository;
import com.chatbot.core.training.repository.CertificationLevelRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificationService {

    private final AgentCertificationRepository certificationRepository;
    private final CertificationLevelRepository certificationLevelRepository;
    private final AgentSkillRepository agentSkillRepository;
    private final com.chatbot.core.training.repository.AgentTrainingProgressRepository trainingProgressRepository;
    private final com.chatbot.core.message.store.service.AgentConversationStatsService agentConversationStatsService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public CertificationLevel createCertificationLevel(CertificationLevel level) {
        Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set");
        }
        level.setTenantId(tenantId);
        return certificationLevelRepository.save(level);
    }

    public List<CertificationLevel> getAllCertificationLevels(Long tenantId) {
        return certificationLevelRepository.findByTenantId(tenantId);
    }

    @Transactional
    public AgentCertification awardCertification(Long agentId, Long certificationLevelId, String issuedBy, Long tenantId) {
        // Check if agent already has this certification
        if (certificationRepository.findByAgentIdAndCertificationLevelIdAndTenantId(agentId, certificationLevelId, tenantId).isPresent()) {
            throw new IllegalStateException("Agent already has this certification");
        }

        // Check if agent meets requirements (tenant-scoped level lookup)
        CertificationLevel level = certificationLevelRepository.findByIdAndTenantId(certificationLevelId, tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Certification level not found"));

        if (!checkCertificationRequirements(agentId, level, tenantId)) {
            throw new IllegalStateException("Agent does not meet certification requirements");
        }

        AgentCertification certification = AgentCertification.builder()
            .agentId(agentId)
            .certificationLevelId(certificationLevelId)
            .issuedBy(issuedBy)
            .build();
        certification.setTenantId(tenantId);
        certification.setIssuedAt(LocalDateTime.now().toString());
        return certificationRepository.save(certification);
    }

    private boolean checkCertificationRequirements(Long agentId, CertificationLevel level, Long tenantId) {
        try {
            // Check required skill scores
            if (level.getRequiredSkillScores() != null && !level.getRequiredSkillScores().isEmpty()) {
                Map<String, Integer> requiredSkills = objectMapper.readValue(
                    level.getRequiredSkillScores(),
                    new TypeReference<Map<String, Integer>>() {}
                );

                List<AgentSkill> agentSkills = agentSkillRepository.findByAgentIdAndTenantId(agentId, tenantId);

                for (Map.Entry<String, Integer> entry : requiredSkills.entrySet()) {
                    String skillCategory = entry.getKey();
                    Integer requiredScore = entry.getValue();

                    AgentSkill.SkillCategory category = AgentSkill.SkillCategory.valueOf(skillCategory);
                    AgentSkill agentSkill = agentSkills.stream()
                        .filter(s -> s.getSkillCategory() == category)
                        .findFirst()
                        .orElse(null);

                    if (agentSkill == null || agentSkill.getScore() == null || agentSkill.getScore() < requiredScore) {
                        log.debug("Agent {} does not meet skill requirement: {} >= {}", agentId, skillCategory, requiredScore);
                        return false;
                    }
                }
            }

            // Check required stats: evaluated over last 90 days
            // Supported keys: totalConversations, resolvedConversations, resolutionRate,
            // avgFirstResponseSeconds, csatScore, slaBreachCount
            if (level.getRequiredStats() != null && !level.getRequiredStats().isEmpty()) {
                Map<String, Double> requiredStats = objectMapper.readValue(
                    level.getRequiredStats(),
                    new TypeReference<Map<String, Double>>() {}
                );
                var stats = agentConversationStatsService.getAgentStats(agentId, tenantId, "90d");
                Map<String, Double> actualStats = Map.of(
                    "totalConversations", stats.getTotalConversations() != null ? stats.getTotalConversations().doubleValue() : 0.0,
                    "resolvedConversations", stats.getResolvedCount() != null ? stats.getResolvedCount().doubleValue() : 0.0,
                    "resolutionRate", stats.getResolutionRate() != null ? stats.getResolutionRate() : 0.0,
                    "avgFirstResponseSeconds", stats.getAvgFirstResponseSeconds() != null ? stats.getAvgFirstResponseSeconds() : 0.0,
                    "csatScore", stats.getCsatScore() != null ? stats.getCsatScore() : 0.0,
                    "slaBreachCount", stats.getSlaBreachCount() != null ? stats.getSlaBreachCount().doubleValue() : 0.0
                );
                for (Map.Entry<String, Double> entry : requiredStats.entrySet()) {
                    String key = entry.getKey();
                    Double threshold = entry.getValue();
                    // "max:" prefix = upper bound (e.g. max:slaBreachCount = 5 means at most 5)
                    boolean isMax = key.startsWith("max:");
                    String statKey = isMax ? key.substring(4) : key;
                    Double actual = actualStats.get(statKey);
                    if (actual == null) {
                        log.debug("Unknown requiredStats key '{}' for level {}", key, level.getLevelName());
                        return false;
                    }
                    boolean met = isMax ? actual <= threshold : actual >= threshold;
                    if (!met) {
                        log.debug("Agent {} stat {} = {} does not meet requirement {}{}", agentId, statKey, actual, isMax ? "<= " : ">= ", threshold);
                        return false;
                    }
                }
            }

            // Check required training: agent must have COMPLETED progress for each required training
            if (level.getRequiredTrainingIds() != null && !level.getRequiredTrainingIds().isEmpty()) {
                List<String> requiredIds = objectMapper.readValue(
                    level.getRequiredTrainingIds(),
                    new TypeReference<List<String>>() {}
                );
                for (String trainingIdStr : requiredIds) {
                    Long trainingId = Long.valueOf(trainingIdStr);
                    boolean completed = trainingProgressRepository
                        .findByAgentIdAndTrainingIdAndTenantId(agentId, trainingId, tenantId)
                        .map(p -> p.getStatus() == AgentTrainingProgress.ProgressStatus.COMPLETED)
                        .orElse(false);
                    if (!completed) {
                        log.debug("Agent {} has not completed required training {}", agentId, trainingId);
                        return false;
                    }
                }
            }

            return true;
        } catch (Exception e) {
            log.error("Error checking certification requirements for agent {}: {}", agentId, e.getMessage(), e);
            return false;
        }
    }

    public List<AgentCertification> getAgentCertifications(Long agentId, Long tenantId) {
        return certificationRepository.findByTenantIdAndAgentId(tenantId, agentId);
    }

    @Transactional
    public void checkAndAutoAwardCertifications(Long agentId, Long tenantId) {
        // Get all active certification levels for tenant
        List<CertificationLevel> levels = certificationLevelRepository.findByTenantId(tenantId).stream()
            .filter(CertificationLevel::getActive)
            .toList();

        for (CertificationLevel level : levels) {
            // Check if agent doesn't have this certification
            if (certificationRepository.findByAgentIdAndCertificationLevelIdAndTenantId(agentId, level.getId(), tenantId).isEmpty()) {
                // Check if agent meets requirements
                if (checkCertificationRequirements(agentId, level, tenantId)) {
                    // Auto-award certification
                    AgentCertification certification = AgentCertification.builder()
                        .agentId(agentId)
                        .certificationLevelId(level.getId())
                        .issuedBy("SYSTEM_AUTO")
                        .build();
                    certification.setTenantId(tenantId);
                    certification.setIssuedAt(LocalDateTime.now().toString());
                    certificationRepository.save(certification);
                }
            }
        }
    }
}
