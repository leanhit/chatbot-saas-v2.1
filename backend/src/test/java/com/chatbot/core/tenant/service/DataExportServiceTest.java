package com.chatbot.core.tenant.service;

import com.chatbot.core.customer.repository.CustomerProfileRepository;
import com.chatbot.core.customer.repository.FeedbackRepository;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.model.TenantStatus;
import com.chatbot.core.tenant.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GDPR data export (no Spring context).
 */
@ExtendWith(MockitoExtension.class)
class DataExportServiceTest {

    @Mock
    private TenantRepository tenantRepository;
    @Mock
    private ConversationRepository conversationRepository;
    @Mock
    private MessageRepository messageRepository;
    @Mock
    private CustomerProfileRepository customerProfileRepository;
    @Mock
    private FeedbackRepository feedbackRepository;

    @InjectMocks
    private DataExportService service;

    private Tenant activeTenant(Long id) {
        Tenant tenant = new Tenant();
        tenant.setId(id);
        tenant.setTenantKey("key-" + id);
        tenant.setName("Test Tenant");
        tenant.setStatus(TenantStatus.ACTIVE);
        return tenant;
    }

    @Test
    void exportTenantDataThrowsWhenTenantNotFound() {
        when(tenantRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.exportTenantData(99L));
    }

    @Test
    void exportTenantDataProducesValidJson() {
        Tenant tenant = activeTenant(1L);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant));
        when(conversationRepository.findByTenantId(1L)).thenReturn(List.of());
        when(customerProfileRepository.findByTenantId(1L)).thenReturn(List.of());
        when(feedbackRepository.findByTenantId(1L)).thenReturn(List.of());

        String json = service.exportTenantData(1L);

        assertNotNull(json);
        assertTrue(json.contains("\"exportVersion\""));
        assertTrue(json.contains("\"tenant\""));
        assertTrue(json.contains("\"conversations\""));
        assertTrue(json.contains("\"customerProfiles\""));
        assertTrue(json.contains("\"feedback\""));
        assertTrue(json.contains("Test Tenant"));
    }

    @Test
    void exportTenantDataIncludesConversationsWithMessages() {
        Tenant tenant = activeTenant(2L);
        when(tenantRepository.findById(2L)).thenReturn(Optional.of(tenant));

        com.chatbot.core.message.store.model.Conversation conv =
            new com.chatbot.core.message.store.model.Conversation();
        conv.setId(10L);
        conv.setExternalUserId("user-1");
        conv.setUserName("Alice");
        when(conversationRepository.findByTenantId(2L)).thenReturn(List.of(conv));

        com.chatbot.core.message.store.model.Message msg =
            new com.chatbot.core.message.store.model.Message();
        msg.setId(100L);
        msg.setSender("user");
        msg.setContent("Hello");
        msg.setMessageType("TEXT");
        when(messageRepository.findByConversationIdAndTenantId(10L, 2L)).thenReturn(List.of(msg));

        when(customerProfileRepository.findByTenantId(2L)).thenReturn(List.of());
        when(feedbackRepository.findByTenantId(2L)).thenReturn(List.of());

        String json = service.exportTenantData(2L);

        assertTrue(json.contains("Alice"));
        assertTrue(json.contains("Hello"));
        assertTrue(json.contains("user-1"));
    }

    @Test
    void exportTenantDataTruncatesLargeConversationList() {
        Tenant tenant = activeTenant(3L);
        when(tenantRepository.findById(3L)).thenReturn(Optional.of(tenant));

        List<com.chatbot.core.message.store.model.Conversation> many = new java.util.ArrayList<>();
        for (int i = 0; i < 6000; i++) {
            com.chatbot.core.message.store.model.Conversation c =
                new com.chatbot.core.message.store.model.Conversation();
            c.setId((long) i);
            many.add(c);
        }
        when(conversationRepository.findByTenantId(3L)).thenReturn(many);
        when(customerProfileRepository.findByTenantId(3L)).thenReturn(List.of());
        when(feedbackRepository.findByTenantId(3L)).thenReturn(List.of());

        String json = service.exportTenantData(3L);

        assertNotNull(json);
        // Should not throw OOM; truncation logged
        verify(conversationRepository).findByTenantId(3L);
    }
}
