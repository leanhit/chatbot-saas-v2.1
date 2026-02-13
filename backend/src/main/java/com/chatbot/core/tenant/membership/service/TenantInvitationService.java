package com.chatbot.core.tenant.membership.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chatbot.core.tenant.membership.dto.InviteMemberRequest;
import com.chatbot.core.tenant.membership.dto.InvitationResponse;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.membership.model.TenantInvitation;
import com.chatbot.core.tenant.membership.model.TenantMember;
import com.chatbot.core.tenant.membership.model.InvitationStatus;
import com.chatbot.core.tenant.membership.model.MembershipStatus;
import com.chatbot.core.tenant.membership.repository.TenantInvitationRepository;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TenantInvitationService {
    
    private final TenantInvitationRepository invitationRepo;
    private final TenantRepository tenantRepo;
    private final UserRepository userRepository;
    private final TenantMemberRepository memberRepo;

    /**
     * Admin thực hiện mời user vào tenant
     */
    @Transactional
    public void inviteMember(Long tenantId, InviteMemberRequest request, User admin) {
        Tenant tenant = tenantRepo.findById(tenantId)
            .orElseThrow(() -> new RuntimeException("Tenant không tồn tại"));

        User userToBeInvited = userRepository.findByEmail(request.getEmail().toLowerCase())
            .orElseThrow(() -> new RuntimeException("Người dùng này chưa có tài khoản trên hệ thống."));

        if (memberRepo.existsByTenantIdAndUserId(tenantId, userToBeInvited.getId())) {
            throw new RuntimeException("Người dùng đã là thành viên của tổ chức này rồi.");
        }

        if (invitationRepo.existsByTenantIdAndEmailAndStatus(tenantId, request.getEmail(), InvitationStatus.PENDING)) {
            throw new RuntimeException("Một lời mời đang ở trạng thái chờ xác nhận.");
        }

        TenantInvitation invitation = TenantInvitation.builder()
            .tenant(tenant)
            .email(request.getEmail().toLowerCase())
            .role(request.getRole())
            .token(UUID.randomUUID().toString())
            .status(InvitationStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .expiresAt(LocalDateTime.now().plusDays(7))
            .invitedBy(admin)
            .build();
            
        invitationRepo.save(invitation);
    }

    /**
     * Lấy danh sách lời mời của một Tenant (Sửa lỗi: Method listInvitations)
     */
    public List<InvitationResponse> listInvitations(Long tenantId) {
        return invitationRepo.findByTenantId(tenantId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Lấy danh sách lời mời đang chờ xử lý của user
     * @param user Người dùng hiện tại
     * @return Danh sách lời mời đang chờ xử lý
     */
    public List<InvitationResponse> getMyPendingInvitations(User user) {
        return invitationRepo.findByEmailAndStatus(
            user.getEmail(),
            InvitationStatus.PENDING
        ).stream()
        .filter(invitation -> invitation.getExpiresAt() == null || 
                            invitation.getExpiresAt().isAfter(LocalDateTime.now()))
        .map(this::convertToResponse)
        .collect(Collectors.toList());
    }

    /**
     * User chấp nhận lời mời
     */
    @Transactional
    public void acceptInvitation(String token, User user) {
        TenantInvitation invitation = invitationRepo.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Lời mời không hợp lệ hoặc đã bị thu hồi."));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new RuntimeException("Lời mời này không còn ở trạng thái chờ.");
        }

        if (!invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new RuntimeException("Bạn không có quyền chấp nhận lời mời này.");
        }

        memberRepo.save(TenantMember.builder()
            .tenant(invitation.getTenant())
            .user(user)
            .role(invitation.getRole())
            .status(MembershipStatus.ACTIVE)
            .joinedAt(LocalDateTime.now())
            .build());

        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepo.save(invitation);
    }

    /**
     * User từ chối lời mời
     */
    @Transactional
    public void rejectInvitation(String token, User user) {
        TenantInvitation invitation = invitationRepo.findByToken(token)
            .orElseThrow(() -> new RuntimeException("Lời mời không tồn tại."));

        if (!invitation.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new RuntimeException("Bạn không có quyền từ chối lời mời này.");
        }

        invitation.setStatus(InvitationStatus.REJECTED);
        invitationRepo.save(invitation);
    }

    /**
     * Admin thu hồi lời mời (Sửa lỗi: Method revokeInvitation)
     */
    @Transactional
    public void revokeInvitation(Long tenantId, Long invitationId) {
        TenantInvitation invitation = invitationRepo.findByIdAndTenantId(invitationId, tenantId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lời mời trong tổ chức này."));
        
        invitation.setStatus(InvitationStatus.REVOKED);
        invitationRepo.save(invitation);
    }

    /**
     * Helper: Convert Entity to DTO
     */
    private InvitationResponse convertToResponse(TenantInvitation invitation) {
        return InvitationResponse.builder()
                .id(invitation.getId())
                .name(invitation.getTenant() != null ? 
                    invitation.getTenant().getName() : null)
                .email(invitation.getEmail())
                .role(invitation.getRole())
                .status(invitation.getStatus())
                .expiresAt(invitation.getExpiresAt())
                .build();
    }
}