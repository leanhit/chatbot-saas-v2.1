package com.chatbot.core.tenant.service;

import com.chatbot.core.tenant.exception.InsufficientPermissionException;
import com.chatbot.core.tenant.exception.TenantStatusTransitionException;
import com.chatbot.core.tenant.membership.model.MembershipStatus;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.model.TenantVisibility;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.AuthRepository;
import com.chatbot.shared.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TenantValidationService {

    private final TenantPermissionValidator permissionValidator;
    private final TenantMemberRepository tenantMemberRepository;
    private final AuthRepository authRepository;

    /**
     * Validate status transition logic
     */
    public void validateStatusTransition(TenantStatus currentStatus, TenantStatus newStatus) {
        if (currentStatus == newStatus) return; // idempotent

        boolean valid = switch (currentStatus) {
            case ACTIVE    -> newStatus == TenantStatus.SUSPENDED || newStatus == TenantStatus.INACTIVE;
            case SUSPENDED -> newStatus == TenantStatus.ACTIVE;
            case INACTIVE  -> newStatus == TenantStatus.ACTIVE;
            default        -> false;
        };

        if (!valid) {
            throw new TenantStatusTransitionException(
                "Không thể chuyển từ trạng thái " + currentStatus + " sang " + newStatus);
        }
    }

    /**
     * Validate access to a private tenant
     */
    public void validatePrivateTenantAccess(Tenant tenant) {
        if (tenant.getVisibility() == TenantVisibility.PRIVATE) {
            String currentUserEmail = permissionValidator.getCurrentUserEmail();
            Long userId = authRepository.findByEmail(currentUserEmail)
                    .map(User::getId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));
            boolean isMember = tenantMemberRepository
                    .findByTenantIdAndUserIdAndStatus(tenant.getId(), userId, MembershipStatus.ACTIVE)
                    .isPresent();
            if (!isMember) {
                throw new InsufficientPermissionException(
                    com.chatbot.shared.exceptions.ErrorCode.CANNOT_ACCESS_TENANT, 
                    "You do not have permission to access this tenant"
                );
            }
        }
    }
}
