package com.chatbot.core.tenant.membership.service;

import com.chatbot.core.user.model.User;
import com.chatbot.core.tenant.membership.dto.*;
import com.chatbot.core.tenant.membership.model.*;
import com.chatbot.core.tenant.membership.repository.TenantJoinRequestRepository;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.tenant.service.TenantAuditLogService;
import com.chatbot.core.tenant.exception.BusinessLogicException;
import com.chatbot.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, transactionManager = "tenantTransactionManager")
public class TenantSelfService {

    private final TenantMemberRepository memberRepo;
    private final TenantJoinRequestRepository joinRequestRepo;
    private final TenantAuditLogService auditLogService;

    /* ================= MY PENDING ================= */

    public List<TenantPendingResponse> getMyPending(User user) {
        return joinRequestRepo.findByUserIdAndStatus(user.getId(), MembershipStatus.PENDING)
                .stream()
                .map(request -> TenantPendingResponse.builder()
                        .id(request.getId())
                        .tenantKey(request.getTenant().getTenantKey())
                        .name(request.getTenant().getName())
                        .status(request.getTenant().getStatus())
                        .visibility(request.getTenant().getVisibility())
                        .requestedAt(request.getCreatedAt())
                        .logoUrl(request.getTenant().getProfile() != null
                                ? request.getTenant().getProfile().getLogoUrl()
                                : null)
                        .build())
                .toList();
    }

    /* ================= LEAVE ================= */

    @Transactional(transactionManager = "tenantTransactionManager")
    public void leaveTenant(Long tenantId, User user) {
        TenantMember member = memberRepo.findByTenant_IdAndUserId(tenantId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bạn không phải thành viên của tenant này"));

        if (member.getRole() == TenantRole.OWNER) {
            throw new BusinessLogicException("OWNER phải chuyển quyền sở hữu trước khi rời tổ chức");
        }

        memberRepo.delete(member);

        auditLogService.logAction(tenantId, user.getEmail(), "LEAVE_TENANT",
            "User left the tenant");
    }
}
