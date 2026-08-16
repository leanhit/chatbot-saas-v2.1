package com.chatbot.core.tenant.repository;

import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.model.TenantVisibility;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    @Query("SELECT t FROM Tenant t WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Tenant> search(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT t FROM Tenant t WHERE " +
        "LOWER(t.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
        "AND t.status = :status " +
        "AND t.visibility = :visibility")
    Page<Tenant> searchActivePublicTenants(
        @Param("keyword") String keyword, 
        @Param("status") TenantStatus status, 
        @Param("visibility") TenantVisibility visibility, 
        Pageable pageable);

    Page<Tenant> findByVisibilityAndStatusAndNameContainingIgnoreCase(
            TenantVisibility visibility, 
            TenantStatus status, 
            String name, 
            Pageable pageable
    );

    // Optimized query with FETCH JOIN to avoid N+1 problem in searchTenants
    @Query("SELECT t FROM Tenant t LEFT JOIN FETCH t.profile LEFT JOIN FETCH t.professional WHERE t.visibility = :visibility AND t.status = :status AND (:name IS NULL OR :name = '' OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<Tenant> findByVisibilityAndStatusAndNameContainingIgnoreCaseWithProfile(
            @Param("visibility") TenantVisibility visibility,
            @Param("status") TenantStatus status,
            @Param("name") String name,
            Pageable pageable
    );

    // Lấy thông tin Tenant kèm Profile bằng ID (Dùng Fetch Join để tối ưu)
    @Query("SELECT t FROM Tenant t LEFT JOIN FETCH t.profile LEFT JOIN FETCH t.professional WHERE t.id = :id")
    Optional<Tenant> findByIdWithProfile(@Param("id") Long id);

    // Lấy thông tin Tenant kèm Profile bằng tenantKey (Dùng Fetch Join để tối ưu)
    @Query("SELECT t FROM Tenant t LEFT JOIN FETCH t.profile LEFT JOIN FETCH t.professional WHERE t.tenantKey = :tenantKey")
    Optional<Tenant> findByTenantKeyWithProfile(@Param("tenantKey") String tenantKey);

    // Tìm tenant bằng tenantKey
    Optional<Tenant> findByTenantKey(String tenantKey);
    
    // Kiểm tra tenantKey tồn tại
    boolean existsByTenantKey(String tenantKey);

    // Lấy danh sách Tenant theo ID kèm Profile
    @Query("SELECT t FROM Tenant t LEFT JOIN FETCH t.profile LEFT JOIN FETCH t.professional WHERE t.id IN :ids")
    List<Tenant> findAllByIdsWithProfile(@Param("ids") List<Long> ids);

    // Tìm tenant theo user ID (thông qua tenant membership)
    @Query("SELECT t FROM Tenant t WHERE EXISTS (" +
           "  SELECT 1 FROM TenantMember tm " +
           "  WHERE tm.tenant.id = t.id AND tm.userId = :userId" +
           ")")
    Optional<Tenant> findByUserId(@Param("userId") Long userId);

    // Kiêm tra user có access vào tenant không (thông qua tenant membership)
    @Query("SELECT COUNT(t) > 0 FROM Tenant t " +
           "WHERE t.id = :tenantId AND EXISTS (" +
           "  SELECT 1 FROM TenantMember tm " +
           "  WHERE tm.tenant.id = t.id AND tm.userId = :userId" +
           ")")
    boolean existsByUserIdAndTenantId(@Param("userId") Long userId, @Param("tenantId") Long tenantId);

    // Tìm tenants chưa có gói dịch vụ
    @Query("SELECT t FROM Tenant t WHERE t.currentPackageId IS NULL")
    List<Tenant> findByCurrentPackageIdIsNull();

    @org.springframework.data.jpa.repository.Modifying
    @Query("UPDATE Tenant t SET t.currentPackageId = :defaultPackageId, t.packageActivatedAt = :activatedAt, t.expiresAt = null WHERE t.currentPackageId IS NULL")
    int initializeTenantsWithDefaultPackage(@Param("defaultPackageId") String defaultPackageId, @Param("activatedAt") LocalDateTime activatedAt);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Tenant t WHERE t.id = :id")
    Optional<Tenant> findByIdWithPessimisticWriteLock(@Param("id") Long id);

    // Tìm các tenant có gói đã hết hạn
    @Query("SELECT t FROM Tenant t WHERE t.expiresAt IS NOT NULL AND t.expiresAt <= :now")
    List<Tenant> findExpiredTenants(@Param("now") LocalDateTime now);

    // Tìm các tenant đã soft-delete trước cutoff date (cho scheduled cleanup)
    @Query("SELECT t FROM Tenant t WHERE t.status = 'DELETED' AND t.updatedAt <= :cutoffDate")
    List<Tenant> findDeletedTenantsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}
