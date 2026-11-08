package com.chatbot.core.tenant.controller;

import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.tenant.model.TenantAuditLog;
import com.chatbot.core.tenant.repository.TenantAuditLogRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogControllerTest {

    @Mock private TenantAuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogController controller;

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    private TenantAuditLog log(Long id, String action) {
        return TenantAuditLog.builder()
            .id(id).tenantId(1L).userEmail("admin@test.com")
            .action(action).details("detail")
            .createdAt(LocalDateTime.now()).build();
    }

    @Test
    @SuppressWarnings("unchecked")
    void returnsBadRequestWhenNoTenantContext() {
        TenantContext.clear();

        ResponseEntity<?> response = controller.getAuditLogs(0, 20, null);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        verifyNoInteractions(auditLogRepository);
    }

    @Test
    @SuppressWarnings("unchecked")
    void returnsPagedLogsForTenant() {
        TenantContext.setTenantId(1L);
        when(auditLogRepository.findByTenantIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(log(1L, "ACTION_A"), log(2L, "ACTION_B"))));

        ResponseEntity<?> response = controller.getAuditLogs(0, 20, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(2L, body.get("totalElements"));
        assertEquals(1, body.get("totalPages"));
        verify(auditLogRepository).findByTenantIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class));
        verify(auditLogRepository, never())
            .findByTenantIdAndActionContainingIgnoreCaseOrderByCreatedAtDesc(any(), any(), any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void appliesActionFilter() {
        TenantContext.setTenantId(1L);
        when(auditLogRepository.findByTenantIdAndActionContainingIgnoreCaseOrderByCreatedAtDesc(
                eq(1L), eq("GDPR"), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(log(1L, "GDPR_DELETION_REQUESTED"))));

        ResponseEntity<?> response = controller.getAuditLogs(0, 20, "GDPR");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertEquals(1L, body.get("totalElements"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void capsPageSizeAt100() {
        TenantContext.setTenantId(1L);
        when(auditLogRepository.findByTenantIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of()));

        controller.getAuditLogs(0, 500, null);

        verify(auditLogRepository).findByTenantIdOrderByCreatedAtDesc(
            eq(1L), argThat(p -> p.getPageSize() == 100));
    }
}
