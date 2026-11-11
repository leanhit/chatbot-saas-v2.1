package com.chatbot.core.tenant.controller;

import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.ConversationStatus;
import com.chatbot.core.message.store.model.Message;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Public tenant API surface authenticated by X-API-Key (TenantApiKeyAuthFilter).
 * TenantContext is set by the filter; all queries are tenant-scoped.
 */
@RestController
@RequestMapping("/api/tenant-api")
@RequiredArgsConstructor
public class TenantPublicApiController {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    @GetMapping("/conversations")
    public ResponseEntity<?> listConversations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        Long tenantId = requireTenant();
        PageRequest pageable = PageRequest.of(page, Math.min(size, 100),
                Sort.by(Sort.Direction.DESC, "updatedAt"));
        ConversationStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = ConversationStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid status: " + status));
            }
        }
        Page<Conversation> result = (statusEnum != null)
                ? conversationRepository.findByTenantIdAndStatus(tenantId, statusEnum, pageable)
                : conversationRepository.findAllByTenantIdOrderByUpdatedAtDesc(tenantId, pageable);

        List<Map<String, Object>> items = result.getContent().stream().map(this::toConversationDto).collect(Collectors.toList());
        Map<String, Object> body = new HashMap<>();
        body.put("content", items);
        body.put("totalElements", result.getTotalElements());
        body.put("page", result.getNumber());
        body.put("size", result.getSize());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<?> getConversation(@PathVariable Long id) {
        Long tenantId = requireTenant();
        return conversationRepository.findByIdAndTenantId(id, tenantId)
                .map(c -> ResponseEntity.ok(toConversationDto(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<?> getMessages(@PathVariable Long id,
                                         @RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "50") int size) {
        Long tenantId = requireTenant();
        // Verify conversation belongs to tenant first
        if (conversationRepository.findByIdAndTenantId(id, tenantId).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Page<Message> result = messageRepository.findByConversationIdAndTenantIdOrderByCreatedAtDesc(
                id, tenantId, PageRequest.of(page, Math.min(size, 200)));
        List<Map<String, Object>> items = result.getContent().stream().map(m -> {
            Map<String, Object> mm = new HashMap<>();
            mm.put("id", m.getId());
            mm.put("sender", m.getSender());
            mm.put("content", m.getContent());
            mm.put("createdAt", m.getCreatedAt());
            return mm;
        }).collect(Collectors.toList());
        Map<String, Object> body = new HashMap<>();
        body.put("content", items);
        body.put("totalElements", result.getTotalElements());
        body.put("page", result.getNumber());
        body.put("size", result.getSize());
        return ResponseEntity.ok(body);
    }

    private Long requireTenant() {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new IllegalStateException("Tenant context not set — valid X-API-Key required");
        }
        return tenantId;
    }

    private Map<String, Object> toConversationDto(Conversation c) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", c.getId());
        dto.put("status", c.getStatus());
        dto.put("channel", c.getChannel());
        dto.put("language", c.getLanguage());
        dto.put("externalUserId", c.getExternalUserId());
        dto.put("userName", c.getUserName());
        dto.put("agentAssignedId", c.getAgentAssignedId());
        dto.put("createdAt", c.getCreatedAt());
        dto.put("updatedAt", c.getUpdatedAt());
        dto.put("resolutionTime", c.getResolutionTime());
        return dto;
    }
}
