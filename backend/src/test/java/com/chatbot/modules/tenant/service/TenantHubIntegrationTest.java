package com.chatbot.modules.tenant.service;

import com.chatbot.modules.tenant.core.dto.CreateTenantRequest;
import com.chatbot.modules.tenant.core.dto.TenantResponse;
import com.chatbot.modules.tenant.core.model.Tenant;
import com.chatbot.modules.tenant.core.model.TenantStatus;
import com.chatbot.modules.tenant.core.repository.TenantRepository;
import com.chatbot.modules.tenant.membership.dto.MemberResponse;
import com.chatbot.modules.tenant.membership.model.TenantMember;
import com.chatbot.modules.tenant.membership.model.TenantRole;
import com.chatbot.modules.tenant.membership.repository.TenantMemberRepository;
import com.chatbot.modules.tenant.core.util.SecurityContextUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tenant Hub v0.1 Integration Tests
 * Business rule validation for core functionality
 */
@ExtendWith(MockitoExtension.class)
class TenantHubIntegrationTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private TenantMemberRepository tenantMemberRepository;

    @Mock
    private SecurityContextUtil securityContextUtil;

    @InjectMocks
    private com.chatbot.modules.tenant.core.service.TenantService tenantService;

    @InjectMocks
    private com.chatbot.modules.tenant.membership.service.TenantMemberService tenantMemberService;

    private UUID tenantId;
    private UUID ownerUserId;
    private UUID adminUserId;
    private UUID memberUserId;
    private String ownerEmail;
    private String adminEmail;
    private String memberEmail;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        ownerUserId = UUID.randomUUID();
        adminUserId = UUID.randomUUID();
        memberUserId = UUID.randomUUID();
        ownerEmail = "owner@example.com";
        adminEmail = "admin@example.com";
        memberEmail = "member@example.com";

        // Mock current user (owner) for tenant service
        when(securityContextUtil.getCurrentUserIdUUID()).thenReturn(ownerUserId);
        when(securityContextUtil.getCurrentUserEmail()).thenReturn(ownerEmail);
    }

    @Test
    void testCreateTenant_AssignsOwnerRole() {
        // Given
        CreateTenantRequest request = new CreateTenantRequest();
        request.setName("Test Tenant");
        
        Tenant savedTenant = Tenant.builder()
                .id(tenantId)
                .name("Test Tenant")
                .status(TenantStatus.ACTIVE)
                .build();
        
        when(tenantRepository.save(any(Tenant.class))).thenReturn(savedTenant);
        when(tenantMemberRepository.save(any(TenantMember.class))).thenAnswer(invocation -> {
            TenantMember member = invocation.getArgument(0);
            ReflectionTestUtils.setField(member, "id", UUID.randomUUID());
            return member;
        });

        // When
        TenantResponse result = tenantService.createTenant(request);

        // Then
        assertThat(result.getName()).isEqualTo("Test Tenant");
        assertThat(result.getStatus()).isEqualTo(TenantStatus.ACTIVE);
        
        // Verify owner membership created
        verify(tenantMemberRepository).save(argThat(member -> 
            member.getTenant().getId().equals(tenantId) &&
            member.getUserId().equals(ownerUserId) &&
            member.getEmail().equals(ownerEmail) &&
            member.getRole() == TenantRole.OWNER
        ));
    }

    @Test
    void testOwnerCanChangeAdminAndMemberRole() {
        // Given
        TenantMember adminMember = createMember(tenantId, adminUserId, adminEmail, TenantRole.ADMIN);
        TenantMember memberMember = createMember(tenantId, memberUserId, memberEmail, TenantRole.MEMBER);
        
        when(tenantMemberRepository.findByTenantIdAndUserId(tenantId, adminUserId))
                .thenReturn(Optional.of(adminMember));
        when(tenantMemberRepository.findByTenantIdAndUserId(tenantId, memberUserId))
                .thenReturn(Optional.of(memberMember));
        when(tenantMemberRepository.save(any(TenantMember.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When - Owner changes ADMIN to MEMBER
        tenantMemberService.updateMemberRole(tenantId, adminUserId, TenantRole.MEMBER);
        
        // Then
        verify(tenantMemberRepository).save(argThat(member -> 
            member.getRole() == TenantRole.MEMBER
        ));

        // When - Owner changes MEMBER to ADMIN
        tenantMemberService.updateMemberRole(tenantId, memberUserId, TenantRole.ADMIN);
        
        // Then
        verify(tenantMemberRepository).save(argThat(member -> 
            member.getRole() == TenantRole.ADMIN
        ));
    }

    @Test
    void testOwnerCannotBeRemoved() {
        // Given
        TenantMember ownerMember = createMember(tenantId, ownerUserId, ownerEmail, TenantRole.OWNER);
        
        when(tenantMemberRepository.findByTenantIdAndUserId(tenantId, ownerUserId))
                .thenReturn(Optional.of(ownerMember));

        // When & Then
        assertThatThrownBy(() -> tenantMemberService.removeMember(tenantId, ownerUserId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot remove OWNER member");
    }

    @Test
    void testAdminCanRemoveMember() {
        // Given
        TenantMember adminMember = createMember(tenantId, adminUserId, adminEmail, TenantRole.ADMIN);
        TenantMember memberMember = createMember(tenantId, memberUserId, memberEmail, TenantRole.MEMBER);
        
        when(tenantMemberRepository.findByTenantIdAndUserId(tenantId, memberUserId))
                .thenReturn(Optional.of(memberMember));

        // When
        tenantMemberService.removeMember(tenantId, memberUserId);

        // Then
        verify(tenantMemberRepository).delete(memberMember);
    }

    @Test
    void testMemberCannotRemoveAnyone() {
        // Given
        TenantMember memberMember = createMember(tenantId, memberUserId, memberEmail, TenantRole.MEMBER);
        TenantMember adminMember = createMember(tenantId, adminUserId, adminEmail, TenantRole.ADMIN);
        
        when(tenantMemberRepository.findByTenantIdAndUserId(tenantId, memberUserId))
                .thenReturn(Optional.of(memberMember));
        when(tenantMemberRepository.findByTenantIdAndUserId(tenantId, adminUserId))
                .thenReturn(Optional.of(adminMember));

        // When & Then - Member tries to remove admin
        assertThatThrownBy(() -> tenantMemberService.removeMember(tenantId, adminUserId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Member not found");
    }

    @Test
    void testSwitchingTenantValidatesMembership() {
        // Given
        Tenant tenant = Tenant.builder()
                .id(tenantId)
                .name("Test Tenant")
                .status(TenantStatus.ACTIVE)
                .build();
        
        TenantMember member = createMember(tenantId, ownerUserId, ownerEmail, TenantRole.OWNER);
        
        when(tenantMemberRepository.findByTenantIdAndUserId(tenantId, ownerUserId))
                .thenReturn(Optional.of(member));

        // When
        TenantResponse result = tenantService.switchTenant(tenantId);

        // Then
        assertThat(result.getId()).isEqualTo(tenantId);
        assertThat(result.getName()).isEqualTo("Test Tenant");
        assertThat(result.getStatus()).isEqualTo(TenantStatus.ACTIVE);
    }

    @Test
    void testSwitchingTenantFailsForNonMember() {
        // Given
        UUID nonMemberUserId = UUID.randomUUID();
        
        when(tenantMemberRepository.findByTenantIdAndUserId(tenantId, nonMemberUserId))
                .thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> tenantService.switchTenant(tenantId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User is not a member of this tenant");
    }

    @Test
    void testSwitchingTenantFailsForInactiveTenant() {
        // Given
        Tenant inactiveTenant = Tenant.builder()
                .id(tenantId)
                .name("Inactive Tenant")
                .status(TenantStatus.SUSPENDED)
                .build();
        
        TenantMember member = createMember(tenantId, ownerUserId, ownerEmail, TenantRole.OWNER);
        
        when(tenantMemberRepository.findByTenantIdAndUserId(tenantId, ownerUserId))
                .thenReturn(Optional.of(member));

        // When & Then
        assertThatThrownBy(() -> tenantService.switchTenant(tenantId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Tenant is not active");
    }

    @Test
    void testGetUserTenants() {
        // Given
        Tenant tenant1 = createTenant(UUID.randomUUID(), "Tenant 1");
        Tenant tenant2 = createTenant(UUID.randomUUID(), "Tenant 2");
        
        when(tenantRepository.findByUserId(ownerUserId))
                .thenReturn(List.of(tenant1, tenant2));

        // When
        List<TenantResponse> result = tenantService.getUserTenants();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Tenant 1");
        assertThat(result.get(1).getName()).isEqualTo("Tenant 2");
    }

    @Test
    void testGetTenantMembers() {
        // Given
        TenantMember owner = createMember(tenantId, ownerUserId, ownerEmail, TenantRole.OWNER);
        TenantMember admin = createMember(tenantId, adminUserId, adminEmail, TenantRole.ADMIN);
        TenantMember member = createMember(tenantId, memberUserId, memberEmail, TenantRole.MEMBER);
        
        when(tenantMemberRepository.findByTenantId(tenantId))
                .thenReturn(List.of(owner, admin, member));

        // When
        List<MemberResponse> result = tenantMemberService.getTenantMembers(tenantId);

        // Then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getRole()).isEqualTo(TenantRole.OWNER);
        assertThat(result.get(1).getRole()).isEqualTo(TenantRole.ADMIN);
        assertThat(result.get(2).getRole()).isEqualTo(TenantRole.MEMBER);
        assertThat(result.get(0).getEmail()).isEqualTo(ownerEmail);
        assertThat(result.get(1).getEmail()).isEqualTo(adminEmail);
        assertThat(result.get(2).getEmail()).isEqualTo(memberEmail);
    }

    @Test
    void testOwnerRoleCannotBeChanged() {
        // Given
        TenantMember ownerMember = createMember(tenantId, ownerUserId, ownerEmail, TenantRole.OWNER);
        
        when(tenantMemberRepository.findByTenantIdAndUserId(tenantId, ownerUserId))
                .thenReturn(Optional.of(ownerMember));

        // When & Then
        assertThatThrownBy(() -> tenantMemberService.updateMemberRole(tenantId, ownerUserId, TenantRole.ADMIN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot change OWNER role");
    }

    @Test
    void testCannotAssignOwnerRole() {
        // Given
        TenantMember member = createMember(tenantId, memberUserId, memberEmail, TenantRole.MEMBER);
        
        when(tenantMemberRepository.findByTenantIdAndUserId(tenantId, memberUserId))
                .thenReturn(Optional.of(member));

        // When & Then
        assertThatThrownBy(() -> tenantMemberService.updateMemberRole(tenantId, memberUserId, TenantRole.OWNER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot assign OWNER role through this method");
    }

    // Helper methods
    private Tenant createTenant(UUID id, String name) {
        return Tenant.builder()
                .id(id)
                .name(name)
                .status(TenantStatus.ACTIVE)
                .build();
    }

    private TenantMember createMember(UUID tenantId, UUID userId, String email, TenantRole role) {
        Tenant tenant = createTenant(tenantId, "Test Tenant");
        return TenantMember.builder()
                .id(UUID.randomUUID())
                .tenant(tenant)
                .userId(userId)
                .email(email)
                .role(role)
                .joinedAt(java.time.LocalDateTime.now())
                .build();
    }
}
