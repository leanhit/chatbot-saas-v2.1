package com.chatbot.core.identity.repository;

import com.chatbot.core.user.model.User;
import com.chatbot.core.identity.model.SystemRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by email and status
     */
    Optional<User> findByEmailAndIsActive(String email, Boolean isActive);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Find user by role
     */
    List<User> findBySystemRole(SystemRole systemRole);
    
    /**
     * Find user by active status
     */
    List<User> findByIsActive(Boolean isActive);
    
    /**
     * Find user by role and active status
     */
    List<User> findBySystemRoleAndIsActive(SystemRole systemRole, Boolean isActive);
    
    /**
     * Find user created after specified time
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :since")
    List<User> findUsersCreatedSince(@Param("since") LocalDateTime since);
    
    /**
     * Find user created before specified time
     */
    @Query("SELECT u FROM User u WHERE u.createdAt <= :before")
    List<User> findUsersCreatedBefore(@Param("before") LocalDateTime before);
    
    /**
     * Count user by role
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.systemRole = :role")
    long countBySystemRole(@Param("role") SystemRole role);
    
    /**
     * Count user by active status
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.isActive = :isActive")
    long countByIsActive(@Param("isActive") Boolean isActive);
    
    /**
     * Search user by email (partial match)
     */
    @Query("SELECT u FROM User u WHERE u.email LIKE %:email%")
    List<User> searchByEmail(@Param("email") String email);
    
    /**
     * Find user by multiple roles
     */
    @Query("SELECT u FROM User u WHERE u.systemRole IN :roles")
    List<User> findBySystemRoleIn(@Param("roles") List<SystemRole> roles);
    
    /**
     * Update last login time for user
     */
    @Query("UPDATE User u SET u.updatedAt = :loginTime WHERE u.id = :userId")
    int updateLastLoginTime(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);
    
    /**
     * Update user active status
     */
    @Query("UPDATE User u SET u.isActive = :isActive WHERE u.id = :userId")
    int updateUserActiveStatus(@Param("userId") Long userId, @Param("isActive") Boolean isActive);
    
    /**
     * Find user statistics
     */
    @Query("SELECT u.systemRole, u.isActive, COUNT(u) FROM User u GROUP BY u.systemRole, u.isActive")
    List<Object[]> getUserStatistics();
    
    /**
     * Find recently created user (last N days)
     */
    @Query("SELECT u FROM User u WHERE u.createdAt >= :since ORDER BY u.createdAt DESC")
    List<User> findRecentlyCreatedUsers(@Param("since") LocalDateTime since);
}
