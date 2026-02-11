package com.chatbot.modules.userInfo.model;

import com.chatbot.core.identity.model.Auth;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_info")
@Getter @Setter
@NoArgsConstructor 
@AllArgsConstructor
@Builder
public class UserInfo {
    @Id
    private Long id; // Sẽ dùng chung ID với Auth

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private Auth auth;

    private String fullName;
    private String phoneNumber;
    private String avatar;
    private String gender;
    
    @Column(name = "bio", length = 500)
    private String bio;
    
    // Professional fields
    private String jobTitle;
    private String department;
    private String company;
    private String linkedinUrl;
    private String website;
    private String location;
    @Column(length = 1000)
    private String skills;
    @Column(length = 1000)
    private String experience;
    @Column(length = 500)
    private String education;
    @Column(length = 500)
    private String certifications;
    @Column(length = 200)
    private String languages;
    private String availability;
    private String hourlyRate;
    private String portfolioUrl;
}