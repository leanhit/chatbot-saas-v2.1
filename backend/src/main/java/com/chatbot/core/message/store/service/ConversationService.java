package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.Message;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.spokes.facebook.user.service.FacebookUserService;
import com.chatbot.core.message.store.model.Channel;
import com.chatbot.core.tenant.infra.TenantContext;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepo;
    private final FacebookConnectionRepository facebookConnectionRepo;
    private final MessageRepository messageRepo;
    private final FacebookUserService facebookUserService;

    /**
     * Tìm kiếm Conversation hiện có hoặc tạo mới nếu chưa tồn tại.
     * Tự động lấy ownerId từ Connection và gán vào Conversation mới.
     * @param channel Kênh tin nhắn (ví dụ: "facebook_messenger")
     */
    public Conversation findOrCreate(UUID connectionId, String externalUserId, Channel channel) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        
        return conversationRepo
                .findByConnectionIdAndExternalUserIdAndTenantId(connectionId, externalUserId, tenantId)
                .orElseGet(() -> {
                    // Lấy ownerId từ Connection.
                    String ownerId = facebookConnectionRepo.findById(connectionId)
                        .map(FacebookConnection::getOwnerId)
                        .orElseThrow(() -> new RuntimeException("Connection not found with ID: " + connectionId));

                    // Tạo conversation mới
                    Conversation c = Conversation.builder()
                            .connectionId(connectionId)
                            .externalUserId(externalUserId)
                            .status("open")
                            .channel(channel) 
                            .isClosedByAgent(false)
                            .isTakenOverByAgent(false)
                            .ownerId(ownerId)
                            .build();
                    
                    // Nếu là kênh Facebook, lấy thông tin người dùng
                    if (channel == Channel.FACEBOOK) {
                        try {
                            // Lấy thông tin kết nối để lấy pageId thực tế
                            FacebookConnection fbConnection = facebookConnectionRepo.findById(connectionId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy kết nối với ID: " + connectionId));
                            
                            String pageId = fbConnection.getPageId();
                            log.info("🔄 Đang lấy thông tin người dùng Facebook - PSID: {}, Page ID: {}", 
                                externalUserId, pageId);
                                
                            var userInfo = facebookUserService.getUserInfo(externalUserId, pageId);
                            
                            if (userInfo != null) {
                                log.info("✅ Đã lấy được thông tin người dùng - Tên: {}, Avatar: {}", 
                                    userInfo.getName(), 
                                    userInfo.getProfilePic() != null ? "[Có ảnh đại diện]" : "[Không có ảnh]");
                                    
                                c.setUserName(userInfo.getName());
                                c.setUserAvatar(userInfo.getProfilePic());
                            } else {
                                log.warn("⚠️ Không lấy được thông tin người dùng từ Facebook cho PSID: {}", externalUserId);
                            }
                        } catch (Exception e) {
                            log.error("❌ Lỗi khi lấy thông tin người dùng từ Facebook - PSID: {}, Lỗi: {}", 
                                externalUserId, e.getMessage(), e);
                        }
                    }
                    
                    return conversationRepo.save(c);
                });
    }

    /**
     * Lấy danh sách Conversations TỔNG THỂ (không lọc theo Owner/Connection).
     * Sắp xếp theo updatedAt để Conversation có tin nhắn mới nhất lên đầu.
     */
    public Page<Conversation> getConversations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        return conversationRepo.findAllByTenantIdOrderByUpdatedAtDesc(tenantId, pageable);
    }
    
    /**
     * Lấy danh sách Conversations, LỌC theo Owner ID.
     * Sắp xếp theo updatedAt để Conversation có tin nhắn mới nhất lên đầu.
     * @param ownerId ID của chủ sở hữu (Owner) để lọc
     */
    public Page<Conversation> getConversationsByOwnerId(String ownerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        return conversationRepo.findByOwnerIdAndTenantIdOrderByUpdatedAtDesc(ownerId, tenantId, pageable);
    }
    
    /**
     * Thêm phương thức để đóng Conversation
     */
    public Conversation closeConversation(Long conversationId) {
        return conversationRepo.findById(conversationId)
            .map(c -> {
                c.setIsClosedByAgent(true);
                c.setStatus("closed");
                // Khi đóng, luôn trả quyền điều khiển về Botpress (mặc dù cuộc hội thoại đã đóng)
                c.setIsTakenOverByAgent(false); 
                return conversationRepo.save(c);
            })
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }

    // =========================================================================
    // PHƯƠNG THỨC MỚI: XỬ LÝ HANDOVER/TAKEOVER
    // =========================================================================

    /**
     * Agent tiếp quản (Takeover) Conversation.
     * Botpress sẽ bị ngắt (isTakenOverByAgent = true).
     * @param conversationId ID của Conversation
     * @param agentAssignedId ID của Agent tiếp quản
     * @return Conversation đã được cập nhật
     */
    public Conversation takeoverConversation(Long conversationId, Long agentAssignedId) {
        return conversationRepo.findById(conversationId)
            .map(c -> {
                if (c.getIsClosedByAgent()) {
                    // Không tiếp quản conversation đã đóng
                    throw new IllegalStateException("Cannot takeover a closed conversation.");
                }
                c.setIsTakenOverByAgent(true); // Ngắt Botpress
                c.setAgentAssignedId(agentAssignedId); // Gán Agent
                c.setStatus("active_agent"); // Cập nhật trạng thái
                return conversationRepo.save(c);
            })
            .orElseThrow(() -> new RuntimeException("Conversation not found for takeover"));
    }

    /**
     * Agent giải phóng (Release) Conversation.
     * Botpress sẽ được kích hoạt lại (isTakenOverByAgent = false).
     * @param conversationId ID của Conversation
     * @return Conversation đã được cập nhật
     */
    public Conversation releaseConversation(Long conversationId) {
        return conversationRepo.findById(conversationId)
            .map(c -> {
                if (c.getIsClosedByAgent()) {
                    // Không release conversation đã đóng (mặc dù hành động close đã làm điều này)
                    throw new IllegalStateException("Cannot release a closed conversation.");
                }
                c.setIsTakenOverByAgent(false); // Kích hoạt lại Botpress
                c.setAgentAssignedId(null); // Bỏ gán Agent
                c.setStatus("open"); // Quay về trạng thái mở để bot xử lý
                return conversationRepo.save(c);
            })
            .orElseThrow(() -> new RuntimeException("Conversation not found for release"));
    }
    
    /**
     * Xóa vĩnh viễn một conversation khỏi hệ thống
     * @param conversationId ID của conversation cần xóa
     * @throws RuntimeException nếu không tìm thấy conversation
     */
    /**
     * Xóa một conversation và tất cả tin nhắn liên quan với kiểm tra quyền sở hữu
     * @param conversationId ID của conversation cần xóa
     * @param ownerId ID của chủ sở hữu
     * @throws RuntimeException nếu không tìm thấy conversation hoặc không có quyền
     */
    @Transactional
    public void deleteConversation(Long conversationId, String ownerId) {
        Conversation conversation = conversationRepo.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));
            
        if (!ownerId.equals(conversation.getOwnerId())) {
            throw new RuntimeException("You don't have permission to delete this conversation");
        }
        
        // Xóa tất cả tin nhắn liên quan
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        messageRepo.deleteByConversationIdAndTenantId(conversationId, tenantId);
        
        // Xóa conversation
        conversationRepo.delete(conversation);
    }
    
    /**
     * Xóa nhiều conversations cùng lúc với kiểm tra quyền sở hữu
     * @param conversationIds Danh sách ID của các conversation cần xóa
     * @param ownerId ID của chủ sở hữu
     * @return Số lượng conversation đã xóa
     */
    @Transactional
    public int deleteConversations(List<Long> conversationIds, String ownerId) {
        if (conversationIds == null || conversationIds.isEmpty()) {
            return 0;
        }
        
        // Lấy danh sách các conversation thuộc owner
        List<Conversation> conversationsToDelete = conversationRepo
            .findAllById(conversationIds)
            .stream()
            .filter(c -> ownerId.equals(c.getOwnerId()))
            .collect(Collectors.toList());
            
        if (!conversationsToDelete.isEmpty()) {
            // Lấy danh sách ID conversation để xóa tin nhắn
            List<Long> idsToDelete = conversationsToDelete.stream()
                .map(Conversation::getId)
                .collect(Collectors.toList());
                
            // Xóa tất cả tin nhắn của các conversation này
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                throw new RuntimeException("Không tìm thấy tenant ID trong context");
            }
            messageRepo.deleteAllByConversationIdInAndTenantId(idsToDelete, tenantId);
            
            // Xóa các conversation
            conversationRepo.deleteAll(conversationsToDelete);
        }
        
        return conversationsToDelete.size();
    }
    
    /**
     * Cập nhật trạng thái isTakenOverByAgent của một conversation
     */
    @Transactional
    public Conversation updateTakenOverStatus(Long conversationId, Boolean isTakenOverByAgent, String ownerId) {

    log.info(
        "Request to update takeover status. ConversationId={}, isTakenOver={}, CallerOwnerId={}",
        conversationId, isTakenOverByAgent, ownerId
    );

    return conversationRepo.findById(conversationId)
        .map(conversation -> {

            // Log trạng thái hiện tại
            log.debug(
                "Before update → ID={}, OwnerId={}, isTakenOver={}, Status={}, AgentAssignedId={}",
                conversation.getId(),
                conversation.getOwnerId(),
                conversation.getIsTakenOverByAgent(),
                conversation.getStatus(),
                conversation.getAgentAssignedId()
            );

            // Kiểm tra quyền
            if (!ownerId.equals(conversation.getOwnerId())) {
                log.warn(
                    "Permission denied. CallerOwnerId={} != ConversationOwnerId={}, ConversationId={}",
                    ownerId, conversation.getOwnerId(), conversationId
                );
                throw new RuntimeException("You don't have permission to update this conversation");
            }

            // Không cho phép update nếu đã đóng
            if (conversation.getIsClosedByAgent()) {
                log.warn("Conversation {} is closed, cannot update takeover status.", conversationId);
                throw new IllegalStateException("Cannot update a closed conversation.");
            }

            // Cập nhật trạng thái takeover
            conversation.setIsTakenOverByAgent(isTakenOverByAgent);

            if (isTakenOverByAgent) {
                // Agent takeover → bot stop
                conversation.setStatus("active_agent");  // Giữ đồng bộ với hàm takeoverConversation()
                log.debug("Conversation {} takeover → status=active_agent", conversationId);
            } else {
                // Release → trả lại bot
                conversation.setStatus("open");
                conversation.setAgentAssignedId(null); // Giải phóng Agent
                log.debug("Conversation {} released → status=open, agentAssignedId reset", conversationId);
            }

            Conversation updated = conversationRepo.save(conversation);

            // Log sau khi lưu
            log.info(
                "Conversation {} updated. Final: isTakenOver={}, Status={}, AgentAssignedId={}",
                updated.getId(),
                updated.getIsTakenOverByAgent(),
                updated.getStatus(),
                updated.getAgentAssignedId()
            );

            return updated;
        })
        .orElseThrow(() -> {
            log.error("Conversation not found with id {}", conversationId);
            return new RuntimeException("Conversation not found with id: " + conversationId);
        });
}

        
    /**
     * @deprecated Sử dụng phiên bản mới có kiểm tra ownerId
     */
    @Deprecated
    public void deleteConversation(Long conversationId) {
        deleteConversation(conversationId, ""); // Gọi phương thức mới với ownerId rỗng
    }
    
    // =========================================================================
    // END PHƯƠNG THỨC HANDOVER
    // =========================================================================
    
    /**
     * Tìm kiếm Conversation hiện có. Rất cần thiết khi Bot/Agent phản hồi, 
     * để lấy Conversation ID cho việc lưu trữ tin nhắn đi.
     */
    public Optional<Conversation> findByConnectionIdAndExternalUserId(UUID connectionId, String externalUserId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        return conversationRepo.findByConnectionIdAndExternalUserIdAndTenantId(connectionId, externalUserId, tenantId);
    }


    /**
     * Lấy danh sách Conversations, có hỗ trợ LỌC theo connectionId.
     * Sắp xếp theo updatedAt để Conversation có tin nhắn mới nhất lên đầu.
     * @param connectionId UUID của Connection để lọc (có thể là null)
     * @param ownerId String của user để lọc (có thể là null)
     */

    public Page<Conversation> getConversationsByOwnerIdAndConnectionId(
            String ownerId,
            UUID connectionId,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        
        return conversationRepo.findByOwnerIdAndConnectionIdAndTenantIdOrderByUpdatedAtDesc(
                ownerId,
                connectionId,
                tenantId,
                pageable
        );
    }

}