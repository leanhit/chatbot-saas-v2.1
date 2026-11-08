package com.chatbot.core.customer.service;

import com.chatbot.core.customer.model.CustomerProfile;
import com.chatbot.core.customer.model.VipConfig;
import com.chatbot.core.customer.repository.CustomerProfileRepository;
import com.chatbot.core.customer.repository.VipConfigRepository;
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

    @Mock
    private VipConfigRepository vipConfigRepository;

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

    // ---------- VipConfig thresholds ----------

    @Test
    void customConfigLowersPremiumThreshold() {
        VipConfig config = VipConfig.builder()
            .basicMinConversations(2)
            .premiumMinConversations(5)
            .premiumMinSatisfaction(3.0)
            .enterpriseMinConversations(20)
            .build();

        CustomerProfile p = profile(1L, 1L, 6, 3.5, CustomerProfile.CustomerTier.FREE);
        // 6 conv >= premiumMin 5, satisfaction 3.5 >= 3.0 -> PREMIUM (defaults would give BASIC)
        assertEquals(CustomerProfile.CustomerTier.PREMIUM, service.computeTier(p, config));
    }

    @Test
    void customConfigRaisesEnterpriseThreshold() {
        VipConfig config = VipConfig.builder()
            .enterpriseMinConversations(100)
            .build();

        CustomerProfile p = profile(1L, 1L, 60, null, CustomerProfile.CustomerTier.FREE);
        // 60 conv: default -> ENTERPRISE, custom -> BASIC
        assertEquals(CustomerProfile.CustomerTier.BASIC, service.computeTier(p, config));
    }

    @Test
    void nullConfigFieldsFallBackToDefaults() {
        VipConfig config = VipConfig.builder().build();
        config.setPremiumMinConversations(null);
        config.setEnterpriseMinConversations(null);
        config.setBasicMinConversations(null);
        config.setPremiumMinSatisfaction(null);

        CustomerProfile p = profile(1L, 1L, 50, null, CustomerProfile.CustomerTier.FREE);
        assertEquals(CustomerProfile.CustomerTier.ENTERPRISE, service.computeTier(p, config));
    }

    @Test
    void recalculateTierUsesTenantVipConfig() {
        VipConfig config = VipConfig.builder()
            .basicMinConversations(100)
            .premiumMinConversations(200)
            .enterpriseMinConversations(300)
            .build();
        CustomerProfile p = profile(1L, 1L, 60, 5.0, CustomerProfile.CustomerTier.FREE);
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(p));
        when(vipConfigRepository.findByTenantIdAndIsDeletedFalse(1L)).thenReturn(Optional.of(config));

        CustomerProfile result = service.recalculateTier(1L, 1L, true);

        // 60 conv with custom thresholds (basic=100) -> FREE, not ENTERPRISE
        assertEquals(CustomerProfile.CustomerTier.FREE, result.getTier());
    }

    @Test
    void recalculateTierFallsBackWhenNoVipConfig() {
        CustomerProfile p = profile(1L, 1L, 60, 5.0, CustomerProfile.CustomerTier.FREE);
        when(customerProfileRepository.findById(1L)).thenReturn(Optional.of(p));
        when(vipConfigRepository.findByTenantIdAndIsDeletedFalse(1L)).thenReturn(Optional.empty());
        when(customerProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CustomerProfile result = service.recalculateTier(1L, 1L, true);

        assertEquals(CustomerProfile.CustomerTier.ENTERPRISE, result.getTier());
    }
}
