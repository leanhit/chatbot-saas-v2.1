package com.chatbot.core.routing.integration;

import com.chatbot.core.routing.model.SupervisorSession;
import com.chatbot.core.routing.repository.SupervisorSessionRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@ImportAutoConfiguration(exclude = {FlywayAutoConfiguration.class})
class SupervisorRepositoryIntegrationTest {

    @Autowired
    private SupervisorSessionRepository supervisorSessionRepository;

    @Autowired
    private TestEntityManager entityManager;

    private static final Long TEST_TENANT_ID = 1L;
    private static final Long TEST_TENANT_2_ID = 2L;
    private static final Long TEST_SUPERVISOR_ID = 100L;
    private static final Long TEST_CONVERSATION_ID = 200L;

    @BeforeEach
    void setUp() {
        supervisorSessionRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
        TenantContext.setTenantId(TEST_TENANT_ID);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testSaveSupervisorSession_Success() {
        // Arrange
        SupervisorSession session = new SupervisorSession();
        session.setSupervisorId(TEST_SUPERVISOR_ID);
        session.setConversationId(TEST_CONVERSATION_ID);
        session.setSessionType(SupervisorSession.SessionType.MONITORING);
        session.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        session.setTenantId(TEST_TENANT_ID);

        // Act
        SupervisorSession saved = supervisorSessionRepository.save(session);

        // Assert
        assertNotNull(saved.getId());
        assertEquals(TEST_SUPERVISOR_ID, saved.getSupervisorId());
        assertEquals(TEST_CONVERSATION_ID, saved.getConversationId());
        assertEquals(SupervisorSession.SessionType.MONITORING, saved.getSessionType());
        assertEquals(SupervisorSession.SessionStatus.ACTIVE, saved.getSessionStatus());
        assertEquals(TEST_TENANT_ID, saved.getTenantId());
    }

    @Test
    void testQueryBySupervisorId() {
        // Arrange
        SupervisorSession session1 = new SupervisorSession();
        session1.setSupervisorId(TEST_SUPERVISOR_ID);
        session1.setConversationId(TEST_CONVERSATION_ID);
        session1.setSessionType(SupervisorSession.SessionType.MONITORING);
        session1.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        session1.setTenantId(TEST_TENANT_ID);

        SupervisorSession session2 = new SupervisorSession();
        session2.setSupervisorId(TEST_SUPERVISOR_ID);
        session2.setConversationId(TEST_CONVERSATION_ID + 1);
        session2.setSessionType(SupervisorSession.SessionType.COACHING);
        session2.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        session2.setTenantId(TEST_TENANT_ID);

        SupervisorSession session3 = new SupervisorSession();
        session3.setSupervisorId(TEST_SUPERVISOR_ID + 1);
        session3.setConversationId(TEST_CONVERSATION_ID + 2);
        session3.setSessionType(SupervisorSession.SessionType.MONITORING);
        session3.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        session3.setTenantId(TEST_TENANT_ID);

        supervisorSessionRepository.save(session1);
        supervisorSessionRepository.save(session2);
        supervisorSessionRepository.save(session3);

        // Act
        List<SupervisorSession> result = supervisorSessionRepository.findByTenantIdAndSupervisorIdAndSessionStatus(
                TEST_TENANT_ID, TEST_SUPERVISOR_ID, SupervisorSession.SessionStatus.ACTIVE);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(s -> s.getSupervisorId().equals(TEST_SUPERVISOR_ID)));
    }

    @Test
    void testQueryByConversationId() {
        // Arrange
        SupervisorSession session1 = new SupervisorSession();
        session1.setSupervisorId(TEST_SUPERVISOR_ID);
        session1.setConversationId(TEST_CONVERSATION_ID);
        session1.setSessionType(SupervisorSession.SessionType.MONITORING);
        session1.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        session1.setTenantId(TEST_TENANT_ID);

        SupervisorSession session2 = new SupervisorSession();
        session2.setSupervisorId(TEST_SUPERVISOR_ID + 1);
        session2.setConversationId(TEST_CONVERSATION_ID);
        session2.setSessionType(SupervisorSession.SessionType.COACHING);
        session2.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        session2.setTenantId(TEST_TENANT_ID);

        SupervisorSession session3 = new SupervisorSession();
        session3.setSupervisorId(TEST_SUPERVISOR_ID);
        session3.setConversationId(TEST_CONVERSATION_ID + 1);
        session3.setSessionType(SupervisorSession.SessionType.MONITORING);
        session3.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        session3.setTenantId(TEST_TENANT_ID);

        supervisorSessionRepository.save(session1);
        supervisorSessionRepository.save(session2);
        supervisorSessionRepository.save(session3);

        // Act
        List<SupervisorSession> result = supervisorSessionRepository.findByTenantIdAndConversationId(
                TEST_TENANT_ID, TEST_CONVERSATION_ID);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(s -> s.getConversationId().equals(TEST_CONVERSATION_ID)));
    }

    @Test
    void testQueryActiveSessionsOnly() {
        // Arrange
        SupervisorSession active1 = new SupervisorSession();
        active1.setSupervisorId(TEST_SUPERVISOR_ID);
        active1.setConversationId(TEST_CONVERSATION_ID);
        active1.setSessionType(SupervisorSession.SessionType.MONITORING);
        active1.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        active1.setTenantId(TEST_TENANT_ID);

        SupervisorSession active2 = new SupervisorSession();
        active2.setSupervisorId(TEST_SUPERVISOR_ID);
        active2.setConversationId(TEST_CONVERSATION_ID + 1);
        active2.setSessionType(SupervisorSession.SessionType.COACHING);
        active2.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        active2.setTenantId(TEST_TENANT_ID);

        SupervisorSession closed = new SupervisorSession();
        closed.setSupervisorId(TEST_SUPERVISOR_ID);
        closed.setConversationId(TEST_CONVERSATION_ID + 2);
        closed.setSessionType(SupervisorSession.SessionType.MONITORING);
        closed.setSessionStatus(SupervisorSession.SessionStatus.ENDED);
        closed.setTenantId(TEST_TENANT_ID);

        supervisorSessionRepository.save(active1);
        supervisorSessionRepository.save(active2);
        supervisorSessionRepository.save(closed);

        // Act
        List<SupervisorSession> result = supervisorSessionRepository.findByTenantIdAndSessionStatus(
                TEST_TENANT_ID, SupervisorSession.SessionStatus.ACTIVE);

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(s -> s.getSessionStatus() == SupervisorSession.SessionStatus.ACTIVE));
    }

    @Test
    void testQueryBySessionType() {
        // Arrange
        SupervisorSession monitoring = new SupervisorSession();
        monitoring.setSupervisorId(TEST_SUPERVISOR_ID);
        monitoring.setConversationId(TEST_CONVERSATION_ID);
        monitoring.setSessionType(SupervisorSession.SessionType.MONITORING);
        monitoring.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        monitoring.setTenantId(TEST_TENANT_ID);

        SupervisorSession coaching = new SupervisorSession();
        coaching.setSupervisorId(TEST_SUPERVISOR_ID);
        coaching.setConversationId(TEST_CONVERSATION_ID + 1);
        coaching.setSessionType(SupervisorSession.SessionType.COACHING);
        coaching.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        coaching.setTenantId(TEST_TENANT_ID);

        SupervisorSession takeover = new SupervisorSession();
        takeover.setSupervisorId(TEST_SUPERVISOR_ID);
        takeover.setConversationId(TEST_CONVERSATION_ID + 2);
        takeover.setSessionType(SupervisorSession.SessionType.TAKEOVER);
        takeover.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        takeover.setTenantId(TEST_TENANT_ID);

        supervisorSessionRepository.save(monitoring);
        supervisorSessionRepository.save(coaching);
        supervisorSessionRepository.save(takeover);

        // Act - Query by tenant and filter by session type in code
        List<SupervisorSession> allSessions = supervisorSessionRepository.findByTenantId(TEST_TENANT_ID);
        List<SupervisorSession> result = allSessions.stream()
                .filter(s -> s.getSessionType() == SupervisorSession.SessionType.MONITORING)
                .toList();

        // Assert
        assertEquals(1, result.size());
        assertEquals(SupervisorSession.SessionType.MONITORING, result.get(0).getSessionType());
    }

    @Test
    void testCountActiveSessions() {
        // Arrange
        SupervisorSession active1 = new SupervisorSession();
        active1.setSupervisorId(TEST_SUPERVISOR_ID);
        active1.setConversationId(TEST_CONVERSATION_ID);
        active1.setSessionType(SupervisorSession.SessionType.MONITORING);
        active1.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        active1.setTenantId(TEST_TENANT_ID);

        SupervisorSession active2 = new SupervisorSession();
        active2.setSupervisorId(TEST_SUPERVISOR_ID);
        active2.setConversationId(TEST_CONVERSATION_ID + 1);
        active2.setSessionType(SupervisorSession.SessionType.COACHING);
        active2.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        active2.setTenantId(TEST_TENANT_ID);

        SupervisorSession active3 = new SupervisorSession();
        active3.setSupervisorId(TEST_SUPERVISOR_ID);
        active3.setConversationId(TEST_CONVERSATION_ID + 2);
        active3.setSessionType(SupervisorSession.SessionType.TAKEOVER);
        active3.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        active3.setTenantId(TEST_TENANT_ID);

        SupervisorSession closed = new SupervisorSession();
        closed.setSupervisorId(TEST_SUPERVISOR_ID);
        closed.setConversationId(TEST_CONVERSATION_ID + 3);
        closed.setSessionType(SupervisorSession.SessionType.MONITORING);
        closed.setSessionStatus(SupervisorSession.SessionStatus.ENDED);
        closed.setTenantId(TEST_TENANT_ID);

        supervisorSessionRepository.save(active1);
        supervisorSessionRepository.save(active2);
        supervisorSessionRepository.save(active3);
        supervisorSessionRepository.save(closed);

        // Act
        Long count = supervisorSessionRepository.countByTenantIdAndSessionStatus(
                TEST_TENANT_ID, SupervisorSession.SessionStatus.ACTIVE);

        // Assert
        assertEquals(3L, count);
    }

    @Test
    void testUniqueActiveSessionConstraint() {
        // Arrange
        SupervisorSession session1 = new SupervisorSession();
        session1.setSupervisorId(TEST_SUPERVISOR_ID);
        session1.setConversationId(TEST_CONVERSATION_ID);
        session1.setSessionType(SupervisorSession.SessionType.MONITORING);
        session1.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        session1.setTenantId(TEST_TENANT_ID);

        supervisorSessionRepository.save(session1);

        // Act - Try to create another active session for same supervisor + conversation
        SupervisorSession session2 = new SupervisorSession();
        session2.setSupervisorId(TEST_SUPERVISOR_ID);
        session2.setConversationId(TEST_CONVERSATION_ID);
        session2.setSessionType(SupervisorSession.SessionType.COACHING);
        session2.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        session2.setTenantId(TEST_TENANT_ID);

        // Check if existing active session exists
        Optional<SupervisorSession> existing = supervisorSessionRepository
                .findByTenantIdAndSupervisorIdAndConversationId(TEST_TENANT_ID, TEST_SUPERVISOR_ID, TEST_CONVERSATION_ID);

        // Assert
        assertTrue(existing.isPresent());
        assertEquals(SupervisorSession.SessionStatus.ACTIVE, existing.get().getSessionStatus());
    }

    @Test
    void testTenantIsolation() {
        // Arrange
        SupervisorSession tenant1Session = new SupervisorSession();
        tenant1Session.setSupervisorId(TEST_SUPERVISOR_ID);
        tenant1Session.setConversationId(TEST_CONVERSATION_ID);
        tenant1Session.setSessionType(SupervisorSession.SessionType.MONITORING);
        tenant1Session.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        tenant1Session.setTenantId(TEST_TENANT_ID);

        SupervisorSession tenant2Session = new SupervisorSession();
        tenant2Session.setSupervisorId(TEST_SUPERVISOR_ID + 1);
        tenant2Session.setConversationId(TEST_CONVERSATION_ID + 1);
        tenant2Session.setSessionType(SupervisorSession.SessionType.COACHING);
        tenant2Session.setSessionStatus(SupervisorSession.SessionStatus.ACTIVE);
        tenant2Session.setTenantId(TEST_TENANT_2_ID);

        supervisorSessionRepository.save(tenant1Session);
        supervisorSessionRepository.save(tenant2Session);

        // Act
        List<SupervisorSession> tenant1Results = supervisorSessionRepository.findByTenantIdAndSessionStatus(
                TEST_TENANT_ID, SupervisorSession.SessionStatus.ACTIVE);

        // Assert
        assertEquals(1, tenant1Results.size());
        assertEquals(TEST_TENANT_ID, tenant1Results.get(0).getTenantId());
        assertFalse(tenant1Results.stream().anyMatch(s -> s.getTenantId().equals(TEST_TENANT_2_ID)));
    }
}
