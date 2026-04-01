package com.chatbot.core.simplepayment.repository;

import com.chatbot.core.simplepayment.model.PackageUpgradeAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PackageUpgradeAuditRepository extends JpaRepository<PackageUpgradeAudit, Long> {

    /**
     * Find audit by payment reference code
     */
    Optional<PackageUpgradeAudit> findByPaymentReferenceCode(String paymentReferenceCode);

    /**
     * Find all upgrades for a tenant
     */
    List<PackageUpgradeAudit> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    /**
     * Find all upgrades for a user
     */
    List<PackageUpgradeAudit> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find successful upgrades
     */
    List<PackageUpgradeAudit> findByUpgradeStatusOrderByCreatedAtDesc(PackageUpgradeAudit.UpgradeStatus status);

    /**
     * Find upgrades in date range
     */
    @Query("SELECT a FROM PackageUpgradeAudit a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<PackageUpgradeAudit> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);

    /**
     * Count upgrades by package
     */
    @Query("SELECT a.toPackageId, COUNT(a) FROM PackageUpgradeAudit a WHERE a.upgradeStatus = 'SUCCESS' GROUP BY a.toPackageId")
    List<Object[]> countUpgradesByPackage();

    /**
     * Find failed upgrades for analysis
     */
    List<PackageUpgradeAudit> findByUpgradeStatusAndCreatedAtAfterOrderByCreatedAtDesc(
            PackageUpgradeAudit.UpgradeStatus status, LocalDateTime after);
}
