package com.chatbot.modules.tenant.membership.repository;

import com.chatbot.modules.tenant.membership.model.TenantInvitation;
import com.chatbot.modules.tenant.membership.model.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TenantInvitationRepository extends JpaRepository<TenantInvitation, Long> {
    
    // Tìm lời mời theo token duy nhất
    Optional<TenantInvitation> findByToken(String token);
    
    // Tìm lời mời theo token và status
    Optional<TenantInvitation> findByTokenAndStatus(String token, InvitationStatus status);
    
    // Kiểm tra tồn tại lời mời để tránh gửi trùng
    boolean existsByTenantIdAndEmailAndStatus(UUID tenantId, String email, InvitationStatus status);
    
    // Lấy danh sách lời mời của một Tenant (Dùng cho API listInvitations)
    List<TenantInvitation> findByTenantId(UUID tenantId);
    
    // Lấy danh sách lời mời chờ xử lý của một User (Dùng khi User xem dashboard thông báo của mình)
    List<TenantInvitation> findByEmailAndStatus(String email, InvitationStatus status);

    // Tìm kiếm lời mời cụ thể trong một Tenant (Tăng tính bảo mật khi revoke)
    Optional<TenantInvitation> findByIdAndTenantId(Long id, UUID tenantId);
    
}