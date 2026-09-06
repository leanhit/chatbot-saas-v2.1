package com.chatbot.core.payment.plan.repository;

import com.chatbot.core.payment.plan.model.PackageUpgradeAudit;
import com.chatbot.core.payment.plan.model.PackageUpgradeAudit.UpgradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PackageUpgradeAuditRepository extends JpaRepository<PackageUpgradeAudit, Long> {

    List<PackageUpgradeAudit> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<PackageUpgradeAudit> findByPaymentReferenceCode(String paymentReferenceCode);

    Optional<PackageUpgradeAudit> findByPaymentReferenceCodeAndUpgradeStatus(
        String paymentReferenceCode, 
        UpgradeStatus status
    );

    List<PackageUpgradeAudit> findByUpgradeStatusAndProcessedAtBefore(
        UpgradeStatus status, 
        LocalDateTime before
    );

    List<PackageUpgradeAudit> findByUserIdOrderByCreatedAtDesc(Long userId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional(value = "sharedTransactionManager", rollbackFor = Exception.class)
    void deleteByTenantId(Long tenantId);
}
