package com.chatbot.core.tenant.core.repository;

import com.chatbot.core.tenant.core.model.Tenant;
import com.chatbot.core.tenant.core.model.TenantStatus;
import com.chatbot.core.tenant.core.model.TenantVisibility;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    // Lấy thông tin Tenant kèm Profile bằng ID (Dùng Fetch Join để tối ưu)
    @Query("SELECT t FROM Tenant t LEFT JOIN FETCH t.profile WHERE t.id = :id")
    Optional<Tenant> findByIdWithProfile(@Param("id") Long id);

    // Lấy thông tin Tenant kèm Profile bằng tenantKey (Dùng Fetch Join để tối ưu)
    @Query("SELECT t FROM Tenant t LEFT JOIN FETCH t.profile WHERE t.tenantKey = :tenantKey")
    Optional<Tenant> findByTenantKeyWithProfile(@Param("tenantKey") String tenantKey);

    // Tìm tenant bằng tenantKey
    Optional<Tenant> findByTenantKey(String tenantKey);
    
    // Kiểm tra tenantKey tồn tại
    boolean existsByTenantKey(String tenantKey);

    // Lấy danh sách Tenant theo ID kèm Profile
    @Query("SELECT t FROM Tenant t LEFT JOIN FETCH t.profile WHERE t.id IN :ids")
    List<Tenant> findAllByIdsWithProfile(@Param("ids") List<Long> ids);
}
