package com.chatbot.core.tenant.membership.service;

import com.chatbot.core.user.model.User;
import com.chatbot.core.tenant.membership.dto.*;
import com.chatbot.core.tenant.membership.model.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TenantMembershipFacade {

    private final TenantMemberService memberService;
    private final TenantJoinRequestService joinService;
    private final TenantSelfService selfService;
    private final TenantInvitationService invitationService;

    /* ================= MEMBERS ================= */

    public Page<MemberResponse> listMembers(Long tenantId, Pageable p) {
        return memberService.listMembers(tenantId, p);
    }

    public MemberResponse getMember(Long tenantId, Long userId) {
        return memberService.getMember(tenantId, userId);
    }

    /** ✅ SPEC: members/me */
    public MemberResponse myMember(Long tenantId, User user) {
        return memberService.getMyMember(tenantId, user);
    }

    public void updateRole(Long tenantId, Long userId, TenantRole role) {
        memberService.updateRole(tenantId, userId, role);
    }

    public void removeMember(Long tenantId, Long userId) {
        memberService.removeMember(tenantId, userId);
    }

    /* ================= JOIN ================= */

    public void requestJoin(Long tenantId, User user) {
        joinService.requestToJoin(tenantId, user);
    }

    public List<MemberResponse> pending(Long tenantId) {
        return joinService.getPendingRequests(tenantId);
    }

    public void updateJoinRequest(Long tenantId, Long requestId, MembershipStatus status) {
        joinService.updateStatus(tenantId, requestId, status);
    }

    /* ================= SELF ================= */

    public List<TenantPendingResponse> myPending(User user) {
        return selfService.getMyPending(user);
    }

    public void leave(Long tenantId, User user) {
        selfService.leaveTenant(tenantId, user);
    }
    
    /**
     * Lấy danh sách lời mời đang chờ xử lý của user
     */
    public List<InvitationResponse> getMyInvitations(User user) {
        return invitationService.getMyPendingInvitations(user);
    }
}
