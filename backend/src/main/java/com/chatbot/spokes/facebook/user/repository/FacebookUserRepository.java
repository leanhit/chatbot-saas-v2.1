package com.chatbot.spokes.facebook.user.repository;

import com.chatbot.spokes.facebook.user.model.FacebookUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface FacebookUserRepository extends JpaRepository<FacebookUser, Long> {
    
    // Lấy theo pageId và tenantId
    @Query("SELECT fu FROM FacebookUser fu WHERE fu.pageId = :pageId AND fu.tenantId = :tenantId")
    Page<FacebookUser> findByPageIdAndTenantId(@Param("pageId") String pageId, @Param("tenantId") Long tenantId, Pageable pageable);

    // Tìm theo psid, pageId và tenantId
    @Query("SELECT fu FROM FacebookUser fu WHERE fu.psid = :psid AND fu.pageId = :pageId AND fu.tenantId = :tenantId")
    Optional<FacebookUser> findByPsidAndPageIdAndTenantId(
        @Param("psid") String psid, 
        @Param("pageId") String pageId, 
        @Param("tenantId") Long tenantId
    );

    // Tìm theo odooPartnerId, pageId và tenantId
    @Query("SELECT fu FROM FacebookUser fu WHERE fu.odooPartnerId = :odooPartnerId AND fu.pageId = :pageId AND fu.tenantId = :tenantId")
    Optional<FacebookUser> findByOdooPartnerIdAndPageIdAndTenantId(
        @Param("odooPartnerId") Integer odooPartnerId, 
        @Param("pageId") String pageId, 
        @Param("tenantId") Long tenantId
    );

    // Kiểm tra tồn tại theo psid, pageId và tenantId
    @Query("SELECT CASE WHEN COUNT(fu) > 0 THEN true ELSE false END FROM FacebookUser fu WHERE fu.psid = :psid AND fu.pageId = :pageId AND fu.tenantId = :tenantId")
    boolean existsByPsidAndPageIdAndTenantId(
        @Param("psid") String psid, 
        @Param("pageId") String pageId, 
        @Param("tenantId") Long tenantId
    );

    // Tìm kiếm theo pageId, tenantId và tên
    @Query("SELECT fu FROM FacebookUser fu WHERE fu.pageId = :pageId AND fu.tenantId = :tenantId AND " +
           "LOWER(fu.name) LIKE LOWER(concat('%', :keyword, '%'))")
    Page<FacebookUser> searchByPageIdAndTenantIdAndNameContaining(
        @Param("pageId") String pageId,
        @Param("tenantId") Long tenantId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    // Tìm theo pageId, tenantId và odooPartnerId là null
    @Query("SELECT fu FROM FacebookUser fu WHERE fu.pageId = :pageId AND fu.tenantId = :tenantId AND fu.odooPartnerId IS NULL")
    Page<FacebookUser> findByPageIdAndTenantIdAndOdooPartnerIdIsNull(
        @Param("pageId") String pageId,
        @Param("tenantId") Long tenantId,
        Pageable pageable
    );

    // ===== NEW METHODS FOR CUSTOMER DATA QUERY =====
    
    // Lấy theo psid và tenantId
    Optional<FacebookUser> findByPsidAndTenantId(String psid, Long tenantId);
    
    // Lấy theo danh sách psid và tenantId
    List<FacebookUser> findByPsidInAndTenantId(Set<String> psids, Long tenantId);
    
    // Tìm kiếm theo tên và tenantId
    @Query("SELECT fu FROM FacebookUser fu WHERE LOWER(fu.name) LIKE LOWER(concat('%', :keyword, '%')) AND fu.tenantId = :tenantId")
    List<FacebookUser> findByNameContainingIgnoreCaseAndTenantId(@Param("keyword") String keyword, @Param("tenantId") Long tenantId);
    
    // Đếm theo tenantId
    long countByTenantId(Long tenantId);
}
