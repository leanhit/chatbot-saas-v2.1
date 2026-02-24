package com.chatbot.spokes.odoo.repository;

import com.chatbot.spokes.odoo.model.FbCapturedPhone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface FbCapturedPhoneRepository extends JpaRepository<FbCapturedPhone, Long> {

    // Kiểm tra sự tồn tại theo số điện thoại và tenantId
    @Query("SELECT CASE WHEN COUNT(fcp) > 0 THEN true ELSE false END FROM FbCapturedPhone fcp WHERE fcp.phoneNumber = :phoneNumber AND fcp.tenantId = :tenantId")
    boolean existsByPhoneNumberAndTenantId(@Param("phoneNumber") String phoneNumber, @Param("tenantId") Long tenantId);
    
    @Deprecated
    boolean existsByPhoneNumber(String phoneNumber);

    // Kiểm tra sự tồn tại theo số điện thoại, ownerId và tenantId
    @Query("SELECT CASE WHEN COUNT(fcp) > 0 THEN true ELSE false END FROM FbCapturedPhone fcp WHERE fcp.phoneNumber = :phoneNumber AND fcp.ownerId = :ownerId AND fcp.tenantId = :tenantId")
    boolean existsByPhoneNumberAndOwnerIdAndTenantId(
        @Param("phoneNumber") String phoneNumber, 
        @Param("ownerId") String ownerId, 
        @Param("tenantId") Long tenantId
    );
    
    @Deprecated
    boolean existsByPhoneNumberAndOwnerId(String phoneNumber, String ownerId);

    // Lấy danh sách các phone theo ownerId và tenantId
    @Query("SELECT fcp FROM FbCapturedPhone fcp WHERE fcp.ownerId = :ownerId AND fcp.tenantId = :tenantId")
    List<FbCapturedPhone> findByOwnerIdAndTenantId(@Param("ownerId") String ownerId, @Param("tenantId") Long tenantId);
    
    @Deprecated
    List<FbCapturedPhone> findByOwnerId(String ownerId);
}
