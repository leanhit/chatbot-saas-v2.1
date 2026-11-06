package com.chatbot.core.training.service;

import com.chatbot.core.message.store.repository.AgentRepository;
import com.chatbot.core.training.model.AgentSkill;
import com.chatbot.core.training.model.AgentTrainingProgress;
import com.chatbot.core.training.model.TrainingMaterial;
import com.chatbot.core.training.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for training completion → skill score rule (no Spring context).
 */
@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock private TrainingMaterialRepository trainingMaterialRepository;
    @Mock private TrainingCategoryRepository trainingCategoryRepository;
    @Mock private AgentTrainingProgressRepository trainingProgressRepository;
    @Mock private AgentSkillRepository agentSkillRepository;
    @Mock private AgentRepository agentRepository;
    @Mock private AgentCertificationRepository certificationRepository;
    @Mock private AgentOnboardingProgressRepository onboardingProgressRepository;
    @Mock private CertificationLevelRepository certificationLevelRepository;
    @Mock private CertificationService certificationService;

    @InjectMocks
    private TrainingService service;

    private TrainingMaterial material(Long id, Set<String> skills) {
        TrainingMaterial m = new TrainingMaterial();
        m.setId(id);
        m.setRequiredSkills(skills);
        return m;
    }

    @Test
    void completingTrainingUpdatesSkillScore() {
        TrainingMaterial m = material(5L, Set.of("COMMUNICATION"));
        when(trainingProgressRepository.findByAgentIdAndTrainingIdAndTenantId(1L, 5L, 1L))
            .thenReturn(Optional.empty());
        when(trainingProgressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(trainingMaterialRepository.findByIdAndTenantId(5L, 1L)).thenReturn(Optional.of(m));
        when(agentSkillRepository.findByAgentIdAndSkillCategoryAndTenantId(1L, AgentSkill.SkillCategory.COMMUNICATION, 1L))
            .thenReturn(Optional.empty());
        when(agentSkillRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AgentTrainingProgress result = service.completeTraining(1L, 5L, 90, 1L);

        assertEquals(AgentTrainingProgress.ProgressStatus.COMPLETED, result.getStatus());
        assertEquals(100, result.getProgress());
        // quiz 90, current 0 → newScore = 45 → BEGINNER
        verify(agentSkillRepository).save(argThat(s ->
            s.getScore() == 45 && s.getSkillCategory() == AgentSkill.SkillCategory.COMMUNICATION));
        verify(certificationService).checkAndAutoAwardCertifications(1L, 1L);
    }

    @Test
    void completingTrainingAveragesWithExistingScore() {
        TrainingMaterial m = material(5L, Set.of("TECHNICAL"));
        AgentSkill existing = new AgentSkill();
        existing.setAgentId(1L);
        existing.setSkillCategory(AgentSkill.SkillCategory.TECHNICAL);
        existing.setScore(60);
        existing.setProficiencyLevel(AgentSkill.ProficiencyLevel.INTERMEDIATE);

        when(trainingProgressRepository.findByAgentIdAndTrainingIdAndTenantId(1L, 5L, 1L))
            .thenReturn(Optional.empty());
        when(trainingProgressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(trainingMaterialRepository.findByIdAndTenantId(5L, 1L)).thenReturn(Optional.of(m));
        when(agentSkillRepository.findByAgentIdAndSkillCategoryAndTenantId(1L, AgentSkill.SkillCategory.TECHNICAL, 1L))
            .thenReturn(Optional.of(existing));
        when(agentSkillRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.completeTraining(1L, 5L, 100, 1L);

        // (60 + 100) / 2 = 80 → EXPERT
        verify(agentSkillRepository).save(argThat(s ->
            s.getScore() == 80 && s.getProficiencyLevel() == AgentSkill.ProficiencyLevel.EXPERT));
    }

    @Test
    void materialFromOtherTenantDoesNotUpdateSkill() {
        when(trainingProgressRepository.findByAgentIdAndTrainingIdAndTenantId(1L, 5L, 1L))
            .thenReturn(Optional.empty());
        when(trainingProgressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(trainingMaterialRepository.findByIdAndTenantId(5L, 1L)).thenReturn(Optional.empty());

        service.completeTraining(1L, 5L, 90, 1L);

        verify(agentSkillRepository, never()).save(any());
    }
}
