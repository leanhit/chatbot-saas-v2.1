package com.chatbot.modules.facebook.facebook.connection.repository;

import com.chatbot.modules.facebook.facebook.connection.model.FacebookConnection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FacebookConnectionRepository extends JpaRepository<FacebookConnection, Long> {
    
    Optional<FacebookConnection> findByTenantIdAndPageId(UUID tenantId, String pageId);
    
    Optional<FacebookConnection> findByPageId(String pageId);
}
