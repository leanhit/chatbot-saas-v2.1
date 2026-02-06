package com.chatbot.modules.auth.security;

import com.chatbot.modules.auth.model.Auth;
import com.chatbot.modules.identity.model.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
public class CustomUserDetails implements UserDetails {
    private static final long serialVersionUID = 1L;
    
    private final Auth auth;
    private final User user;
    private final String username;
    private final String password;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(Auth auth) {
        this.auth = auth;
        this.user = null;
        this.username = auth.getEmail();
        this.password = auth.getPassword();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + auth.getSystemRole().name()));
    }

    public CustomUserDetails(User user) {
        this.auth = null;
        this.user = user;
        this.username = user.getEmail();
        this.password = ""; // No password needed for JWT validation
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getStatus().name()));
    }

    // Thêm getter cho auth object
    public Auth getAuth() {
        return auth;
    }

    // Thêm getter cho user object
    public User getUser() {
        return user;
    }

    // Thêm getter cho userId
    public Long getUserId() {
        if (auth != null) {
            return auth.getId();
        } else if (user != null) {
            return user.getId();
        } else {
            throw new IllegalStateException("Neither auth nor user is set");
        }
    }
    
    // Thêm getter cho userId UUID
    public UUID getUserIdUUID() {
        if (auth != null) {
            return UUID.nameUUIDFromBytes(("auth_" + auth.getId()).getBytes());
        } else if (user != null) {
            return UUID.nameUUIDFromBytes(("user_" + user.getId()).getBytes());
        } else {
            throw new IllegalStateException("Neither auth nor user is set");
        }
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        if (auth != null) {
            return Boolean.TRUE.equals(auth.getIsActive());
        } else if (user != null) {
            return user.getStatus() == com.chatbot.modules.identity.model.UserStatus.ACTIVE;
        } else {
            return false;
        }
    }
}
