package com.chatbot.core.routing.repository;

import com.chatbot.core.routing.model.SupervisorSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupervisorSessionRepository extends JpaRepository<SupervisorSession, Long> {

    List<SupervisorSession> findByTenantId(Long tenantId);

    Optional<SupervisorSession> findByTenantIdAndSupervisorIdAndConversationId(Long tenantId, Long supervisorId, Long conversationId);

    List<SupervisorSession> findByTenantIdAndSupervisorId(Long tenantId, Long supervisorId);

    List<SupervisorSession> findByTenantIdAndConversationId(Long tenantId, Long conversationId);

    List<SupervisorSession> findByTenantIdAndSessionStatus(Long tenantId, SupervisorSession.SessionStatus sessionStatus);

    List<SupervisorSession> findByTenantIdAndSupervisorIdAndSessionStatus(Long tenantId, Long supervisorId, SupervisorSession.SessionStatus sessionStatus);

    long countByTenantIdAndSessionStatus(Long tenantId, SupervisorSession.SessionStatus sessionStatus);

    // Additional methods for IntelligentSupervisorService
    List<SupervisorSession> findByTenantIdOrderByJoinedAtDesc(Long tenantId);

    List<SupervisorSession> findByTenantIdAndSupervisorIdOrderByJoinedAtDesc(Long tenantId, Long supervisorId);

    List<SupervisorSession> findByTenantIdAndConversationIdOrderByJoinedAtDesc(Long tenantId, Long conversationId);

    Optional<SupervisorSession> findFirstByTenantIdAndSupervisorIdAndConversationIdAndSessionStatusOrderByJoinedAtDesc(Long tenantId, Long supervisorId, Long conversationId, SupervisorSession.SessionStatus status);

    List<SupervisorSession> findByTenantIdAndSessionType(Long tenantId, SupervisorSession.SessionType sessionType);

    List<SupervisorSession> findByTenantIdAndMonitoredAgentId(Long tenantId, Long monitoredAgentId);

    long countByTenantIdAndSupervisorIdAndSessionStatus(Long tenantId, Long supervisorId, SupervisorSession.SessionStatus status);

    Optional<SupervisorSession> findByIdAndTenantId(Long id, Long tenantId);
}
