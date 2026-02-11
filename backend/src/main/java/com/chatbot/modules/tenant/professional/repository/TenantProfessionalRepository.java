package com.chatbot.modules.tenant.professional.repository;

import com.chatbot.modules.tenant.professional.model.TenantProfessional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantProfessionalRepository extends JpaRepository<TenantProfessional, Long> {
    Optional<TenantProfessional> findByTenantId(Long tenantId);
    void deleteByTenantId(Long tenantId);
}
