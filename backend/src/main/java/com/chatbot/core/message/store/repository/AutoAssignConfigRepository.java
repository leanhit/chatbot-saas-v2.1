package com.chatbot.core.message.store.repository;

import com.chatbot.core.message.store.model.AutoAssignConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AutoAssignConfigRepository extends JpaRepository<AutoAssignConfig, Long> {
    
    Optional<AutoAssignConfig> findByTenantId(Long tenantId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional(value = "messageTransactionManager", rollbackFor = Exception.class)
    @org.springframework.data.jpa.repository.Query("DELETE FROM AutoAssignConfig a WHERE a.tenantId = :tenantId")
    void deleteByTenantId(@org.springframework.data.repository.query.Param("tenantId") Long tenantId);
}
