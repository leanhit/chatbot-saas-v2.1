package com.chatbot.core.user.repository;

import com.chatbot.core.user.profile.UserProfile;
import com.chatbot.core.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * User Profile Repository - Data access for user profiles
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    Optional<UserProfile> findByUserId(Long userId);
    Optional<UserProfile> findByUser(User user);

    @Modifying
    @Query(value = "INSERT INTO user_profiles (user_id, created_at, updated_at) VALUES (:userId, NOW(), NOW())", nativeQuery = true)
    void insertProfile(Long userId);
}
