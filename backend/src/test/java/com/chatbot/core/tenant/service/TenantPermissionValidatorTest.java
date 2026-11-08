package com.chatbot.core.tenant.service;

import com.chatbot.core.identity.model.SystemRole;
import com.chatbot.core.membership.model.MembershipStatus;
import com.chatbot.core.membership.model.TenantMember;
import com.chatbot.core.membership.model.TenantRole;
import com.chatbot.core.membership.repository.TenantMemberRepository;
import com.chatbot.core.tenant.exception.InsufficientPermissionException;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.AuthRepository;
import com.chatbot.core.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantPermissionValidatorTest {

    @Mock private UserRepository userRepository;
    @Mock private AuthRepository authRepository;
    @Mock private TenantMemberRepository tenantMemberRepository;

    @InjectMocks
    private TenantPermissionValidator validator;

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAs(String email) {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(email, null, java.util.List.of()));
    }

    private User user(Long id, SystemRole role) {
        User u = new User();
        u.setId(id);
        u.setEmail("u@test.com");
        u.setSystemRole(role);
        return u;
    }

    private TenantMember member(TenantRole role) {
        TenantMember m = new TenantMember();
        m.setRole(role);
        return m;
    }

    // ---------- getCurrentUserEmail ----------

    @Test
    void getCurrentUserEmailThrowsWhenUnauthenticated() {
        SecurityContextHolder.clearContext();
        assertThrows(InsufficientPermissionException.class, validator::getCurrentUserEmail);
    }

    @Test
    void getCurrentUserEmailReturnsAuthenticatedName() {
        authenticateAs("agent@test.com");
        assertEquals("agent@test.com", validator.getCurrentUserEmail());
    }

    // ---------- getCurrentUser ----------

    @Test
    void getCurrentUserReturnsUserFromAuthRepository() {
        authenticateAs("agent@test.com");
        User u = user(1L, SystemRole.USER);
        when(authRepository.findByEmail("agent@test.com")).thenReturn(Optional.of(u));
        assertEquals(1L, validator.getCurrentUser().getId());
    }

    @Test
    void getCurrentUserThrowsWhenUserMissing() {
        authenticateAs("ghost@test.com");
        when(authRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());
        assertThrows(com.chatbot.shared.exceptions.ResourceNotFoundException.class,
            validator::getCurrentUser);
    }

    // ---------- isAdmin ----------

    @Test
    void isAdminTrueForAdminRole() {
        when(userRepository.findByEmail("admin@test.com"))
            .thenReturn(Optional.of(user(1L, SystemRole.ADMIN)));
        assertTrue(validator.isAdmin("admin@test.com"));
    }

    @Test
    void isAdminFalseForRegularUser() {
        when(userRepository.findByEmail("u@test.com"))
            .thenReturn(Optional.of(user(1L, SystemRole.USER)));
        assertFalse(validator.isAdmin("u@test.com"));
    }

    @Test
    void isAdminFalseWhenUserNotFound() {
        when(userRepository.findByEmail("none@test.com")).thenReturn(Optional.empty());
        assertFalse(validator.isAdmin("none@test.com"));
    }

    // ---------- isOwner / isActiveMember / isOwnerOrEditor ----------

    @Test
    void isOwnerTrueForOwnerMember() {
        when(authRepository.findByEmail("o@test.com")).thenReturn(Optional.of(user(1L, null)));
        when(tenantMemberRepository.findByTenantIdAndUserIdAndStatus(1L, 1L, MembershipStatus.ACTIVE))
            .thenReturn(Optional.of(member(TenantRole.OWNER)));
        assertTrue(validator.isOwner(1L, "o@test.com"));
    }

    @Test
    void isOwnerFalseForEditor() {
        when(authRepository.findByEmail("e@test.com")).thenReturn(Optional.of(user(2L, null)));
        when(tenantMemberRepository.findByTenantIdAndUserIdAndStatus(1L, 2L, MembershipStatus.ACTIVE))
            .thenReturn(Optional.of(member(TenantRole.EDITOR)));
        assertFalse(validator.isOwner(1L, "e@test.com"));
    }

    @Test
    void isOwnerFalseWhenUserUnknown() {
        when(authRepository.findByEmail("x@test.com")).thenReturn(Optional.empty());
        assertFalse(validator.isOwner(1L, "x@test.com"));
        verifyNoInteractions(tenantMemberRepository);
    }

    @Test
    void isActiveMemberTrueWhenMembershipExists() {
        when(authRepository.findByEmail("m@test.com")).thenReturn(Optional.of(user(3L, null)));
        when(tenantMemberRepository.findByTenantIdAndUserIdAndStatus(1L, 3L, MembershipStatus.ACTIVE))
            .thenReturn(Optional.of(member(TenantRole.EDITOR)));
        assertTrue(validator.isActiveMember(1L, "m@test.com"));
    }

    @Test
    void isOwnerOrEditorTrueForBothRoles() {
        when(authRepository.findByEmail("o@test.com")).thenReturn(Optional.of(user(1L, null)));
        when(tenantMemberRepository.findByTenantIdAndUserIdAndStatus(1L, 1L, MembershipStatus.ACTIVE))
            .thenReturn(Optional.of(member(TenantRole.OWNER)));
        assertTrue(validator.isOwnerOrEditor(1L, "o@test.com"));

        when(authRepository.findByEmail("e@test.com")).thenReturn(Optional.of(user(2L, null)));
        when(tenantMemberRepository.findByTenantIdAndUserIdAndStatus(1L, 2L, MembershipStatus.ACTIVE))
            .thenReturn(Optional.of(member(TenantRole.EDITOR)));
        assertTrue(validator.isOwnerOrEditor(1L, "e@test.com"));
    }

    // ---------- isAdminOrOwner ----------

    @Test
    void isAdminOrOwnerShortCircuitsOnAdmin() {
        when(userRepository.findByEmail("admin@test.com"))
            .thenReturn(Optional.of(user(1L, SystemRole.ADMIN)));
        assertTrue(validator.isAdminOrOwner(1L, "admin@test.com"));
        verifyNoInteractions(authRepository);
    }

    // ---------- canAssignRole ----------

    @Test
    void canAssignRoleAdminCanAssignAnything() {
        when(userRepository.findByEmail("admin@test.com"))
            .thenReturn(Optional.of(user(1L, SystemRole.ADMIN)));
        assertTrue(validator.canAssignRole(1L, "admin@test.com", TenantRole.OWNER));
    }

    @Test
    void canAssignRoleOwnerCannotAssignOwner() {
        when(authRepository.findByEmail("o@test.com")).thenReturn(Optional.of(user(1L, null)));
        when(tenantMemberRepository.findByTenantIdAndUserIdAndStatus(1L, 1L, MembershipStatus.ACTIVE))
            .thenReturn(Optional.of(member(TenantRole.OWNER)));
        assertFalse(validator.canAssignRole(1L, "o@test.com", TenantRole.OWNER));
        assertTrue(validator.canAssignRole(1L, "o@test.com", TenantRole.EDITOR));
    }

    @Test
    void canAssignRoleEditorCannotAssign() {
        when(authRepository.findByEmail("e@test.com")).thenReturn(Optional.of(user(2L, null)));
        when(tenantMemberRepository.findByTenantIdAndUserIdAndStatus(1L, 2L, MembershipStatus.ACTIVE))
            .thenReturn(Optional.of(member(TenantRole.EDITOR)));
        assertFalse(validator.canAssignRole(1L, "e@test.com", TenantRole.EDITOR));
    }

    @Test
    void canAssignRoleFalseForUnknownUser() {
        when(userRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());
        when(authRepository.findByEmail("ghost@test.com")).thenReturn(Optional.empty());
        assertFalse(validator.canAssignRole(1L, "ghost@test.com", TenantRole.EDITOR));
    }
}
