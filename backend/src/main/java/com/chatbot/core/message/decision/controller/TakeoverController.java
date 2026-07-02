package com.chatbot.core.message.decision.controller;

import com.chatbot.core.message.decision.model.TakeoverMessage;
import com.chatbot.core.message.decision.service.TakeoverService;
import com.chatbot.core.message.decision.websocket.TakeoverWebSocketHandler;
import com.chatbot.core.message.decision.exception.ConversationException;
import com.chatbot.core.message.store.service.MessageService;
import com.chatbot.core.message.store.service.ConversationService;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.shared.exceptions.ErrorCode;
import com.chatbot.shared.exceptions.ResourceNotFoundException;
import com.chatbot.shared.exceptions.BaseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/takeover")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("isAuthenticated()")
public class TakeoverController {

    private final TakeoverService takeoverService;
    private final TakeoverWebSocketHandler websocketHandler;
    private final ObjectMapper objectMapper;
    private final MessageService messageService;
    private final ConversationService conversationService;
    private final ConversationRepository conversationRepo;
    private final com.chatbot.core.tenant.service.TenantPermissionValidator permissionValidator;

    // UI gửi tin nhắn → centralized xử lý qua TakeoverService
    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@Valid @RequestBody TakeoverMessage message) {
        try {
            log.debug("=== RECEIVED AGENT MESSAGE FOR TAKEOVER ===");
            log.debug("Message ID: {}", message.getId());
            log.debug("Conversation ID: {}", message.getConversationId());
            log.debug("Content: {}", message.getContent());
            log.debug("Sender: {}", message.getSender());
            
            String conversationIdStr = message.getConversationId();
            Long conversationIdLong = Long.parseLong(conversationIdStr);
            
            // Reply Lock Check: Kiểm tra xem conversation có đang được agent khác phụ trách không
            Conversation conversation = conversationRepo.findById(conversationIdLong)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
            
            Long currentUserId = permissionValidator.getCurrentUser().getId();
            String currentUserEmail = permissionValidator.getCurrentUserEmail();
            
            // Check if user has active membership of the conversation's tenant
            if (!permissionValidator.isActiveMember(conversation.getTenantId(), currentUserEmail)) {
                log.warn("🚫 [Tenant Lock] Agent {} attempted to send message to conversation {} in tenant {} but is not a member", 
                    currentUserEmail, conversationIdLong, conversation.getTenantId());
                throw new ConversationException(ErrorCode.NOT_CONVERSATION_MEMBER, 
                    "Bạn không thuộc Tenant của cuộc hội thoại này.");
            }
            
            if (conversation.getAgentAssignedId() != null && 
                !conversation.getAgentAssignedId().equals(currentUserId)) {
                // Conversation đã được gán cho agent khác
                // Kiểm tra xem user hiện tại có phải Owner/Admin không (quyền tối cao)
                boolean isManager = permissionValidator.isAdminOrOwner(conversation.getTenantId(), currentUserEmail);
                
                if (!isManager) {
                    log.warn("🚫 [Reply Lock] Agent {} (ID: {}) attempted to send message to conversation {} assigned to agent {}", 
                        currentUserEmail, currentUserId, conversationIdLong, conversation.getAgentAssignedId());
                    throw new ConversationException(ErrorCode.CONVERSATION_ASSIGNED_TO_OTHER, 
                        "Cuộc hội thoại này đã được phân công cho Agent khác.")
                        .withDetail("assignedAgentId", conversation.getAgentAssignedId());
                }
                
                // Owner/Admin được phép gửi tin nhắn ghi đè, nhưng log cảnh báo
                log.info("⚠️ [Reply Lock Override] Manager {} sending message to conversation {} assigned to agent {}", 
                    currentUserEmail, conversationIdLong, conversation.getAgentAssignedId());
            }
            
            // Check for duplicate message (idempotency)
            if (message.getId() != null && messageService.messageExists(message.getId())) {
                log.info("Message already processed: {}", message.getId());
                return ResponseEntity.ok().body("{\"message\": \"Message already processed\"}");
            }
            
            // Generate unique ID for the message if not provided
            if (message.getId() == null) {
                message.setId("agent_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000));
                log.info("Generated new Message ID: {}", message.getId());
            }
            message.setTimestamp(System.currentTimeMillis());
            message.setSender("agent"); // Ensure sender is set to agent
 
            log.debug("Calling TakeoverService...");
            // 1. Centralized: Lưu vào DB + Gửi đến Facebook + Push WebSocket thông qua TakeoverService
            // Truyền currentUserId để audit log biết agent nào gửi tin nhắn
            takeoverService.saveAndSendAgentMessage(message, conversationIdLong, currentUserId);
            
            log.info("✅ [Takeover] Agent message processed centrally. ID: {}, Conversation: {}", 
                message.getId(), conversationIdLong);
            
            return ResponseEntity.ok().body("{\"message\": \"Message sent successfully\"}");
            
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error sending message: {}", e.getMessage(), e);
            throw new ConversationException(ErrorCode.INTERNAL_ERROR, "Failed to send message: " + e.getMessage(), e);
        }
    }

    // Lấy lịch sử tin nhắn
    @GetMapping("/history/{conversationId}")
    public List<TakeoverMessage> getHistory(@PathVariable String conversationId) {
        Long conversationIdLong = Long.parseLong(conversationId);
        Conversation conversation = conversationRepo.findById(conversationIdLong)
            .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
            
        String currentUserEmail = permissionValidator.getCurrentUserEmail();
        if (!permissionValidator.isActiveMember(conversation.getTenantId(), currentUserEmail)) {
            log.warn("🚫 [Tenant Lock] User {} attempted to access history for conversation {} in tenant {} but is not a member", 
                currentUserEmail, conversationIdLong, conversation.getTenantId());
            throw new ConversationException(ErrorCode.NOT_CONVERSATION_MEMBER, "You are not a member of this conversation's tenant");
        }
        
        return takeoverService.getMessages(conversationId)
                .stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, TakeoverMessage.class);
                    } catch (Exception e) {
                        e.printStackTrace(); 
                        return null;
                    }
                })
                .filter(msg -> msg != null)
                .collect(Collectors.toList());
    }

    // --------------------------------------------------------------------------
    // ENDPOINT MỚI: ASSIGN AGENT TO CONVERSATION
    // --------------------------------------------------------------------------
    @PostMapping("/{conversationId}/assign")
    public ResponseEntity<?> assignConversation(
            @PathVariable Long conversationId,
            @RequestBody Map<String, Long> requestBody) {
        try {
            Long agentId = requestBody.get("agentId");
            String currentUserEmail = permissionValidator.getCurrentUserEmail();
            
            Conversation conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
            
            // Check if user has active membership of the conversation's tenant
            if (!permissionValidator.isActiveMember(conversation.getTenantId(), currentUserEmail)) {
                throw new ConversationException(ErrorCode.FORBIDDEN, 
                    "Bạn không thuộc Tenant của cuộc hội thoại này");
            }
            
            // Check if user is trying to assign someone else
            if (agentId != null && !agentId.equals(permissionValidator.getCurrentUser().getId())) {
                // Only Owner/Admin can assign others
                if (!permissionValidator.isAdminOrOwner(conversation.getTenantId(), currentUserEmail)) {
                    throw new ConversationException(ErrorCode.INSUFFICIENT_PERMISSION, 
                        "Bạn không có quyền phân công conversation cho người khác");
                }
            }
            
            // Update conversation assignment
            if (agentId == null) {
                // Release assignment
                if (conversation.getAgentAssignedId() != null && !conversation.getAgentAssignedId().equals(permissionValidator.getCurrentUser().getId())) {
                    if (!permissionValidator.isAdminOrOwner(conversation.getTenantId(), currentUserEmail)) {
                        throw new ConversationException(ErrorCode.INSUFFICIENT_PERMISSION, 
                            "Bạn không có quyền giải phóng conversation của người khác");
                    }
                }
                conversation.setAgentAssignedId(null);
                conversation.setIsTakenOverByAgent(false);
                conversation.setStatus("open");
                log.info("🔓 Conversation {} released by {}", conversationId, currentUserEmail);
            } else {
                // Assign to agent
                conversation.setAgentAssignedId(agentId);
                conversation.setIsTakenOverByAgent(true);
                conversation.setStatus("active_agent");
                log.info("🔒 Conversation {} assigned to agent {} by {}", conversationId, agentId, currentUserEmail);
            }
            
            conversationRepo.save(conversation);
            
            // Broadcast via WebSocket
            TakeoverMessage notification = new TakeoverMessage(
                "system_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000),
                String.valueOf(conversationId),
                "system",
                agentId == null ? "Conversation đã được giải phóng" : "Conversation đã được phân công",
                System.currentTimeMillis()
            );
            websocketHandler.sendToConversation(String.valueOf(conversationId), notification);
            
            return ResponseEntity.ok().body(Map.of(
                "message", agentId == null ? "Conversation released successfully" : "Conversation assigned successfully",
                "conversationId", conversationId,
                "agentAssignedId", agentId != null ? agentId : "",
                "isTakenOver", agentId != null
            ));
        } catch (ConversationException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Cannot assign conversation: {}", e.getMessage());
            throw new ConversationException(ErrorCode.CANNOT_ASSIGN_CONVERSATION, "Cannot assign conversation", e);
        }
    }

    // --------------------------------------------------------------------------
    // ENDPOINT MỚI: TAKEOVER CONVERSATION
    // --------------------------------------------------------------------------
    @PostMapping("/take/{conversationId}")
    public ResponseEntity<?> takeOverConversation(@PathVariable Long conversationId) {
        try {
            // Get conversation for permission check
            Conversation conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
            
            // Get current user ID
            Long currentUserId = permissionValidator.getCurrentUser().getId();
            String currentUserEmail = permissionValidator.getCurrentUserEmail();
            
            // Check if user has active membership of the conversation's tenant
            if (!permissionValidator.isActiveMember(conversation.getTenantId(), currentUserEmail)) {
                throw new ConversationException(ErrorCode.FORBIDDEN, 
                    "Bạn không thuộc Tenant của cuộc hội thoại này");
            }
            
            // Use takeoverConversation method which properly sets agentAssignedId
            conversationService.takeoverConversation(conversationId, currentUserId);
            
            // 2. Gửi thông báo qua WebSocket về tất cả sessions trong conversation
            TakeoverMessage takeoverNotification = new TakeoverMessage(
                "system_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000),
                String.valueOf(conversationId),
                "system",
                "🔒 Conversation has been taken over by agent",
                System.currentTimeMillis()
            );
            websocketHandler.sendToConversation(String.valueOf(conversationId), takeoverNotification);
            
            // 3. Broadcast takeover event tenant-wide để agents khác thấy realtime update
            try {
                Map<String, Object> takeoverEvent = Map.of(
                    "type", "TAKEOVER_EVENT",
                    "data", Map.of(
                        "conversationId", conversationId,
                        "action", "taken_over",
                        "timestamp", System.currentTimeMillis()
                    )
                );
                String takeoverEventJson = objectMapper.writeValueAsString(takeoverEvent);
                websocketHandler.broadcastToTenant(conversation.getTenantId(), takeoverEventJson);
                log.info("📡 Broadcasted takeover event to tenant {}", conversation.getTenantId());
            } catch (Exception e) {
                log.error("❌ Failed to broadcast takeover event: {}", e.getMessage());
            }
            
            log.info("🔒 Conversation {} taken over successfully", conversationId);
            return ResponseEntity.ok().body(Map.of(
                "message", "Conversation taken over successfully",
                "conversationId", conversationId,
                "isTakenOver", true,
                "takenAt", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("❌ Cannot takeover conversation: {}", e.getMessage());
            throw new ConversationException(ErrorCode.CANNOT_TAKEOVER_CONVERSATION, "Cannot takeover conversation", e);
        }
    }

    // --------------------------------------------------------------------------
    // ENDPOINT MỚI: RELEASE CONVERSATION
    // --------------------------------------------------------------------------
    @PostMapping("/release/{conversationId}")
    public ResponseEntity<?> releaseConversation(@PathVariable Long conversationId) {
        try {
            // 1. Get conversation to check ownership
            Conversation conversation = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation not found"));
            
            String currentUserEmail = permissionValidator.getCurrentUserEmail();
            
            // Check if user has active membership of the conversation's tenant
            if (!permissionValidator.isActiveMember(conversation.getTenantId(), currentUserEmail)) {
                throw new ConversationException(ErrorCode.FORBIDDEN, 
                    "Bạn không thuộc Tenant của cuộc hội thoại này");
            }
            
            // Check if user has permission to release (must be assigned agent or Admin/Owner)
            if (conversation.getAgentAssignedId() != null && !conversation.getAgentAssignedId().equals(permissionValidator.getCurrentUser().getId())) {
                if (!permissionValidator.isAdminOrOwner(conversation.getTenantId(), currentUserEmail)) {
                    throw new ConversationException(ErrorCode.INSUFFICIENT_PERMISSION, 
                        "Bạn không có quyền giải phóng conversation của người khác");
                }
            }
            
            // 2. Dùng releaseConversation() — gán agentAssignedId = null, status = "open"
            // Không dùng updateTakenOverStatus() vì nó kiểm tra ownerId của FacebookConnection,
            // không phải agent đang thực hiện release — sẽ gây lỗi 403 cho agent thông thường.
            conversationService.releaseConversation(conversationId);
            
            // 3. Gửi thông báo qua WebSocket đến conversation
            TakeoverMessage releaseNotification = new TakeoverMessage(
                "system_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000),
                String.valueOf(conversationId),
                "system",
                "Conversation has been released - bot can now respond",
                System.currentTimeMillis()
            );
            websocketHandler.sendToConversation(String.valueOf(conversationId), releaseNotification);
            
            // 4. Broadcast release event tenant-wide để agents khác thấy realtime update
            try {
                Map<String, Object> releaseEvent = Map.of(
                    "type", "TAKEOVER_EVENT",
                    "data", Map.of(
                        "conversationId", conversationId,
                        "action", "released",
                        "timestamp", System.currentTimeMillis()
                    )
                );
                String releaseEventJson = objectMapper.writeValueAsString(releaseEvent);
                websocketHandler.broadcastToTenant(conversation.getTenantId(), releaseEventJson);
                log.info("📡 Broadcasted release event to tenant {}", conversation.getTenantId());
            } catch (Exception e) {
                log.error("❌ Failed to broadcast release event: {}", e.getMessage());
            }
            
            log.info("Conversation {} released successfully", conversationId);
            return ResponseEntity.ok().body(Map.of(
                "message", "Conversation released successfully",
                "conversationId", conversationId,
                "isTakenOver", false,
                "releasedAt", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            log.error("Cannot release conversation: {}", e.getMessage());
            throw new ConversationException(ErrorCode.CANNOT_RELEASE_CONVERSATION, "Cannot release conversation", e);
        }
    }

    // --------------------------------------------------------------------------
    // ENDPOINT: GET TAKEOVER STATUS — trả trạng thái thực từ DB
    // --------------------------------------------------------------------------
    @GetMapping("/status/{conversationId}")
    public ResponseEntity<?> getTakeoverStatus(@PathVariable Long conversationId) {
        try {
            Conversation c = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new ConversationException(ErrorCode.CONVERSATION_NOT_FOUND, "Conversation not found"));
                
            String currentUserEmail = permissionValidator.getCurrentUserEmail();
            if (!permissionValidator.isActiveMember(c.getTenantId(), currentUserEmail)) {
                throw new ConversationException(ErrorCode.FORBIDDEN, 
                    "Bạn không thuộc Tenant của cuộc hội thoại này");
            }
            
            return ResponseEntity.ok().body((Object) Map.of(
                "conversationId", conversationId,
                "isTakenOver", c.getIsTakenOverByAgent() != null && c.getIsTakenOverByAgent(),
                "agentId", c.getAgentAssignedId() != null ? c.getAgentAssignedId().toString() : "",
                "status", c.getStatus() != null ? c.getStatus() : "open"
            ));
        } catch (ConversationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConversationException(ErrorCode.INTERNAL_ERROR, "Cannot get takeover status", e);
        }
    }

    // --------------------------------------------------------------------------
    // ENDPOINT: GET ACTIVE TAKEOVERS — lấy danh sách conversations đang bị agent takeover
    // --------------------------------------------------------------------------
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getActiveTakeovers() {
        try {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                return ResponseEntity.ok().body(List.of());
            }
            List<Conversation> activeTakeovers = conversationRepo
                .findByIsTakenOverByAgentAndTenantId(true, tenantId);

            List<Map<String, Object>> result = activeTakeovers.stream()
                .map(c -> {
                    Map<String, Object> m = new java.util.HashMap<>();
                    m.put("conversationId", c.getId());
                    m.put("externalUserId", c.getExternalUserId());
                    m.put("userName", c.getUserName() != null ? c.getUserName() : "");
                    m.put("agentAssignedId", c.getAgentAssignedId() != null ? c.getAgentAssignedId() : "");
                    m.put("status", c.getStatus());
                    m.put("takenAt", c.getUpdatedAt() != null ? c.getUpdatedAt().toString() : "");
                    return m;
                })
                .collect(Collectors.toList());

            log.info("📋 Found {} active takeovers for tenant {}", result.size(), tenantId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Cannot get active takeovers: {}", e.getMessage());
            throw new ConversationException(ErrorCode.INTERNAL_ERROR, "Cannot get active takeovers", e);
        }
    }
}