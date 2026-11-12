package com.chatbot.core.penny.service;

import com.chatbot.core.penny.context.ConversationContext;
import com.chatbot.core.penny.context.storage.DatabaseContextStorage;
import com.chatbot.core.penny.dto.request.MiddlewareRequest;
import com.chatbot.core.penny.model.PennyBot;
import com.chatbot.core.penny.model.PennyBotType;
import com.chatbot.core.penny.model.PennyKnowledgeDocument;
import com.chatbot.core.penny.repository.PennyBotRepository;
import com.chatbot.core.penny.repository.PennyKnowledgeDocumentRepository;
import com.chatbot.core.tenant.infra.TenantContext;
import com.chatbot.config.AbstractTestcontainersIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test: Penny Bot tenant isolation for documents and context.
 *
 * <p>Covers the P0 requirements from pennyBot_review.md:
 * <ul>
 *   <li>Tenant A cannot read/delete documents owned by Tenant B.</li>
 *   <li>Context is not shared between different (tenant, bot, user) combinations.</li>
 *   <li>Redis miss falls back to DB for context retrieval.</li>
 * </ul>
 *
 * <p>Uses Testcontainers (PostgreSQL + Redis) via
 * {@link AbstractTestcontainersIntegrationTest}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PennyTenantIsolationIntegrationTest extends AbstractTestcontainersIntegrationTest {

    private static final Long TENANT_A = 100L;
    private static final Long TENANT_B = 200L;

    @Autowired
    private PennyBotRepository pennyBotRepository;

    @Autowired
    private PennyKnowledgeDocumentRepository documentRepository;

    @Autowired
    private DocumentProcessingService documentProcessingService;

    @Autowired
    private DatabaseContextStorage databaseContextStorage;

    // ------------------------------------------------------------------ setup

    private PennyBot botA;
    private PennyBot botB;

    @BeforeEach
    void setUp() {
        TenantContext.clear();

        botA = pennyBotRepository.save(PennyBot.builder()
                .id(UUID.randomUUID())
                .tenantId(TENANT_A)
                .ownerId("ownerA")
                .botName("Bot A")
                .botType(PennyBotType.SUPPORT)
                .pennyBotId("penny-a-" + UUID.randomUUID())
                .isActive(true)
                .isEnabled(true)
                .build());

        botB = pennyBotRepository.save(PennyBot.builder()
                .id(UUID.randomUUID())
                .tenantId(TENANT_B)
                .ownerId("ownerB")
                .botName("Bot B")
                .botType(PennyBotType.SUPPORT)
                .pennyBotId("penny-b-" + UUID.randomUUID())
                .isActive(true)
                .isEnabled(true)
                .build());
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
        pennyBotRepository.deleteAll();
        documentRepository.deleteAll();
    }

    // ------------------------------------------------- document isolation tests

    @Test
    @Order(1)
    @DisplayName("Tenant A cannot read document belonging to Tenant B")
    void documentOfTenantBIsHiddenFromTenantA() {
        // Tenant B uploads a document
        PennyKnowledgeDocument docB = PennyKnowledgeDocument.builder()
                .id(UUID.randomUUID())
                .botId(botB.getId())
                .tenantId(TENANT_B)
                .documentName("Secret B")
                .fileName("secret.pdf")
                .fileType("PDF")
                .fileSize(1024L)
                .status("COMPLETED")
                .build();
        documentRepository.save(docB);

        // Tenant A tries to fetch Tenant B's document by its ID
        PennyKnowledgeDocument result = documentProcessingService.getDocument(docB.getId(), TENANT_A);

        // Must return null — no existence leak
        assertThat(result).isNull();
    }

    @Test
    @Order(2)
    @DisplayName("listDocuments for Tenant A does not include Tenant B documents")
    void listDocumentsDoesNotLeakCrossTenant() {
        // Tenant A doc
        PennyKnowledgeDocument docA = PennyKnowledgeDocument.builder()
                .id(UUID.randomUUID())
                .botId(botA.getId())
                .tenantId(TENANT_A)
                .documentName("Doc A")
                .fileName("a.pdf")
                .fileType("PDF")
                .fileSize(512L)
                .status("COMPLETED")
                .build();
        documentRepository.save(docA);

        // Tenant B doc
        PennyKnowledgeDocument docB = PennyKnowledgeDocument.builder()
                .id(UUID.randomUUID())
                .botId(botB.getId())
                .tenantId(TENANT_B)
                .documentName("Doc B")
                .fileName("b.pdf")
                .fileType("PDF")
                .fileSize(512L)
                .status("COMPLETED")
                .build();
        documentRepository.save(docB);

        var tenantADocs = documentProcessingService.getDocumentsByBotAndTenant(botA.getId(), TENANT_A);

        assertThat(tenantADocs)
                .isNotEmpty()
                .allSatisfy(doc -> assertThat(doc.getTenantId()).isEqualTo(TENANT_A));
    }

    @Test
    @Order(3)
    @DisplayName("processDocument rejects botId that belongs to different tenant")
    void processDocumentRejectsWrongTenant() {
        // botA belongs to TENANT_A; try to upload a document with TENANT_B
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf",
                "%PDF-1.4 fake pdf content".getBytes(StandardCharsets.UTF_8));

        assertThatThrownBy(() ->
                documentProcessingService.processDocument(file, botA.getId(), TENANT_B, "Bad Upload", "attacker"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bot not found for tenant");
    }

    // --------------------------------------------------- context isolation tests

    @Test
    @Order(4)
    @DisplayName("Context saved for Tenant A is not visible to Tenant B")
    void contextIsolatedByTenant() {
        // Save context under Tenant A
        TenantContext.setTenantId(TENANT_A);
        ConversationContext ctxA = ConversationContext.builder()
                .contextId(UUID.randomUUID().toString())
                .tenantId(TENANT_A)
                .botId(botA.getId().toString())
                .userId("user-1")
                .platform("web")
                .status("active")
                .build();
        databaseContextStorage.saveContext(ctxA);

        // Now switch to Tenant B and try to load the same external user/platform
        TenantContext.setTenantId(TENANT_B);
        MiddlewareRequest requestB = MiddlewareRequest.builder()
                .botId(botB.getId().toString())
                .userId("user-1")
                .platform("web")
                .build();
        ConversationContext loaded = databaseContextStorage.loadContext(requestB);

        assertThat(loaded).isNull();
    }

    @Test
    @Order(5)
    @DisplayName("Context persisted to DB can be retrieved (simulates Redis miss)")
    void contextFallbackFromDb() {
        TenantContext.setTenantId(TENANT_A);

        ConversationContext saved = ConversationContext.builder()
                .contextId(UUID.randomUUID().toString())
                .tenantId(TENANT_A)
                .botId(botA.getId().toString())
                .userId("user-db-fallback")
                .platform("telegram")
                .status("active")
                .build();

        databaseContextStorage.saveContext(saved);

        // Load directly from DB (bypassing Redis) via MiddlewareRequest
        MiddlewareRequest request = MiddlewareRequest.builder()
                .botId(botA.getId().toString())
                .userId("user-db-fallback")
                .platform("telegram")
                .build();
        ConversationContext loaded = databaseContextStorage.loadContext(request);

        assertThat(loaded).isNotNull();
        assertThat(loaded.getUserId()).isEqualTo("user-db-fallback");
        assertThat(loaded.getTenantId()).isEqualTo(TENANT_A);
        assertThat(loaded.getBotId()).isEqualTo(botA.getId().toString());
    }

    @Test
    @Order(6)
    @DisplayName("Two bots of different tenants with same userId do not share context")
    void differentBotsDoNotShareContext() {
        // Tenant A saves context
        TenantContext.setTenantId(TENANT_A);
        ConversationContext ctxA = ConversationContext.builder()
                .contextId(UUID.randomUUID().toString())
                .tenantId(TENANT_A)
                .botId(botA.getId().toString())
                .userId("shared-user")
                .platform("web")
                .status("active")
                .build();
        databaseContextStorage.saveContext(ctxA);

        // Tenant B tries to load with the same userId
        TenantContext.setTenantId(TENANT_B);
        MiddlewareRequest requestB = MiddlewareRequest.builder()
                .botId(botB.getId().toString())
                .userId("shared-user")
                .platform("web")
                .build();
        ConversationContext ctxB = databaseContextStorage.loadContext(requestB);

        assertThat(ctxB).isNull();
    }
}
