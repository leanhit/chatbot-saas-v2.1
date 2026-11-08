package com.chatbot.core.tenant.service;

import com.chatbot.core.tenant.model.TenantAuditLog;
import com.chatbot.core.tenant.repository.TenantAuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantAuditLogServiceTest {

    @Mock private TenantAuditLogRepository auditLogRepository;

    @InjectMocks
    private TenantAuditLogService service;

    @Test
    void logActionPersistsEntry() {
        when(auditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.logAction(1L, "admin@test.com", "CREATE_TENANT", "created tenant X");

        ArgumentCaptor<TenantAuditLog> captor = ArgumentCaptor.forClass(TenantAuditLog.class);
        verify(auditLogRepository).save(captor.capture());
        TenantAuditLog saved = captor.getValue();
        assertEquals(1L, saved.getTenantId());
        assertEquals("admin@test.com", saved.getUserEmail());
        assertEquals("CREATE_TENANT", saved.getAction());
        assertEquals("created tenant X", saved.getDetails());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void logActionSwallowsPersistenceErrors() {
        when(auditLogRepository.save(any())).thenThrow(new RuntimeException("db down"));

        // must not throw — audit log never fails main flow
        assertDoesNotThrow(() -> service.logAction(1L, "a@b.c", "ACTION", "details"));
    }

    @Test
    void logActionAsyncDelegatesToSync() {
        when(auditLogRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.logActionAsync(2L, "u@t.com", "UPDATE", "x");

        verify(auditLogRepository).save(any(TenantAuditLog.class));
    }
}
