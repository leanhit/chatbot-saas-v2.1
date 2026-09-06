package com.chatbot.core.payment.plan.repository;

import com.chatbot.core.payment.plan.model.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    Optional<Discount> findByCode(String code);

    List<Discount> findByIsActiveTrueOrderByCreatedAtDesc();

    @Query("SELECT d FROM Discount d WHERE d.isActive = true " +
           "AND (d.validFrom IS NULL OR d.validFrom <= :now) " +
           "AND (d.validUntil IS NULL OR d.validUntil >= :now) " +
           "ORDER BY d.createdAt DESC")
    List<Discount> findActiveDiscounts(@Param("now") LocalDateTime now);

    @Query("SELECT d FROM Discount d WHERE d.code = :code " +
           "AND d.isActive = true " +
           "AND (d.validFrom IS NULL OR d.validFrom <= :now) " +
           "AND (d.validUntil IS NULL OR d.validUntil >= :now)")
    Optional<Discount> findActiveDiscountByCode(@Param("code") String code, @Param("now") LocalDateTime now);

    @Query("SELECT d FROM Discount d WHERE d.applicablePackageId IS NULL OR d.applicablePackageId = :packageId")
    List<Discount> findApplicableDiscountsForPackage(@Param("packageId") String packageId);

    boolean existsByCode(String code);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional(value = "sharedTransactionManager", rollbackFor = Exception.class)
    void deleteByCode(String code);
}
