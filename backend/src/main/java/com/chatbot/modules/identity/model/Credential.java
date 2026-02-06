package com.chatbot.modules.identity.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Credential entity - contains ONLY password-related data
 * TODO: Add MFA support in future
 * TODO: Add account lockout logic
 */
@Entity
@Table(name = "user_credentials")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Credential {

    @Id
    private Long userId; // Same as User.id
    
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false)
    private String password;
    
    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;
    
    @Column(name = "failed_attempts")
    @Builder.Default
    private Integer failedAttempts = 0;
    
    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;
    
    @Column(name = "source")
    @Builder.Default
    private String source = "IDENTITY"; // LEGACY | IDENTITY | SSO | OAUTH
    
    @Column(name = "provider")
    @Builder.Default
    private String provider = "LOCAL"; // LOCAL | GOOGLE | FACEBOOK
    
    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
    
    @Column(name = "metadata")
    @Builder.Default
    private String metadata = "{}"; // JSON string for additional data
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (passwordChangedAt == null) {
            passwordChangedAt = now;
        }
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        // Update password_changed_at only when password changes
        // TODO: Implement proper password change tracking
    }
    
    // NO other credential types
    // NO social auth data
    // NO MFA data
}
