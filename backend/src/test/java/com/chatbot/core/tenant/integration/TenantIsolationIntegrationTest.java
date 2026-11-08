package com.chatbot.core.tenant.integration;

import com.chatbot.config.AbstractTestcontainersIntegrationTest;
import com.chatbot.core.analytics.model.AnalyticsDailySummary;
import com.chatbot.core.analytics.model.CustomReport;
import com.chatbot.core.analytics.repository.AnalyticsDailySummaryRepository;
import com.chatbot.core.analytics.repository.CustomReportRepository;
import com.chatbot.core.customer.model.CustomerProfile;
import com.chatbot.core.customer.repository.CustomerProfileRepository;
import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.ConversationStatus;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.core.training.model.TrainingMaterial;
import com.chatbot.core.training.repository.TrainingMaterialRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Cross-module tenant isolation tests on real PostgreSQL.
 * Verifies that entities created under TenantContext A are never
 * visible to tenant-scoped queries of tenant B, across modules:
 * customer, training, analytics, message store.
 */
class TenantIsolationIntegrationTest extends AbstractTestcontainersIntegrationTest {

    @Autowired
    private CustomerProfileRepository customerProfileRepository;

    @Autowired
    private TrainingMaterialRepository trainingMaterialRepository;

    @Autowired
    private CustomReportRepository customReportRepository;

    @Autowired
    private AnalyticsDailySummaryRepository analyticsDailySummaryRepository;

    @Autowired
    private ConversationRepository conversationRepository;

    private static final Long TENANT_A = 1L;
    private static final Long TENANT_B = 2L;

    @BeforeEach
    void setUp() {
        customerProfileRepository.deleteAll();
        trainingMaterialRepository.deleteAll();
        customReportRepository.deleteAll();
        analyticsDailySummaryRepository.deleteAll();
        conversationRepository.deleteAll();
        TenantContext.setTenantId(TENANT_A);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void customerProfilesAreIsolatedByTenant() {
        CustomerProfile profileA = saveCustomerProfile("ext-a", "Customer A");
        switchToTenant(TENANT_B);
        CustomerProfile profileB = saveCustomerProfile("ext-b", "Customer B");

        List<CustomerProfile> forA = customerProfileRepository.findByTenantId(TENANT_A);
        List<CustomerProfile> forB = customerProfileRepository.findByTenantId(TENANT_B);

        assertEquals(1, forA.size());
        assertEquals(profileA.getId(), forA.get(0).getId());
        assertEquals(1, forB.size());
        assertEquals(profileB.getId(), forB.get(0).getId());
        assertNotEquals(forA.get(0).getTenantId(), forB.get(0).getTenantId());
    }

    @Test
    void trainingMaterialsAreIsolatedByTenant() {
        TrainingMaterial materialA = saveTrainingMaterial("Material A");
        switchToTenant(TENANT_B);
        TrainingMaterial materialB = saveTrainingMaterial("Material B");

        List<TrainingMaterial> forA = trainingMaterialRepository.findByTenantId(TENANT_A);
        List<TrainingMaterial> forB = trainingMaterialRepository.findByTenantId(TENANT_B);

        assertEquals(1, forA.size());
        assertEquals(materialA.getId(), forA.get(0).getId());
        assertEquals(1, forB.size());
        assertEquals(materialB.getId(), forB.get(0).getId());
    }

    @Test
    void customReportsAreIsolatedByTenant() {
        CustomReport reportA = saveCustomReport("Report A");
        switchToTenant(TENANT_B);
        CustomReport reportB = saveCustomReport("Report B");

        List<CustomReport> forA = customReportRepository.findByTenantIdAndIsDeletedFalse(TENANT_A);
        List<CustomReport> forB = customReportRepository.findByTenantIdAndIsDeletedFalse(TENANT_B);

        assertEquals(1, forA.size());
        assertEquals(reportA.getId(), forA.get(0).getId());
        assertEquals(1, forB.size());
        assertEquals(reportB.getId(), forB.get(0).getId());

        // Tenant B cannot fetch tenant A's report by id + tenantId
        assertTrue(customReportRepository
                .findByIdAndTenantIdAndIsDeletedFalse(reportA.getId(), TENANT_B)
                .isEmpty());
    }

    @Test
    void analyticsSummariesAreIsolatedByTenant() {
        AnalyticsDailySummary summaryA = saveDailySummary(LocalDate.now());
        switchToTenant(TENANT_B);
        AnalyticsDailySummary summaryB = saveDailySummary(LocalDate.now());

        List<AnalyticsDailySummary> forA = analyticsDailySummaryRepository
                .findByTenantIdAndDateRange(TENANT_A, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));
        List<AnalyticsDailySummary> forB = analyticsDailySummaryRepository
                .findByTenantIdAndDateRange(TENANT_B, LocalDate.now().minusDays(1), LocalDate.now().plusDays(1));

        assertEquals(1, forA.size());
        assertEquals(summaryA.getId(), forA.get(0).getId());
        assertEquals(1, forB.size());
        assertEquals(summaryB.getId(), forB.get(0).getId());
    }

    @Test
    void conversationsAreIsolatedByTenant() {
        Conversation convA = saveConversation("user-a");
        switchToTenant(TENANT_B);
        Conversation convB = saveConversation("user-b");

        List<Conversation> forA = conversationRepository.findByTenantId(TENANT_A);
        List<Conversation> forB = conversationRepository.findByTenantId(TENANT_B);

        assertEquals(1, forA.size());
        assertEquals(convA.getId(), forA.get(0).getId());
        assertEquals(1, forB.size());
        assertEquals(convB.getId(), forB.get(0).getId());
    }

    @Test
    void tenantContextAutoAssignsTenantIdOnPersist() {
        switchToTenant(TENANT_A);
        CustomerProfile profile = saveCustomerProfile("ext-auto", "Auto Tenant");
        assertEquals(TENANT_A, profile.getTenantId());
    }

    @Test
    void creatingEntityWithoutTenantContextFails() {
        TenantContext.clear();
        CustomerProfile orphan = new CustomerProfile();
        orphan.setExternalId("ext-orphan");
        orphan.setName("Orphan");
        // tenantId intentionally not set and no TenantContext -> @PrePersist must throw
        assertThrows(Exception.class, () -> customerProfileRepository.save(orphan));
    }

    // ---------- helpers ----------

    private void switchToTenant(Long tenantId) {
        TenantContext.clear();
        TenantContext.setTenantId(tenantId);
    }

    private CustomerProfile saveCustomerProfile(String externalId, String name) {
        CustomerProfile profile = new CustomerProfile();
        profile.setExternalId(externalId);
        profile.setName(name);
        profile.setStatus(CustomerProfile.CustomerStatus.ACTIVE);
        profile.setTier(CustomerProfile.CustomerTier.FREE);
        return customerProfileRepository.save(profile);
    }

    private TrainingMaterial saveTrainingMaterial(String title) {
        TrainingMaterial material = new TrainingMaterial();
        material.setTitle(title);
        material.setType(TrainingMaterial.TrainingType.DOCUMENT);
        material.setStatus(TrainingMaterial.TrainingStatus.PUBLISHED);
        return trainingMaterialRepository.save(material);
    }

    private CustomReport saveCustomReport(String name) {
        CustomReport report = new CustomReport();
        report.setName(name);
        report.setReportType(CustomReport.ReportType.CONVERSATIONS);
        report.setDateRangeDays(30);
        return customReportRepository.save(report);
    }

    private AnalyticsDailySummary saveDailySummary(LocalDate date) {
        AnalyticsDailySummary summary = new AnalyticsDailySummary();
        summary.setSummaryDate(date);
        summary.setTotalConversations(10);
        summary.setNewConversations(5);
        summary.setClosedConversations(3);
        summary.setTotalMessages(100);
        summary.setUserMessages(40);
        summary.setBotMessages(45);
        summary.setAgentMessages(15);
        summary.setEscalationsCount(2);
        summary.setNegativeSentimentCount(0);
        summary.setAiSuggestionsCount(0);
        summary.setAiSuggestionsAccepted(0);
        summary.setAiSuggestionsRejected(0);
        return analyticsDailySummaryRepository.save(summary);
    }

    private Conversation saveConversation(String externalUserId) {
        Conversation conversation = new Conversation();
        conversation.setExternalUserId(externalUserId);
        conversation.setStatus(ConversationStatus.OPEN);
        return conversationRepository.save(conversation);
    }
}
