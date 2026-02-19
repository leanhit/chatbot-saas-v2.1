package com.chatbot.core.message.store.repository;

import com.chatbot.core.message.store.model.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    // Phương thức đã có: Tìm kiếm chính xác 1 Conversation dựa trên kết nối, người dùng ngoài và tenant
    @Query("SELECT c FROM Conversation c WHERE c.connectionId = :connectionId AND c.externalUserId = :externalUserId AND c.tenantId = :tenantId")
    Optional<Conversation> findByConnectionIdAndExternalUserIdAndTenantId(
        @Param("connectionId") UUID connectionId, 
        @Param("externalUserId") String externalUserId,
        @Param("tenantId") Long tenantId
    );

    // Phương thức đã có: Phân trang tất cả Conversations theo connectionId và tenant
    @Query("SELECT c FROM Conversation c WHERE c.connectionId = :connectionId AND c.tenantId = :tenantId")
    Page<Conversation> findByConnectionIdAndTenantId(
        @Param("connectionId") UUID connectionId, 
        @Param("tenantId") Long tenantId, 
        Pageable pageable
    );

    // 1. Phân trang và sắp xếp cho UI chính theo tenant
    // Sắp xếp theo updatedAt để đảm bảo các cuộc trò chuyện có tin nhắn mới nhất hiển thị đầu tiên
    @Query("SELECT c FROM Conversation c WHERE c.tenantId = :tenantId ORDER BY c.updatedAt DESC")
    Page<Conversation> findAllByTenantIdOrderByUpdatedAtDesc(
        @Param("tenantId") Long tenantId, 
        Pageable pageable
    );
    
    // 2. Lọc theo trạng thái, Agent phụ trách và tenant
    @Query("SELECT c FROM Conversation c WHERE c.status = :status AND c.agentAssignedId = :agentAssignedId AND c.tenantId = :tenantId")
    Page<Conversation> findByStatusAndAgentAssignedIdAndTenantId(
        @Param("status") String status, 
        @Param("agentAssignedId") Long agentAssignedId,
        @Param("tenantId") Long tenantId, 
        Pageable pageable
    );

    // 3. Lấy ra danh sách các ID tin nhắn cuối cùng cho một nhóm Conversation theo tenant
    @Query("SELECT c.lastMessageId FROM Conversation c WHERE c.id IN :conversationIds AND c.tenantId = :tenantId AND c.lastMessageId IS NOT NULL")
    List<Long> findLastMessageIdsByIdInAndTenantId(
        @Param("conversationIds") List<Long> conversationIds,
        @Param("tenantId") Long tenantId
    );

    // 4. Lọc linh hoạt theo connectionId, tenant và sắp xếp theo updatedAt
    @Query("SELECT c FROM Conversation c WHERE c.tenantId = :tenantId AND (:connectionId IS NULL OR c.connectionId = :connectionId) ORDER BY c.updatedAt DESC")
    Page<Conversation> findByConnectionIdFlexibleAndTenantId(
        @Param("connectionId") UUID connectionId,
        @Param("tenantId") Long tenantId, 
        Pageable pageable
    );

    // 5. Lọc theo Owner ID, tenant và sắp xếp theo updatedAt
    @Query("SELECT c FROM Conversation c WHERE c.ownerId = :ownerId AND c.tenantId = :tenantId ORDER BY c.updatedAt DESC")
    Page<Conversation> findByOwnerIdAndTenantIdOrderByUpdatedAtDesc(
        @Param("ownerId") String ownerId,
        @Param("tenantId") Long tenantId, 
        Pageable pageable
    );

    // 6. Tìm tất cả Conversation đang được Agent tiếp quản theo tenant
    @Query("SELECT c FROM Conversation c WHERE c.isTakenOverByAgent = :isTakenOverByAgent AND c.tenantId = :tenantId")
    List<Conversation> findByIsTakenOverByAgentAndTenantId(
        @Param("isTakenOverByAgent") Boolean isTakenOverByAgent,
        @Param("tenantId") Long tenantId
    );
    
    // 6b. Tìm tất cả Conversation đang được Agent tiếp quản từ TẤT CẢ các tenant (cho Scheduler)
    @Query("SELECT c FROM Conversation c WHERE c.isTakenOverByAgent = :isTakenOverByAgent")
    List<Conversation> findAllByIsTakenOverByAgent(@Param("isTakenOverByAgent") Boolean isTakenOverByAgent);
    
    // 7. Tìm tất cả Conversation của Owner, Connection và tenant
    @Query("SELECT c FROM Conversation c WHERE c.ownerId = :ownerId AND c.connectionId = :connectionId AND c.tenantId = :tenantId ORDER BY c.updatedAt DESC")
    Page<Conversation> findByOwnerIdAndConnectionIdAndTenantIdOrderByUpdatedAtDesc(
        @Param("ownerId") String ownerId,
        @Param("connectionId") UUID connectionId,
        @Param("tenantId") Long tenantId,
        Pageable pageable
    );
}