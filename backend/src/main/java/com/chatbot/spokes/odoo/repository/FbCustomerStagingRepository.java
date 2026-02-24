package com.chatbot.spokes.odoo.repository;

import com.chatbot.spokes.odoo.model.CustomerStatus;
import com.chatbot.spokes.odoo.model.FbCustomerStaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
public interface FbCustomerStagingRepository extends JpaRepository<FbCustomerStaging, String> {

    // Lấy theo ownerId và tenantId
    @Query("SELECT fcs FROM FbCustomerStaging fcs WHERE fcs.ownerId = :ownerId AND fcs.tenantId = :tenantId")
    List<FbCustomerStaging> findByOwnerIdAndTenantId(@Param("ownerId") String ownerId, @Param("tenantId") Long tenantId);
    
    @Deprecated
    List<FbCustomerStaging> findByOwnerId(String ownerId);

    // Lấy theo ownerId, status và tenantId
    @Query("SELECT fcs FROM FbCustomerStaging fcs WHERE fcs.ownerId = :ownerId AND fcs.status = :status AND fcs.tenantId = :tenantId")
    List<FbCustomerStaging> findByOwnerIdAndStatusAndTenantId(
        @Param("ownerId") String ownerId, 
        @Param("status") CustomerStatus status, 
        @Param("tenantId") Long tenantId
    );
    
    @Deprecated
    List<FbCustomerStaging> findByOwnerIdAndStatus(String ownerId, CustomerStatus status);

    // Tìm theo psid, ownerId và tenantId
    @Query("SELECT fcs FROM FbCustomerStaging fcs WHERE fcs.psid = :psid AND fcs.ownerId = :ownerId AND fcs.tenantId = :tenantId")
    Optional<FbCustomerStaging> findByPsidAndOwnerIdAndTenantId(
        @Param("psid") String psid, 
        @Param("ownerId") String ownerId, 
        @Param("tenantId") Long tenantId
    );
    
    @Deprecated
    Optional<FbCustomerStaging> findByPsidAndOwnerId(String psid, String ownerId);

    // Xóa theo psid, ownerId và tenantId
    @Modifying
    @Transactional
    @Query("DELETE FROM FbCustomerStaging fcs WHERE fcs.psid = :psid AND fcs.ownerId = :ownerId AND fcs.tenantId = :tenantId")
    void deleteByPsidAndOwnerIdAndTenantId(
        @Param("psid") String psid, 
        @Param("ownerId") String ownerId, 
        @Param("tenantId") Long tenantId
    );
    
    @Deprecated
    @Transactional
    void deleteByPsidAndOwnerId(String psid, String ownerId);
}
