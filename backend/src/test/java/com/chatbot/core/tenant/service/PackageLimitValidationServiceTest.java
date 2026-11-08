package com.chatbot.core.tenant.service;

import com.chatbot.core.payment.plan.model.Package;
import com.chatbot.core.payment.plan.service.PackageService;
import com.chatbot.core.penny.repository.PennyBotRepository;
import com.chatbot.core.tenant.exception.BusinessLogicException;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackageLimitValidationServiceTest {

    @Mock private TenantPackageService tenantPackageService;
    @Mock private PackageService packageService;
    @Mock private PennyBotRepository pennyBotRepository;
    @Mock private TenantRepository tenantRepository;

    @InjectMocks
    private PackageLimitValidationService service;

    private Package pkg(String name, int limit) {
        Package p = new Package();
        p.setName(name);
        p.setPackageId("pkg-" + name.toLowerCase());
        p.setChatbotLimit(limit);
        return p;
    }

    private void stubTenantExists(Long tenantId) {
        when(tenantRepository.findByIdWithPessimisticWriteLock(tenantId))
            .thenReturn(Optional.of(new Tenant()));
    }

    // ---------- canCreateMoreChatbots ----------

    @Test
    void canCreateReturnsFalseWhenNoPackage() {
        when(tenantPackageService.getCurrentTenantPackage(1L)).thenReturn(null);
        assertFalse(service.canCreateMoreChatbots(1L));
    }

    @Test
    void canCreateTrueWhenUnderLimit() {
        when(tenantPackageService.getCurrentTenantPackage(1L)).thenReturn(pkg("Pro", 5));
        when(pennyBotRepository.countByTenantIdAndIsActiveTrue(1L)).thenReturn(3L);
        assertTrue(service.canCreateMoreChatbots(1L));
    }

    @Test
    void canCreateFalseWhenAtLimit() {
        when(tenantPackageService.getCurrentTenantPackage(1L)).thenReturn(pkg("Pro", 5));
        when(pennyBotRepository.countByTenantIdAndIsActiveTrue(1L)).thenReturn(5L);
        assertFalse(service.canCreateMoreChatbots(1L));
    }

    @Test
    void canCreateTrueWhenUnlimitedNegativeOne() {
        when(tenantPackageService.getCurrentTenantPackage(1L)).thenReturn(pkg("Enterprise", -1));
        when(pennyBotRepository.countByTenantIdAndIsActiveTrue(1L)).thenReturn(999L);
        assertTrue(service.canCreateMoreChatbots(1L));
    }

    @Test
    void canCreateTrueWhenUnlimitedMaxValue() {
        when(tenantPackageService.getCurrentTenantPackage(1L)).thenReturn(pkg("Enterprise", Integer.MAX_VALUE));
        when(pennyBotRepository.countByTenantIdAndIsActiveTrue(1L)).thenReturn(999L);
        assertTrue(service.canCreateMoreChatbots(1L));
    }

    // ---------- getRemainingChatbotSlots ----------

    @Test
    void remainingSlotsComputesDifference() {
        when(tenantPackageService.getCurrentTenantPackage(1L)).thenReturn(pkg("Pro", 5));
        when(pennyBotRepository.countByTenantIdAndIsActiveTrue(1L)).thenReturn(3L);
        assertEquals(2, service.getRemainingChatbotSlots(1L));
    }

    @Test
    void remainingSlotsNeverNegative() {
        when(tenantPackageService.getCurrentTenantPackage(1L)).thenReturn(pkg("Pro", 5));
        when(pennyBotRepository.countByTenantIdAndIsActiveTrue(1L)).thenReturn(10L);
        assertEquals(0, service.getRemainingChatbotSlots(1L));
    }

    @Test
    void remainingSlotsUnlimitedReturnsMaxValue() {
        when(tenantPackageService.getCurrentTenantPackage(1L)).thenReturn(pkg("Ent", -1));
        assertEquals(Integer.MAX_VALUE, service.getRemainingChatbotSlots(1L));
    }

    @Test
    void remainingSlotsZeroWhenNoPackage() {
        when(tenantPackageService.getCurrentTenantPackage(1L)).thenReturn(null);
        assertEquals(0, service.getRemainingChatbotSlots(1L));
    }

    // ---------- validateChatbotCreation ----------

    @Test
    void validateThrowsWhenTenantMissing() {
        when(tenantRepository.findByIdWithPessimisticWriteLock(99L)).thenReturn(Optional.empty());
        assertThrows(BusinessLogicException.class, () -> service.validateChatbotCreation(99L));
    }

    @Test
    void validateThrowsWhenNoPackage() {
        stubTenantExists(1L);
        when(tenantPackageService.getCurrentTenantPackage(1L)).thenReturn(null);
        assertThrows(BusinessLogicException.class, () -> service.validateChatbotCreation(1L));
    }

    @Test
    void validateThrowsWhenLimitExceeded() {
        stubTenantExists(1L);
        when(tenantPackageService.getCurrentTenantPackage(1L)).thenReturn(pkg("Pro", 2));
        when(pennyBotRepository.countByTenantIdAndIsActiveTrue(1L)).thenReturn(2L);
        BusinessLogicException ex = assertThrows(BusinessLogicException.class,
            () -> service.validateChatbotCreation(1L));
        assertTrue(ex.getMessage().contains("limit exceeded"));
    }

    @Test
    void validatePassesWhenUnderLimit() {
        stubTenantExists(1L);
        when(tenantPackageService.getCurrentTenantPackage(1L)).thenReturn(pkg("Pro", 5));
        when(pennyBotRepository.countByTenantIdAndIsActiveTrue(1L)).thenReturn(4L);
        assertDoesNotThrow(() -> service.validateChatbotCreation(1L));
    }

    @Test
    void validatePassesWhenUnlimited() {
        stubTenantExists(1L);
        when(tenantPackageService.getCurrentTenantPackage(1L)).thenReturn(pkg("Ent", -1));
        when(pennyBotRepository.countByTenantIdAndIsActiveTrue(1L)).thenReturn(1000L);
        assertDoesNotThrow(() -> service.validateChatbotCreation(1L));
    }

    // ---------- getChatbotLimitInfo ----------

    @Test
    void limitInfoForNoPackage() {
        when(tenantPackageService.getCurrentTenantPackage(1L)).thenReturn(null);
        var info = service.getChatbotLimitInfo(1L);
        assertEquals("No Package", info.getPackageName());
        assertEquals(0, info.getTotalLimit());
        assertFalse(info.getCanCreateMore());
        assertFalse(info.getIsUnlimited());
    }

    @Test
    void limitInfoForLimitedPackage() {
        when(tenantPackageService.getCurrentTenantPackage(1L)).thenReturn(pkg("Pro", 5));
        when(pennyBotRepository.countByTenantIdAndIsActiveTrue(1L)).thenReturn(3L);
        var info = service.getChatbotLimitInfo(1L);
        assertEquals("Pro", info.getPackageName());
        assertEquals(5, info.getTotalLimit());
        assertEquals(3, info.getCurrentCount());
        assertEquals(2, info.getRemainingSlots());
        assertTrue(info.getCanCreateMore());
        assertFalse(info.getIsUnlimited());
    }

    @Test
    void limitInfoForUnlimitedPackage() {
        when(tenantPackageService.getCurrentTenantPackage(1L)).thenReturn(pkg("Ent", -1));
        when(pennyBotRepository.countByTenantIdAndIsActiveTrue(1L)).thenReturn(50L);
        var info = service.getChatbotLimitInfo(1L);
        assertTrue(info.getIsUnlimited());
        assertTrue(info.getCanCreateMore());
        assertEquals(Integer.MAX_VALUE, info.getRemainingSlots());
    }
}
