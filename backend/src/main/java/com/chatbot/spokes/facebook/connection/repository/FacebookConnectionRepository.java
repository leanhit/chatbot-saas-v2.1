// src/main/java/com/chatbot/connections/repositories/FacebookConnectionRepository.java

package com.chatbot.spokes.facebook.connection.repository;

import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import org.springframework.data.domain.Page; // Thêm dòng này
import org.springframework.data.domain.Pageable; // Thêm dòng này
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List; 
import java.util.Optional;
import java.util.UUID;

public interface FacebookConnectionRepository extends JpaRepository<FacebookConnection, UUID> {
    // Tìm theo BotId và tenantId
    @Query("SELECT fc FROM FacebookConnection fc WHERE fc.botId = :botId AND fc.tenantId = :tenantId")
    Optional<FacebookConnection> findByBotIdAndTenantId(@Param("botId") String botId, @Param("tenantId") Long tenantId);
    
    // 4. Tìm theo pageId (cho webhook lookup - không cần tenantId)
    @Query("SELECT fc FROM FacebookConnection fc WHERE fc.pageId = :pageId AND fc.isEnabled = true AND fc.isActive = true")
    Optional<FacebookConnection> findByPageIdForWebhook(@Param("pageId") String pageId);
    
    // 4b. Tìm tất cả theo pageId (để debug)
    @Query("SELECT fc FROM FacebookConnection fc WHERE fc.pageId = :pageId")
    List<FacebookConnection> findAllByPageId(@Param("pageId") String pageId);
    
    @Deprecated
    Optional<FacebookConnection> findByPageId(String pageId);
    
    @Query("SELECT fc FROM FacebookConnection fc WHERE fc.tenantId = :tenantId AND fc.pageId = :pageId")
    Optional<FacebookConnection> findByTenantIdAndPageId(@Param("tenantId") Long tenantId, @Param("pageId") String pageId);
    
    // Tìm theo ownerId và tenantId
    @Query("SELECT fc FROM FacebookConnection fc WHERE fc.ownerId = :ownerId AND fc.tenantId = :tenantId")
    List<FacebookConnection> findByOwnerIdAndTenantId(@Param("ownerId") String ownerId, @Param("tenantId") Long tenantId);
    
    @Query("SELECT fc FROM FacebookConnection fc WHERE fc.ownerId = :ownerId AND fc.tenantId = :tenantId")
    Page<FacebookConnection> findByOwnerIdAndTenantId(@Param("ownerId") String ownerId, @Param("tenantId") Long tenantId, Pageable pageable);
    
    @Deprecated
    List<FacebookConnection> findByOwnerId(String ownerId);
    @Deprecated
    Page<FacebookConnection> findByOwnerId(String ownerId, Pageable pageable);

    // Tìm theo ownerId, isActive và tenantId
    @Query("SELECT fc FROM FacebookConnection fc WHERE fc.ownerId = :ownerId AND fc.isActive = true AND fc.tenantId = :tenantId")
    List<FacebookConnection> findByOwnerIdAndIsActiveTrueAndTenantId(@Param("ownerId") String ownerId, @Param("tenantId") Long tenantId);
    
    @Query("SELECT fc FROM FacebookConnection fc WHERE fc.ownerId = :ownerId AND fc.isActive = true AND fc.tenantId = :tenantId")
    Page<FacebookConnection> findByOwnerIdAndIsActiveTrueAndTenantId(@Param("ownerId") String ownerId, @Param("tenantId") Long tenantId, Pageable pageable);
    
    @Deprecated
    List<FacebookConnection> findByOwnerIdAndIsActiveTrue(@Param("ownerId") String ownerId);
    @Deprecated
    Page<FacebookConnection> findByOwnerIdAndIsActiveTrue(@Param("ownerId") String ownerId, Pageable pageable);

    @Query("SELECT fc FROM FacebookConnection fc WHERE fc.tenantId = :tenantId AND fc.pageId = :pageId AND fc.isActive = true")
    Optional<FacebookConnection> findByTenantIdAndPageIdAndIsActiveTrue(@Param("tenantId") Long tenantId, @Param("pageId") String pageId);
    
    @Deprecated
    @Query("SELECT fc FROM FacebookConnection fc WHERE fc.pageId = :pageId AND fc.isActive = true AND fc.tenantId = :#{@tenantContext.tenantId}")
    Optional<FacebookConnection> findByPageIdAndIsActiveTrue(@Param("pageId") String pageId);

}