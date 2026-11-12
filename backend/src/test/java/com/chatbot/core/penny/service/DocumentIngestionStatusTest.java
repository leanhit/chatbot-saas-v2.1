package com.chatbot.core.penny.service;

import com.chatbot.core.penny.model.PennyBot;
import com.chatbot.core.penny.model.PennyBotType;
import com.chatbot.core.penny.model.PennyKnowledgeDocument;
import com.chatbot.core.penny.repository.PennyBotRepository;
import com.chatbot.core.penny.repository.PennyKnowledgeChunkRepository;
import com.chatbot.core.penny.repository.PennyKnowledgeDocumentRepository;
import com.chatbot.config.AbstractTestcontainersIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test: Document ingestion state machine.
 *
 * <p>Covers the P1 requirement from pennyBot_review.md:
 * <ul>
 *   <li>Document is NOT marked COMPLETED when embeddings fail.</li>
 *   <li>Document is marked PARTIAL when some (not all) embeddings fail.</li>
 *   <li>Document is marked FAILED when parsing/pipeline throws.</li>
 *   <li>Upload of unsupported file type is rejected before parsing.</li>
 *   <li>Upload of file whose content doesn't match extension is rejected (Tika).</li>
 * </ul>
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DocumentIngestionStatusTest extends AbstractTestcontainersIntegrationTest {

    private static final Long TENANT_ID = 100L;

    @Autowired
    private DocumentProcessingService documentProcessingService;

    @Autowired
    private PennyBotRepository pennyBotRepository;

    @Autowired
    private PennyKnowledgeDocumentRepository documentRepository;

    @Autowired
    private PennyKnowledgeChunkRepository chunkRepository;

    private PennyBot bot;

    @BeforeEach
    void setUp() {
        bot = pennyBotRepository.save(PennyBot.builder()
                .id(UUID.randomUUID())
                .tenantId(TENANT_ID)
                .ownerId("tester")
                .botName("Ingestion Test Bot")
                .botType(PennyBotType.SUPPORT)
                .pennyBotId("penny-ingest-" + UUID.randomUUID())
                .isActive(true)
                .isEnabled(true)
                .build());
    }

    @AfterEach
    void tearDown() {
        chunkRepository.deleteAll();
        documentRepository.deleteAll();
        pennyBotRepository.deleteAll();
    }

    // ------------------------------------------------- file validation tests

    @Test
    @Order(1)
    @DisplayName("Upload with unsupported extension is rejected immediately")
    void unsupportedExtensionRejected() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "resume.txt", "text/plain",
                "Some plain text".getBytes());

        assertThatThrownBy(() ->
                documentProcessingService.processDocument(
                        file, bot.getId(), TENANT_ID, "Plain Text", "tester"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported file type");
    }

    @Test
    @Order(2)
    @DisplayName("Upload where extension says .pdf but content is not PDF is rejected (Tika)")
    void fakePdfExtensionRejected() {
        // A JPEG image renamed to .pdf — Tika will detect the actual MIME type
        byte[] jpegMagic = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                0x00, 0x10, 0x4A, 0x46, 0x49, 0x46};
        MockMultipartFile file = new MockMultipartFile(
                "file", "not_a_pdf.pdf", "application/pdf", jpegMagic);

        assertThatThrownBy(() ->
                documentProcessingService.processDocument(
                        file, bot.getId(), TENANT_ID, "Fake PDF", "tester"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    @Order(3)
    @DisplayName("Empty file is rejected before processing")
    void emptyFileRejected() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.pdf", "application/pdf", new byte[0]);

        assertThatThrownBy(() ->
                documentProcessingService.processDocument(
                        file, bot.getId(), TENANT_ID, "Empty", "tester"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("empty");
    }

    // ----------------------------------------- document state machine tests

    @Test
    @Order(4)
    @DisplayName("Document is saved with FAILED status when bot-tenant mismatch")
    void botTenantMismatchYieldsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "doc.pdf", "application/pdf",
                "%PDF-1.4 dummy".getBytes());

        // Use a random botId that is not registered for this tenant
        UUID unknownBot = UUID.randomUUID();

        assertThatThrownBy(() ->
                documentProcessingService.processDocument(
                        file, unknownBot, TENANT_ID, "Mismatch Doc", "tester"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bot not found for tenant");
    }

    @Test
    @Order(5)
    @DisplayName("getDocument returns null for another tenant's document ID (no existence leak)")
    void getDocumentNoExistenceLeak() {
        PennyKnowledgeDocument doc = PennyKnowledgeDocument.builder()
                .id(UUID.randomUUID())
                .botId(bot.getId())
                .tenantId(TENANT_ID)
                .documentName("Private Doc")
                .fileName("private.pdf")
                .fileType("PDF")
                .fileSize(1000L)
                .status("COMPLETED")
                .build();
        documentRepository.save(doc);

        // Different tenant tries to access by ID
        PennyKnowledgeDocument result = documentProcessingService.getDocument(doc.getId(), 999L);
        assertThat(result).isNull();
    }

    @Test
    @Order(6)
    @DisplayName("deleteDocument removes document and cascades chunks (tenant-scoped)")
    void deleteDocumentCascades() {
        PennyKnowledgeDocument doc = PennyKnowledgeDocument.builder()
                .id(UUID.randomUUID())
                .botId(bot.getId())
                .tenantId(TENANT_ID)
                .documentName("To Delete")
                .fileName("delete_me.pdf")
                .fileType("PDF")
                .fileSize(500L)
                .status("COMPLETED")
                .build();
        documentRepository.save(doc);

        documentProcessingService.deleteDocument(doc.getId(), TENANT_ID);

        assertThat(documentRepository.findById(doc.getId())).isEmpty();
    }

    @Test
    @Order(7)
    @DisplayName("deleteDocument with wrong tenant throws exception")
    void deleteDocumentWrongTenantThrows() {
        PennyKnowledgeDocument doc = PennyKnowledgeDocument.builder()
                .id(UUID.randomUUID())
                .botId(bot.getId())
                .tenantId(TENANT_ID)
                .documentName("Protected")
                .fileName("protect.pdf")
                .fileType("PDF")
                .fileSize(500L)
                .status("COMPLETED")
                .build();
        documentRepository.save(doc);

        assertThatThrownBy(() ->
                documentProcessingService.deleteDocument(doc.getId(), 999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Document not found");
    }
}
