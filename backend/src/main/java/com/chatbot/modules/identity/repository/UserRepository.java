package com.chatbot.modules.identity.repository;

import com.chatbot.modules.identity.model.User;
import com.chatbot.modules.identity.model.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User repository for identity operations
 * TODO: Add user search functionality
 * TODO: Add bulk operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    Optional<User> findByEmailAndStatus(String email, UserStatus status);
}
