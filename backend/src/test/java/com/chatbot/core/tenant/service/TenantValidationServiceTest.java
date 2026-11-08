package com.chatbot.core.tenant.service;

import com.chatbot.core.membership.model.MembershipStatus;
import com.chatbot.core.membership.repository.TenantMemberRepository;
import com.chatbot.core.tenant.exception.InsufficientPermissionException;
import com.chatbot.core.tenant.exception.TenantStatusTransitionException;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.model.TenantVisibility;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.AuthRepository;
import com.chatbot.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantValidationServiceTest {

    @Mock private TenantPermissionValidator permissionValidator;
    @Mock private TenantMemberRepository tenantMemberRepository;
    @Mock private AuthRepository authRepository;

    @InjectMocks
    private TenantValidationService service;

    // ---------- validateStatusTransition ----------

    @Test
    void sameStatusIsIdempotent() {
        assertDoesNotThrow(() -> service.validateStatusTransition(TenantStatus.ACTIVE, TenantStatus.ACTIVE));
    }

    @Test
    void activeCanSuspend() {
        assertDoesNotThrow(() -> service.validateStatusTransition(TenantStatus.ACTIVE, TenantStatus.SUSPENDED));
    }

    @Test
    void activeCanDeactivate() {
        assertDoesNotThrow(() -> service.validateStatusTransition(TenantStatus.ACTIVE, TenantStatus.INACTIVE));
    }

    @Test
    void suspendedCanReactivate() {
        assertDoesNotThrow(() -> service.validateStatusTransition(TenantStatus.SUSPENDED, TenantStatus.ACTIVE));
    }

    @Test
    void inactiveCanReactivate() {
        assertDoesNotThrow(() -> service.validateStatusTransition(TenantStatus.INACTIVE, TenantStatus.ACTIVE));
    }

    @Test
    void suspendedCannotGoInactive() {
        assertThrows(TenantStatusTransitionException.class,
            () -> service.validateStatusTransition(TenantStatus.SUSPENDED, TenantStatus.INACTIVE));
    }

    @Test
    void inactiveCannotSuspend() {
        assertThrows(TenantStatusTransitionException.class,
            () -> service.validateStatusTransition(TenantStatus.INACTIVE, TenantStatus.SUSPENDED));
    }

    // ---------- validatePrivateTenantAccess ----------

    private Tenant tenant(Long id, TenantVisibility visibility) {
        Tenant t = new Tenant();
        t.setId(id);
        t.setVisibility(visibility);
        return t;
    }

    @Test
    void publicTenantAlwaysAccessible() {
        assertDoesNotThrow(() -> service.validatePrivateTenantAccess(tenant(1L, TenantVisibility.PUBLIC)));
        verifyNoInteractions(permissionValidator, authRepository, tenantMemberRepository);
    }

    @Test
    void privateTenantAllowsActiveMember() {
        Tenant t = tenant(1L, TenantVisibility.PRIVATE);
        when(permissionValidator.getCurrentUserEmail()).thenReturn("member@test.com");
        User u = new User();
        u.setId(5L);
        when(authRepository.findByEmail("member@test.com")).thenReturn(Optional.of(u));
        when(tenantMemberRepository.findByTenantIdAndUserIdAndStatus(1L, 5L, MembershipStatus.ACTIVE))
            .thenReturn(Optional.of(new com.chatbot.core.membership.model.TenantMember()));

        assertDoesNotThrow(() -> service.validatePrivateTenantAccess(t));
    }

    @Test
    void privateTenantRejectsNonMember() {
        Tenant t = tenant(1L, TenantVisibility.PRIVATE);
        when(permissionValidator.getCurrentUserEmail()).thenReturn("outsider@test.com");
        User u = new User();
        u.setId(9L);
        when(authRepository.findByEmail("outsider@test.com")).thenReturn(Optional.of(u));
        when(tenantMemberRepository.findByTenantIdAndUserIdAndStatus(1L, 9L, MembershipStatus.ACTIVE))
            .thenReturn(Optional.empty());

        assertThrows(InsufficientPermissionException.class,
            () -> service.validatePrivateTenantAccess(t));
    }

    @Test
    void privateTenantRejectsUnknownUser() {
        Tenant t = tenant(1L, TenantVisibility.PRIVATE);
        when(permissionValidator.getCurrentUserEmail()).thenReturn("ghost@test.com");
        when(authRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> service.validatePrivateTenantAccess(t));
    }
}
