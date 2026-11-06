package com.chatbot.core.tenant.service;

import com.chatbot.core.customer.model.CustomerProfile;
import com.chatbot.core.customer.model.Feedback;
import com.chatbot.core.customer.repository.CustomerProfileRepository;
import com.chatbot.core.customer.repository.FeedbackRepository;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.Message;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GDPR data portability export (Phase 6).
 *
 * Exports all tenant-owned data (conversations, messages, customer profiles,
 * feedback) as a single JSON document. Read-only; never modifies data.
 * Admin-only access enforced at controller level.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DataExportService {

    /** Safety caps to avoid unbounded memory usage on large tenants. */
    private static final int MAX_CONVERSATIONS = 5000;
    private static final int MAX_MESSAGES_PER_CONVERSATION = 1000;
    private static final int MAX_CUSTOMER_PROFILES = 10000;
    private static final int MAX_FEEDBACK = 10000;

    private final TenantRepository tenantRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final FeedbackRepository feedbackRepository;

    /**
     * Export all data for a tenant as a JSON string.
     *
     * @param tenantId tenant to export
     * @return JSON document with tenant, conversations, messages, customers, feedback
     * @throws IllegalArgumentException if tenant not found
     */
    public String exportTenantData(Long tenantId) {
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + tenantId));

        log.info("[DataExport] Starting export for tenant {} ({})", tenantId, tenant.getName());

        Map<String, Object> export = new HashMap<>();
        export.put("exportVersion", "1.0");
        export.put("exportedAt", LocalDateTime.now().toString());
        export.put("tenant", Map.of(
            "id", tenant.getId(),
            "tenantKey", tenant.getTenantKey() != null ? tenant.getTenantKey() : "",
            "name", tenant.getName() != null ? tenant.getName() : "",
            "status", tenant.getStatus() != null ? tenant.getStatus().name() : "",
            "createdAt", tenant.getCreatedAt() != null ? tenant.getCreatedAt().toString() : ""
        ));

        export.put("conversations", exportConversations(tenantId));
        export.put("customerProfiles", exportCustomerProfiles(tenantId));
        export.put("feedback", exportFeedback(tenantId));

        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(export);
            log.info("[DataExport] Export completed for tenant {}", tenantId);
            return json;
        } catch (Exception e) {
            log.error("[DataExport] Failed to serialize export for tenant {}: {}", tenantId, e.getMessage(), e);
            throw new RuntimeException("Failed to serialize tenant export", e);
        }
    }

    private List<Map<String, Object>> exportConversations(Long tenantId) {
        List<Conversation> conversations = conversationRepository.findByTenantId(tenantId);
        if (conversations.size() > MAX_CONVERSATIONS) {
            log.warn("[DataExport] Tenant {} has {} conversations, truncating to {}", tenantId, conversations.size(), MAX_CONVERSATIONS);
            conversations = conversations.subList(0, MAX_CONVERSATIONS);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Conversation c : conversations) {
            Map<String, Object> convMap = new HashMap<>();
            convMap.put("id", c.getId());
            convMap.put("externalUserId", c.getExternalUserId());
            convMap.put("userName", c.getUserName());
            convMap.put("status", c.getStatus() != null ? c.getStatus().name() : null);
            convMap.put("channel", c.getChannel() != null ? c.getChannel().name() : null);
            convMap.put("agentAssignedId", c.getAgentAssignedId());
            convMap.put("createdAt", c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);
            convMap.put("updatedAt", c.getUpdatedAt() != null ? c.getUpdatedAt().toString() : null);
            convMap.put("resolutionTime", c.getResolutionTime() != null ? c.getResolutionTime().toString() : null);
            convMap.put("messages", exportMessages(tenantId, c.getId()));
            result.add(convMap);
        }
        return result;
    }

    private List<Map<String, Object>> exportMessages(Long tenantId, Long conversationId) {
        List<Message> messages = messageRepository.findByConversationIdAndTenantId(conversationId, tenantId);
        if (messages.size() > MAX_MESSAGES_PER_CONVERSATION) {
            messages = messages.subList(messages.size() - MAX_MESSAGES_PER_CONVERSATION, messages.size());
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Message m : messages) {
            Map<String, Object> msgMap = new HashMap<>();
            msgMap.put("id", m.getId());
            msgMap.put("sender", m.getSender());
            msgMap.put("content", m.getContent());
            msgMap.put("messageType", m.getMessageType());
            msgMap.put("createdAt", m.getCreatedAt() != null ? m.getCreatedAt().toString() : null);
            result.add(msgMap);
        }
        return result;
    }

    private List<Map<String, Object>> exportCustomerProfiles(Long tenantId) {
        List<CustomerProfile> profiles = customerProfileRepository.findByTenantId(tenantId);
        if (profiles.size() > MAX_CUSTOMER_PROFILES) {
            log.warn("[DataExport] Tenant {} has {} customer profiles, truncating to {}", tenantId, profiles.size(), MAX_CUSTOMER_PROFILES);
            profiles = profiles.subList(0, MAX_CUSTOMER_PROFILES);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (CustomerProfile p : profiles) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("email", p.getEmail());
            map.put("phone", p.getPhone());
            map.put("status", p.getStatus() != null ? p.getStatus().name() : null);
            map.put("tier", p.getTier() != null ? p.getTier().name() : null);
            map.put("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().toString() : null);
            result.add(map);
        }
        return result;
    }

    private List<Map<String, Object>> exportFeedback(Long tenantId) {
        List<Feedback> feedbackList = feedbackRepository.findByTenantId(tenantId);
        if (feedbackList.size() > MAX_FEEDBACK) {
            feedbackList = feedbackList.subList(0, MAX_FEEDBACK);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Feedback f : feedbackList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", f.getId());
            map.put("customerProfileId", f.getCustomerProfileId());
            map.put("conversationId", f.getConversationId());
            map.put("type", f.getType() != null ? f.getType().name() : null);
            map.put("source", f.getSource() != null ? f.getSource().name() : null);
            map.put("rating", f.getRating());
            map.put("comment", f.getComment());
            map.put("category", f.getCategory());
            map.put("submittedAt", f.getSubmittedAt() != null ? f.getSubmittedAt().toString() : null);
            result.add(map);
        }
        return result;
    }
}
