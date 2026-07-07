package com.chatbot.core.tenant.service;

import com.chatbot.core.tenant.dto.CreateTenantRequest;
import com.chatbot.core.tenant.dto.TenantResponse;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.profile.repository.TenantProfileRepository;
import com.chatbot.core.tenant.profile.service.TenantProfileService;
import com.chatbot.core.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.core.tenant.membership.service.TenantMembershipFacade;
import com.chatbot.core.user.service.UserService;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TenantServiceIntegrationTest {

    @Mock private TenantRepository tenantRepository;
    @Mock private AuthRepository authRepository;
    @Mock private UserRepository userRepository;
    @Mock private TenantMemberRepository tenantMemberRepository;
    @Mock private TenantMembershipFacade tenantMembershipFacade;
    @Mock private TenantPackageService tenantPackageService;
    @Mock private TenantProfileRepository tenantProfileRepository;
    @Mock private TenantProfileService tenantProfileService;
    @Mock private AddressService addressService;
    @Mock private TenantAuditLogService auditLogService;
    @Mock private TenantPermissionValidator permissionValidator;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TenantService tenantService;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("test@example.com", "password")
        );
        lenient().when(permissionValidator.getCurrentUser()).thenReturn(user);
        lenient().when(permissionValidator.getCurrentUserEmail()).thenReturn("test@example.com");
        lenient().when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
    }

    @Test
    void testCreateTenant() {
        CreateTenantRequest request = new CreateTenantRequest();
        request.setName("Integration Test Tenant");
        
        Tenant mockTenant = new Tenant();
        mockTenant.setId(10L);
        mockTenant.setName("Integration Test Tenant");
        mockTenant.setTenantKey("uuid-1234");
        
        when(tenantRepository.save(any(Tenant.class))).thenReturn(mockTenant);
        
        TenantResponse saved = tenantService.createTenant(request);
        
        assertNotNull(saved.getId());
        assertEquals("Integration Test Tenant", saved.getName());
        assertEquals("uuid-1234", saved.getTenantKey());
        
        verify(tenantRepository, times(1)).save(any(Tenant.class));
    }
}
