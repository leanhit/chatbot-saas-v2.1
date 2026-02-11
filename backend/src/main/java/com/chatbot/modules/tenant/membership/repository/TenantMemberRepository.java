package com.chatbot.modules.tenant.membership.repository;

import com.chatbot.modules.tenant.membership.model.MembershipStatus;
import com.chatbot.modules.tenant.membership.model.TenantMember;
import com.chatbot.modules.tenant.membership.model.TenantRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TenantMemberRepository
        extends JpaRepository<TenantMember, Long> {

    /* =========================
       BASIC FIND / EXISTS
       ========================= */

    Optional<TenantMember> findByTenant_IdAndUser_Id(Long tenantId, Long userId);

    boolean existsByTenant_IdAndUser_Id(Long tenantId, Long userId);

    boolean existsByTenant_IdAndRole(Long tenantId, TenantRole role);

    boolean existsByTenant_IdAndUser_IdAndRole(
            Long tenantId,
            Long userId,
            TenantRole role
    );

    @Query("SELECT COUNT(tm) > 0 FROM TenantMember tm " +
           "JOIN tm.user u " +
           "WHERE tm.tenant.id = :tenantId " +
           "AND u.email = :userEmail " +
           "AND tm.role = :role " +
           "AND tm.status = :status")
    boolean existsByTenantIdAndUserEmailAndRoleAndStatus(
            @Param("tenantId") Long tenantId,
            @Param("userEmail") String userEmail,
            @Param("role") TenantRole role,
            @Param("status") MembershipStatus status
    );

    /* =========================
       STATUS AWARE (IMPORTANT)
       ========================= */

    Optional<TenantMember> findByTenant_IdAndUser_IdAndStatus(
            Long tenantId,
            Long userId,
            MembershipStatus status
    );

    boolean existsByTenant_IdAndUser_IdAndStatus(
            Long tenantId,
            Long userId,
            MembershipStatus status
    );

    Page<TenantMember> findByTenant_IdAndStatus(
            Long tenantId,
            MembershipStatus status,
            Pageable pageable
    );

    List<TenantMember> findByTenant_IdAndStatus(
            Long tenantId,
            MembershipStatus status
    );

    @Query("SELECT tm FROM TenantMember tm " +
           "JOIN tm.user u " +
           "WHERE tm.tenant.id = :tenantId " +
           "AND u.email = :userEmail " +
           "AND tm.status = :status")
    Optional<TenantMember> findByTenantIdAndUserEmailAndStatus(
            @Param("tenantId") Long tenantId,
            @Param("userEmail") String userEmail,
            @Param("status") MembershipStatus status
    );

    List<TenantMember> findByUser_IdAndStatus(
            Long userId,
            MembershipStatus status
    );

    /* =========================
       LIST MEMBERS
       ========================= */

    Page<TenantMember> findByTenant_Id(Long tenantId, Pageable pageable);

    List<TenantMember> findByTenant_Id(Long tenantId);

    /* =========================
       USER ↔ TENANT QUERIES
       ========================= */

    List<TenantMember> findByUserEmail(String email);

    @Query("""
        SELECT tm FROM TenantMember tm
        JOIN FETCH tm.tenant
        WHERE tm.user.email = :email
    """)
    List<TenantMember> findByUserEmailWithTenant(@Param("email") String email);

    @Query("""
        SELECT tm FROM TenantMember tm
        JOIN FETCH tm.tenant
        WHERE tm.user.id = :userId
          AND tm.status = 'ACTIVE'
    """)
    List<TenantMember> findActiveTenantsOfUser(@Param("userId") Long userId);

    @Query("""
        SELECT tm FROM TenantMember tm
        JOIN FETCH tm.tenant
        WHERE tm.tenant.id = :tenantId
          AND tm.user.email = :userEmail
    """)
    Optional<TenantMember> findByTenantIdAndUserEmail(
            @Param("tenantId") Long tenantId,
            @Param("userEmail") String userEmail
    );

    /* =========================
       ROLE / PERMISSION
       ========================= */

    long countByTenant_IdAndRole(Long tenantId, TenantRole role);

    @Query("""
        SELECT COUNT(tm) > 0 FROM TenantMember tm
        WHERE tm.tenant.id = :tenantId
          AND tm.user.id = :userId
          AND tm.role IN :roles
          AND tm.status = 'ACTIVE'
    """)
    boolean hasAnyRole(
            @Param("tenantId") Long tenantId,
            @Param("userId") Long userId,
            @Param("roles") List<TenantRole> roles
    );

    
    // Kiểm tra tồn tại lời mời để tránh gửi trùng
    boolean existsByTenantIdAndUserId(Long tenantId, Long userId);
}
