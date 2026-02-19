package com.chatbot.core.message.store.repository;

import com.chatbot.core.message.store.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Phương thức đã có: Lấy tin nhắn theo ConversationId và tenantId, sắp xếp ngược thời gian
    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId AND m.tenantId = :tenantId ORDER BY m.createdAt DESC")
    Page<Message> findByConversationIdAndTenantIdOrderByCreatedAtDesc(
        @Param("conversationId") Long conversationId, 
        @Param("tenantId") Long tenantId, 
        Pageable pageable
    );

    // Phương thức mới 1: Đếm số lượng tin nhắn chưa đọc trong 1 Conversation
    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversationId = :conversationId AND m.tenantId = :tenantId AND m.isRead = :isRead")
    long countByConversationIdAndTenantIdAndIsRead(
        @Param("conversationId") Long conversationId, 
        @Param("tenantId") Long tenantId, 
        @Param("isRead") Boolean isRead
    );
    
    // Phương thức mới 2: Truy vấn nhiều tin nhắn bằng ID và tenantId
    @Query("SELECT m FROM Message m WHERE m.id IN :ids AND m.tenantId = :tenantId")
    List<Message> findAllByIdInAndTenantId(@Param("ids") List<Long> ids, @Param("tenantId") Long tenantId);
    
    // Phương thức xóa tin nhắn theo conversationId và tenantId
    @Modifying
    @Transactional
    @Query("DELETE FROM Message m WHERE m.conversationId = :conversationId AND m.tenantId = :tenantId")
    int deleteByConversationIdAndTenantId(
        @Param("conversationId") Long conversationId, 
        @Param("tenantId") Long tenantId
    );
    
    // Tìm tin nhắn theo ID, tenantId và kiểm tra owner thông qua conversation
    @Query("SELECT m FROM Message m WHERE m.id = :messageId AND m.tenantId = :tenantId AND EXISTS " +
           "(SELECT 1 FROM Conversation c WHERE c.id = m.conversationId AND c.tenantId = :tenantId AND c.ownerId = :ownerId)")
    Optional<Message> findByIdAndTenantIdAndOwnerId(
        @Param("messageId") Long messageId, 
        @Param("tenantId") Long tenantId, 
        @Param("ownerId") String ownerId
    );
    
    // Tìm nhiều tin nhắn theo danh sách ID, tenantId và kiểm tra owner
    @Query("SELECT m FROM Message m WHERE m.id IN :messageIds AND m.tenantId = :tenantId AND EXISTS " +
           "(SELECT 1 FROM Conversation c WHERE c.id = m.conversationId AND c.tenantId = :tenantId AND c.ownerId = :ownerId)")
    List<Message> findAllByIdsAndTenantIdAndOwnerId(
        @Param("messageIds") List<Long> messageIds, 
        @Param("tenantId") Long tenantId, 
        @Param("ownerId") String ownerId
    );
    
    // Lấy tất cả tin nhắn của một conversation với tenantId
    @Query("SELECT m FROM Message m WHERE m.conversationId = :conversationId AND m.tenantId = :tenantId")
    List<Message> findByConversationIdAndTenantId(
        @Param("conversationId") Long conversationId, 
        @Param("tenantId") Long tenantId
    );
    
    // Xóa tất cả tin nhắn thuộc các conversation được chỉ định và tenantId
    @Modifying
    @Query("DELETE FROM Message m WHERE m.conversationId IN :conversationIds AND m.tenantId = :tenantId")
    int deleteAllByConversationIdInAndTenantId(
        @Param("conversationIds") List<Long> conversationIds, 
        @Param("tenantId") Long tenantId
    );
}