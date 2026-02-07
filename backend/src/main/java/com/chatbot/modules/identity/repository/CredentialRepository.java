package com.chatbot.modules.identity.repository;

import com.chatbot.modules.identity.model.Credential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CredentialRepository extends JpaRepository<Credential, UUID> {

    Optional<Credential> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    @Modifying
    @Query("UPDATE Credential c SET c.failedAttempts = :attempts WHERE c.userId = :userId")
    int updateFailedAttempts(@Param("userId") UUID userId,
                             @Param("attempts") Integer attempts);

    @Modifying
    @Query("UPDATE Credential c SET c.lockedUntil = :lockedUntil WHERE c.userId = :userId")
    int updateLockedUntil(@Param("userId") UUID userId,
                           @Param("lockedUntil") LocalDateTime lockedUntil);
}
