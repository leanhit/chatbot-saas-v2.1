package com.chatbot.core.routing.service;

import com.chatbot.core.routing.dto.SupervisorSessionDTO;
import com.chatbot.core.routing.model.SupervisorSession;
import com.chatbot.core.routing.repository.SupervisorSessionRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SupervisorSessionServiceMockitoTest {

    @Mock
    private SupervisorSessionRepository supervisorSessionRepository;

    @InjectMocks
    private SupervisorSessionService supervisorSessionService;

    private MockedStatic<TenantContext> tenantContextMock;

    private static final Long TEST_TENANT_ID = 1L;
    private static final Long TEST_SUPERVISOR_ID = 100L;
    private static final Long TEST_CONVERSATION_ID = 200L;

    @BeforeEach
    void setUp() {
        tenantContextMock = mockStatic(TenantContext.class);
        tenantContextMock.when(TenantContext::getTenantId).thenReturn(TEST_TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        tenantContextMock.close();
    }

    @Test
    void testCreateSupervisorSession_Monitoring_ReturnsActiveSession() {
        // Arrange
        SupervisorSession.SessionType sessionType = SupervisorSession.SessionType.MONITORING;

        when(supervisorSessionRepository.findByTenantIdAndSupervisorIdAndConversationId(
                TEST_TENANT_ID, TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID))
                .thenReturn(Optional.empty());

        SupervisorSession savedSession = new SupervisorSession();
        savedSession.setId(1L);
        savedSession.setTenantId(TEST_TENANT_ID);
        savedSession.setSupervisorId(TEST_SUPERVISOR_ID);
        savedSession.setConversationId(TEST_CONVERSATION_ID);
        savedSession.setSessionType(sessionType);
        savedSession.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        savedSession.setIsSilent(true);
        savedSession.setCanIntervene(false);
        savedSession.setInterventionCount(0);

        when(supervisorSessionRepository.save(any(SupervisorSession.class))).thenReturn(savedSession);

        // Act
        SupervisorSessionDTO result = supervisorSessionService.createSupervisorSession(
                TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID, sessionType);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_SUPERVISOR_ID, result.getSupervisorId());
        assertEquals(TEST_CONVERSATION_ID, result.getConversationId());
        assertEquals(sessionType, result.getSessionType());
        assertEquals(SupervisorSession.SessionStatus.ACTIVE, result.getSessionStatus());
        assertTrue(result.getIsSilent());
        assertFalse(result.getCanIntervene());
        assertEquals(0, result.getInterventionCount());
        verify(supervisorSessionRepository).save(any(SupervisorSession.class));
    }

    @Test
    void testCreateSupervisorSession_Coaching_ReturnsActiveSession() {
        // Arrange
        SupervisorSession.SessionType sessionType = SupervisorSession.SessionType.COACHING;

        when(supervisorSessionRepository.findByTenantIdAndSupervisorIdAndConversationId(
                TEST_TENANT_ID, TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID))
                .thenReturn(Optional.empty());

        SupervisorSession savedSession = new SupervisorSession();
        savedSession.setId(1L);
        savedSession.setTenantId(TEST_TENANT_ID);
        savedSession.setSupervisorId(TEST_SUPERVISOR_ID);
        savedSession.setConversationId(TEST_CONVERSATION_ID);
        savedSession.setSessionType(sessionType);
        savedSession.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        savedSession.setIsSilent(false);
        savedSession.setCanIntervene(true);
        savedSession.setInterventionCount(0);

        when(supervisorSessionRepository.save(any(SupervisorSession.class))).thenReturn(savedSession);

        // Act
        SupervisorSessionDTO result = supervisorSessionService.createSupervisorSession(
                TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID, sessionType);

        // Assert
        assertNotNull(result);
        assertEquals(sessionType, result.getSessionType());
        assertFalse(result.getIsSilent());
        assertTrue(result.getCanIntervene());
    }

    @Test
    void testCreateSupervisorSession_Takeover_ReturnsActiveSession() {
        // Arrange
        SupervisorSession.SessionType sessionType = SupervisorSession.SessionType.TAKEOVER;

        when(supervisorSessionRepository.findByTenantIdAndSupervisorIdAndConversationId(
                TEST_TENANT_ID, TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID))
                .thenReturn(Optional.empty());

        SupervisorSession savedSession = new SupervisorSession();
        savedSession.setId(1L);
        savedSession.setTenantId(TEST_TENANT_ID);
        savedSession.setSupervisorId(TEST_SUPERVISOR_ID);
        savedSession.setConversationId(TEST_CONVERSATION_ID);
        savedSession.setSessionType(sessionType);
        savedSession.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        savedSession.setIsSilent(false);
        savedSession.setCanIntervene(true);
        savedSession.setInterventionCount(0);

        when(supervisorSessionRepository.save(any(SupervisorSession.class))).thenReturn(savedSession);

        // Act
        SupervisorSessionDTO result = supervisorSessionService.createSupervisorSession(
                TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID, sessionType);

        // Assert
        assertNotNull(result);
        assertEquals(sessionType, result.getSessionType());
        assertFalse(result.getIsSilent());
        assertTrue(result.getCanIntervene());
    }

    @Test
    void testCreateSupervisorSession_ActiveSessionExists_ThrowsException() {
        // Arrange
        SupervisorSession.SessionType sessionType = SupervisorSession.SessionType.MONITORING;

        SupervisorSession existingSession = new SupervisorSession();
        existingSession.setId(1L);
        existingSession.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);

        when(supervisorSessionRepository.findByTenantIdAndSupervisorIdAndConversationId(
                TEST_TENANT_ID, TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID))
                .thenReturn(Optional.of(existingSession));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            supervisorSessionService.createSupervisorSession(
                    TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID, sessionType);
        });
    }

    @Test
    void testEndSupervisorSession_ReturnsEndedSession() {
        // Arrange
        Long sessionId = 1L;

        SupervisorSession session = new SupervisorSession();
        session.setId(sessionId);
        session.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);

        when(supervisorSessionRepository.findByIdAndTenantId(sessionId, TEST_TENANT_ID)).thenReturn(Optional.of(session));

        SupervisorSession updatedSession = new SupervisorSession();
        updatedSession.setId(sessionId);
        updatedSession.setSessionStatus(SupervisorSession.SessionStatus.ENDED);
        updatedSession.setDurationSeconds(300);
        updatedSession.setNotes("Test notes");
        updatedSession.setRating(5);

        when(supervisorSessionRepository.save(any(SupervisorSession.class))).thenAnswer(invocation -> {
            SupervisorSession saved = invocation.getArgument(0);
            saved.setSessionStatus(SupervisorSession.SessionStatus.ENDED);
            saved.setDurationSeconds(300);
            saved.setNotes("Test notes");
            saved.setRating(5);
            return saved;
        });

        // Act
        SupervisorSessionDTO result = supervisorSessionService.endSupervisorSession(sessionId, "Test notes", 5);

        // Assert
        assertNotNull(result);
        assertEquals(SupervisorSession.SessionStatus.ENDED, result.getSessionStatus());
        assertEquals("Test notes", result.getNotes());
        assertEquals(5, result.getRating());
        verify(supervisorSessionRepository).save(any(SupervisorSession.class));
    }

    @Test
    void testEndSupervisorSession_SessionNotActive_ThrowsException() {
        // Arrange
        Long sessionId = 1L;

        SupervisorSession session = new SupervisorSession();
        session.setId(sessionId);
        session.setSessionStatus(SupervisorSession.SessionStatus.ENDED);

        when(supervisorSessionRepository.findByIdAndTenantId(sessionId, TEST_TENANT_ID)).thenReturn(Optional.of(session));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            supervisorSessionService.endSupervisorSession(sessionId, null, null);
        });
    }

    @Test
    void testInterruptSupervisorSession_ReturnsInterruptedSession() {
        // Arrange
        Long sessionId = 1L;

        SupervisorSession session = new SupervisorSession();
        session.setId(sessionId);
        session.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);

        when(supervisorSessionRepository.findByIdAndTenantId(sessionId, TEST_TENANT_ID)).thenReturn(Optional.of(session));

        when(supervisorSessionRepository.save(any(SupervisorSession.class))).thenAnswer(invocation -> {
            SupervisorSession saved = invocation.getArgument(0);
            saved.setSessionStatus(SupervisorSession.SessionStatus.INTERRUPTED);
            saved.setDurationSeconds(120);
            return saved;
        });

        // Act
        SupervisorSessionDTO result = supervisorSessionService.interruptSupervisorSession(sessionId);

        // Assert
        assertNotNull(result);
        assertEquals(SupervisorSession.SessionStatus.INTERRUPTED, result.getSessionStatus());
        assertNotNull(result.getLeftAt());
        verify(supervisorSessionRepository).save(any(SupervisorSession.class));
    }

    @Test
    void testInterruptSupervisorSession_SessionNotActive_ThrowsException() {
        // Arrange
        Long sessionId = 1L;

        SupervisorSession session = new SupervisorSession();
        session.setId(sessionId);
        session.setSessionStatus(SupervisorSession.SessionStatus.ENDED);

        when(supervisorSessionRepository.findByIdAndTenantId(sessionId, TEST_TENANT_ID)).thenReturn(Optional.of(session));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            supervisorSessionService.interruptSupervisorSession(sessionId);
        });
    }

    @Test
    void testRecordIntervention_IncrementsCount() {
        // Arrange
        Long sessionId = 1L;

        SupervisorSession session = new SupervisorSession();
        session.setId(sessionId);
        session.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        session.setCanIntervene(true);
        session.setInterventionCount(0);

        when(supervisorSessionRepository.findByIdAndTenantId(sessionId, TEST_TENANT_ID)).thenReturn(Optional.of(session));

        SupervisorSession updatedSession = new SupervisorSession();
        updatedSession.setId(sessionId);
        updatedSession.setInterventionCount(1);

        when(supervisorSessionRepository.save(any(SupervisorSession.class))).thenReturn(updatedSession);

        // Act
        SupervisorSessionDTO result = supervisorSessionService.recordIntervention(sessionId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getInterventionCount());
        verify(supervisorSessionRepository).save(any(SupervisorSession.class));
    }

    @Test
    void testRecordIntervention_SessionNotActive_ThrowsException() {
        // Arrange
        Long sessionId = 1L;

        SupervisorSession session = new SupervisorSession();
        session.setId(sessionId);
        session.setSessionStatus(SupervisorSession.SessionStatus.ENDED);

        when(supervisorSessionRepository.findByIdAndTenantId(sessionId, TEST_TENANT_ID)).thenReturn(Optional.of(session));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            supervisorSessionService.recordIntervention(sessionId);
        });
    }

    @Test
    void testRecordIntervention_InterventionNotAllowed_ThrowsException() {
        // Arrange
        Long sessionId = 1L;

        SupervisorSession session = new SupervisorSession();
        session.setId(sessionId);
        session.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        session.setCanIntervene(false);

        when(supervisorSessionRepository.findByIdAndTenantId(sessionId, TEST_TENANT_ID)).thenReturn(Optional.of(session));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            supervisorSessionService.recordIntervention(sessionId);
        });
    }

    @Test
    void testUpdateMonitoredAgent_UpdatesAgentId() {
        // Arrange
        Long sessionId = 1L;
        Long monitoredAgentId = 50L;

        SupervisorSession session = new SupervisorSession();
        session.setId(sessionId);

        when(supervisorSessionRepository.findByIdAndTenantId(sessionId, TEST_TENANT_ID)).thenReturn(Optional.of(session));

        SupervisorSession updatedSession = new SupervisorSession();
        updatedSession.setId(sessionId);
        updatedSession.setMonitoredAgentId(monitoredAgentId);

        when(supervisorSessionRepository.save(any(SupervisorSession.class))).thenReturn(updatedSession);

        // Act
        SupervisorSessionDTO result = supervisorSessionService.updateMonitoredAgent(sessionId, monitoredAgentId);

        // Assert
        assertNotNull(result);
        assertEquals(monitoredAgentId, result.getMonitoredAgentId());
        verify(supervisorSessionRepository).save(any(SupervisorSession.class));
    }

    @Test
    void testGetActiveSessionsBySupervisor_ReturnsActiveSessions() {
        // Arrange
        SupervisorSession session1 = new SupervisorSession();
        session1.setId(1L);
        session1.setSupervisorId(TEST_SUPERVISOR_ID);
        session1.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);

        SupervisorSession session2 = new SupervisorSession();
        session2.setId(2L);
        session2.setSupervisorId(TEST_SUPERVISOR_ID);
        session2.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);

        when(supervisorSessionRepository.findByTenantIdAndSupervisorIdAndSessionStatus(
                TEST_TENANT_ID, TEST_SUPERVISOR_ID, SupervisorSession.SessionStatus.ACTIVE))
                .thenReturn(List.of(session1, session2));

        // Act
        List<SupervisorSessionDTO> result = supervisorSessionService.getActiveSessionsBySupervisor(TEST_SUPERVISOR_ID);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void testGetSessionsByConversation_ReturnsSessions() {
        // Arrange
        SupervisorSession session1 = new SupervisorSession();
        session1.setId(1L);
        session1.setConversationId(TEST_CONVERSATION_ID);

        when(supervisorSessionRepository.findByTenantIdAndConversationId(TEST_TENANT_ID, TEST_CONVERSATION_ID))
                .thenReturn(List.of(session1));

        // Act
        List<SupervisorSessionDTO> result = supervisorSessionService.getSessionsByConversation(TEST_CONVERSATION_ID);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetActiveSessionCount_ReturnsCount() {
        // Arrange
        when(supervisorSessionRepository.countByTenantIdAndSessionStatus(
                TEST_TENANT_ID, SupervisorSession.SessionStatus.ACTIVE))
                .thenReturn(5L);

        // Act
        Long count = supervisorSessionService.getActiveSessionCount();

        // Assert
        assertEquals(5L, count);
    }

    @Test
    void testGetAllActiveSessions_ReturnsAllActive() {
        // Arrange
        SupervisorSession session1 = new SupervisorSession();
        session1.setId(1L);
        session1.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);

        when(supervisorSessionRepository.findByTenantIdAndSessionStatus(
                TEST_TENANT_ID, SupervisorSession.SessionStatus.ACTIVE))
                .thenReturn(List.of(session1));

        // Act
        List<SupervisorSessionDTO> result = supervisorSessionService.getAllActiveSessions();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
