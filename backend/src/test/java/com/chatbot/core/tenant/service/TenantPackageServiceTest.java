package com.chatbot.core.tenant.service;

import com.chatbot.core.payment.plan.model.Package;
import com.chatbot.core.payment.plan.repository.PackageRepository;
import com.chatbot.core.tenant.dto.TenantPackageInfo;
import com.chatbot.core.tenant.exception.BusinessLogicException;
import com.chatbot.core.tenant.exception.TenantNotFoundException;
import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.user.model.User;
import com.chatbot.core.user.repository.AuthRepository;
import com.chatbot.core.cache.CacheService;
import com.chatbot.shared.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantPackageServiceTest {

    @Mock private TenantRepository tenantRepository;
    @Mock private PackageRepository packageRepository;
    @Mock private AuthRepository authRepository;
    @Mock private TenantAuditLogService auditLogService;
    @Mock private CacheService cacheService;

    @InjectMocks
    private TenantPackageService service;

    @BeforeEach
    void setDefaults() {
        ReflectionTestUtils.setField(service, "defaultPackageId", "free");
        ReflectionTestUtils.setField(service, "maxDurationYears", 10);
    }

    private Package pkg(String id, String name, String duration, boolean free) {
        Package p = new Package();
        p.setPackageId(id);
        p.setName(name);
        p.setDuration(duration);
        p.setPrice(free ? BigDecimal.ZERO : new BigDecimal("99000"));
        p.setCurrency("VND");
        p.setChatbotLimit(5);
        p.setMessageLimit(1000);
        return p;
    }

    private Tenant tenant(Long id, String key, String packageId, LocalDateTime expiresAt) {
        Tenant t = new Tenant();
        t.setId(id);
        t.setTenantKey(key);
        t.setCurrentPackageId(packageId);
        t.setExpiresAt(expiresAt);
        return t;
    }

    // ---------- assignDefaultPackageToTenant ----------

    @Test
    void assignDefaultSetsFreePackage() {
        Tenant t = tenant(1L, "acme", null, null);
        when(packageRepository.findByPackageId("free")).thenReturn(Optional.of(pkg("free", "Free", "1 month", true)));
        when(tenantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.assignDefaultPackageToTenant(t);

        assertEquals("free", t.getCurrentPackageId());
        assertNotNull(t.getPackageActivatedAt());
        assertNull(t.getExpiresAt()); // free has no expiry
        verify(tenantRepository).save(t);
        verify(cacheService, atLeastOnce()).delete(anyString());
    }

    @Test
    void assignDefaultThrowsWhenPackageMissing() {
        Tenant t = tenant(1L, "acme", null, null);
        when(packageRepository.findByPackageId("free")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.assignDefaultPackageToTenant(t));
        verify(tenantRepository, never()).save(any());
    }

    // ---------- upgradeTenantPackage ----------

    @Test
    void upgradeThrowsWhenTenantMissing() {
        when(tenantRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(TenantNotFoundException.class, () -> service.upgradeTenantPackage(99L, "pro"));
    }

    @Test
    void upgradeThrowsWhenPackageMissing() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant(1L, "acme", "free", null)));
        when(packageRepository.findByPackageId("pro")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.upgradeTenantPackage(1L, "pro"));
    }

    @Test
    void upgradeToFreeClearsExpiry() {
        Tenant t = tenant(1L, "acme", "pro", LocalDateTime.now().plusDays(30));
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(t));
        when(packageRepository.findByPackageId("free")).thenReturn(Optional.of(pkg("free", "Free", "1 month", true)));
        when(tenantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.upgradeTenantPackage(1L, "free");

        assertEquals("free", t.getCurrentPackageId());
        assertNull(t.getExpiresAt());
        verify(auditLogService).logAction(eq(1L), anyString(), eq("UPGRADE_PACKAGE"), anyString());
    }

    @Test
    void upgradeToPaidSetsExpiryFromNow() {
        Tenant t = tenant(1L, "acme", "free", null);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(t));
        when(packageRepository.findByPackageId("pro")).thenReturn(Optional.of(pkg("pro", "Pro", "1 month", false)));
        when(tenantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.upgradeTenantPackage(1L, "pro");

        assertNotNull(t.getExpiresAt());
        // ~1 month from now
        assertTrue(t.getExpiresAt().isAfter(LocalDateTime.now().plusDays(25)));
        assertTrue(t.getExpiresAt().isBefore(LocalDateTime.now().plusDays(35)));
    }

    @Test
    void upgradeExtendsFromExistingFutureExpiry() {
        LocalDateTime futureExpiry = LocalDateTime.now().plusDays(20);
        Tenant t = tenant(1L, "acme", "pro", futureExpiry);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(t));
        when(packageRepository.findByPackageId("business")).thenReturn(Optional.of(pkg("business", "Biz", "1 month", false)));
        when(tenantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.upgradeTenantPackage(1L, "business");

        // base = existing expiry (20 days) + 1 month ≈ 50 days
        assertTrue(t.getExpiresAt().isAfter(LocalDateTime.now().plusDays(45)));
    }

    @Test
    void upgradeCapsExpiryAtMaxDuration() {
        Tenant t = tenant(1L, "acme", "free", null);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(t));
        when(packageRepository.findByPackageId("enterprise")).thenReturn(Optional.of(pkg("enterprise", "Ent", "20 years", false)));
        when(tenantRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.upgradeTenantPackage(1L, "enterprise");

        // capped at 10 years
        assertTrue(t.getExpiresAt().isBefore(LocalDateTime.now().plusYears(11)));
        assertTrue(t.getExpiresAt().isAfter(LocalDateTime.now().plusYears(9)));
    }

    @Test
    void upgradeWithInvalidDurationThrows() {
        Tenant t = tenant(1L, "acme", "free", null);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(t));
        when(packageRepository.findByPackageId("bad")).thenReturn(Optional.of(pkg("bad", "Bad", "forever", false)));

        assertThrows(BusinessLogicException.class, () -> service.upgradeTenantPackage(1L, "bad"));
    }

    // ---------- extractTenantIdWithFallback ----------

    @Test
    void extractTenantIdFromKey() {
        when(tenantRepository.findByTenantKey("acme")).thenReturn(Optional.of(tenant(5L, "acme", "free", null)));
        assertEquals(5L, service.extractTenantIdWithFallback("acme", null));
    }

    @Test
    void extractTenantIdFallsBackToUser() {
        when(tenantRepository.findByTenantKey("bad")).thenReturn(Optional.empty());
        User u = new User(); u.setId(7L);
        when(authRepository.findByEmail("u@test.com")).thenReturn(Optional.of(u));
        when(tenantRepository.findByUserId(7L)).thenReturn(List.of(tenant(9L, "t", "free", null)));

        assertEquals(9L, service.extractTenantIdWithFallback("bad", "u@test.com"));
    }

    @Test
    void extractTenantIdThrowsWhenNothingResolves() {
        assertThrows(TenantNotFoundException.class,
            () -> service.extractTenantIdWithFallback(null, null));
    }

    // ---------- getCurrentTenantPackage (cache + expiry) ----------

    @Test
    void getCurrentTenantPackageReturnsCached() {
        Package cached = pkg("pro", "Pro", "1 month", false);
        when(cacheService.get(anyString(), eq(Package.class))).thenReturn(cached);

        assertSame(cached, service.getCurrentTenantPackage(1L));
        verifyNoInteractions(tenantRepository);
    }

    @Test
    void getCurrentTenantPackageFallsBackToDefaultWhenExpired() {
        Tenant t = tenant(1L, "acme", "pro", LocalDateTime.now().minusDays(1));
        when(cacheService.get(anyString(), eq(Package.class))).thenReturn(null);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(t));
        when(packageRepository.findByPackageId("free")).thenReturn(Optional.of(pkg("free", "Free", "1 month", true)));

        Package result = service.getCurrentTenantPackage(1L);

        assertEquals("free", result.getPackageId());
        verify(cacheService).set(anyString(), eq(result), any());
    }

    @Test
    void getCurrentTenantPackageFallsBackWhenNoPackageAssigned() {
        Tenant t = tenant(1L, "acme", null, null);
        when(cacheService.get(anyString(), eq(Package.class))).thenReturn(null);
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(t));
        when(packageRepository.findByPackageId("free")).thenReturn(Optional.of(pkg("free", "Free", "1 month", true)));

        assertEquals("free", service.getCurrentTenantPackage(1L).getPackageId());
    }

    // ---------- hasTenantPackage ----------

    @Test
    void hasTenantPackageChecksCurrentPackageId() {
        when(tenantRepository.findById(1L)).thenReturn(Optional.of(tenant(1L, "acme", "pro", null)));
        assertTrue(service.hasTenantPackage(1L, "pro"));
        assertFalse(service.hasTenantPackage(1L, "free"));
    }

    // ---------- initializeExistingTenants ----------

    @Test
    void initializeExistingTenantsDelegates() {
        when(tenantRepository.initializeTenantsWithDefaultPackage(eq("free"), any())).thenReturn(3);
        service.initializeExistingTenants();
        verify(tenantRepository).initializeTenantsWithDefaultPackage(eq("free"), any());
    }
}
