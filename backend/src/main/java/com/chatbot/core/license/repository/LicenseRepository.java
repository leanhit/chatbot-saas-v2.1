package com.chatbot.core.license.repository;

import com.chatbot.core.license.model.License;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LicenseRepository extends JpaRepository<License, Long> {



    Optional<License> findByUserId(Long userId);

    @Query("SELECT l FROM License l WHERE l.userId = :userId AND l.isActive = true AND (l.expiresAt IS NULL OR l.expiresAt > CURRENT_TIMESTAMP)")
    Optional<License> findActiveLicenseByUserId(@Param("userId") Long userId);

    @Query("SELECT l FROM License l WHERE l.userId = :userId AND l.isActive = true AND (l.expiresAt IS NULL OR l.expiresAt > CURRENT_TIMESTAMP) AND l.planName = :planName")
    Optional<License> findActiveLicenseByUserIdAndPlanName(@Param("userId") Long userId, @Param("planName") String planName);

    List<License> findByUserIdAndIsActive(Long userId, Boolean isActive);

    @Query("SELECT l FROM License l WHERE l.expiresAt < CURRENT_TIMESTAMP")
    List<License> findExpiredLicenses();

    boolean existsByUserIdAndIsActive(Long userId, Boolean isActive);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM License l WHERE l.userId = :userId AND l.isActive = true AND (l.expiresAt IS NULL OR l.expiresAt > CURRENT_TIMESTAMP)")
    boolean hasActiveLicense(@Param("userId") Long userId);
}
