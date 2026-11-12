package com.chatbot.core.penny.service;

import com.chatbot.core.penny.kb.EmbeddingService;
import com.chatbot.core.penny.model.PennyBot;
import com.chatbot.core.penny.model.PennyBotType;
import com.chatbot.core.penny.model.PennyKnowledgeDocument;
import com.chatbot.core.penny.repository.PennyBotRepository;
import com.chatbot.core.penny.repository.PennyKnowledgeChunkRepository;
import com.chatbot.core.penny.repository.PennyKnowledgeDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for DocumentProcessingService document state machine and tenant scoping.
 */
@ExtendWith(MockitoExtension.class)
class DocumentProcessingServiceTest {

    @Mock private DocumentParsingService documentParsingService;
    @Mock private TextChunkerService textChunkerService;
    @Mock private EmbeddingService embeddingService;
    @Mock private PennyKnowledgeDocumentRepository documentRepository;
    @Mock private PennyKnowledgeChunkRepository chunkRepository;
    @Mock private PennyBotRepository pennyBotRepository;

    private DocumentProcessingService service;

    private final UUID botId = UUID.randomUUID();
    private final Long tenantId = 100L;
    private final Long otherTenantId = 200L;

    @BeforeEach
    void setUp() {
        service = new DocumentProcessingService(
            documentParsingService, textChunkerService, embeddingService,
            documentRepository, chunkRepository, pennyBotRepository);
    }

    private PennyBot activeBot(Long tid) {
        return PennyBot.builder()
            .id(botId).tenantId(tid).ownerId("owner@test.com")
            .botName("Bot").botType(PennyBotType.SUPPORT)
            .pennyBotId("penny-1").isActive(true).isEnabled(true)
            .build();
    }

    private MultipartFile pdfFile() {
        return new MockMultipartFile("file", "doc.pdf", "application/pdf",
            "%PDF-1.4 fake content".getBytes());
    }

    private void stubHappyParse(int chunkCount) throws Exception {
        DocumentParsingService.DocumentParseResult parseResult = new DocumentParsingService.DocumentParseResult();
        parseResult.setFileType("PDF");
        parseResult.setTotalPages(1);
        parseResult.setPages(List.of());
        when(documentParsingService.parseDocument(any())).thenReturn(parseResult);

        List<TextChunkerService.TextChunk> chunks = new java.util.ArrayList<>();
        for (int i = 0; i < chunkCount; i++) {
            TextChunkerService.TextChunk chunk = new TextChunkerService.TextChunk();
            chunk.setChunkIndex(i);
            chunk.setText("chunk " + i);
            chunk.setTokens(10);
            chunk.setPageNumber(1);
            chunks.add(chunk);
        }
        when(textChunkerService.chunkDocumentPages(any())).thenReturn(chunks);
        when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    @DisplayName("all embeddings succeed -> status COMPLETED")
    void allEmbeddingsSucceed_completed() throws Exception {
        when(pennyBotRepository.findByIdAndTenantId(botId, tenantId))
            .thenReturn(Optional.of(activeBot(tenantId)));
        stubHappyParse(3);
        when(embeddingService.generateEmbedding(any())).thenReturn(new float[]{0.1f});
        when(embeddingService.getEmbeddingModel()).thenReturn("text-embedding-3-small");

        PennyKnowledgeDocument result = service.processDocument(pdfFile(), botId, tenantId, "doc", "u");

        assertEquals("COMPLETED", result.getStatus());
        assertEquals(3, result.getTotalChunks());
    }

    @Test
    @DisplayName("some embeddings fail -> status PARTIAL")
    void someEmbeddingsFail_partial() throws Exception {
        when(pennyBotRepository.findByIdAndTenantId(botId, tenantId))
            .thenReturn(Optional.of(activeBot(tenantId)));
        stubHappyParse(4);
        when(embeddingService.generateEmbedding(any()))
            .thenReturn(new float[]{0.1f})
            .thenReturn(null)
            .thenReturn(new float[]{0.2f})
            .thenReturn(null);
        when(embeddingService.getEmbeddingModel()).thenReturn("text-embedding-3-small");

        PennyKnowledgeDocument result = service.processDocument(pdfFile(), botId, tenantId, "doc", "u");

        assertEquals("PARTIAL", result.getStatus());
        assertEquals(4, result.getTotalChunks());
    }

    @Test
    @DisplayName("all embeddings fail -> status FAILED (not searchable)")
    void allEmbeddingsFail_failed() throws Exception {
        when(pennyBotRepository.findByIdAndTenantId(botId, tenantId))
            .thenReturn(Optional.of(activeBot(tenantId)));
        stubHappyParse(2);
        when(embeddingService.generateEmbedding(any())).thenReturn(null);
        when(embeddingService.getEmbeddingModel()).thenReturn("text-embedding-3-small");

        PennyKnowledgeDocument result = service.processDocument(pdfFile(), botId, tenantId, "doc", "u");

        assertEquals("FAILED", result.getStatus());
    }

    @Test
    @DisplayName("parse error -> status FAILED and exception propagates")
    void parseError_failed() throws Exception {
        when(pennyBotRepository.findByIdAndTenantId(botId, tenantId))
            .thenReturn(Optional.of(activeBot(tenantId)));
        when(documentRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(documentParsingService.parseDocument(any()))
            .thenThrow(new RuntimeException("corrupt pdf"));

        assertThrows(Exception.class,
            () -> service.processDocument(pdfFile(), botId, tenantId, "doc", "u"));
    }

    @Test
    @DisplayName("bot from another tenant -> rejected")
    void crossTenantBot_rejected() {
        when(pennyBotRepository.findByIdAndTenantId(botId, tenantId))
            .thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
            () -> service.processDocument(pdfFile(), botId, tenantId, "doc", "u"));
    }

    @Test
    @DisplayName("getDocument returns null for other tenant (no existence leak)")
    void getDocument_crossTenant_null() {
        PennyKnowledgeDocument doc = PennyKnowledgeDocument.builder()
            .id(UUID.randomUUID()).botId(botId).tenantId(tenantId)
            .documentName("d").fileName("f.pdf").fileType("PDF")
            .fileSize(1L).status("COMPLETED").build();
        when(documentRepository.findById(doc.getId())).thenReturn(Optional.of(doc));

        assertNull(service.getDocument(doc.getId(), otherTenantId));
        assertNotNull(service.getDocument(doc.getId(), tenantId));
    }

    @Test
    @DisplayName("unsupported file type rejected before processing")
    void unsupportedFile_rejected() {
        when(pennyBotRepository.findByIdAndTenantId(botId, tenantId))
            .thenReturn(Optional.of(activeBot(tenantId)));
        MultipartFile exe = new MockMultipartFile("file", "evil.exe", "application/octet-stream",
            "MZ".getBytes());

        assertThrows(IllegalArgumentException.class,
            () -> service.processDocument(exe, botId, tenantId, "doc", "u"));
        verify(documentRepository, never()).save(any());
    }
}
