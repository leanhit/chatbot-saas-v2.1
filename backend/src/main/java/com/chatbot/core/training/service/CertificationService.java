package com.chatbot.core.training.service;

import com.chatbot.core.training.model.AgentCertification;
import com.chatbot.core.training.model.CertificationLevel;
import com.chatbot.core.training.repository.AgentCertificationRepository;
import com.chatbot.core.training.repository.CertificationLevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificationService {

    private final AgentCertificationRepository certificationRepository;
    private final CertificationLevelRepository certificationLevelRepository;

    public CertificationLevel createCertificationLevel(CertificationLevel level) {
        return certificationLevelRepository.save(level);
    }

    public List<CertificationLevel> getAllCertificationLevels(Long tenantId) {
        return certificationLevelRepository.findAll();
    }

    public AgentCertification awardCertification(Long agentId, Long certificationLevelId, String issuedBy, Long tenantId) {
        AgentCertification certification = AgentCertification.builder()
            .agentId(agentId)
            .certificationLevelId(certificationLevelId)
            .issuedBy(issuedBy)
            .build();
        return certificationRepository.save(certification);
    }

    public List<AgentCertification> getAgentCertifications(Long agentId, Long tenantId) {
        return certificationRepository.findAll();
    }
}
