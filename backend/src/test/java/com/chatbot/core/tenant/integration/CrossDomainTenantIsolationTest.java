package com.chatbot.core.tenant.integration;

import com.chatbot.config.AbstractTestcontainersIntegrationTest;
import com.chatbot.core.analytics.model.CustomReport;
import com.chatbot.core.analytics.repository.CustomReportRepository;
import com.chatbot.core.config.environment.model.EnvironmentConfig;
import com.chatbot.core.config.environment.repository.EnvironmentConfigRepository;
import com.chatbot.core.config.runtime.model.ConfigScope;
import com.chatbot.core.config.runtime.model.ConfigType;
import com.chatbot.core.config.runtime.model.RuntimeConfig;
import com.chatbot.core.config.runtime.repository.RuntimeConfigRepository;
import com.chatbot.core.customer.model.CustomerProfile;
import com.chatbot.core.customer.repository.CustomerProfileRepository;
import com.chatbot.core.message.store.model.Channel;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.ConversationStatus;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.payment.transaction.model.PaymentStatus;
import com.chatbot.core.payment.transaction.model.SimplePayment;
import com.chatbot.core.payment.transaction.repository.SimplePaymentRepository;
import com.chatbot.core.training.model.TrainingMaterial;
import com.chatbot.core.training.repository.TrainingMaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cross-tenant isolation tests for domains that previously lacked coverage:
 * message, customer, training, analytics, payment, config.
 * Each test seeds identical data under TENANT_1 and TENANT_2, then asserts
 * tenant-scoped queries never leak rows across tenants.
 */
class CrossDomainTenantIsolationTest extends AbstractTestcontainersIntegrationTest {

    private static final Long T1 = 1L;
    private static final Long T2 = 2L;

    @Autowired private ConversationRepository conversationRepository;
    @Autowired private CustomerProfileRepository customerProfileRepository;
    @Autowired private TrainingMaterialRepository trainingMaterialRepository;
    @Autowired private CustomReportRepository customReportRepository;
    @Autowired private SimplePaymentRepository simplePaymentRepository;
    @Autowired private RuntimeConfigRepository runtimeConfigRepository;
    @Autowired private EnvironmentConfigRepository environmentConfigRepository;

    @BeforeEach
    void setUp() {
        conversationRepository.deleteAll();
        customerProfileRepository.deleteAll();
        trainingMaterialRepository.deleteAll();
        customReportRepository.deleteAll();
        simplePaymentRepository.deleteAll();
        runtimeConfigRepository.deleteAll();
        environmentConfigRepository.deleteAll();
    }

    // ==================== MESSAGE ====================

    @Test
    void message_conversationsAreTenantIsolated() {
        conversationRepository.save(conversation(T1, "user-t1"));
        conversationRepository.save(conversation(T2, "user-t2"));

        List<Conversation> t1 = conversationRepository
                .findAllByTenantIdOrderByUpdatedAtDesc(T1, Pageable.unpaged()).getContent();
        List<Conversation> t2 = conversationRepository
                .findAllByTenantIdOrderByUpdatedAtDesc(T2, Pageable.unpaged()).getContent();

        assertEquals(1, t1.size());
        assertEquals(1, t2.size());
        assertEquals(T1, t1.get(0).getTenantId());
        assertEquals(T2, t2.get(0).getTenantId());
        assertEquals("user-t1", t1.get(0).getExternalUserId());
        assertEquals("user-t2", t2.get(0).getExternalUserId());
    }

    @Test
    void message_findByIdAndTenantIdRejectsCrossTenant() {
        Conversation t2Conv = conversationRepository.save(conversation(T2, "user-t2"));

        assertTrue(conversationRepository.findByIdAndTenantId(t2Conv.getId(), T1).isEmpty());
        assertTrue(conversationRepository.findByIdAndTenantId(t2Conv.getId(), T2).isPresent());
    }

    // ==================== CUSTOMER ====================

    @Test
    void customer_profilesAreTenantIsolated() {
        customerProfileRepository.save(customer(T1, "ext-t1"));
        customerProfileRepository.save(customer(T2, "ext-t2"));

        List<CustomerProfile> t1 = customerProfileRepository.findByTenantId(T1);
        List<CustomerProfile> t2 = customerProfileRepository.findByTenantId(T2);

        assertEquals(1, t1.size());
        assertEquals(1, t2.size());
        assertEquals("ext-t1", t1.get(0).getExternalId());
        assertEquals("ext-t2", t2.get(0).getExternalId());

        assertTrue(customerProfileRepository.findByIdAndTenantId(t2.get(0).getId(), T1).isEmpty());
    }

    // ==================== TRAINING ====================

    @Test
    void training_materialsAreTenantIsolated() {
        trainingMaterialRepository.save(training(T1, "T1 Material"));
        trainingMaterialRepository.save(training(T2, "T2 Material"));

        List<TrainingMaterial> t1 = trainingMaterialRepository.findByTenantId(T1);
        List<TrainingMaterial> t2 = trainingMaterialRepository.findByTenantId(T2);

        assertEquals(1, t1.size());
        assertEquals(1, t2.size());
        assertEquals("T1 Material", t1.get(0).getTitle());
        assertEquals("T2 Material", t2.get(0).getTitle());

        assertTrue(trainingMaterialRepository.findByIdAndTenantId(t2.get(0).getId(), T1).isEmpty());
    }

    // ==================== ANALYTICS ====================

    @Test
    void analytics_reportsAreTenantIsolated() {
        customReportRepository.save(report(T1, "T1 Report"));
        customReportRepository.save(report(T2, "T2 Report"));

        List<CustomReport> t1 = customReportRepository.findByTenantIdAndIsDeletedFalse(T1);
        List<CustomReport> t2 = customReportRepository.findByTenantIdAndIsDeletedFalse(T2);

        assertEquals(1, t1.size());
        assertEquals(1, t2.size());
        assertEquals("T1 Report", t1.get(0).getName());
        assertEquals("T2 Report", t2.get(0).getName());

        assertTrue(customReportRepository.findByIdAndTenantIdAndIsDeletedFalse(t2.get(0).getId(), T1).isEmpty());
    }

    // ==================== PAYMENT ====================

    @Test
    void payment_transactionsAreTenantIsolated() {
        simplePaymentRepository.save(payment(T1, "REF-T1-" + UUID.randomUUID()));
        simplePaymentRepository.save(payment(T2, "REF-T2-" + UUID.randomUUID()));

        List<SimplePayment> t1 = simplePaymentRepository.findByTenantId(T1);
        List<SimplePayment> t2 = simplePaymentRepository.findByTenantId(T2);

        assertEquals(1, t1.size());
        assertEquals(1, t2.size());
        assertEquals(T1, t1.get(0).getTenantId());
        assertEquals(T2, t2.get(0).getTenantId());
    }

    // ==================== CONFIG ====================

    @Test
    void runtimeConfigsAreTenantIsolated() {
        runtimeConfigRepository.save(runtimeConfig(T1, "t1.feature.flag"));
        runtimeConfigRepository.save(runtimeConfig(T2, "t2.feature.flag"));

        List<RuntimeConfig> t1 = runtimeConfigRepository.findByTenantIdOrderByConfigKey(T1);
        List<RuntimeConfig> t2 = runtimeConfigRepository.findByTenantIdOrderByConfigKey(T2);

        assertEquals(1, t1.size());
        assertEquals(1, t2.size());
        assertEquals("t1.feature.flag", t1.get(0).getConfigKey());
        assertEquals("t2.feature.flag", t2.get(0).getConfigKey());
    }

    @Test
    void environmentConfigsAreTenantIsolated() {
        environmentConfigRepository.save(envConfig(T1, "t1.env.key"));
        environmentConfigRepository.save(envConfig(T2, "t2.env.key"));

        List<EnvironmentConfig> t1 = environmentConfigRepository
                .findByEnvironmentAndTenantIdAndIsActiveOrderByConfigKey("production", T1, true);
        List<EnvironmentConfig> t2 = environmentConfigRepository
                .findByEnvironmentAndTenantIdAndIsActiveOrderByConfigKey("production", T2, true);

        assertEquals(1, t1.size());
        assertEquals(1, t2.size());
        assertEquals("t1.env.key", t1.get(0).getConfigKey());
        assertEquals("t2.env.key", t2.get(0).getConfigKey());
    }

    // ==================== FIXTURES ====================

    private Conversation conversation(Long tenantId, String externalUserId) {
        Conversation c = new Conversation();
        c.setTenantId(tenantId);
        c.setConnectionId(UUID.randomUUID());
        c.setExternalUserId(externalUserId);
        c.setStatus(ConversationStatus.OPEN);
        c.setChannel(Channel.FACEBOOK);
        return c;
    }

    private CustomerProfile customer(Long tenantId, String externalId) {
        CustomerProfile p = new CustomerProfile();
        p.setTenantId(tenantId);
        p.setExternalId(externalId);
        p.setName("Customer " + externalId);
        p.setStatus(CustomerProfile.CustomerStatus.ACTIVE);
        return p;
    }

    private TrainingMaterial training(Long tenantId, String title) {
        TrainingMaterial m = new TrainingMaterial();
        m.setTenantId(tenantId);
        m.setTitle(title);
        m.setType(TrainingMaterial.TrainingType.DOCUMENT);
        m.setStatus(TrainingMaterial.TrainingStatus.PUBLISHED);
        return m;
    }

    private CustomReport report(Long tenantId, String name) {
        CustomReport r = new CustomReport();
        r.setTenantId(tenantId);
        r.setName(name);
        r.setReportType(CustomReport.ReportType.CONVERSATIONS);
        r.setIsDeleted(false);
        return r;
    }

    private SimplePayment payment(Long tenantId, String referenceCode) {
        SimplePayment p = new SimplePayment();
        p.setTenantId(tenantId);
        p.setUserId(tenantId);
        p.setAmount(new BigDecimal("100000"));
        p.setCurrency("VND");
        p.setReferenceCode(referenceCode);
        p.setStatus(PaymentStatus.PENDING);
        return p;
    }

    private RuntimeConfig runtimeConfig(Long tenantId, String key) {
        RuntimeConfig c = new RuntimeConfig();
        c.setTenantId(tenantId);
        c.setConfigKey(key);
        c.setConfigValue("true");
        c.setConfigType(ConfigType.BOOLEAN);
        c.setConfigScope(ConfigScope.TENANT);
        c.setIsEncrypted(false);
        c.setIsReadonly(false);
        c.setVersion(1);
        return c;
    }

    private EnvironmentConfig envConfig(Long tenantId, String key) {
        EnvironmentConfig c = new EnvironmentConfig();
        c.setTenantId(tenantId);
        c.setEnvironment("production");
        c.setConfigKey(key);
        c.setConfigValue("value-" + tenantId);
        c.setIsEncrypted(false);
        c.setIsActive(true);
        return c;
    }
}
