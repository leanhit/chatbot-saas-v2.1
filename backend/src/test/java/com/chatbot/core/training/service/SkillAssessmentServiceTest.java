package com.chatbot.core.training.service;

import com.chatbot.core.training.model.AgentSkill;
import com.chatbot.core.training.repository.AgentSkillRepository;
import com.chatbot.core.training.repository.AgentTrainingProgressRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SkillAssessmentServiceTest {

    @Mock private AgentSkillRepository agentSkillRepository;
    @Mock private AgentTrainingProgressRepository trainingProgressRepository;

    @InjectMocks
    private SkillAssessmentService service;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void updateSkillCreatesNewSkillWithCorrectProficiency() {
        when(agentSkillRepository.findByAgentIdAndSkillCategoryAndTenantId(
                1L, AgentSkill.SkillCategory.COMMUNICATION, 1L))
            .thenReturn(Optional.empty());
        when(agentSkillRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AgentSkill result = service.updateSkill(1L, "communication", 85, 1L);

        assertEquals(AgentSkill.ProficiencyLevel.EXPERT, result.getProficiencyLevel());
        assertEquals(85, result.getScore());
        assertEquals(1L, result.getTenantId());
    }

    @Test
    void proficiencyThresholds() {
        when(agentSkillRepository.findByAgentIdAndSkillCategoryAndTenantId(any(), any(), any()))
            .thenReturn(Optional.empty());
        when(agentSkillRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertEquals(AgentSkill.ProficiencyLevel.BEGINNER,
            service.updateSkill(1L, "TECHNICAL", 0, 1L).getProficiencyLevel());
        assertEquals(AgentSkill.ProficiencyLevel.BEGINNER,
            service.updateSkill(1L, "TECHNICAL", 59, 1L).getProficiencyLevel());
        assertEquals(AgentSkill.ProficiencyLevel.INTERMEDIATE,
            service.updateSkill(1L, "TECHNICAL", 60, 1L).getProficiencyLevel());
        assertEquals(AgentSkill.ProficiencyLevel.INTERMEDIATE,
            service.updateSkill(1L, "TECHNICAL", 79, 1L).getProficiencyLevel());
        assertEquals(AgentSkill.ProficiencyLevel.EXPERT,
            service.updateSkill(1L, "TECHNICAL", 80, 1L).getProficiencyLevel());
    }

    @Test
    void updateSkillUpdatesExistingRecord() {
        AgentSkill existing = AgentSkill.builder()
            .agentId(1L).skillCategory(AgentSkill.SkillCategory.TECHNICAL)
            .score(40).proficiencyLevel(AgentSkill.ProficiencyLevel.BEGINNER).build();
        when(agentSkillRepository.findByAgentIdAndSkillCategoryAndTenantId(
                1L, AgentSkill.SkillCategory.TECHNICAL, 1L))
            .thenReturn(Optional.of(existing));
        when(agentSkillRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AgentSkill result = service.updateSkill(1L, "technical", 65, 1L);

        assertSame(existing, result);
        assertEquals(65, result.getScore());
        assertEquals(AgentSkill.ProficiencyLevel.INTERMEDIATE, result.getProficiencyLevel());
    }

    @Test
    void updateSkillWithoutCategoryCreatesGeneric() {
        when(agentSkillRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AgentSkill result = service.updateSkill(1L, null, 50, 1L);

        assertNull(result.getSkillCategory());
        assertEquals(1L, result.getTenantId());
    }

    @Test
    void getAgentSkillsRequiresTenantContext() {
        TenantContext.clear();
        assertThrows(IllegalStateException.class, () -> service.getAgentSkills(1L));
    }

    @Test
    void getAgentSkillsUsesTenantContext() {
        TenantContext.setTenantId(5L);
        when(agentSkillRepository.findByAgentIdAndTenantId(1L, 5L)).thenReturn(List.of());

        assertTrue(service.getAgentSkills(1L).isEmpty());
        verify(agentSkillRepository).findByAgentIdAndTenantId(1L, 5L);
    }

    @Test
    void getAgentSkillsExplicitTenant() {
        when(agentSkillRepository.findByAgentIdAndTenantId(1L, 3L)).thenReturn(List.of());
        assertTrue(service.getAgentSkills(1L, 3L).isEmpty());
    }
}
