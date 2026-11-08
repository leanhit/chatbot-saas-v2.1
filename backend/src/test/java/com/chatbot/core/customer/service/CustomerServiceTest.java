package com.chatbot.core.customer.service;

import com.chatbot.core.customer.dto.CustomerDto;
import com.chatbot.core.customer.dto.CustomerStatsDto;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.spokes.facebook.user.model.FacebookUser;
import com.chatbot.spokes.facebook.user.repository.FacebookUserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock private FacebookUserRepository facebookUserRepository;

    @InjectMocks
    private CustomerService service;

    private final Pageable pageable = PageRequest.of(0, 10);

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    private FacebookUser fbUser(String psid, String name, Integer odooPartnerId) {
        FacebookUser u = new FacebookUser();
        u.setPsid(psid);
        u.setName(name);
        u.setProfilePic("https://pic/" + psid);
        u.setOdooPartnerId(odooPartnerId);
        u.setTenantId(1L);
        return u;
    }

    private <T> T withTenant(java.util.function.Supplier<T> fn) {
        return TenantContext.executeWithTenantId(1L, () -> fn.get());
    }

    @Test
    void getCustomersReturnsEmptyWhenNoTenant() {
        TenantContext.clear();
        Page<CustomerDto> result = service.getCustomers(pageable);
        assertTrue(result.isEmpty());
        verifyNoInteractions(facebookUserRepository);
    }

    @Test
    void getCustomersMapsUsersToDto() {
        FacebookUser mapped = fbUser("psid-1", "Alice", 100);
        FacebookUser unmapped = fbUser("psid-2", null, null);
        when(facebookUserRepository.findByTenantId(1L, pageable))
            .thenReturn(new PageImpl<>(List.of(mapped, unmapped), pageable, 2));

        Page<CustomerDto> result = withTenant(() -> service.getCustomers(pageable));

        assertEquals(2, result.getTotalElements());
        CustomerDto first = result.getContent().get(0);
        assertEquals("psid-1", first.getPsid());
        assertEquals("Alice", first.getDisplayName());
        assertEquals("COMPLETED", first.getStatus()); // has odooPartnerId
        CustomerDto second = result.getContent().get(1);
        assertEquals("PENDING", second.getStatus()); // no odooPartnerId
        assertEquals("Customer psid-", second.getDisplayName().substring(0, 14)); // fallback name
    }

    @Test
    void getCustomerByPsidReturnsDto() {
        when(facebookUserRepository.findByPsidAndTenantId("psid-1", 1L))
            .thenReturn(Optional.of(fbUser("psid-1", "Bob", null)));

        CustomerDto dto = withTenant(() -> service.getCustomerByPsid("psid-1"));

        assertEquals("Bob", dto.getDisplayName());
        assertEquals("PENDING", dto.getStatus());
    }

    @Test
    void getCustomerByPsidThrowsWhenMissing() {
        when(facebookUserRepository.findByPsidAndTenantId("nope", 1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
            () -> withTenant(() -> service.getCustomerByPsid("nope")));
    }

    @Test
    void getCustomerByPsidThrowsWhenNoTenant() {
        TenantContext.clear();
        assertThrows(RuntimeException.class, () -> service.getCustomerByPsid("psid-1"));
    }

    @Test
    void searchCustomersBlankKeywordFallsBackToAll() {
        when(facebookUserRepository.findByTenantId(1L, pageable))
            .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        Page<CustomerDto> result = withTenant(() -> service.searchCustomers("  ", pageable));

        assertTrue(result.isEmpty());
        verify(facebookUserRepository).findByTenantId(1L, pageable);
        verify(facebookUserRepository, never()).searchByTenantIdAndNameContaining(any(), any(), any());
    }

    @Test
    void searchCustomersWithKeyword() {
        when(facebookUserRepository.searchByTenantIdAndNameContaining(1L, "ali", pageable))
            .thenReturn(new PageImpl<>(List.of(fbUser("p1", "Alice", null)), pageable, 1));

        Page<CustomerDto> result = withTenant(() -> service.searchCustomers("  ali ", pageable));

        assertEquals(1, result.getTotalElements());
        verify(facebookUserRepository).searchByTenantIdAndNameContaining(1L, "ali", pageable);
    }

    @Test
    void getCustomersByStatusPendingUsesUnmappedQuery() {
        when(facebookUserRepository.findByPageIdAndTenantIdAndOdooPartnerIdIsNull(isNull(), eq(1L), eq(pageable)))
            .thenReturn(new PageImpl<>(List.of(fbUser("p1", "A", null)), pageable, 1));

        Page<CustomerDto> result = withTenant(() -> service.getCustomersByStatus("pending", pageable));

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getCustomersByStatusOtherFallsBackToAll() {
        when(facebookUserRepository.findByTenantId(1L, pageable))
            .thenReturn(new PageImpl<>(List.of(), pageable, 0));

        withTenant(() -> service.getCustomersByStatus("COMPLETED", pageable));

        verify(facebookUserRepository).findByTenantId(1L, pageable);
    }

    @Test
    void getCustomerStatsComputesCounts() {
        when(facebookUserRepository.countByTenantId(1L)).thenReturn(7L);

        CustomerStatsDto stats = withTenant(() -> service.getCustomerStats());

        assertEquals(7, stats.getTotalCustomers());
        assertEquals(7, stats.getPendingCustomers()); // completed hardcoded 0
        assertEquals(0, stats.getCompletedCustomers());
        assertEquals(0, stats.getTotalCapturedPhones());
    }

    @Test
    void getCustomerStatsZeroWhenNoTenant() {
        TenantContext.clear();
        CustomerStatsDto stats = service.getCustomerStats();
        assertEquals(0, stats.getTotalCustomers());
        verifyNoInteractions(facebookUserRepository);
    }

    @Test
    void getAvailableStatuses() {
        assertEquals(List.of("PENDING", "COMPLETED", "FAILED"), service.getAvailableStatuses());
    }
}
