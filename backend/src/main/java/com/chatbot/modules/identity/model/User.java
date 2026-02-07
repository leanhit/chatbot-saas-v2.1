package com.chatbot.modules.identity.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Global user identity entity - contains ONLY identity fields
 * TODO: Add user profile management in separate module
 */
@Entity
@Table(name = "identity_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
    
    @Column(name = "locale", nullable = false, length = 10, columnDefinition = "varchar(10) default 'vi'")
    @Builder.Default
    private String locale = "vi";
    
    @Column(name = "failed_attempts", nullable = false)
    @Builder.Default
    private Integer failedAttempts = 0;
    
    @Column(name = "token_type", nullable = false)
    @Builder.Default
    private String tokenType = "Bearer";
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Credential credential;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // Business logic helpers
    public boolean isActive() {
        return UserStatus.ACTIVE.equals(status);
    }
    
    // Thêm getter methods cho compatibility
    public UUID getId() {
        return this.id;
    }
    
    public String getEmail() {
        return this.email;
    }
    
    // NO business fields
    // NO profile fields
    // NO role fields
    // NO permission fields
}
