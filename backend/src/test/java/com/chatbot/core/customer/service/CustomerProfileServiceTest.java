package com.chatbot.core.customer.service;

import com.chatbot.core.customer.dto.CreateCustomerProfileRequest;
import com.chatbot.core.customer.dto.UpdateCustomerProfileRequest;
import com.chatbot.core.customer.model.CustomerProfile;
import com.chatbot.core.customer.repository.CustomerProfileRepository;
import com.chatbot.core.customer.repository.CustomerNoteRepository;
import com.chatbot.core.customer.repository.FeedbackRepository;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.core.tenant.service.TenantAuditLogService;
import com.chatbot.core.tenant.service.TenantPermissionValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerProfileServiceTest {

    @Mock private CustomerProfileRepository customerProfileRepository;
    @Mock private FeedbackRepository feedbackRepository;
    @Mock private CustomerNoteRepository customerNoteRepository;
    @Mock private TenantAuditLogService auditLogService;
    @Mock private TenantPermissionValidator permissionValidator;
    @Mock private ConversationRepository conversationRepository;
    @Mock private MessageRepository messageRepository;

    @InjectMocks
    private CustomerProfileService service;

    private CustomerProfile profile(Long id, Long tenantId) {
        CustomerProfile p = new CustomerProfile();
        p.setId(id);
        p.setTenantId(tenantId);
        p.setExternalId("ext-" + id);
        p.setName("Customer " + id);
        p.setStatus(CustomerProfile.CustomerStatus.ACTIVE);
        p.setTier(CustomerProfile.CustomerTier.FREE);
        p.setTotalConversations(0);
        p.setTotalMessages(0);
        p.setIsBlocked(false);
        return p;
    }

    private CreateCustomerProfileRequest createRequest(String externalId) {
        CreateCustomerProfileRequest r = new CreateCustomerProfileRequest();
        r.setExternalId(externalId);
        r.setName("New Customer");
        return r;
    }

    // ---------- create ----------

    @Test
    void createProfileAppliesDefaults() {
        when(customerProfileRepository.existsByExternalIdAndTenantId("ext-1", 1L)).thenReturn(false);
        when(customerProfileRepository.save(any())).thenAnswer(inv -> {
            CustomerProfile p = inv.getArgument(0);
            p.setId(1L);
            return p;
        });

        var dto = service.createCustomerProfile(createRequest("ext-1"), 1L);

        assertEquals("ext-1", dto.getExternalId());
        assertEquals("ACTIVE", dto.getStatus());
        assertEquals("FREE", dto.getTier());
        assertEquals(0, dto.getTotalConversations());
        assertEquals(Boolean.FALSE, dto.getIsBlocked());
    }

    @Test
    void createProfileRejectsDuplicateExternalId() {
        when(customerProfileRepository.existsByExternalIdAndTenantId("ext-1", 1L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
            () -> service.createCustomerProfile(createRequest("ext-1"), 1L));
        verify(customerProfileRepository, never()).save(any());
    }

    // ---------- get ----------

    @Test
    void getByIdReturnsDto() {
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(profile(1L, 1L)));
        assertEquals("ext-1", service.getCustomerProfileById(1L, 1L).getExternalId());
    }

    @Test
    void getByIdRejectsOtherTenant() {
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(profile(1L, 2L)));
        assertThrows(IllegalArgumentException.class, () -> service.getCustomerProfileById(1L, 1L));
    }

    @Test
    void getByIdNotFound() {
        when(customerProfileRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.getCustomerProfileById(99L, 1L));
    }

    @Test
    void getByExternalIdDelegates() {
        when(customerProfileRepository.findByExternalIdAndTenantId("ext-1", 1L))
            .thenReturn(Optional.of(profile(1L, 1L)));
        assertEquals(1L, service.getCustomerProfileByExternalId("ext-1", 1L).getId());
    }

    @Test
    void getAllAndFiltersDelegate() {
        when(customerProfileRepository.findByTenantId(1L)).thenReturn(List.of(profile(1L, 1L)));
        when(customerProfileRepository.findByTenantIdAndStatus(1L, CustomerProfile.CustomerStatus.ACTIVE))
            .thenReturn(List.of(profile(1L, 1L)));
        when(customerProfileRepository.findByTenantIdAndTier(1L, CustomerProfile.CustomerTier.PREMIUM))
            .thenReturn(List.of());

        assertEquals(1, service.getAllCustomerProfiles(1L).size());
        assertEquals(1, service.getCustomerProfilesByStatus(1L, "ACTIVE").size());
        assertTrue(service.getCustomerProfilesByTier(1L, "PREMIUM").isEmpty());
    }

    // ---------- update ----------

    @Test
    void updateAppliesOnlyProvidedFields() {
        CustomerProfile p = profile(1L, 1L);
        p.setEmail("old@test.com");
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(p));
        when(customerProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateCustomerProfileRequest req = new UpdateCustomerProfileRequest();
        req.setName("Updated Name");

        var dto = service.updateCustomerProfile(1L, req, 1L);

        assertEquals("Updated Name", dto.getName());
        assertEquals("old@test.com", dto.getEmail()); // untouched fields preserved
        assertEquals("ACTIVE", dto.getStatus()); // status unchanged
    }

    @Test
    void updateBlockSetsBlockedState() {
        CustomerProfile p = profile(1L, 1L);
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(p));
        when(customerProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateCustomerProfileRequest req = new UpdateCustomerProfileRequest();
        req.setIsBlocked(true);
        req.setBlockedReason("spam");

        var dto = service.updateCustomerProfile(1L, req, 1L);

        assertEquals(Boolean.TRUE, dto.getIsBlocked());
        assertEquals("spam", dto.getBlockedReason());
        assertNotNull(dto.getBlockedAt());
        assertEquals("BLOCKED", dto.getStatus());
    }

    @Test
    void updateUnblockRestoresActive() {
        CustomerProfile p = profile(1L, 1L);
        p.setIsBlocked(true);
        p.setStatus(CustomerProfile.CustomerStatus.BLOCKED);
        p.setBlockedReason("spam");
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(p));
        when(customerProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateCustomerProfileRequest req = new UpdateCustomerProfileRequest();
        req.setIsBlocked(false);

        var dto = service.updateCustomerProfile(1L, req, 1L);

        assertEquals(Boolean.FALSE, dto.getIsBlocked());
        assertNull(dto.getBlockedReason());
        assertNull(dto.getBlockedAt());
        assertEquals("ACTIVE", dto.getStatus());
    }

    @Test
    void updateRejectsOtherTenant() {
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(profile(1L, 2L)));
        assertThrows(IllegalArgumentException.class,
            () -> service.updateCustomerProfile(1L, new UpdateCustomerProfileRequest(), 1L));
    }

    // ---------- delete ----------

    @Test
    void deleteRemovesProfile() {
        CustomerProfile p = profile(1L, 1L);
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(p));

        service.deleteCustomerProfile(1L, 1L);

        verify(customerProfileRepository).delete(p);
    }

    @Test
    void deleteRejectsOtherTenant() {
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(profile(1L, 2L)));
        assertThrows(IllegalArgumentException.class,
            () -> service.deleteCustomerProfile(1L, 1L));
        verify(customerProfileRepository, never()).delete(any());
    }

    // ---------- updateConversationStats ----------

    @Test
    void statsIncrementCounters() {
        CustomerProfile p = profile(1L, 1L);
        p.setTotalConversations(4);
        p.setTotalMessages(40);
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(p));
        when(customerProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateConversationStats(1L, 1L, 10, null);

        assertEquals(5, p.getTotalConversations());
        assertEquals(50, p.getTotalMessages());
        assertNotNull(p.getLastContactAt());
    }

    @Test
    void statsComputesRollingAverageResponseTime() {
        CustomerProfile p = profile(1L, 1L);
        p.setTotalConversations(2);
        p.setAvgResponseTimeSeconds(100); // avg of previous 2
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(p));
        when(customerProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateConversationStats(1L, 1L, null, 400);

        // new totalConversations = 3; avg = (100*2 + 400)/3 = 200
        assertEquals(3, p.getTotalConversations());
        assertEquals(200, p.getAvgResponseTimeSeconds());
    }

    @Test
    void statsNullMessageCountTreatedAsZero() {
        CustomerProfile p = profile(1L, 1L);
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(p));
        when(customerProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.updateConversationStats(1L, 1L, null, null);

        assertEquals(1, p.getTotalConversations());
        assertEquals(0, p.getTotalMessages());
        assertNull(p.getAvgResponseTimeSeconds());
    }

    @Test
    void statsRejectsOtherTenant() {
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(profile(1L, 2L)));
        assertThrows(IllegalArgumentException.class,
            () -> service.updateConversationStats(1L, 1L, 1, null));
    }
}
