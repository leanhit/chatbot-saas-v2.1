package com.chatbot.core.routing.repository;

import com.chatbot.core.routing.model.SupervisorSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IntelligentSupervisorSessionRepository extends JpaRepository<SupervisorSession, Long> {

    List<SupervisorSession> findByTenantIdOrderByJoinedAtDesc(Long tenantId);

    List<SupervisorSession> findByTenantIdAndSupervisorIdOrderByJoinedAtDesc(Long tenantId, Long supervisorId);

    List<SupervisorSession> findByTenantIdAndConversationIdOrderByJoinedAtDesc(Long tenantId, Long conversationId);

    Optional<SupervisorSession> findFirstByTenantIdAndSupervisorIdAndConversationIdAndSessionStatusOrderByJoinedAtDesc(Long tenantId, Long supervisorId, Long conversationId, SupervisorSession.SessionStatus status);

    List<SupervisorSession> findByTenantIdAndSessionStatus(Long tenantId, SupervisorSession.SessionStatus status);

    List<SupervisorSession> findByTenantIdAndSessionType(Long tenantId, SupervisorSession.SessionType sessionType);

    List<SupervisorSession> findByTenantIdAndMonitoredAgentId(Long tenantId, Long monitoredAgentId);

    long countByTenantIdAndSupervisorIdAndSessionStatus(Long tenantId, Long supervisorId, SupervisorSession.SessionStatus status);

    long countByTenantIdAndSessionStatus(Long tenantId, SupervisorSession.SessionStatus status);
}
