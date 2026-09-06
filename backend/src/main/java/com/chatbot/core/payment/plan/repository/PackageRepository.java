package com.chatbot.core.payment.plan.repository;

import com.chatbot.core.payment.plan.model.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {

    Optional<Package> findByPackageId(String packageId);

    List<Package> findByIsActiveTrueOrderBySortOrderAsc();

    List<Package> findAllByOrderBySortOrderAsc();

    boolean existsByPackageId(String packageId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional(value = "sharedTransactionManager", rollbackFor = Exception.class)
    void deleteByPackageId(String packageId);
}
