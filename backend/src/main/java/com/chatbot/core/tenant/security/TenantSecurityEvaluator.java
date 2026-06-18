package com.chatbot.core.tenant.security;

import com.chatbot.core.tenant.service.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component("tenantSecurity")
@RequiredArgsConstructor
public class TenantSecurityEvaluator {

    private final TenantService tenantService;
    private final com.chatbot.core.tenant.service.TenantPermissionValidator permissionValidator;

    /**
     * Check if current user is member of tenant
     */
    public boolean isTenantMember(Long tenantId) {
        try {
            String userEmail = permissionValidator.getCurrentUserEmail();
            return permissionValidator.isActiveMember(tenantId, userEmail);
        } catch (Exception e) {
            log.error("Error checking tenant membership", e);
            return false;
        }
    }

    /**
     * Check if current user is member of tenant by tenant key
     */
    public boolean isTenantMemberByKey(String tenantKey) {
        try {
            Long tenantId = tenantService.getTenantIdByKey(tenantKey);
            if (tenantId == null) {
                return false;
            }
            return isTenantMember(tenantId);
        } catch (Exception e) {
            log.error("Error checking tenant membership by key", e);
            return false;
        }
    }

    /**
     * Check if current user is tenant owner
     */
    public boolean isTenantOwner(Long tenantId) {
        try {
            String userEmail = permissionValidator.getCurrentUserEmail();
            return permissionValidator.isOwner(tenantId, userEmail);
        } catch (Exception e) {
            log.error("Error checking tenant ownership", e);
            return false;
        }
    }

    /**
     * Check if current user can manage tenant
     */
    public boolean canManageTenant(Long tenantId) {
        try {
            String userEmail = permissionValidator.getCurrentUserEmail();
            return permissionValidator.isAdminOrOwner(tenantId, userEmail);
        } catch (Exception e) {
            log.error("Error checking tenant management permission", e);
            return false;
        }
    }

    /**
     * Check if current user is admin
     */
    public boolean isAdmin() {
        try {
            String userEmail = permissionValidator.getCurrentUserEmail();
            return permissionValidator.isAdmin(userEmail);
        } catch (Exception e) {
            log.error("Error checking admin role", e);
            return false;
        }
    }
}
