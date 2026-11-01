package com.chatbot.core.routing.service;

import com.chatbot.core.routing.dto.SupervisorSessionDTO;
import com.chatbot.core.routing.model.SupervisorSession;
import com.chatbot.core.routing.repository.SupervisorSessionRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SupervisorSessionServiceTest {

    @Mock
    private SupervisorSessionRepository supervisorSessionRepository;

    @InjectMocks
    private SupervisorSessionService supervisorSessionService;

    private static final Long TEST_TENANT_ID = 1L;
    private static final Long TEST_SUPERVISOR_ID = 100L;
    private static final Long TEST_CONVERSATION_ID = 200L;
    private static final Long TEST_AGENT_ID = 10L;

    @BeforeEach
    void setUp() {
        // Reset before each test
    }

    @Test
    void testCreateSupervisorSession_Success() {
        // Arrange
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);

            SupervisorSession session = createSupervisorSession();
            session.setId(1L);

            when(supervisorSessionRepository.findByTenantIdAndSupervisorIdAndConversationId(
                    TEST_TENANT_ID, TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID))
                    .thenReturn(Optional.empty());
            when(supervisorSessionRepository.save(any(SupervisorSession.class))).thenReturn(session);

            // Act
            SupervisorSessionDTO result = supervisorSessionService.createSupervisorSession(
                    TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID, SupervisorSession.SessionType.MONITORING);

            // Assert
            assertNotNull(result);
            assertEquals(TEST_SUPERVISOR_ID, result.getSupervisorId());
            assertEquals(TEST_CONVERSATION_ID, result.getConversationId());
            assertEquals(SupervisorSession.SessionType.MONITORING, result.getSessionType());
            assertEquals(SupervisorSession.SessionStatus.ACTIVE, result.getSessionStatus());
            verify(supervisorSessionRepository, times(1)).save(any(SupervisorSession.class));
        }
    }

    @Test
    void testCreateSupervisorSession_DuplicateActiveSession_ThrowsException() {
        // Arrange
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);

            SupervisorSession existingSession = createSupervisorSession();
            existingSession.setId(1L);

            when(supervisorSessionRepository.findByTenantIdAndSupervisorIdAndConversationId(
                    TEST_TENANT_ID, TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID))
                    .thenReturn(Optional.of(existingSession));

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                supervisorSessionService.createSupervisorSession(
                        TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID, SupervisorSession.SessionType.MONITORING);
            });

            assertEquals("Active supervisor session already exists for supervisor 100 and conversation 200", exception.getMessage());
            verify(supervisorSessionRepository, never()).save(any(SupervisorSession.class));
        }
    }

    @Test
    void testEndSupervisorSession_Success() {
        // Arrange
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);

            SupervisorSession session = createSupervisorSession();
            session.setId(1L);
            session.setJoinedAt(LocalDateTime.now().minusMinutes(10));

            when(supervisorSessionRepository.findById(1L)).thenReturn(Optional.of(session));
            when(supervisorSessionRepository.save(any(SupervisorSession.class))).thenReturn(session);

            // Act
            SupervisorSessionDTO result = supervisorSessionService.endSupervisorSession(1L, "Good session", 5);

            // Assert
            assertNotNull(result);
            assertEquals(SupervisorSession.SessionStatus.ENDED, result.getSessionStatus());
            assertNotNull(result.getLeftAt());
            assertTrue(result.getDurationSeconds() > 0);
            assertEquals("Good session", result.getNotes());
            assertEquals(5, result.getRating());
        }
    }

    @Test
    void testEndSupervisorSession_NotActive_ThrowsException() {
        // Arrange
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);

            SupervisorSession session = createSupervisorSession();
            session.setId(1L);
            session.setSessionStatus(SupervisorSession.SessionStatus.ENDED);

            when(supervisorSessionRepository.findById(1L)).thenReturn(Optional.of(session));

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                supervisorSessionService.endSupervisorSession(1L, null, null);
            });

            assertEquals("Cannot end a session that is not active", exception.getMessage());
        }
    }

    @Test
    void testInterruptSupervisorSession_Success() {
        // Arrange
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);

            SupervisorSession session = createSupervisorSession();
            session.setId(1L);
            session.setJoinedAt(LocalDateTime.now().minusMinutes(5));

            when(supervisorSessionRepository.findById(1L)).thenReturn(Optional.of(session));
            when(supervisorSessionRepository.save(any(SupervisorSession.class))).thenReturn(session);

            // Act
            SupervisorSessionDTO result = supervisorSessionService.interruptSupervisorSession(1L);

            // Assert
            assertNotNull(result);
            assertEquals(SupervisorSession.SessionStatus.INTERRUPTED, result.getSessionStatus());
            assertNotNull(result.getLeftAt());
            assertTrue(result.getDurationSeconds() > 0);
        }
    }

    @Test
    void testRecordIntervention_Success() {
        // Arrange
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);

            SupervisorSession session = createSupervisorSession();
            session.setId(1L);
            session.setCanIntervene(true);
            session.setInterventionCount(2);

            when(supervisorSessionRepository.findById(1L)).thenReturn(Optional.of(session));
            when(supervisorSessionRepository.save(any(SupervisorSession.class))).thenReturn(session);

            // Act
            SupervisorSessionDTO result = supervisorSessionService.recordIntervention(1L);

            // Assert
            assertNotNull(result);
            assertEquals(3, result.getInterventionCount());
        }
    }

    @Test
    void testRecordIntervention_NotAllowed_ThrowsException() {
        // Arrange
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);

            SupervisorSession session = createSupervisorSession();
            session.setId(1L);
            session.setCanIntervene(false);

            when(supervisorSessionRepository.findById(1L)).thenReturn(Optional.of(session));

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                supervisorSessionService.recordIntervention(1L);
            });

            assertEquals("Intervention not allowed for this session type", exception.getMessage());
        }
    }

    @Test
    void testUpdateMonitoredAgent_Success() {
        // Arrange
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);

            SupervisorSession session = createSupervisorSession();
            session.setId(1L);

            when(supervisorSessionRepository.findById(1L)).thenReturn(Optional.of(session));
            when(supervisorSessionRepository.save(any(SupervisorSession.class))).thenReturn(session);

            // Act
            SupervisorSessionDTO result = supervisorSessionService.updateMonitoredAgent(1L, TEST_AGENT_ID);

            // Assert
            assertNotNull(result);
            assertEquals(TEST_AGENT_ID, result.getMonitoredAgentId());
        }
    }

    @Test
    void testGetActiveSessionsBySupervisor_Success() {
        // Arrange
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);

            SupervisorSession session = createSupervisorSession();
            session.setId(1L);

            when(supervisorSessionRepository.findByTenantIdAndSupervisorIdAndSessionStatus(
                    TEST_TENANT_ID, TEST_SUPERVISOR_ID, SupervisorSession.SessionStatus.ACTIVE))
                    .thenReturn(java.util.List.of(session));

            // Act
            var result = supervisorSessionService.getActiveSessionsBySupervisor(TEST_SUPERVISOR_ID);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
        }
    }

    @Test
    void testGetActiveSessionCount_Success() {
        // Arrange
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);

            when(supervisorSessionRepository.countByTenantIdAndSessionStatus(
                    TEST_TENANT_ID, SupervisorSession.SessionStatus.ACTIVE))
                    .thenReturn(3L);

            // Act
            Long result = supervisorSessionService.getActiveSessionCount();

            // Assert
            assertEquals(3L, result);
        }
    }

    @Test
    void testGetSessionsByConversation_Success() {
        // Arrange
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);

            SupervisorSession session = createSupervisorSession();
            session.setId(1L);

            when(supervisorSessionRepository.findByTenantIdAndConversationId(TEST_TENANT_ID, TEST_CONVERSATION_ID))
                    .thenReturn(java.util.List.of(session));

            // Act
            var result = supervisorSessionService.getSessionsByConversation(TEST_CONVERSATION_ID);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(TEST_CONVERSATION_ID, result.get(0).getConversationId());
        }
    }

    // Helper methods
    private SupervisorSession createSupervisorSession() {
        SupervisorSession session = new SupervisorSession();
        session.setSupervisorId(TEST_SUPERVISOR_ID);
        session.setConversationId(TEST_CONVERSATION_ID);
        session.setSessionType(SupervisorSession.SessionType.MONITORING);
        session.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        session.setCanIntervene(true);
        session.setInterventionCount(0);
        session.setDurationSeconds(0);
        session.setTenantId(TEST_TENANT_ID);
        session.setJoinedAt(LocalDateTime.now());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        return session;
    }
}
