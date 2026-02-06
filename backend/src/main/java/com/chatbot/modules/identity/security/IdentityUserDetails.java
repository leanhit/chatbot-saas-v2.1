package com.chatbot.modules.identity.security;

import com.chatbot.modules.identity.model.User;
import com.chatbot.modules.identity.model.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security UserDetails wrapper for Identity User
 * TODO: Add authority mapping based on user status
 * TODO: Add custom authorities if needed
 */
@Data
@AllArgsConstructor
public class IdentityUserDetails implements UserDetails {
    
    private User user;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Basic authority - all authenticated users have IDENTITY_USER role
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_IDENTITY_USER"));
    }
    
    @Override
    public String getPassword() {
        // Password is handled separately in Credential entity
        return null;
    }
    
    @Override
    public String getUsername() {
        return user.getEmail();
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        // TODO: Check account lockout status from Credential entity
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        return UserStatus.ACTIVE.equals(user.getStatus());
    }
}
