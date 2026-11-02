package com.chatbot.core.routing.smoke;

import com.chatbot.core.routing.dto.SupervisorSessionDTO;
import com.chatbot.core.routing.model.SupervisorSession;
import com.chatbot.core.routing.repository.SupervisorSessionRepository;
import com.chatbot.core.routing.service.SupervisorSessionService;
import com.chatbot.core.tenant.infra.TenantContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Disabled("Requires Docker and external services - use Mockito tests instead")
class SupervisorSmokeTest {

    @Autowired
    private SupervisorSessionService supervisorSessionService;

    @Autowired
    private SupervisorSessionRepository supervisorSessionRepository;

    private static final Long TEST_TENANT_ID = 1L;
    private static final Long TEST_SUPERVISOR_ID = 100L;
    private static final Long TEST_CONVERSATION_ID = 200L;

    @BeforeEach
    void setUp() {
        supervisorSessionRepository.deleteAll();
        TenantContext.setTenantId(TEST_TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testStartSupervisorSession() {
        // Arrange
        SupervisorSession.SessionType sessionType = SupervisorSession.SessionType.MONITORING;

        // Act - Start session
        SupervisorSessionDTO result = supervisorSessionService.createSupervisorSession(
                TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID, sessionType);

        // Assert - Session created active=true
        assertNotNull(result);
        assertEquals(TEST_SUPERVISOR_ID, result.getSupervisorId());
        assertEquals(TEST_CONVERSATION_ID, result.getConversationId());
        assertEquals(sessionType, result.getSessionType());
        assertEquals(SupervisorSession.SessionStatus.ACTIVE, result.getSessionStatus());
        assertNotNull(result.getJoinedAt());
        assertTrue(result.getIsSilent());
        assertFalse(result.getCanIntervene());
        assertEquals(0, result.getInterventionCount());
    }

    @Test
    void testSupervisorIntervention() {
        // Arrange - Active session exists
        SupervisorSessionDTO session = supervisorSessionService.createSupervisorSession(
                TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID, SupervisorSession.SessionType.COACHING);

        // Act - Call intervene
        SupervisorSessionDTO result = supervisorSessionService.recordIntervention(session.getId());

        // Assert - Intervention count increments
        assertNotNull(result);
        assertEquals(1, result.getInterventionCount());
    }

    @Test
    void testEndSupervisorSession() {
        // Arrange - Active session
        SupervisorSessionDTO session = supervisorSessionService.createSupervisorSession(
                TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID, SupervisorSession.SessionType.MONITORING);

        // Act - End session
        SupervisorSessionDTO result = supervisorSessionService.endSupervisorSession(session.getId(), "Test notes", 5);

        // Assert - Status = CLOSED and endedAt not null
        assertNotNull(result);
        assertEquals(SupervisorSession.SessionStatus.ENDED, result.getSessionStatus());
        assertNotNull(result.getLeftAt());
        assertNotNull(result.getDurationSeconds());
        assertEquals("Test notes", result.getNotes());
        assertEquals(5, result.getRating());
    }

    @Test
    void testInterruptSession() {
        // Arrange - Active session
        SupervisorSessionDTO session = supervisorSessionService.createSupervisorSession(
                TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID, SupervisorSession.SessionType.TAKEOVER);

        // Act - Interrupt session
        SupervisorSessionDTO result = supervisorSessionService.interruptSupervisorSession(session.getId());

        // Assert - Status changed and ended/flag set
        assertNotNull(result);
        assertEquals(SupervisorSession.SessionStatus.INTERRUPTED, result.getSessionStatus());
        assertNotNull(result.getLeftAt());
        assertNotNull(result.getDurationSeconds());
    }

    @Test
    void testQuerySessionsBySupervisor() {
        // Arrange - Multiple sessions
        supervisorSessionService.createSupervisorSession(
                TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID, SupervisorSession.SessionType.MONITORING);

        supervisorSessionService.createSupervisorSession(
                TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID + 1, SupervisorSession.SessionType.COACHING);

        supervisorSessionService.createSupervisorSession(
                TEST_SUPERVISOR_ID + 1, TEST_CONVERSATION_ID + 2, SupervisorSession.SessionType.MONITORING);

        // Act - Query by supervisorId
        var sessions = supervisorSessionService.getActiveSessionsBySupervisor(TEST_SUPERVISOR_ID);

        // Assert - Proper subset
        assertEquals(2, sessions.size());
        assertTrue(sessions.stream().allMatch(s -> s.getSupervisorId().equals(TEST_SUPERVISOR_ID)));
    }

    @Test
    void testQuerySessionsByConversation() {
        // Arrange
        supervisorSessionService.createSupervisorSession(
                TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID, SupervisorSession.SessionType.MONITORING);

        supervisorSessionService.createSupervisorSession(
                TEST_SUPERVISOR_ID + 1, TEST_CONVERSATION_ID, SupervisorSession.SessionType.COACHING);

        supervisorSessionService.createSupervisorSession(
                TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID + 1, SupervisorSession.SessionType.TAKEOVER);

        // Act - Query by conversationId
        var sessions = supervisorSessionService.getSessionsByConversation(TEST_CONVERSATION_ID);

        // Assert - Matching session(s)
        assertEquals(2, sessions.size());
        assertTrue(sessions.stream().allMatch(s -> s.getConversationId().equals(TEST_CONVERSATION_ID)));
    }

    @Test
    void testCountActiveSessions() {
        // Arrange - 2 active + 1 closed
        SupervisorSessionDTO session1 = supervisorSessionService.createSupervisorSession(
                TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID, SupervisorSession.SessionType.MONITORING);

        SupervisorSessionDTO session2 = supervisorSessionService.createSupervisorSession(
                TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID + 1, SupervisorSession.SessionType.COACHING);

        SupervisorSessionDTO session3 = supervisorSessionService.createSupervisorSession(
                TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID + 2, SupervisorSession.SessionType.TAKEOVER);

        // End one session
        supervisorSessionService.endSupervisorSession(session3.getId(), null, null);

        // Act - Count active
        long count = supervisorSessionService.getActiveSessionCount();

        // Assert - 2
        assertEquals(2L, count);
    }

    @Test
    void testDuplicateActiveSessionRejected() {
        // Arrange - Same supervisor + same conversation + active already exists
        supervisorSessionService.createSupervisorSession(
                TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID, SupervisorSession.SessionType.MONITORING);

        // Act & Assert - Rejected
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            supervisorSessionService.createSupervisorSession(
                    TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID, SupervisorSession.SessionType.COACHING);
        });

        assertTrue(exception.getMessage().contains("Active supervisor session already exists"));
    }
}
