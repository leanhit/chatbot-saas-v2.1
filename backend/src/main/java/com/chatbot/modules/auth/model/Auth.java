package com.chatbot.modules.auth.model;

import com.chatbot.modules.userInfo.model.UserInfo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "auth_users")
public class Auth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private SystemRole systemRole;

    @Builder.Default
    @Column(name = "is_active", nullable = false, columnDefinition = "boolean default true")
    private Boolean isActive = true;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private AuthStatus status = AuthStatus.ACTIVE;

    @OneToOne(mappedBy = "auth", cascade = CascadeType.ALL)
    private UserInfo userInfo;
}

