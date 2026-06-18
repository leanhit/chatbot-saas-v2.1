package com.chatbot.core.tenant.membership.repository;

import com.chatbot.core.tenant.membership.model.MembershipStatus;
import com.chatbot.core.tenant.membership.model.TenantMember;
import com.chatbot.core.tenant.membership.model.TenantRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TenantMemberRepository extends JpaRepository<TenantMember, Long> {

    /* =========================
       BASIC FIND / EXISTS
       ========================= */

    Optional<TenantMember> findByTenant_IdAndUserId(Long tenantId, Long userId);
    boolean existsByTenant_IdAndUserId(Long tenantId, Long userId);
    boolean existsByTenant_IdAndRole(Long tenantId, TenantRole role);
    boolean existsByTenant_IdAndUserIdAndRole(Long tenantId, Long userId, TenantRole role);

    @Query("SELECT COUNT(tm) > 0 FROM TenantMember tm " +
           "WHERE tm.tenant.id = :tenantId " +
           "AND tm.userId = :userId " +
           "AND tm.role = :role " +
           "AND tm.status = :status")
    boolean existsByTenantIdAndUserIdAndRoleAndStatus(@Param("tenantId") Long tenantId,
                                                       @Param("userId") Long userId,
                                                       @Param("role") TenantRole role,
                                                       @Param("status") MembershipStatus status);

    /* =========================
       STATUS AWARE (IMPORTANT)
       ========================= */

    Optional<TenantMember> findByTenant_IdAndUserIdAndStatus(Long tenantId, Long userId, MembershipStatus status);
    boolean existsByTenant_IdAndUserIdAndStatus(Long tenantId, Long userId, MembershipStatus status);

    Page<TenantMember> findByTenant_IdAndStatus(Long tenantId, MembershipStatus status, Pageable pageable);
    List<TenantMember> findByTenant_IdAndStatus(Long tenantId, MembershipStatus status);

    @Query("SELECT tm FROM TenantMember tm " +
           "WHERE tm.tenant.id = :tenantId " +
           "AND tm.userId = :userId " +
           "AND tm.status = :status")
    Optional<TenantMember> findByTenantIdAndUserIdAndStatus(@Param("tenantId") Long tenantId,
                                                             @Param("userId") Long userId,
                                                             @Param("status") MembershipStatus status);

    // Updated derived method name
    List<TenantMember> findByUserIdAndStatus(Long userId, MembershipStatus status);

    /* =========================
       LIST MEMBERS
       ========================= */

    Page<TenantMember> findByTenant_Id(Long tenantId, Pageable pageable);
    List<TenantMember> findByTenant_Id(Long tenantId);

    /* =========================
       USER ↔ TENANT QUERIES
       ========================= */

    List<TenantMember> findByUserId(Long userId);

    @Query("""
        SELECT tm FROM TenantMember tm
        JOIN FETCH tm.tenant
        WHERE tm.userId = :userId
    """)
    List<TenantMember> findByUserIdWithTenant(@Param("userId") Long userId);

    @Query("""
        SELECT tm FROM TenantMember tm
        JOIN FETCH tm.tenant
        WHERE tm.userId = :userId
          AND tm.status = 'ACTIVE'
    """)
    List<TenantMember> findActiveTenantsOfUser(@Param("userId") Long userId);

    @Query("""
        SELECT tm FROM TenantMember tm
        JOIN FETCH tm.tenant
        WHERE tm.userId = :userId
          AND tm.tenant.id IN :tenantIds
    """)
    List<TenantMember> findByUserIdAndTenantIdIn(@Param("userId") Long userId, 
                                                  @Param("tenantIds") List<Long> tenantIds);

    @Query("""
        SELECT tm FROM TenantMember tm
        JOIN FETCH tm.tenant
        WHERE tm.tenant.id = :tenantId
          AND tm.userId = :userId
    """)
    Optional<TenantMember> findByTenantIdAndUserId(@Param("tenantId") Long tenantId,
                                                   @Param("userId") Long userId);

    /* =========================
       ROLE / PERMISSION
       ========================= */

    long countByTenant_IdAndRole(Long tenantId, TenantRole role);

    @Query("""
        SELECT COUNT(tm) > 0 FROM TenantMember tm
        WHERE tm.tenant.id = :tenantId
          AND tm.userId = :userId
          AND tm.role IN :roles
          AND tm.status = 'ACTIVE'
    """)
    boolean hasAnyRole(@Param("tenantId") Long tenantId,
                       @Param("userId") Long userId,
                       @Param("roles") List<TenantRole> roles);

    // Kiểm tra tồn tại lời mời để tránh gửi trùng
    boolean existsByTenantIdAndUserId(Long tenantId, Long userId);
}
