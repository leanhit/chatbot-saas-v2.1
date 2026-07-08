package com.chatbot.core.tenant.membership.repository;

import com.chatbot.core.tenant.membership.model.TenantInvitation;
import com.chatbot.core.tenant.membership.model.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TenantInvitationRepository extends JpaRepository<TenantInvitation, Long> {
    
    // Tìm lời mời theo token duy nhất
    Optional<TenantInvitation> findByToken(String token);
    
    // Kiểm tra tồn tại lời mời để tránh gửi trùng
    boolean existsByTenantIdAndEmailAndStatus(Long tenantId, String email, InvitationStatus status);
    
    // Lấy danh sách lời mời theo tenant, email và status (để check expired)
    List<TenantInvitation> findByTenantIdAndEmailAndStatus(Long tenantId, String email, InvitationStatus status);
    
    // Lấy danh sách lời mời của một Tenant (Dùng cho API listInvitations)
    List<TenantInvitation> findByTenantId(Long tenantId);
    
    // Lấy danh sách lời mời chờ xử lý của một User (Dùng khi User xem dashboard thông báo của mình)
    List<TenantInvitation> findByEmailAndStatus(String email, InvitationStatus status);

    // Tìm kiếm lời mời cụ thể trong một Tenant (Tăng tính bảo mật khi revoke)
    Optional<TenantInvitation> findByIdAndTenantId(Long id, Long tenantId);
    
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional(value = "tenantTransactionManager", rollbackFor = Exception.class)
    @org.springframework.data.jpa.repository.Query("DELETE FROM TenantInvitation i WHERE i.tenant.id = :tenantId")
    void deleteByTenantId(@org.springframework.data.repository.query.Param("tenantId") Long tenantId);
}