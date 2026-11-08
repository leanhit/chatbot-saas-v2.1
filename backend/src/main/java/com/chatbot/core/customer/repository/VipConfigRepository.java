package com.chatbot.core.customer.repository;

import com.chatbot.core.customer.model.VipConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VipConfigRepository extends JpaRepository<VipConfig, Long> {

    Optional<VipConfig> findByTenantIdAndIsDeletedFalse(Long tenantId);
}
