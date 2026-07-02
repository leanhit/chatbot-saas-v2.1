package com.chatbot.core.message.store.repository;

import com.chatbot.core.message.store.model.AutoAssignConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AutoAssignConfigRepository extends JpaRepository<AutoAssignConfig, Long> {
    
    Optional<AutoAssignConfig> findByTenantId(Long tenantId);
}
