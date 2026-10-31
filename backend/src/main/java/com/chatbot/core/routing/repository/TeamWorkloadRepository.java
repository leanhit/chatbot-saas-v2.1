package com.chatbot.core.routing.repository;

import com.chatbot.core.routing.model.TeamWorkload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamWorkloadRepository extends JpaRepository<TeamWorkload, Long> {

    List<TeamWorkload> findByTenantId(Long tenantId);

    Optional<TeamWorkload> findByTenantIdAndTeamId(Long tenantId, Long teamId);

    List<TeamWorkload> findByTenantIdAndStatus(Long tenantId, TeamWorkload.TeamStatus status);

    List<TeamWorkload> findByTenantIdAndStatusOrderByPriorityScoreDesc(Long tenantId, TeamWorkload.TeamStatus status);
}
