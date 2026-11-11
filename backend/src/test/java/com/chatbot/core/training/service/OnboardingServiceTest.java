package com.chatbot.core.training.service;

import com.chatbot.core.training.model.AgentOnboardingProgress;
import com.chatbot.core.training.model.AgentOnboardingTask;
import com.chatbot.core.training.repository.AgentOnboardingProgressRepository;
import com.chatbot.core.training.repository.AgentOnboardingTaskRepository;
import com.chatbot.core.tenant.service.TenantAuditLogService;
import com.chatbot.core.tenant.service.TenantPermissionValidator;
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
class OnboardingServiceTest {

    @Mock private AgentOnboardingTaskRepository onboardingTaskRepository;
    @Mock private AgentOnboardingProgressRepository onboardingProgressRepository;
    @Mock private TenantAuditLogService auditLogService;
    @Mock private TenantPermissionValidator permissionValidator;

    @InjectMocks
    private OnboardingService service;

    private AgentOnboardingTask task(Long id, boolean required) {
        AgentOnboardingTask t = new AgentOnboardingTask();
        t.setId(id);
        t.setTitle("Task " + id);
        t.setRequired(required);
        t.setStatus(AgentOnboardingTask.TaskStatus.ACTIVE);
        return t;
    }

    private AgentOnboardingProgress progress(Long agentId, Long taskId, AgentOnboardingProgress.CompletionStatus status) {
        return AgentOnboardingProgress.builder()
            .agentId(agentId).taskId(taskId).status(status).build();
    }

    @Test
    void isOnboardingCompleteTrueWhenNoTasks() {
        when(onboardingTaskRepository.findByTenantId(1L)).thenReturn(List.of());
        assertTrue(service.isOnboardingComplete(1L, 1L));
    }

    @Test
    void isOnboardingCompleteFalseWhenRequiredTaskMissing() {
        when(onboardingTaskRepository.findByTenantId(1L)).thenReturn(List.of(task(10L, true)));
        when(onboardingProgressRepository.findByAgentIdAndTaskIdAndTenantId(1L, 10L, 1L))
            .thenReturn(Optional.empty());

        assertFalse(service.isOnboardingComplete(1L, 1L));
    }

    @Test
    void isOnboardingCompleteFalseWhenRequiredTaskInProgress() {
        when(onboardingTaskRepository.findByTenantId(1L)).thenReturn(List.of(task(10L, true)));
        when(onboardingProgressRepository.findByAgentIdAndTaskIdAndTenantId(1L, 10L, 1L))
            .thenReturn(Optional.of(progress(1L, 10L, AgentOnboardingProgress.CompletionStatus.IN_PROGRESS)));

        assertFalse(service.isOnboardingComplete(1L, 1L));
    }

    @Test
    void isOnboardingCompleteTrueWhenAllRequiredCompleted() {
        when(onboardingTaskRepository.findByTenantId(1L)).thenReturn(List.of(
            task(10L, true), task(11L, false)));
        when(onboardingProgressRepository.findByAgentIdAndTaskIdAndTenantId(1L, 10L, 1L))
            .thenReturn(Optional.of(progress(1L, 10L, AgentOnboardingProgress.CompletionStatus.COMPLETED)));

        assertTrue(service.isOnboardingComplete(1L, 1L));
        // optional task never queried
        verify(onboardingProgressRepository, never())
            .findByAgentIdAndTaskIdAndTenantId(1L, 11L, 1L);
    }

    @Test
    void completeTaskThrowsWhenProgressMissing() {
        when(onboardingProgressRepository.findByAgentIdAndTaskIdAndTenantId(1L, 10L, 1L))
            .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> service.completeTask(1L, 10L, 1L, "done"));
    }

    @Test
    void completeTaskMarksCompleted() {
        AgentOnboardingProgress p = progress(1L, 10L, AgentOnboardingProgress.CompletionStatus.IN_PROGRESS);
        when(onboardingProgressRepository.findByAgentIdAndTaskIdAndTenantId(1L, 10L, 1L))
            .thenReturn(Optional.of(p));
        when(onboardingProgressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        AgentOnboardingProgress result = service.completeTask(1L, 10L, 1L, "finished reading");

        assertEquals(AgentOnboardingProgress.CompletionStatus.COMPLETED, result.getStatus());
        assertEquals("finished reading", result.getNotes());
        assertNotNull(result.getCompletedAt());
    }

    @Test
    void approveTaskSetsSupervisorApproval() {
        AgentOnboardingProgress p = progress(1L, 10L, AgentOnboardingProgress.CompletionStatus.COMPLETED);
        when(onboardingProgressRepository.findByAgentIdAndTaskIdAndTenantId(1L, 10L, 1L))
            .thenReturn(Optional.of(p));
        when(onboardingProgressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.approveTask(1L, 10L, 99L, "looks good", 1L);

        assertEquals(AgentOnboardingProgress.SupervisorApproval.APPROVED, p.getSupervisorApproval());
        assertEquals(99L, p.getSupervisorId());
        assertEquals("looks good", p.getSupervisorComments());
    }

    @Test
    void rejectTaskSetsRejectedApproval() {
        AgentOnboardingProgress p = progress(1L, 10L, AgentOnboardingProgress.CompletionStatus.COMPLETED);
        when(onboardingProgressRepository.findByAgentIdAndTaskIdAndTenantId(1L, 10L, 1L))
            .thenReturn(Optional.of(p));
        when(onboardingProgressRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.rejectTask(1L, 10L, 99L, "redo quiz", 1L);

        assertEquals(AgentOnboardingProgress.SupervisorApproval.REJECTED, p.getSupervisorApproval());
    }
}
