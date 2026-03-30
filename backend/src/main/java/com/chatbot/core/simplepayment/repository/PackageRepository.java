package com.chatbot.core.simplepayment.repository;

import com.chatbot.core.simplepayment.model.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {
    
    Optional<Package> findByPackageId(String packageId);
    
    List<Package> findByIsActiveTrueOrderBySortOrderAsc();
    
    List<Package> findAllByOrderBySortOrderAsc();
    
    @Query("SELECT p FROM Package p WHERE p.isActive = true ORDER BY p.sortOrder ASC")
    List<Package> findActivePackagesOrdered();
    
    @Query("SELECT COUNT(p) > 0 FROM Package p WHERE p.packageId = :packageId")
    boolean existsByPackageId(@Param("packageId") String packageId);
    
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Package p")
    boolean hasAnyPackages();
    
    @Modifying
    @Query("DELETE FROM Package")
    void deleteAll();
}
