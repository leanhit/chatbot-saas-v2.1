package com.chatbot.core.customer.service;

import com.chatbot.core.customer.model.CustomerProfile;
import com.chatbot.core.customer.repository.CustomerProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for customer segmentation rules (no Spring context).
 */
@ExtendWith(MockitoExtension.class)
class CustomerSegmentationServiceTest {

    @Mock
    private CustomerProfileRepository customerProfileRepository;

    @InjectMocks
    private CustomerSegmentationService service;

    private CustomerProfile profile(Long id, Long tenantId, int conversations, Double satisfaction, CustomerProfile.CustomerTier tier) {
        CustomerProfile p = new CustomerProfile();
        p.setId(id);
        p.setTenantId(tenantId);
        p.setTotalConversations(conversations);
        p.setSatisfactionScore(satisfaction);
        p.setTier(tier);
        return p;
    }

    @Test
    void freeTierForNewCustomer() {
        CustomerProfile p = profile(1L, 1L, 0, null, CustomerProfile.CustomerTier.FREE);
        assertEquals(CustomerProfile.CustomerTier.FREE, service.computeTier(p));
    }

    @Test
    void basicTierAtThreeConversations() {
        CustomerProfile p = profile(1L, 1L, 3, null, CustomerProfile.CustomerTier.FREE);
        assertEquals(CustomerProfile.CustomerTier.BASIC, service.computeTier(p));
    }

    @Test
    void premiumRequiresTenConversationsAndSatisfaction() {
        CustomerProfile p = profile(1L, 1L, 10, 4.5, CustomerProfile.CustomerTier.FREE);
        assertEquals(CustomerProfile.CustomerTier.PREMIUM, service.computeTier(p));
    }

    @Test
    void tenConversationsLowSatisfactionStaysBasic() {
        CustomerProfile p = profile(1L, 1L, 10, 2.0, CustomerProfile.CustomerTier.FREE);
        assertEquals(CustomerProfile.CustomerTier.BASIC, service.computeTier(p));
    }

    @Test
    void enterpriseAtFiftyConversations() {
        CustomerProfile p = profile(1L, 1L, 50, null, CustomerProfile.CustomerTier.FREE);
        assertEquals(CustomerProfile.CustomerTier.ENTERPRISE, service.computeTier(p));
    }

    @Test
    void recalculateUpgradesTier() {
        CustomerProfile p = profile(1L, 1L, 12, 4.8, CustomerProfile.CustomerTier.FREE);
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(p));
        when(customerProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CustomerProfile result = service.recalculateTier(1L, 1L, false);

        assertEquals(CustomerProfile.CustomerTier.PREMIUM, result.getTier());
    }

    @Test
    void recalculateNeverDowngradesWithoutForce() {
        CustomerProfile p = profile(1L, 1L, 1, null, CustomerProfile.CustomerTier.ENTERPRISE);
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(p));

        CustomerProfile result = service.recalculateTier(1L, 1L, false);

        assertEquals(CustomerProfile.CustomerTier.ENTERPRISE, result.getTier());
        verify(customerProfileRepository, never()).save(any());
    }

    @Test
    void forceRecalculateAppliesComputedTier() {
        CustomerProfile p = profile(1L, 1L, 1, null, CustomerProfile.CustomerTier.ENTERPRISE);
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(p));
        when(customerProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CustomerProfile result = service.recalculateTier(1L, 1L, true);

        assertEquals(CustomerProfile.CustomerTier.FREE, result.getTier());
    }

    @Test
    void otherTenantProfileRejected() {
        CustomerProfile p = profile(1L, 2L, 100, 5.0, CustomerProfile.CustomerTier.FREE);
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(p));

        assertThrows(SecurityException.class, () -> service.recalculateTier(1L, 1L, false));
    }
}
