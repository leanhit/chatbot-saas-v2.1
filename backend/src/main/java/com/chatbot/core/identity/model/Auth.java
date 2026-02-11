package com.chatbot.core.identity.model;

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
@Table(name = "users")
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

    @OneToOne(mappedBy = "auth", cascade = CascadeType.ALL)
    private UserInfo userInfo;
}

