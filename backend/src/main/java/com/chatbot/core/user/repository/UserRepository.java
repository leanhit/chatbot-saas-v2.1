package com.chatbot.core.user.repository;

import com.chatbot.core.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

/**
 * User Repository - Data access for system users
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    Optional<User> findByEmailAndIsActive(String email, Boolean isActive);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.profile WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR CAST(u.id AS string) LIKE CONCAT('%', :keyword, '%')) " +
           "AND (:role IS NULL OR u.systemRole = :role)")
    org.springframework.data.domain.Page<User> searchUsers(
        @Param("keyword") String keyword, 
        @Param("role") com.chatbot.core.identity.model.SystemRole role, 
        org.springframework.data.domain.Pageable pageable);
    
    /**
     * Find user by ID with pessimistic write lock to prevent race conditions
     * Used for balance operations
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u WHERE u.id = :userId")
    Optional<User> findByIdWithLock(@Param("userId") Long userId);
}
