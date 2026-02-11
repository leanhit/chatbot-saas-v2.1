package com.chatbot.modules.messaging.messStore.controller;
import lombok.extern.slf4j.Slf4j;

import com.chatbot.modules.messaging.messStore.dto.ConversationDTO;
import com.chatbot.modules.messaging.messStore.model.Conversation;
import com.chatbot.modules.messaging.messStore.model.Channel;
import com.chatbot.modules.messaging.messStore.mapper.ConversationMapper;
import com.chatbot.modules.messaging.messStore.service.ConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
@Slf4j
public class ConversationController {

    private final ConversationService conversationService;
    private final ConversationMapper conversationMapper; // Giả định đã có

    // --------------------------------------------------------------------------
    // 1. LẤY TẤT CẢ CONVERSATIONS (Default)
    // --------------------------------------------------------------------------
    @GetMapping
    public Page<ConversationDTO> listAllConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        log.info("Fetching all conversations, page: " + page + ", size: " + size);
        return conversationService
                .getConversations(page, size)
                .map(conversationMapper::toDTO);
    }

    // --------------------------------------------------------------------------
    // 2 LỌC THEO CONNECTION ID của Owner
    // --------------------------------------------------------------------------
    @GetMapping("/by-owner-connection")
    public Page<ConversationDTO> getConversationsByOwnerAndConnection(
            @RequestParam UUID connectionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Principal principal
    ) {
        String ownerId = principal.getName();

        return conversationService
                .getConversationsByOwnerIdAndConnectionId(ownerId, connectionId, page, size)
                .map(conversationMapper::toDTO);
    }

    // --------------------------------------------------------------------------
    // 3. LỌC THEO OWNER ID (Dành cho UI quản lý các Conversation của 1 người
    // dùng/công ty)
    // --------------------------------------------------------------------------
    @GetMapping("/by-owner")
    public Page<ConversationDTO> listByOwnerId(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,        
            Principal principal) {        
        String ownerId = principal.getName();        
        log.info("Fetching conversations by ownerId: " + ownerId);
        return conversationService
                .getConversationsByOwnerId(ownerId, page, size)
                .map(conversationMapper::toDTO);
    }

    // --------------------------------------------------------------------------
    // CÁC ENDPOINT KHÁC
    // --------------------------------------------------------------------------

    // POST /api/conversations/{conversationId}/close
    // Endpoint để đóng cuộc trò chuyện
    @PostMapping("/{conversationId}/close")
    public ResponseEntity<ConversationDTO> close(@PathVariable Long conversationId) {
        try {
            Conversation c = conversationService.closeConversation(conversationId);
            return ResponseEntity.ok(conversationMapper.toDTO(c));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found or could not be closed", e);
        }
    }

    // POST /api/conversations/find-or-create (Hỗ trợ tạo mới/debug/webhook)
    @PostMapping("/find-or-create")
    public ConversationDTO findOrCreate(
            @RequestParam UUID connectionId,
            @RequestParam String externalUserId,
            @RequestParam Channel channel) {
        Conversation c = conversationService.findOrCreate(connectionId, externalUserId, channel);
        return conversationMapper.toDTO(c);
    }

    // =========================================================================
    // ENDPOINT MỚI: XỬ LÝ HANDOVER/TAKEOVER
    // =========================================================================

    /**
     * Endpoint để Agent tiếp quản (Takeover) Conversation.
     * Botpress sẽ bị ngắt.
     * POST /api/conversations/{conversationId}/takeover?agentId={agentId}
     * 
     * @param conversationId ID của Conversation
     * @param agentId        ID của Agent đang thực hiện tiếp quản
     * @return ConversationDTO đã được cập nhật
     */
    @PostMapping("/{conversationId}/takeover")
    public ResponseEntity<ConversationDTO> takeover(
            @PathVariable Long conversationId,
            @RequestParam Long agentId // Agent ID được truyền từ Agent UI
    ) {
        try {
            Conversation c = conversationService.takeoverConversation(conversationId, agentId);
            return ResponseEntity.ok(conversationMapper.toDTO(c));
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found for takeover", e);
        }
    }

    /**
     * Endpoint để Agent giải phóng (Release) Conversation.
     * Botpress sẽ được kích hoạt lại.
     * POST /api/conversations/{conversationId}/release
     * 
     * @param conversationId ID của Conversation
     * @return ConversationDTO đã được cập nhật
     */
    @PostMapping("/{conversationId}/release")
    public ResponseEntity<ConversationDTO> release(@PathVariable Long conversationId) {
        try {
            Conversation c = conversationService.releaseConversation(conversationId);
            return ResponseEntity.ok(conversationMapper.toDTO(c));
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversation not found for release", e);
        }
    }

    // =========================================================================
    // ENDPOINT MỚI: CẬP NHẬT TRẠNG THÁI TAKEN OVER
    // =========================================================================

    /**
     * Cập nhật trạng thái isTakenOverByAgent của một conversation
     * PATCH /api/conversations/{conversationId}/taken-over
     * 
     * @param conversationId ID của conversation cần cập nhật
     * @param isTakenOver Giá trị mới cho trạng thái take over (true/false)
     * @param principal Thông tin người dùng hiện tại
     * @return ConversationDTO đã được cập nhật
     */
    @PatchMapping("/{conversationId}/taken-over")
    public ResponseEntity<ConversationDTO> updateTakenOverStatus(
            @PathVariable Long conversationId,
            @RequestParam Boolean isTakenOver,
            Principal principal) {
        
        try {
            String ownerId = principal.getName();
            Conversation updatedConversation = conversationService.updateTakenOverStatus(conversationId, isTakenOver, ownerId);
            return ResponseEntity.ok(conversationMapper.toDTO(updatedConversation));
        } catch (RuntimeException e) {
            if (e.getMessage().contains("permission")) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
            }
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        }
    }
    
    // =========================================================================
    // END ENDPOINT MỚI
    // =========================================================================

    /**
     * Xóa vĩnh viễn một conversation khỏi hệ thống
     * DELETE /api/conversations/{conversationId}
     * @param conversationId ID của conversation cần xóa
     * @param principal Thông tin người dùng hiện tại
     * @return 204 No Content nếu xóa thành công
     */
    @DeleteMapping("/{conversationId}")
    public ResponseEntity<Void> deleteConversation(
        @PathVariable Long conversationId,
        Principal principal) {
        
        try {
            String ownerId = principal.getName(); // Lấy username từ Principal
            conversationService.deleteConversation(conversationId, ownerId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e);
        }
    }

   // --------------------------------------------------------------------------
   // ENDPOINT MỚI: XÓA NHIỀU CONVERSATIONS
   // --------------------------------------------------------------------------

  /**
     *      * Xóa vĩnh viễn nhiều conversations khỏi hệ thống.
     *      * DELETE /api/conversations/batch-delete
     *      * @param conversationIds Danh sách ID của conversations cần xóa (trong
     * Request Body)
     *      * @param principal Thông tin người dùng hiện tại
     *      * @return 204 No Content nếu xóa thành công
     *      
     */    

    @DeleteMapping("/batch-delete")
    public ResponseEntity<Void> deleteConversations(
            @RequestBody List<Long> conversationIds,
            Principal principal) {
        
        if (conversationIds == null || conversationIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Danh sách ID không được để trống.");
        }
        
        try {
            String ownerId = principal.getName();
            // Giả định ConversationService có method mới: deleteConversationsByIds
            conversationService.deleteConversations(conversationIds, ownerId);
            return ResponseEntity.noContent().build();
        } catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không được phép xóa một số hội thoại.", e);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi khi xóa hàng loạt hội thoại.", e);
        }
    }
}