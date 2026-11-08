package com.chatbot.core.tenant.service;

import com.chatbot.core.membership.repository.TenantInvitationRepository;
import com.chatbot.core.membership.repository.TenantJoinRequestRepository;
import com.chatbot.core.membership.repository.TenantMemberRepository;
import com.chatbot.core.message.store.repository.AgentRepository;
import com.chatbot.core.message.store.repository.AutoAssignConfigRepository;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.core.message.store.repository.RoutingRuleRepository;
import com.chatbot.core.message.store.repository.SLAConfigurationRepository;
import com.chatbot.core.payment.invoice.repository.InvoiceRepository;
import com.chatbot.core.payment.transaction.repository.SimplePaymentRepository;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.profile.repository.TenantProfileRepository;
import com.chatbot.core.tenant.professional.repository.TenantProfessionalRepository;
import com.chatbot.core.tenant.repository.TenantAuditLogRepository;
import com.chatbot.core.tenant.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantCleanupServiceTest {

    @Mock private TenantMemberRepository tenantMemberRepository;
    @Mock private TenantProfileRepository tenantProfileRepository;
    @Mock private TenantProfessionalRepository tenantProfessionalRepository;
    @Mock private TenantInvitationRepository tenantInvitationRepository;
    @Mock private TenantJoinRequestRepository tenantJoinRequestRepository;
    @Mock private TenantAuditLogRepository tenantAuditLogRepository;
    @Mock private TenantRepository tenantRepository;
    @Mock private ConversationRepository conversationRepository;
    @Mock private MessageRepository messageRepository;
    @Mock private AgentRepository agentRepository;
    @Mock private RoutingRuleRepository routingRuleRepository;
    @Mock private SLAConfigurationRepository slaConfigurationRepository;
    @Mock private AutoAssignConfigRepository autoAssignConfigRepository;
    @Mock private SimplePaymentRepository simplePaymentRepository;
    @Mock private InvoiceRepository invoiceRepository;

    @InjectMocks
    private TenantCleanupService service;

    @Test
    void cleanupTenantDataDeletesFromAllRepositories() {
        service.cleanupTenantData(1L);

        verify(messageRepository).deleteByTenantId(1L);
        verify(conversationRepository).deleteByTenantId(1L);
        verify(agentRepository).deleteByTenantId(1L);
        verify(routingRuleRepository).deleteByTenantId(1L);
        verify(slaConfigurationRepository).deleteByTenantId(1L);
        verify(autoAssignConfigRepository).deleteByTenantId(1L);
        verify(simplePaymentRepository).deleteByTenantId(1L);
        verify(invoiceRepository).deleteByTenantId(1L);
        verify(tenantMemberRepository).deleteByTenantId(1L);
        verify(tenantInvitationRepository).deleteByTenantId(1L);
        verify(tenantJoinRequestRepository).deleteByTenantId(1L);
        verify(tenantProfileRepository).deleteByTenantId(1L);
        verify(tenantProfessionalRepository).deleteByTenantId(1L);
        verify(tenantAuditLogRepository).deleteByTenantId(1L);
    }

    @Test
    void cleanupContinuesWhenOneRepositoryFails() {
        doThrow(new RuntimeException("db error")).when(messageRepository).deleteByTenantId(1L);

        // must not throw — each cleanup step swallows its own errors
        assertDoesNotThrow(() -> service.cleanupTenantData(1L));

        // subsequent cleanups still ran
        verify(conversationRepository).deleteByTenantId(1L);
        verify(tenantAuditLogRepository).deleteByTenantId(1L);
    }

    @Test
    void scheduledCleanupDoesNothingWhenNoDeletedTenants() {
        when(tenantRepository.findDeletedTenantsOlderThan(any())).thenReturn(List.of());

        service.scheduledCleanup();

        verify(messageRepository, never()).deleteByTenantId(any());
        verify(tenantMemberRepository, never()).deleteByTenantId(any());
    }

    @Test
    void scheduledCleanupProcessesEachDeletedTenant() {
        Tenant t1 = new Tenant(); t1.setId(10L);
        Tenant t2 = new Tenant(); t2.setId(20L);
        when(tenantRepository.findDeletedTenantsOlderThan(any())).thenReturn(List.of(t1, t2));

        service.scheduledCleanup();

        verify(messageRepository).deleteByTenantId(10L);
        verify(messageRepository).deleteByTenantId(20L);
        verify(tenantAuditLogRepository).deleteByTenantId(10L);
        verify(tenantAuditLogRepository).deleteByTenantId(20L);
    }

    @Test
    void scheduledCleanupContinuesWhenTenantCleanupFails() {
        Tenant t1 = new Tenant(); t1.setId(10L);
        Tenant t2 = new Tenant(); t2.setId(20L);
        when(tenantRepository.findDeletedTenantsOlderThan(any())).thenReturn(List.of(t1, t2));
        // make every repo throw for tenant 10 only — simulate via messageRepository
        doThrow(new RuntimeException("fail")).when(messageRepository).deleteByTenantId(10L);

        assertDoesNotThrow(() -> service.scheduledCleanup());

        // tenant 20 still processed
        verify(messageRepository).deleteByTenantId(20L);
    }

    @Test
    void scheduledCleanupPropagatesRepositoryLookupFailure() {
        when(tenantRepository.findDeletedTenantsOlderThan(any()))
            .thenThrow(new RuntimeException("query failed"));

        assertThrows(RuntimeException.class, () -> service.scheduledCleanup());
    }
}
