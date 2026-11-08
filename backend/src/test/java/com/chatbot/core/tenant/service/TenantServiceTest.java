package com.chatbot.core.tenant.service;

import com.chatbot.core.membership.model.MembershipStatus;
import com.chatbot.core.membership.model.TenantMember;
import com.chatbot.core.membership.model.TenantRole;
import com.chatbot.core.membership.repository.TenantMemberRepository;
import com.chatbot.core.message.store.service.AgentProvisioningService;
import com.chatbot.core.tenant.dto.CreateTenantRequest;
import com.chatbot.core.tenant.dto.TenantBasicInfoRequest;
import com.chatbot.core.tenant.dto.TenantContactInfoRequest;
import com.chatbot.core.tenant.exception.InsufficientPermissionException;
import com.chatbot.core.tenant.exception.InvalidTenantKeyException;
import com.chatbot.core.tenant.exception.TenantNotFoundException;
import com.chatbot.core.tenant.exception.TenantStatusTransitionException;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.model.TenantVisibility;
import com.chatbot.core.tenant.profile.model.TenantProfile;
import com.chatbot.core.tenant.profile.repository.TenantProfileRepository;
import com.chatbot.core.tenant.profile.service.TenantProfileService;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.AuthRepository;
import com.chatbot.core.user.repository.UserRepository;
import com.chatbot.shared.address.service.AddressService;
import com.chatbot.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock private TenantRepository tenantRepository;
    @Mock private AuthRepository authRepository;
    @Mock private UserRepository userRepository;
    @Mock private TenantMemberRepository tenantMemberRepository;
    @Mock private TenantPackageService tenantPackageService;
    @Mock private TenantProfileRepository tenantProfileRepository;
    @Mock private TenantProfileService tenantProfileService;
    @Mock private AddressService addressService;
    @Mock private TenantAuditLogService auditLogService;
    @Mock private TenantPermissionValidator permissionValidator;
    @Mock private TenantCleanupService tenantCleanupService;
    @Mock private TenantValidationService tenantValidationService;
    @Mock private AgentProvisioningService agentProvisioningService;
    @Mock private PlatformTransactionManager tenantTransactionManager;

    @InjectMocks
    private TenantService service;

    @BeforeEach
    void setDefaults() {
        ReflectionTestUtils.setField(service, "trialDays", 30);
    }

    private Tenant tenant(Long id, String key, TenantStatus status) {
        Tenant t = new Tenant();
        t.setId(id);
        t.setTenantKey(key);
        t.setName("Tenant " + key);
        t.setStatus(status);
        t.setVisibility(TenantVisibility.PUBLIC);
        return t;
    }

    private User user(Long id, String email) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        return u;
    }

    private TenantMember member(Tenant tenant, Long userId, TenantRole role) {
        TenantMember m = new TenantMember();
        m.setTenant(tenant);
        m.setUserId(userId);
        m.setRole(role);
        m.setStatus(MembershipStatus.ACTIVE);
        return m;
    }

    // ---------- getTenant / getTenantIdByKey / getTenantInfoByKey ----------

    @Test
    void getTenantReturnsTenant() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant(1L, "acme", TenantStatus.ACTIVE)));
        assertEquals("acme", service.getTenant(1L).getTenantKey());
    }

    @Test
    void getTenantThrowsWhenMissing() {
        when(tenantRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(TenantNotFoundException.class, () -> service.getTenant(99L));
    }

    @Test
    void getTenantIdByKeyReturnsIdOrNull() {
        when(tenantRepository.findByTenantKey("acme")).thenReturn(Optional.of(tenant(5L, "acme", TenantStatus.ACTIVE)));
        assertEquals(5L, service.getTenantIdByKey("acme"));
        when(tenantRepository.findByTenantKey("nope")).thenReturn(Optional.empty());
        assertNull(service.getTenantIdByKey("nope"));
    }

    @Test
    void getTenantInfoByKeyResolvesByKey() {
        when(tenantRepository.findByTenantKey("acme")).thenReturn(Optional.of(tenant(5L, "acme", TenantStatus.ACTIVE)));
        var info = service.getTenantInfoByKey("acme");
        assertEquals(5L, info.getId());
        assertEquals(TenantStatus.ACTIVE, info.getStatus());
    }

    @Test
    void getTenantInfoByKeyFallsBackToNumericId() {
        when(tenantRepository.findByTenantKey("42")).thenReturn(Optional.empty());
        when(tenantRepository.findById(42L)).thenReturn(Optional.of(tenant(42L, "num", TenantStatus.ACTIVE)));
        var info = service.getTenantInfoByKey("42");
        assertEquals(42L, info.getId());
    }

    @Test
    void getTenantInfoByKeyReturnsNullForBlankOrUnknown() {
        assertNull(service.getTenantInfoByKey(null));
        assertNull(service.getTenantInfoByKey("  "));
        when(tenantRepository.findByTenantKey("ghost")).thenReturn(Optional.empty());
        assertNull(service.getTenantInfoByKey("ghost"));
    }

    // ---------- getTenantForCurrentUser ----------

    @Test
    void getTenantForCurrentUserReturnsTenantForMember() {
        Tenant t = tenant(1L, "acme", TenantStatus.ACTIVE);
        when(permissionValidator.getCurrentUserEmail()).thenReturn("u@t.com");
        when(authRepository.findByEmail("u@t.com")).thenReturn(Optional.of(user(7L, "u@t.com")));
        when(tenantMemberRepository.findByTenantIdAndUserIdAndStatus(1L, 7L, MembershipStatus.ACTIVE))
            .thenReturn(Optional.of(member(t, 7L, TenantRole.EDITOR)));

        var resp = service.getTenantForCurrentUser(1L);
        assertEquals("acme", resp.getTenantKey());
    }

    @Test
    void getTenantForCurrentUserRejectsNonMember() {
        when(permissionValidator.getCurrentUserEmail()).thenReturn("u@t.com");
        when(authRepository.findByEmail("u@t.com")).thenReturn(Optional.of(user(7L, "u@t.com")));
        when(tenantMemberRepository.findByTenantIdAndUserIdAndStatus(1L, 7L, MembershipStatus.ACTIVE))
            .thenReturn(Optional.empty());

        assertThrows(InsufficientPermissionException.class, () -> service.getTenantForCurrentUser(1L));
    }

    // ---------- status transitions ----------

    @Test
    void suspendTenantRequiresAdmin() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant(1L, "acme", TenantStatus.ACTIVE)));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("user@t.com");
        when(permissionValidator.isAdmin("user@t.com")).thenReturn(false);

        assertThrows(InsufficientPermissionException.class, () -> service.suspendTenant(1L));
        verify(tenantRepository, never()).save(any());
    }

    @Test
    void suspendTenantSetsStatusAndAudits() {
        Tenant t = tenant(1L, "acme", TenantStatus.ACTIVE);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(t));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("admin@t.com");
        when(permissionValidator.isAdmin("admin@t.com")).thenReturn(true);
        when(tenantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.suspendTenant(1L);

        assertEquals(TenantStatus.SUSPENDED, t.getStatus());
        verify(tenantValidationService).validateStatusTransition(TenantStatus.ACTIVE, TenantStatus.SUSPENDED);
        verify(auditLogService).logAction(1L, "admin@t.com", "SUSPEND_TENANT", "Tenant suspended");
    }

    @Test
    void suspendTenantPropagatesInvalidTransition() {
        Tenant t = tenant(1L, "acme", TenantStatus.SUSPENDED);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(t));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("admin@t.com");
        when(permissionValidator.isAdmin("admin@t.com")).thenReturn(true);
        doThrow(new TenantStatusTransitionException("bad")).when(tenantValidationService)
            .validateStatusTransition(TenantStatus.SUSPENDED, TenantStatus.SUSPENDED);

        assertThrows(TenantStatusTransitionException.class, () -> service.suspendTenant(1L));
    }

    @Test
    void deactivateTenantRequiresOwner() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant(1L, "acme", TenantStatus.ACTIVE)));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("editor@t.com");
        when(permissionValidator.isOwner(1L, "editor@t.com")).thenReturn(false);

        assertThrows(InsufficientPermissionException.class, () -> service.deactivateTenant(1L));
    }

    @Test
    void deactivateTenantSetsInactive() {
        Tenant t = tenant(1L, "acme", TenantStatus.ACTIVE);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(t));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("owner@t.com");
        when(permissionValidator.isOwner(1L, "owner@t.com")).thenReturn(true);
        when(tenantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.deactivateTenant(1L);

        assertEquals(TenantStatus.INACTIVE, t.getStatus());
        verify(auditLogService).logAction(1L, "owner@t.com", "DEACTIVATE_TENANT", "Tenant deactivated");
    }

    // ---------- deleteTenant ----------

    @Test
    void deleteTenantThrowsWhenNotFound() {
        when(tenantRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(TenantNotFoundException.class, () -> service.deleteTenant(99L));
    }

    @Test
    void deleteTenantRejectsNonAdminNonOwner() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant(1L, "acme", TenantStatus.ACTIVE)));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("user@t.com");
        when(permissionValidator.isAdminOrOwner(1L, "user@t.com")).thenReturn(false);

        assertThrows(InsufficientPermissionException.class, () -> service.deleteTenant(1L));
        verify(tenantCleanupService, never()).cleanupTenantData(any());
    }

    @Test
    void deleteTenantSoftDeletesAndCleansUp() {
        Tenant t = tenant(1L, "acme", TenantStatus.ACTIVE);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(t));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("admin@t.com");
        when(permissionValidator.isAdminOrOwner(1L, "admin@t.com")).thenReturn(true);
        when(tenantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.deleteTenant(1L);

        assertEquals(TenantStatus.DELETED, t.getStatus());
        verify(tenantCleanupService).cleanupTenantData(1L);
        verify(auditLogService).logAction(1L, "admin@t.com", "DELETE_TENANT", "Tenant soft-deleted");
    }

    @Test
    void deleteTenantSwallowsCleanupFailure() {
        Tenant t = tenant(1L, "acme", TenantStatus.ACTIVE);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(t));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("admin@t.com");
        when(permissionValidator.isAdminOrOwner(1L, "admin@t.com")).thenReturn(true);
        when(tenantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("cleanup fail")).when(tenantCleanupService).cleanupTenantData(1L);

        assertDoesNotThrow(() -> service.deleteTenant(1L));
        assertEquals(TenantStatus.DELETED, t.getStatus());
    }

    // ---------- updateBasicInfo ----------

    @Test
    void updateBasicInfoRejectsEmptyKey() {
        assertThrows(InvalidTenantKeyException.class,
            () -> service.updateBasicInfo("  ", new TenantBasicInfoRequest()));
    }

    @Test
    void updateBasicInfoUpdatesFields() {
        Tenant t = tenant(1L, "acme", TenantStatus.ACTIVE);
        when(tenantRepository.findByTenantKey("acme")).thenReturn(Optional.of(t));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("owner@t.com");
        when(permissionValidator.isAdminOrOwner(1L, "owner@t.com")).thenReturn(true);
        when(tenantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TenantBasicInfoRequest req = new TenantBasicInfoRequest();
        req.setName("New Name");
        req.setVisibility(TenantVisibility.PRIVATE);

        var resp = service.updateBasicInfo("acme", req);

        assertEquals("New Name", t.getName());
        assertEquals(TenantVisibility.PRIVATE, t.getVisibility());
        assertEquals("New Name", resp.getName());
        verify(auditLogService).logAction(eq(1L), eq("owner@t.com"), eq("UPDATE_BASIC_INFO"), anyString());
    }

    @Test
    void updateBasicInfoValidatesStatusTransition() {
        Tenant t = tenant(1L, "acme", TenantStatus.ACTIVE);
        when(tenantRepository.findByTenantKey("acme")).thenReturn(Optional.of(t));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("admin@t.com");
        when(permissionValidator.isAdminOrOwner(1L, "admin@t.com")).thenReturn(true);
        when(tenantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TenantBasicInfoRequest req = new TenantBasicInfoRequest();
        req.setStatus(TenantStatus.SUSPENDED);

        service.updateBasicInfo("acme", req);

        assertEquals(TenantStatus.SUSPENDED, t.getStatus());
        verify(tenantValidationService).validateStatusTransition(TenantStatus.ACTIVE, TenantStatus.SUSPENDED);
    }

    @Test
    void updateBasicInfoRejectsNonPrivileged() {
        when(tenantRepository.findByTenantKey("acme")).thenReturn(Optional.of(tenant(1L, "acme", TenantStatus.ACTIVE)));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("user@t.com");
        when(permissionValidator.isAdminOrOwner(1L, "user@t.com")).thenReturn(false);

        assertThrows(InsufficientPermissionException.class,
            () -> service.updateBasicInfo("acme", new TenantBasicInfoRequest()));
    }

    // ---------- updateContactInfo ----------

    @Test
    void updateContactInfoCreatesProfileWhenMissing() {
        Tenant t = tenant(1L, "acme", TenantStatus.ACTIVE);
        when(tenantRepository.findByTenantKey("acme")).thenReturn(Optional.of(t));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("owner@t.com");
        when(permissionValidator.isAdminOrOwner(1L, "owner@t.com")).thenReturn(true);
        when(tenantProfileRepository.findByTenant_Id(1L)).thenReturn(Optional.empty());
        when(tenantProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TenantContactInfoRequest req = new TenantContactInfoRequest();
        req.setEmail("contact@acme.com");
        req.setPhone("0901234567");

        service.updateContactInfo("acme", req);

        verify(tenantProfileRepository, times(2)).save(any(TenantProfile.class)); // create + update
        verify(auditLogService).logAction(eq(1L), eq("owner@t.com"), eq("UPDATE_CONTACT_INFO"), anyString());
    }

    @Test
    void updateContactInfoUpdatesExistingProfile() {
        Tenant t = tenant(1L, "acme", TenantStatus.ACTIVE);
        TenantProfile profile = new TenantProfile();
        when(tenantRepository.findByTenantKey("acme")).thenReturn(Optional.of(t));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("owner@t.com");
        when(permissionValidator.isAdminOrOwner(1L, "owner@t.com")).thenReturn(true);
        when(tenantProfileRepository.findByTenant_Id(1L)).thenReturn(Optional.of(profile));
        when(tenantProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TenantContactInfoRequest req = new TenantContactInfoRequest();
        req.setWebsite("https://acme.com");

        service.updateContactInfo("acme", req);

        assertEquals("https://acme.com", profile.getWebsite());
        verify(tenantProfileRepository, times(1)).save(profile);
    }

    // ---------- switchTenantByKey ----------

    @Test
    void switchTenantByKeyThrowsWhenMissing() {
        when(tenantRepository.findByTenantKey("ghost")).thenReturn(Optional.empty());
        assertThrows(TenantNotFoundException.class, () -> service.switchTenantByKey("ghost"));
    }

    // ---------- getUserTenantsDetail ----------

    @Test
    void getUserTenantsDetailReturnsEmptyForNoMemberships() {
        when(permissionValidator.getCurrentUserEmail()).thenReturn("u@t.com");
        when(authRepository.findByEmail("u@t.com")).thenReturn(Optional.of(user(7L, "u@t.com")));
        when(tenantMemberRepository.findByUserIdWithTenant(7L)).thenReturn(List.of());

        assertTrue(service.getUserTenantsDetail().isEmpty());
    }

    // ---------- createTenant ----------

    @Test
    void createTenantOrchestratesFullFlow() {
        // user lookup (userTransactionManager path — getCurrentUser uses userRepository)
        when(permissionValidator.getCurrentUserEmail()).thenReturn("owner@t.com");
        User u = user(7L, "owner@t.com");
        when(userRepository.findByEmail("owner@t.com")).thenReturn(Optional.of(u));

        // tenant tx manager mock
        TransactionStatus txStatus = mock(TransactionStatus.class);
        when(tenantTransactionManager.getTransaction(any())).thenReturn(txStatus);

        // tenant save → assign id/key
        when(tenantRepository.save(any())).thenAnswer(inv -> {
            Tenant t = inv.getArgument(0);
            t.setId(1L);
            t.setTenantKey("acme");
            return t;
        });
        when(tenantMemberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant(1L, "acme", TenantStatus.ACTIVE)));

        CreateTenantRequest req = new CreateTenantRequest();
        req.setName("Acme");
        req.setVisibility(TenantVisibility.PUBLIC);

        var resp = service.createTenant(req);

        assertNotNull(resp);
        verify(tenantMemberRepository).save(argThat(m ->
            m.getRole() == TenantRole.OWNER && m.getUserId().equals(7L)));
        verify(addressService).getOrCreateSingleAddress(eq(1L), any(), eq(1L));
        verify(agentProvisioningService).provisionAgent(any());
        verify(tenantPackageService).assignDefaultPackageToTenant(any());
        verify(auditLogService).logAction(eq(1L), eq("owner@t.com"), eq("CREATE_TENANT"), anyString());
    }

    @Test
    void createTenantThrowsWhenUserNotFound() {
        when(permissionValidator.getCurrentUserEmail()).thenReturn("ghost@t.com");
        when(userRepository.findByEmail("ghost@t.com")).thenReturn(Optional.empty());

        CreateTenantRequest req = new CreateTenantRequest();
        req.setName("Acme");

        assertThrows(ResourceNotFoundException.class, () -> service.createTenant(req));
        verify(tenantRepository, never()).save(any());
    }
}
