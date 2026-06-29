package com.chatbot.spokes.odoo.repository;

import com.chatbot.spokes.odoo.model.CustomerStatus;
import com.chatbot.spokes.odoo.model.FbCustomerStaging;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;


@Repository
public interface FbCustomerStagingRepository extends JpaRepository<FbCustomerStaging, String> {

    // Lấy theo ownerId và tenantId
    @Query("SELECT fcs FROM FbCustomerStaging fcs WHERE fcs.ownerId = :ownerId AND fcs.tenantId = :tenantId")
    List<FbCustomerStaging> findByOwnerIdAndTenantId(@Param("ownerId") String ownerId, @Param("tenantId") Long tenantId);

    // Lấy theo ownerId, status và tenantId
    @Query("SELECT fcs FROM FbCustomerStaging fcs WHERE fcs.ownerId = :ownerId AND fcs.status = :status AND fcs.tenantId = :tenantId")
    List<FbCustomerStaging> findByOwnerIdAndStatusAndTenantId(
        @Param("ownerId") String ownerId, 
        @Param("status") CustomerStatus status, 
        @Param("tenantId") Long tenantId
    );

    // Tìm theo psid, ownerId và tenantId
    @Query("SELECT fcs FROM FbCustomerStaging fcs WHERE fcs.psid = :psid AND fcs.ownerId = :ownerId AND fcs.tenantId = :tenantId")
    Optional<FbCustomerStaging> findByPsidAndOwnerIdAndTenantId(
        @Param("psid") String psid, 
        @Param("ownerId") String ownerId, 
        @Param("tenantId") Long tenantId
    );

    // Xóa theo psid, ownerId và tenantId
    @Modifying
    @Transactional
    @Query("DELETE FROM FbCustomerStaging fcs WHERE fcs.psid = :psid AND fcs.ownerId = :ownerId AND fcs.tenantId = :tenantId")
    void deleteByPsidAndOwnerIdAndTenantId(
        @Param("psid") String psid, 
        @Param("ownerId") String ownerId, 
        @Param("tenantId") Long tenantId
    );

    // ===== NEW METHODS FOR CUSTOMER DATA QUERY =====
    
    // Lấy theo tenantId với pagination
    Page<FbCustomerStaging> findByTenantIdOrderByUpdatedAtDesc(Long tenantId, Pageable pageable);
    
    // Lấy theo tenantId và status với pagination
    Page<FbCustomerStaging> findByTenantIdAndStatusOrderByUpdatedAtDesc(Long tenantId, CustomerStatus status, Pageable pageable);
    
    // Lấy theo psid và tenantId
    Optional<FbCustomerStaging> findByPsidAndTenantId(String psid, Long tenantId);
    
    // Lấy theo danh sách psid và tenantId
    List<FbCustomerStaging> findByPsidInAndTenantId(Set<String> psids, Long tenantId);
    
    // Lấy theo danh sách psid và tenantId có phân trang
    Page<FbCustomerStaging> findByPsidInAndTenantId(Set<String> psids, Long tenantId, Pageable pageable);
    
    // Tìm theo phone chứa keyword và tenantId
    @Query("SELECT fcs FROM FbCustomerStaging fcs WHERE fcs.phones LIKE %:keyword% AND fcs.tenantId = :tenantId")
    List<FbCustomerStaging> findByPhonesContainingAndTenantId(@Param("keyword") String keyword, @Param("tenantId") Long tenantId);
    
    // Tìm kiếm khách hàng phân trang trực tiếp ở database theo phone hoặc tên FacebookUser
    @Query("SELECT fcs FROM FbCustomerStaging fcs WHERE fcs.tenantId = :tenantId AND (" +
           "fcs.phones LIKE %:keyword% OR " +
           "EXISTS (SELECT fu FROM FacebookUser fu WHERE fu.psid = fcs.psid AND fu.tenantId = :tenantId AND LOWER(fu.name) LIKE LOWER(CONCAT('%', :keyword, '%')))" +
           ")")
    Page<FbCustomerStaging> searchCustomersWithDatabasePagination(
            @Param("keyword") String keyword, 
            @Param("tenantId") Long tenantId, 
            Pageable pageable);

    // Đếm theo tenantId
    long countByTenantId(Long tenantId);
    
    // Đếm theo tenantId và status
    long countByTenantIdAndStatus(Long tenantId, CustomerStatus status);
}
