package com.chatbot.core.message.store.controller;

import com.chatbot.core.message.store.dto.ConversationDTO;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.Channel;
import com.chatbot.core.message.store.mapper.ConversationMapper;
import com.chatbot.core.message.store.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
// import io.swagger.v3.oas.annotations.responses.ApiResponse; // Use fully qualified name to avoid conflict
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Tag(name = "Conversation Management", description = "Conversation and messaging operations")
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
    @Operation(
        summary = "Get conversations by owner and connection",
        description = "Retrieve conversations for a specific owner and connection",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conversations retrieved successfully",
                content = @Content(schema = @Schema(implementation = Page.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
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
    @Operation(
        summary = "Get conversations by owner",
        description = "Retrieve all conversations for a specific owner",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conversations retrieved successfully",
                content = @Content(schema = @Schema(implementation = Page.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
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
    @Operation(
        summary = "Close conversation",
        description = "Close an active conversation",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conversation closed successfully",
                content = @Content(schema = @Schema(implementation = ConversationDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversation not found")
        }
    )
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
    @Operation(
        summary = "Find or create conversation",
        description = "Find existing conversation or create new one for user",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conversation found or created successfully",
                content = @Content(schema = @Schema(implementation = ConversationDTO.class)))
        }
    )
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
    @Operation(
        summary = "Takeover conversation",
        description = "Agent takes over control of conversation from bot",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conversation taken over successfully",
                content = @Content(schema = @Schema(implementation = ConversationDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot takeover conversation"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversation not found")
        }
    )
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
    @Operation(
        summary = "Release conversation",
        description = "Agent releases conversation back to bot",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Conversation released successfully",
                content = @Content(schema = @Schema(implementation = ConversationDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Cannot release conversation"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversation not found")
        }
    )
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
    @Operation(
        summary = "Update taken over status",
        description = "Update the taken over status of a conversation",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Status updated successfully",
                content = @Content(schema = @Schema(implementation = ConversationDTO.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Permission denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversation not found")
        }
    )
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
    @Operation(
        summary = "Delete conversation",
        description = "Permanently delete a conversation",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Conversation deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Permission denied"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Conversation not found")
        }
    )
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
    @Operation(
        summary = "Batch delete conversations",
        description = "Delete multiple conversations at once",
        responses = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Conversations deleted successfully"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid conversation IDs list"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Permission denied")
        }
    )
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