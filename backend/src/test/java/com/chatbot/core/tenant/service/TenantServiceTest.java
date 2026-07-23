package com.chatbot.core.tenant.service;

import com.chatbot.core.tenant.dto.CreateTenantRequest;
import com.chatbot.core.tenant.dto.TenantBasicInfoRequest;
import com.chatbot.core.tenant.dto.TenantContactInfoRequest;
import com.chatbot.core.tenant.dto.TenantResponse;
import com.chatbot.core.tenant.exception.*;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.model.TenantVisibility;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.membership.model.TenantMember;
import com.chatbot.core.tenant.membership.model.TenantRole;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.tenant.membership.model.MembershipStatus;
import com.chatbot.core.tenant.profile.model.TenantProfile;
import com.chatbot.core.tenant.profile.repository.TenantProfileRepository;
import com.chatbot.core.tenant.profile.service.TenantProfileService;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.AuthRepository;
import com.chatbot.core.user.repository.UserRepository;
import com.chatbot.shared.address.service.AddressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private AuthRepository authRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TenantMemberRepository tenantMemberRepository;

    @Mock
    private TenantPackageService tenantPackageService;

    @Mock
    private TenantProfileRepository tenantProfileRepository;

    @Mock
    private TenantProfileService tenantProfileService;

    @Mock
    private AddressService addressService;

    @Mock
    private TenantAuditLogService auditLogService;

    @Mock
    private TenantPermissionValidator permissionValidator;

    @Mock
    private TenantCleanupService tenantCleanupService;

    @Mock
    private TenantValidationService tenantValidationService;

    @InjectMocks
    private TenantService tenantService;

    private User testUser;
    private Tenant testTenant;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testTenant = new Tenant();
        testTenant.setId(1L);
        testTenant.setName("Test Tenant");
        testTenant.setTenantKey("test-tenant-key");
        testTenant.setStatus(TenantStatus.ACTIVE);
        testTenant.setVisibility(TenantVisibility.PUBLIC);

        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("test@example.com", "password")
        );
    }

    @Test
    void createTenant_Success() {
        // Arrange
        CreateTenantRequest request = new CreateTenantRequest();
        request.setName("New Tenant");

        when(permissionValidator.getCurrentUserEmail()).thenReturn("test@example.com");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);
        doNothing().when(tenantPackageService).assignDefaultPackageToTenant(any(Tenant.class));

        // Act
        TenantResponse response = tenantService.createTenant(request);

        // Assert
        assertNotNull(response);
        assertEquals("Test Tenant", response.getName());
        verify(tenantRepository, times(1)).save(any(Tenant.class));
        verify(tenantMemberRepository, times(1)).save(any(TenantMember.class));
        verify(auditLogService, times(1)).logAction(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void getTenant_Success() {
        // Arrange
        when(tenantRepository.findById(anyLong())).thenReturn(Optional.of(testTenant));

        // Act
        Tenant tenant = tenantService.getTenant(1L);

        // Assert
        assertNotNull(tenant);
        assertEquals("Test Tenant", tenant.getName());
    }

    @Test
    void getTenant_NotFound() {
        // Arrange
        when(tenantRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TenantNotFoundException.class, () -> tenantService.getTenant(1L));
    }

    @Test
    void getTenantIdByKey_Success() {
        // Arrange
        when(tenantRepository.findByTenantKey(anyString())).thenReturn(Optional.of(testTenant));

        // Act
        Long tenantId = tenantService.getTenantIdByKey("test-tenant-key");

        // Assert
        assertEquals(1L, tenantId);
    }

    @Test
    void getTenantIdByKey_NotFound() {
        // Arrange
        when(tenantRepository.findByTenantKey(anyString())).thenReturn(Optional.empty());

        // Act
        Long tenantId = tenantService.getTenantIdByKey("nonexistent-key");

        // Assert
        assertNull(tenantId);
    }

    @Test
    void suspendTenant_Success() {
        // Arrange
        when(tenantRepository.findById(anyLong())).thenReturn(Optional.of(testTenant));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("admin@example.com");
        when(permissionValidator.isAdmin(anyString())).thenReturn(true);
        doNothing().when(tenantValidationService).validateStatusTransition(any(), any());
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);

        // Act
        tenantService.suspendTenant(1L);

        // Assert
        assertEquals(TenantStatus.SUSPENDED, testTenant.getStatus());
        verify(auditLogService, times(1)).logAction(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void suspendTenant_NotAdmin() {
        // Arrange
        when(tenantRepository.findById(anyLong())).thenReturn(Optional.of(testTenant));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("user@example.com");
        when(permissionValidator.isAdmin(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(InsufficientPermissionException.class, () -> tenantService.suspendTenant(1L));
    }

    @Test
    void activateTenant_Success() {
        // Arrange
        testTenant.setStatus(TenantStatus.SUSPENDED);
        when(tenantRepository.findById(anyLong())).thenReturn(Optional.of(testTenant));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("admin@example.com");
        when(permissionValidator.isAdmin(anyString())).thenReturn(true);
        doNothing().when(tenantValidationService).validateStatusTransition(any(), any());
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);

        // Act
        tenantService.activateTenant(1L);

        // Assert
        assertEquals(TenantStatus.ACTIVE, testTenant.getStatus());
        verify(auditLogService, times(1)).logAction(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void deactivateTenant_Success() {
        // Arrange
        when(tenantRepository.findById(anyLong())).thenReturn(Optional.of(testTenant));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("test@example.com");
        when(permissionValidator.isOwner(anyLong(), anyString())).thenReturn(true);
        doNothing().when(tenantValidationService).validateStatusTransition(any(), any());
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);

        // Act
        tenantService.deactivateTenant(1L);

        // Assert
        assertEquals(TenantStatus.INACTIVE, testTenant.getStatus());
        verify(auditLogService, times(1)).logAction(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void deactivateTenant_NotOwner() {
        // Arrange
        when(tenantRepository.findById(anyLong())).thenReturn(Optional.of(testTenant));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("user@example.com");
        when(permissionValidator.isOwner(anyLong(), anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(InsufficientPermissionException.class, () -> tenantService.deactivateTenant(1L));
    }

    @Test
    void deleteTenant_Success() {
        // Arrange
        when(tenantRepository.findById(anyLong())).thenReturn(Optional.of(testTenant));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("test@example.com");
        when(permissionValidator.isAdminOrOwner(anyLong(), anyString())).thenReturn(true);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);
        doNothing().when(tenantCleanupService).cleanupTenantData(anyLong());

        // Act
        tenantService.deleteTenant(1L);

        // Assert
        assertEquals(TenantStatus.DELETED, testTenant.getStatus());
        verify(auditLogService, times(1)).logAction(anyLong(), anyString(), anyString(), anyString());
        verify(tenantCleanupService, times(1)).cleanupTenantData(anyLong());
    }

    @Test
    void deleteTenant_NotAuthorized() {
        // Arrange
        when(tenantRepository.findById(anyLong())).thenReturn(Optional.of(testTenant));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("user@example.com");
        when(permissionValidator.isAdminOrOwner(anyLong(), anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(InsufficientPermissionException.class, () -> tenantService.deleteTenant(1L));
    }

    @Test
    void updateBasicInfo_Success() {
        // Arrange
        TenantBasicInfoRequest request = new TenantBasicInfoRequest();
        request.setName("Updated Name");

        when(tenantRepository.findByTenantKey(anyString())).thenReturn(Optional.of(testTenant));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("test@example.com");
        when(permissionValidator.isAdminOrOwner(anyLong(), anyString())).thenReturn(true);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);

        // Act
        TenantResponse response = tenantService.updateBasicInfo("test-tenant-key", request);

        // Assert
        assertNotNull(response);
        assertEquals("Updated Name", testTenant.getName());
        verify(auditLogService, times(1)).logAction(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void updateBasicInfo_EmptyTenantKey() {
        // Arrange
        TenantBasicInfoRequest request = new TenantBasicInfoRequest();

        // Act & Assert
        assertThrows(InvalidTenantKeyException.class, () -> 
            tenantService.updateBasicInfo("", request));
    }

    @Test
    void updateContactInfo_Success() {
        // Arrange
        TenantContactInfoRequest request = new TenantContactInfoRequest();
        request.setEmail("contact@example.com");
        request.setPhone("1234567890");
        request.setWebsite("https://example.com");

        TenantProfile profile = new TenantProfile();
        profile.setId(1L);

        when(tenantRepository.findByTenantKey(anyString())).thenReturn(Optional.of(testTenant));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("test@example.com");
        when(permissionValidator.isAdminOrOwner(anyLong(), anyString())).thenReturn(true);
        when(tenantProfileRepository.findByTenant_Id(anyLong())).thenReturn(Optional.of(profile));
        when(tenantProfileRepository.save(any(TenantProfile.class))).thenReturn(profile);

        // Act
        TenantResponse response = tenantService.updateContactInfo("test-tenant-key", request);

        // Assert
        assertNotNull(response);
        assertEquals("contact@example.com", profile.getContactEmail());
        assertEquals("1234567890", profile.getContactPhone());
        assertEquals("https://example.com", profile.getWebsite());
        verify(auditLogService, times(1)).logAction(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void updateContactInfo_CreateProfileIfNotExists() {
        // Arrange
        TenantContactInfoRequest request = new TenantContactInfoRequest();
        request.setEmail("contact@example.com");

        when(tenantRepository.findByTenantKey(anyString())).thenReturn(Optional.of(testTenant));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("test@example.com");
        when(permissionValidator.isAdminOrOwner(anyLong(), anyString())).thenReturn(true);
        when(tenantProfileRepository.findByTenant_Id(anyLong())).thenReturn(Optional.empty());
        when(tenantProfileRepository.save(any(TenantProfile.class))).thenAnswer(invocation -> {
            TenantProfile p = invocation.getArgument(0);
            p.setId(1L);
            return p;
        });

        // Act
        TenantResponse response = tenantService.updateContactInfo("test-tenant-key", request);

        // Assert
        assertNotNull(response);
        verify(tenantProfileRepository, atLeastOnce()).save(any(TenantProfile.class));
    }

    @Test
    void switchTenant_Success() {
        // Arrange
        when(permissionValidator.getCurrentUserEmail()).thenReturn("test@example.com");
        when(authRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(tenantMemberRepository.findByTenantIdAndUserIdAndStatus(anyLong(), anyLong(), any()))
            .thenReturn(Optional.of(createMockTenantMember()));

        // Act
        TenantResponse response = tenantService.switchTenant(1L);

        // Assert
        assertNotNull(response);
    }

    @Test
    void switchTenantByKey_Success() {
        // Arrange
        when(tenantRepository.findByTenantKey(anyString())).thenReturn(Optional.of(testTenant));
        when(permissionValidator.getCurrentUserEmail()).thenReturn("test@example.com");
        when(authRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(tenantMemberRepository.findByTenantIdAndUserIdAndStatus(anyLong(), anyLong(), any()))
            .thenReturn(Optional.of(createMockTenantMember()));

        // Act
        TenantResponse response = tenantService.switchTenantByKey("test-tenant-key");

        // Assert
        assertNotNull(response);
    }

    private TenantMember createMockTenantMember() {
        TenantMember member = new TenantMember();
        member.setTenant(testTenant);
        member.setUserId(1L);
        member.setRole(TenantRole.OWNER);
        member.setStatus(MembershipStatus.ACTIVE);
        return member;
    }
}
