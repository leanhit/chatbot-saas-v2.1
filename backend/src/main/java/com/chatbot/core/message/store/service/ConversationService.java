package com.chatbot.core.message.store.service;

import com.chatbot.core.message.decision.exception.ConversationNotFoundException;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.Message;
import com.chatbot.core.message.store.dto.ConversationStatisticsDTO;
import com.chatbot.core.message.store.dto.ChartDataPointDTO;
import com.chatbot.core.message.store.dto.ActivityDTO;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.core.message.store.model.Channel;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.shared.messenger.ChannelMessengerService;
import com.chatbot.shared.messenger.ChannelUserInfo;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final ChannelMessengerService channelMessengerService;
    private final MessageRepository messageRepo;
    private final RoutingRuleService routingRuleService;
    private final ConversationEndWorkflow conversationEndWorkflow;

    /**
     * Tìm kiếm Conversation hiện có hoặc tạo mới nếu chưa tồn tại.
     * Tự động lấy ownerId từ Connection và gán vào Conversation mới.
     * @param channel Kênh tin nhắn (ví dụ: "facebook_messenger")
     */
    public Conversation findOrCreate(UUID connectionId, String externalUserId, Channel channel) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new com.chatbot.shared.exceptions.BaseException(com.chatbot.shared.exceptions.ErrorCode.TENANT_CONTEXT_MISSING, "Tenant ID not found in context");
        }
        
        return conversationRepo
                .findByConnectionIdAndExternalUserIdAndTenantId(connectionId, externalUserId, tenantId)
                .orElseGet(() -> {
                    String ownerId = channelMessengerService.getOwnerIdForConnection(connectionId);
                    if (ownerId == null) {
                        throw new ConversationNotFoundException("Connection not found with ID: " + connectionId);
                    }

                    Conversation c = Conversation.builder()
                            .connectionId(connectionId)
                            .externalUserId(externalUserId)
                            .status("open")
                            .channel(channel)
                            .isClosedByAgent(false)
                            .isTakenOverByAgent(false)
                            .ownerId(ownerId)
                            .customerTier("Standard")
                            .language("en")
                            .build();
                    
                    ChannelUserInfo userInfo = channelMessengerService.getUserInfo(connectionId, externalUserId);
                    if (userInfo != null) {
                        log.info("✅ Obtained user profile info for external user ID: {} - Name: {}", externalUserId, userInfo.getName());
                        c.setUserName(userInfo.getName());
                        c.setUserAvatar(userInfo.getAvatarUrl());
                        extractAndStoreUserAttributes(c, userInfo);
                    }
                    
                    Conversation savedConversation = conversationRepo.save(c);
                    
                    try {
                        routingRuleService.applyRoutingRules(savedConversation);
                    } catch (Exception e) {
                        log.error("Error applying routing rules to conversation {}", savedConversation.getId(), e);
                    }
                    
                    return savedConversation;
                });
    }

    @SuppressWarnings("unchecked")
    private void extractAndStoreUserAttributes(Conversation conversation, ChannelUserInfo userInfo) {
        try {
            if (userInfo.getAttributes() != null && !userInfo.getAttributes().isEmpty()) {
                String existingAttributes = conversation.getCustomAttributes();
                java.util.Map<String, Object> allAttributes;
                
                if (existingAttributes != null && !existingAttributes.isEmpty()) {
                    allAttributes = new com.fasterxml.jackson.databind.ObjectMapper().readValue(
                        existingAttributes,
                        java.util.Map.class
                    );
                } else {
                    allAttributes = new java.util.HashMap<>();
                }
                
                allAttributes.put("channelUserAttributes", userInfo.getAttributes());
                conversation.setCustomAttributes(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(allAttributes));
                
                log.info("Extracted and stored {} attributes from channel user info for conversation {}", 
                    userInfo.getAttributes().size(), conversation.getId());
            }
        } catch (Exception e) {
            log.error("Error extracting user attributes from channel user info", e);
        }
    }

    /**
     * Lấy danh sách Conversations TỔNG THỂ (không lọc theo Owner/Connection).
     * Sắp xếp theo updatedAt để Conversation có tin nhắn mới nhất lên đầu.
     */
    public Page<Conversation> getConversations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new com.chatbot.shared.exceptions.BaseException(com.chatbot.shared.exceptions.ErrorCode.TENANT_CONTEXT_MISSING, "Tenant ID not found in context");
        }
        return conversationRepo.findAllByTenantIdOrderByUpdatedAtDesc(tenantId, pageable);
    }

    /**
     * Bot Inbox: Lấy danh sách conversations đang được bot xử lý (isTakenOverByAgent = false)
     * Sắp xếp theo updatedAt để Conversation có tin nhắn mới nhất lên đầu.
     */
    public Page<Conversation> getBotInboxConversations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new com.chatbot.shared.exceptions.BaseException(com.chatbot.shared.exceptions.ErrorCode.TENANT_CONTEXT_MISSING, "Tenant ID not found in context");
        }
        return conversationRepo.findBotInboxConversationsByTenantId(tenantId, pageable);
    }

    /**
     * Agent Inbox: Lấy danh sách conversations đang được agent xử lý (isTakenOverByAgent = true)
     * Sắp xếp theo updatedAt để Conversation có tin nhắn mới nhất lên đầu.
     */
    public Page<Conversation> getAgentInboxConversations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new com.chatbot.shared.exceptions.BaseException(com.chatbot.shared.exceptions.ErrorCode.TENANT_CONTEXT_MISSING, "Tenant ID not found in context");
        }
        return conversationRepo.findAgentInboxConversationsByTenantId(tenantId, pageable);
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
            throw new com.chatbot.shared.exceptions.BaseException(com.chatbot.shared.exceptions.ErrorCode.TENANT_CONTEXT_MISSING, "Tenant ID not found in context");
        }
        return conversationRepo.findByOwnerIdAndTenantIdOrderByUpdatedAtDesc(ownerId, tenantId, pageable);
    }
    
    /**
     * Thêm phương thức để đóng Conversation
     */
    @Transactional(transactionManager = "messageTransactionManager", rollbackFor = Exception.class)
    public Conversation closeConversation(Long conversationId) {
        Conversation conversation = conversationRepo.findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException("Conversation not found"));
        
        conversationEndWorkflow.handleConversationEnd(conversationId, "agent_closed");
        
        return conversationRepo.findById(conversationId).orElse(conversation);
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
    @Transactional(transactionManager = "messageTransactionManager", rollbackFor = Exception.class)
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
            .orElseThrow(() -> new ConversationNotFoundException("Conversation not found for takeover"));
    }

    /**
     * Agent giải phóng (Release) Conversation.
     * Botpress sẽ được kích hoạt lại (isTakenOverByAgent = false).
     * @param conversationId ID của Conversation
     * @return Conversation đã được cập nhật
     */
    @Transactional(transactionManager = "messageTransactionManager", rollbackFor = Exception.class)
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
            .orElseThrow(() -> new ConversationNotFoundException("Conversation not found for release"));
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
    @Transactional(transactionManager = "messageTransactionManager", rollbackFor = Exception.class)
    public void deleteConversation(Long conversationId, String ownerId) {
        Conversation conversation = conversationRepo.findById(conversationId)
            .orElseThrow(() -> new ConversationNotFoundException("Conversation not found with id: " + conversationId));
            
        if (!ownerId.equals(conversation.getOwnerId())) {
            throw new com.chatbot.shared.exceptions.UnauthorizedException("You don't have permission to delete this conversation");
        }
        
        // Xóa tất cả tin nhắn liên quan
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new com.chatbot.shared.exceptions.BaseException(com.chatbot.shared.exceptions.ErrorCode.TENANT_CONTEXT_MISSING, "Tenant ID not found in context");
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
    @Transactional(transactionManager = "messageTransactionManager", rollbackFor = Exception.class)
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
                throw new com.chatbot.shared.exceptions.BaseException(com.chatbot.shared.exceptions.ErrorCode.TENANT_CONTEXT_MISSING, "Tenant ID not found in context");
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
    public Conversation updateTakenOverStatus(Long conversationId, Boolean isTakenOverByAgent, Long agentAssignedId, String ownerId) {

    log.info(
        "Request to update takeover status. ConversationId={}, isTakenOver={}, agentAssignedId={}, CallerOwnerId={}",
        conversationId, isTakenOverByAgent, agentAssignedId, ownerId
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
                throw new com.chatbot.shared.exceptions.UnauthorizedException("You don't have permission to update this conversation");
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
                conversation.setAgentAssignedId(agentAssignedId);
                log.debug("Conversation {} takeover → status=active_agent, agentAssignedId={}", conversationId, agentAssignedId);
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
            return new ConversationNotFoundException("Conversation not found with id: " + conversationId);
        });
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
            throw new com.chatbot.shared.exceptions.BaseException(com.chatbot.shared.exceptions.ErrorCode.TENANT_CONTEXT_MISSING, "Tenant ID not found in context");
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
            throw new com.chatbot.shared.exceptions.BaseException(com.chatbot.shared.exceptions.ErrorCode.TENANT_CONTEXT_MISSING, "Tenant ID not found in context");
        }
        
        return conversationRepo.findByOwnerIdAndConnectionIdAndTenantIdOrderByUpdatedAtDesc(
                ownerId,
                connectionId,
                tenantId,
                pageable
        );
    }

    // --------------------------------------------------------------------------
    // MISSING METHODS FOR FRONTEND API
    // --------------------------------------------------------------------------

    /**
     * Get conversation by ID
     */
    public Conversation getConversationById(Long conversationId) {
        Long tenantId = TenantContext.getTenantId();
        return conversationRepo.findByIdAndTenantId(conversationId, tenantId)
                .orElseThrow(() -> new ConversationNotFoundException("Conversation not found"));
    }

    /**
     * Get conversation by ID - trả về Optional
     */
    public Optional<Conversation> findConversationById(Long conversationId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new com.chatbot.shared.exceptions.BaseException(com.chatbot.shared.exceptions.ErrorCode.TENANT_CONTEXT_MISSING, "Tenant ID not found in context");
        }
        return conversationRepo.findByIdAndTenantId(conversationId, tenantId);
    }

    /**
     * Create new conversation
     */
    @Transactional(transactionManager = "messageTransactionManager", rollbackFor = Exception.class)
    public Conversation createConversation(Conversation conversation) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new com.chatbot.shared.exceptions.BaseException(com.chatbot.shared.exceptions.ErrorCode.TENANT_CONTEXT_MISSING, "Tenant ID not found in context");
        }
        
        // Set tenant ID
        conversation.setTenantId(tenantId);
        
        // Set default values
        if (conversation.getStatus() == null) {
            conversation.setStatus("open");
        }
        if (conversation.getIsTakenOverByAgent() == null) {
            conversation.setIsTakenOverByAgent(false);
        }
        if (conversation.getIsClosedByAgent() == null) {
            conversation.setIsClosedByAgent(false);
        }
        
        return conversationRepo.save(conversation);
    }

    /**
     * Update conversation
     */
    public Conversation updateConversation(Long conversationId, Object conversationDTO, String ownerId) {
        Conversation conversation = getConversationById(conversationId);
        if (!ownerId.equals(conversation.getOwnerId())) {
            throw new com.chatbot.shared.exceptions.UnauthorizedException("You don't have permission to update this conversation");
        }
        // Status update if DTO contains status field (handled via PATCH endpoints)
        return conversationRepo.save(conversation);
    }

    /**
     * Search conversations with real filtering
     */
    public Page<Conversation> searchConversations(String ownerId, String query, String channel, String dateRange, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Long tenantId = TenantContext.getTenantId();
        if (query != null && !query.isBlank()) {
            return conversationRepo.searchByOwnerIdAndTenantId(ownerId, tenantId, query.trim(), pageable);
        }
        return conversationRepo.findByOwnerIdAndTenantIdOrderByUpdatedAtDesc(ownerId, tenantId, pageable);
    }
}