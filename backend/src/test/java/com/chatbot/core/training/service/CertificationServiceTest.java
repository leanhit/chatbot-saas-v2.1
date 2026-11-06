package com.chatbot.core.training.service;

import com.chatbot.core.training.model.AgentCertification;
import com.chatbot.core.training.model.AgentSkill;
import com.chatbot.core.training.model.CertificationLevel;
import com.chatbot.core.training.repository.AgentCertificationRepository;
import com.chatbot.core.training.repository.AgentSkillRepository;
import com.chatbot.core.training.repository.AgentTrainingProgressRepository;
import com.chatbot.core.training.repository.CertificationLevelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for certification threshold rules (no Spring context).
 */
@ExtendWith(MockitoExtension.class)
class CertificationServiceTest {

    @Mock
    private AgentCertificationRepository certificationRepository;
    @Mock
    private CertificationLevelRepository certificationLevelRepository;
    @Mock
    private AgentSkillRepository agentSkillRepository;
    @Mock
    private AgentTrainingProgressRepository trainingProgressRepository;
    @Mock
    private com.chatbot.core.message.store.service.AgentConversationStatsService agentConversationStatsService;

    @InjectMocks
    private CertificationService service;

    private CertificationLevel level(Long id, String requiredSkillScores) {
        CertificationLevel l = new CertificationLevel();
        l.setId(id);
        l.setLevelName("Gold");
        l.setActive(true);
        l.setRequiredSkillScores(requiredSkillScores);
        return l;
    }

    private AgentSkill skill(Long agentId, AgentSkill.SkillCategory category, int score) {
        AgentSkill s = new AgentSkill();
        s.setAgentId(agentId);
        s.setSkillCategory(category);
        s.setScore(score);
        return s;
    }

    @Test
    void awardsCertificationWhenSkillThresholdMet() {
        CertificationLevel lvl = level(10L, "{\"COMMUNICATION\": 80}");
        when(certificationRepository.findByAgentIdAndCertificationLevelIdAndTenantId(1L, 10L, 1L))
            .thenReturn(Optional.empty());
        when(certificationLevelRepository.findByIdAndTenantId(10L, 1L)).thenReturn(Optional.of(lvl));
        when(agentSkillRepository.findByAgentIdAndTenantId(1L, 1L))
            .thenReturn(List.of(skill(1L, AgentSkill.SkillCategory.COMMUNICATION, 85)));
        when(certificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AgentCertification result = service.awardCertification(1L, 10L, "admin", 1L);

        assertNotNull(result);
        assertEquals(1L, result.getAgentId());
    }

    @Test
    void rejectsCertificationWhenSkillBelowThreshold() {
        CertificationLevel lvl = level(10L, "{\"COMMUNICATION\": 80}");
        when(certificationRepository.findByAgentIdAndCertificationLevelIdAndTenantId(1L, 10L, 1L))
            .thenReturn(Optional.empty());
        when(certificationLevelRepository.findByIdAndTenantId(10L, 1L)).thenReturn(Optional.of(lvl));
        when(agentSkillRepository.findByAgentIdAndTenantId(1L, 1L))
            .thenReturn(List.of(skill(1L, AgentSkill.SkillCategory.COMMUNICATION, 50)));

        assertThrows(IllegalStateException.class,
            () -> service.awardCertification(1L, 10L, "admin", 1L));
        verify(certificationRepository, never()).save(any());
    }

    @Test
    void rejectsDuplicateCertification() {
        when(certificationRepository.findByAgentIdAndCertificationLevelIdAndTenantId(1L, 10L, 1L))
            .thenReturn(Optional.of(new AgentCertification()));

        assertThrows(IllegalStateException.class,
            () -> service.awardCertification(1L, 10L, "admin", 1L));
    }

    @Test
    void rejectsLevelFromOtherTenant() {
        when(certificationRepository.findByAgentIdAndCertificationLevelIdAndTenantId(1L, 10L, 1L))
            .thenReturn(Optional.empty());
        when(certificationLevelRepository.findByIdAndTenantId(10L, 1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> service.awardCertification(1L, 10L, "admin", 1L));
    }

    @Test
    void rejectsWhenStatsBelowThreshold() {
        CertificationLevel lvl = level(11L, null);
        lvl.setRequiredStats("{\"totalConversations\": 10}");
        var statsDto = new com.chatbot.core.message.store.dto.AgentConversationStatsDTO();
        statsDto.setTotalConversations(3L);
        statsDto.setResolvedCount(1L);
        statsDto.setResolutionRate(33.3);
        statsDto.setCsatScore(4.0);

        when(certificationRepository.findByAgentIdAndCertificationLevelIdAndTenantId(1L, 11L, 1L))
            .thenReturn(Optional.empty());
        when(certificationLevelRepository.findByIdAndTenantId(11L, 1L)).thenReturn(Optional.of(lvl));
        when(agentConversationStatsService.getAgentStats(1L, 1L, "90d")).thenReturn(statsDto);

        assertThrows(IllegalStateException.class,
            () -> service.awardCertification(1L, 11L, "admin", 1L));
        verify(certificationRepository, never()).save(any());
    }

    @Test
    void awardsWhenStatsMeetThreshold() {
        CertificationLevel lvl = level(11L, null);
        lvl.setRequiredStats("{\"totalConversations\": 5, \"csatScore\": 4.0}");
        var statsDto = new com.chatbot.core.message.store.dto.AgentConversationStatsDTO();
        statsDto.setTotalConversations(20L);
        statsDto.setResolvedCount(18L);
        statsDto.setResolutionRate(90.0);
        statsDto.setCsatScore(4.5);

        when(certificationRepository.findByAgentIdAndCertificationLevelIdAndTenantId(1L, 11L, 1L))
            .thenReturn(Optional.empty());
        when(certificationLevelRepository.findByIdAndTenantId(11L, 1L)).thenReturn(Optional.of(lvl));
        when(agentConversationStatsService.getAgentStats(1L, 1L, "90d")).thenReturn(statsDto);
        when(certificationRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AgentCertification result = service.awardCertification(1L, 11L, "admin", 1L);
        assertNotNull(result);
    }

    @Test
    void autoAwardSkipsWhenRequirementsNotMet() {
        CertificationLevel lvl = level(10L, "{\"COMMUNICATION\": 90}");
        when(certificationLevelRepository.findByTenantId(1L)).thenReturn(List.of(lvl));
        when(certificationRepository.findByAgentIdAndCertificationLevelIdAndTenantId(1L, 10L, 1L))
            .thenReturn(Optional.empty());
        when(agentSkillRepository.findByAgentIdAndTenantId(1L, 1L))
            .thenReturn(List.of(skill(1L, AgentSkill.SkillCategory.COMMUNICATION, 70)));

        service.checkAndAutoAwardCertifications(1L, 1L);

        verify(certificationRepository, never()).save(any());
    }
}
