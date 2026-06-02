package com.chatbot.core.identity.model;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    // Application-level join: store userId as Long instead of @ManyToOne relationship
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(nullable = false)
    private Instant expiryDate;
    
    @Column(nullable = false)
    private Instant createdDate;
    
    @PrePersist
    protected void onCreate() {
        createdDate = Instant.now();
        if (expiryDate == null) {
            // Default 30 days expiry
            expiryDate = Instant.now().plusSeconds(30L * 24 * 60 * 60);
        }
    }
    
    public boolean isExpired() {
        return Instant.now().isAfter(expiryDate);
    }
}
