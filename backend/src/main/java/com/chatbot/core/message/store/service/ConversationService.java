package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.Message;
import com.chatbot.core.message.store.dto.ConversationStatisticsDTO;
import com.chatbot.core.message.store.dto.ChartDataPointDTO;
import com.chatbot.core.message.store.dto.ActivityDTO;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.spokes.facebook.user.service.FacebookUserService;
import com.chatbot.core.message.store.model.Channel;
import com.chatbot.core.tenant.infra.TenantContext;

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
    private final FacebookConnectionRepository facebookConnectionRepo;
    private final MessageRepository messageRepo;
    private final FacebookUserService facebookUserService;
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
                            .customerTier("Standard") // Default tier
                            .language("en") // Default language
                            .build();
                    
                    // Nếu là kênh Facebook, lấy thông tin người dùng
                    if (channel == Channel.FACEBOOK) {
                        try {
                            // Lấy thông tin kết nối để lấy pageId thực tế
                            FacebookConnection fbConnection = facebookConnectionRepo.findById(connectionId)
                                .orElseThrow(() -> new com.chatbot.shared.exceptions.BaseException(com.chatbot.shared.exceptions.ErrorCode.CONNECTION_NOT_FOUND, "Connection not found with ID: " + connectionId));
                            
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
                                
                                // Extract additional attributes for attribute-based routing
                                extractAndStoreUserAttributes(c, userInfo);
                            } else {
                                log.warn("⚠️ Không lấy được thông tin người dùng từ Facebook cho PSID: {}", externalUserId);
                            }
                        } catch (Exception e) {
                            log.error("❌ Lỗi khi lấy thông tin người dùng từ Facebook - PSID: {}, Lỗi: {}", 
                                externalUserId, e.getMessage(), e);
                        }
                    }
                    
                    Conversation savedConversation = conversationRepo.save(c);
                    
                    // Apply routing rules to the new conversation
                    try {
                        routingRuleService.applyRoutingRules(savedConversation);
                    } catch (Exception e) {
                        log.error("Error applying routing rules to conversation {}", savedConversation.getId(), e);
                    }
                    
                    return savedConversation;
                });
    }

    /**
     * Extract and store user attributes from Facebook user info for attribute-based routing
     * Implements Phase 1.3: Attribute-based Routing
     */
    @SuppressWarnings("unchecked")
    private void extractAndStoreUserAttributes(Conversation conversation, com.chatbot.spokes.facebook.user.dto.FacebookUserInfo userInfo) {
        try {
            java.util.Map<String, Object> attributes = new java.util.HashMap<>();
            
            // Extract available attributes from FacebookUserInfo
            if (userInfo.getName() != null) {
                attributes.put("name", userInfo.getName());
                // Try to extract first name from full name
                String[] nameParts = userInfo.getName().split(" ", 2);
                if (nameParts.length > 0) {
                    attributes.put("firstName", nameParts[0]);
                }
                if (nameParts.length > 1) {
                    attributes.put("lastName", nameParts[1]);
                }
            }
            if (userInfo.getPsid() != null) {
                attributes.put("psid", userInfo.getPsid());
            }
            if (userInfo.getProfilePic() != null) {
                attributes.put("hasProfilePic", true);
            }
            if (userInfo.getOdooPartnerId() != null) {
                attributes.put("odooPartnerId", userInfo.getOdooPartnerId());
                attributes.put("isOdooCustomer", true);
            }
            if (userInfo.getLastInteraction() != null) {
                attributes.put("lastInteraction", userInfo.getLastInteraction().toString());
            }
            
            // Store in custom attributes as JSON
            if (!attributes.isEmpty()) {
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
                
                allAttributes.put("facebookUserAttributes", attributes);
                conversation.setCustomAttributes(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(allAttributes));
                
                log.info("Extracted and stored {} attributes from Facebook user info for conversation {}", 
                    attributes.size(), conversation.getId());
            }
        } catch (Exception e) {
            log.error("Error extracting user attributes from Facebook user info", e);
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
    @Transactional
    public Conversation closeConversation(Long conversationId) {
        Conversation conversation = conversationRepo.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
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
    @Transactional
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
    @Transactional
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
            return new RuntimeException("Conversation not found with id: " + conversationId);
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
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
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
    @Transactional
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
            throw new RuntimeException("You don't have permission to update this conversation");
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