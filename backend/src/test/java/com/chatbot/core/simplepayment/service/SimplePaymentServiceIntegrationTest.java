package com.chatbot.core.simplepayment.service;

import com.chatbot.core.simplepayment.model.Package;
import com.chatbot.core.simplepayment.model.PaymentStatus;
import com.chatbot.core.simplepayment.model.SimplePayment;
import com.chatbot.core.simplepayment.repository.PackageRepository;
import com.chatbot.core.simplepayment.repository.SimplePaymentRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import com.chatbot.config.AbstractTestcontainersIntegrationTest;

/**
 * Integration test for SimplePaymentService using Testcontainers PostgreSQL database
 * Tests payment flow with real PostgreSQL database operations
 */
class SimplePaymentServiceIntegrationTest extends AbstractTestcontainersIntegrationTest {

    @Autowired
    private SimplePaymentRepository paymentRepository;

    @Autowired
    private PackageRepository packageRepository;

    private Package testPackage;
    private Long testUserId = 1L;
    private Long testTenantId = 1L;

    @BeforeEach
    void setUp() {
        // Clean up database
        paymentRepository.deleteAll();
        packageRepository.deleteAll();

        // Set tenant context
        TenantContext.setTenantId(testTenantId);

        // Create test package
        testPackage = new Package();
        testPackage.setPackageId("PKG-001");
        testPackage.setName("Basic Package");
        testPackage.setPrice(BigDecimal.valueOf(100000));
        testPackage.setCurrency("VND");
        testPackage.setDuration("1 month");
        testPackage.setMessageLimit(1000);
        testPackage.setChatbotLimit(5);
        testPackage.setHasPrioritySupport(false);
        testPackage.setHasAnalytics(true);
        testPackage.setHasAdvancedAnalytics(false);
        testPackage.setHasCustomIntegrations(false);
        testPackage.setHasDedicatedSupport(false);
        testPackage.setHasCustomFeatures(false);
        testPackage.setHasSlaGuarantee(false);
        testPackage.setIsActive(true);
        testPackage.setSortOrder(1);
        testPackage.setCreatedAt(LocalDateTime.now());
        testPackage.setUpdatedAt(LocalDateTime.now());
        testPackage = packageRepository.save(testPackage);
    }

    @AfterEach
    void tearDown() {
        paymentRepository.deleteAll();
        packageRepository.deleteAll();
        TenantContext.clear();
    }

    @Test
    void testPackageRepository_SaveAndFind() {
        // Act
        Optional<Package> foundPackage = packageRepository.findByPackageId("PKG-001");

        // Assert
        assertTrue(foundPackage.isPresent());
        assertEquals("Basic Package", foundPackage.get().getName());
        assertEquals(0, BigDecimal.valueOf(100000).compareTo(foundPackage.get().getPrice()));
    }

    @Test
    void testPaymentRepository_SaveAndFindByReference() {
        // Arrange
        SimplePayment payment = new SimplePayment();
        payment.setReferenceCode("REF-TEST-001");
        payment.setAmount(BigDecimal.valueOf(100000));
        payment.setStatus(PaymentStatus.PENDING);
        payment.setUserId(testUserId);
        payment.setTenantId(testTenantId);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Act
        Optional<SimplePayment> foundPayment = paymentRepository.findByReferenceCode("REF-TEST-001");

        // Assert
        assertTrue(foundPayment.isPresent());
        assertEquals("REF-TEST-001", foundPayment.get().getReferenceCode());
        assertEquals(PaymentStatus.PENDING, foundPayment.get().getStatus());
        assertEquals(testUserId, foundPayment.get().getUserId());
    }

    @Test
    void testPaymentRepository_FindByReference_NotFound() {
        // Act
        Optional<SimplePayment> foundPayment = paymentRepository.findByReferenceCode("REF-NONEXISTENT");

        // Assert
        assertFalse(foundPayment.isPresent());
    }
}
