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
}
