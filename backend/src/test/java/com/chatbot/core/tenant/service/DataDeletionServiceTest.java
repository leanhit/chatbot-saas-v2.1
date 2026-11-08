package com.chatbot.core.tenant.service;

import com.chatbot.core.customer.model.CustomerProfile;
import com.chatbot.core.customer.repository.CustomerProfileRepository;
import com.chatbot.core.customer.repository.FeedbackRepository;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.core.tenant.model.DataDeletionRequest;
import com.chatbot.core.tenant.repository.DataDeletionRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataDeletionServiceTest {

    @Mock private DataDeletionRequestRepository requestRepository;
    @Mock private CustomerProfileRepository customerProfileRepository;
    @Mock private FeedbackRepository feedbackRepository;
    @Mock private ConversationRepository conversationRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private TenantAuditLogService auditLogService;

    @InjectMocks
    private DataDeletionService service;

    private DataDeletionRequest request(Long id, DataDeletionRequest.Status status) {
        return DataDeletionRequest.builder()
            .id(id).tenantId(1L).tenantKey("tk").requestedBy("admin@test.com")
            .status(status).scheduledAt(LocalDateTime.now().plusDays(30))
            .build();
    }

    @Test
    void createRequestSetsPendingWithGracePeriod() {
        when(requestRepository.save(any())).thenAnswer(inv -> {
            DataDeletionRequest r = inv.getArgument(0);
            r.setId(1L);
            return r;
        });

        DataDeletionRequest saved = service.createRequest(1L, "tk", "admin@test.com", "GDPR request");

        assertEquals(DataDeletionRequest.Status.PENDING, saved.getStatus());
        assertTrue(saved.getScheduledAt().isAfter(LocalDateTime.now().plusDays(29)));
        verify(auditLogService).logAction(eq(1L), eq("admin@test.com"), eq("GDPR_DELETION_REQUESTED"), anyString());
    }

    @Test
    void cancelRequestOnlyAllowsPending() {
        DataDeletionRequest completed = request(1L, DataDeletionRequest.Status.COMPLETED);
        when(requestRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(completed));

        assertThrows(IllegalStateException.class,
            () -> service.cancelRequest(1L, 1L, "admin@test.com"));
    }

    @Test
    void cancelPendingRequestMarksCancelled() {
        DataDeletionRequest pending = request(1L, DataDeletionRequest.Status.PENDING);
        when(requestRepository.findByIdAndTenantId(1L, 1L)).thenReturn(Optional.of(pending));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DataDeletionRequest result = service.cancelRequest(1L, 1L, "admin@test.com");

        assertEquals(DataDeletionRequest.Status.CANCELLED, result.getStatus());
        assertEquals("admin@test.com", result.getProcessedBy());
        assertNotNull(result.getProcessedAt());
    }

    @Test
    void cancelRequestRejectsOtherTenant() {
        when(requestRepository.findByIdAndTenantId(1L, 2L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class,
            () -> service.cancelRequest(1L, 2L, "admin@test.com"));
    }

    @Test
    void processRequestAnonymizesAndDeletes() {
        DataDeletionRequest pending = request(1L, DataDeletionRequest.Status.PENDING);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(pending));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CustomerProfile p = new CustomerProfile();
        p.setId(10L);
        p.setName("Real Name");
        p.setEmail("real@test.com");
        p.setPhone("0901234567");
        p.setAvatarUrl("http://avatar");
        when(customerProfileRepository.findByTenantId(1L)).thenReturn(List.of(p));
        when(customerProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(feedbackRepository.deleteByTenantId(1L)).thenReturn(5);
        when(messageRepository.deleteByTenantId(1L)).thenReturn(100);
        when(conversationRepository.deleteByTenantId(1L)).thenReturn(20);

        DataDeletionRequest result = service.processRequest(1L, "admin@test.com");

        assertEquals(DataDeletionRequest.Status.COMPLETED, result.getStatus());
        assertEquals("[deleted]", p.getName());
        assertEquals("deleted-10@anonymized.local", p.getEmail());
        assertNull(p.getPhone());
        assertNull(p.getAvatarUrl());
        assertTrue(result.getResultSummary().contains("anonymized_profiles=1"));
        assertTrue(result.getResultSummary().contains("deleted_messages=100"));
        verify(auditLogService).logAction(eq(1L), eq("admin@test.com"), eq("GDPR_DELETION_COMPLETED"), anyString());
    }

    @Test
    void processRequestRejectsAlreadyCompleted() {
        DataDeletionRequest done = request(1L, DataDeletionRequest.Status.COMPLETED);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(done));

        assertThrows(IllegalStateException.class,
            () -> service.processRequest(1L, "admin@test.com"));
    }

    @Test
    void processRequestMarksFailedOnError() {
        DataDeletionRequest pending = request(1L, DataDeletionRequest.Status.PENDING);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(pending));
        when(requestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(customerProfileRepository.findByTenantId(1L)).thenThrow(new RuntimeException("db down"));

        DataDeletionRequest result = service.processRequest(1L, "admin@test.com");

        assertEquals(DataDeletionRequest.Status.FAILED, result.getStatus());
        assertTrue(result.getResultSummary().contains("FAILED"));
    }
}
