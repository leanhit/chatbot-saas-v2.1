package com.chatbot.core.tenant.repository;

import com.chatbot.core.tenant.model.DataDeletionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DataDeletionRequestRepository extends JpaRepository<DataDeletionRequest, Long> {

    List<DataDeletionRequest> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    Optional<DataDeletionRequest> findByIdAndTenantId(Long id, Long tenantId);

    List<DataDeletionRequest> findByStatusAndScheduledAtBefore(DataDeletionRequest.Status status, java.time.LocalDateTime before);
}
