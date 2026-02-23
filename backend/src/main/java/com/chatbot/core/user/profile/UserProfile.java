package com.chatbot.core.user.profile;

import com.chatbot.core.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * User Profile Entity - Chứa thông tin chi tiết của system user
 * Sử dụng @MapsId để chia sẻ ID với User
 */
@Entity
@Table(name = "user_profiles", 
    indexes = {
        @Index(name = "idx_user_profile_user", columnList = "user_id")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {

    /**
     * Dùng chung ID với User (giống UserInfo ↔ Auth cũ)
     */
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ===== Basic Information =====
    @Column(length = 100)
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(length = 500)
    private String avatar;

    @Column(length = 10)
    private String gender;

    @Column(name = "bio", length = 500)
    private String bio;
    
    // Email field from UserInfo
    @Column(length = 255)
    private String email;

    // ===== Professional Information =====
    @Column(name = "job_title", length = 100)
    private String jobTitle;

    @Column(name = "department", length = 100)
    private String department;

    @Column(length = 100)
    private String company;

    @Column(name = "linkedin_url", length = 500)
    private String linkedinUrl;

    @Column(length = 500)
    private String website;

    @Column(length = 200)
    private String location;

    @Column(length = 1000)
    private String skills;

    @Column(name = "experience", length = 1000)
    private String experience;

    @Column(length = 500)
    private String education;

    @Column(name = "certifications", length = 500)
    private String certifications;

    @Column(name = "languages")
    private String languages;

    @Column(name = "availability", length = 100)
    private String availability;

    @Column(name = "hourly_rate", length = 50)
    private String hourlyRate;

    @Column(name = "portfolio_url", length = 500)
    private String portfolioUrl;

    // ===== audit =====
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
