package com.chatbot.core.penny.service;

import com.chatbot.core.penny.kb.EmbeddingService;
import com.chatbot.core.penny.model.PennyBot;
import com.chatbot.core.penny.model.PennyKnowledgeChunk;
import com.chatbot.core.penny.model.PennyKnowledgeDocument;
import com.chatbot.core.penny.repository.PennyBotRepository;
import com.chatbot.core.penny.repository.PennyKnowledgeChunkRepository;
import com.chatbot.core.penny.repository.PennyKnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * DocumentProcessingService — Orchestrates document parsing, chunking, and embedding
 *
 * Handles the complete pipeline for uploading and processing knowledge documents:
 * 1. Parse document (PDF/DOCX/XLSX)
 * 2. Chunk text into segments
 * 3. Generate embeddings for chunks
 * 4. Store chunks in database with pgvector support
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentProcessingService {

    private final DocumentParsingService documentParsingService;
    private final TextChunkerService textChunkerService;
    private final EmbeddingService embeddingService;
    private final PennyKnowledgeDocumentRepository documentRepository;
    private final PennyKnowledgeChunkRepository chunkRepository;
    private final PennyBotRepository pennyBotRepository;

    private final Tika tika = new Tika();

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "docx", "xlsx");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/zip" // OOXML containers may be detected as generic zip
    );

    @Value("${penny.rag.max-file-size-bytes:20971520}")
    private long maxFileSizeBytes = 20_971_520L; // 20MB default when not Spring-injected

    @Value("${penny.rag.max-chunks:5000}")
    private int maxChunks = 5000;

    /**
     * Process uploaded document through the complete RAG pipeline
     * 
     * @param file Uploaded file
     * @param botId Bot UUID
     * @param tenantId Tenant ID
     * @param documentName Display name for the document
     * @param uploadedBy User who uploaded the document
     * @return Processed document entity
     *
     * NOTE: intentionally NOT @Transactional — parsing and embedding are slow
     * external operations; holding a DB transaction open for their duration
     * would pin a connection. Each repository call commits independently.
     */
    public PennyKnowledgeDocument processDocument(
            MultipartFile file,
            UUID botId,
            Long tenantId,
            String documentName,
            String uploadedBy) throws Exception {

        log.info("Starting document processing: {} for bot {}", documentName, botId);

        validateBotBelongsToTenant(botId, tenantId);
        validateFile(file);

        // Create document record
        PennyKnowledgeDocument document = PennyKnowledgeDocument.builder()
            .id(UUID.randomUUID())
            .botId(botId)
            .tenantId(tenantId)
            .documentName(documentName)
            .fileName(file.getOriginalFilename())
            .fileType(getFileType(file.getOriginalFilename()))
            .fileSize(file.getSize())
            .status("PROCESSING")
            .uploadedBy(uploadedBy)
            .build();

        document = documentRepository.save(document);

        return runProcessingPipeline(file, document, botId, tenantId);
    }

    /**
     * Run the parse → chunk → embed pipeline on an existing document record.
     * Shared by initial upload and reprocess (which must update in place).
     */
    private PennyKnowledgeDocument runProcessingPipeline(
            MultipartFile file,
            PennyKnowledgeDocument document,
            UUID botId,
            Long tenantId) throws Exception {

        document.setStatus("PROCESSING");
        document.setFileName(file.getOriginalFilename());
        document.setFileType(getFileType(file.getOriginalFilename()));
        document.setFileSize(file.getSize());
        documentRepository.save(document);

        try {
            // Step 1: Parse document
            DocumentParsingService.DocumentParseResult parseResult = documentParsingService.parseDocument(file);
            document.setTotalPages(parseResult.getTotalPages());
            documentRepository.save(document);

            log.info("Parsed document: {} pages", parseResult.getTotalPages());

            // Step 2: Chunk text
            List<TextChunkerService.TextChunk> chunks = textChunkerService.chunkDocumentPages(parseResult.getPages());
            if (chunks.size() > maxChunks) {
                log.warn("Document {} produced {} chunks, truncating to {}", document.getId(), chunks.size(), maxChunks);
                chunks = new java.util.ArrayList<>(chunks.subList(0, maxChunks));
            }
            log.info("Created {} chunks from document", chunks.size());

            // Step 3: Generate embeddings and save chunks
            int savedChunks = 0;
            int embeddedChunks = 0;
            int failedEmbeddings = 0;
            for (TextChunkerService.TextChunk chunk : chunks) {
                float[] embedding;
                try {
                    embedding = embeddingService.generateEmbedding(chunk.getText());
                } catch (Exception e) {
                    log.warn("Embedding failed for chunk {} of document {}: {}", chunk.getChunkIndex(), document.getId(), e.getMessage());
                    embedding = null;
                }
                if (embedding != null) {
                    embeddedChunks++;
                } else {
                    failedEmbeddings++;
                }
                
                PennyKnowledgeChunk chunkEntity = PennyKnowledgeChunk.builder()
                    .id(UUID.randomUUID())
                    .documentId(document.getId())
                    .botId(botId)
                    .tenantId(tenantId)
                    .chunkIndex(chunk.getChunkIndex())
                    .chunkText(chunk.getText())
                    .chunkTokens(chunk.getTokens())
                    .embedding(embedding != null ? floatArrayToString(embedding) : null)
                    .embeddingModel(embeddingService.getEmbeddingModel())
                    .pageNumber(chunk.getPageNumber())
                    .sheetName(chunk.getSheetName())
                    .build();

                chunkRepository.save(chunkEntity);
                savedChunks++;
            }

            // Update document status based on embedding outcome:
            // COMPLETED = all chunks embedded, PARTIAL = some failed,
            // FAILED = no chunk could be embedded (not searchable via vectors).
            document.setTotalChunks(savedChunks);
            document.setProcessedAt(LocalDateTime.now());
            if (savedChunks == 0) {
                document.setStatus("FAILED");
                document.setMetadata(buildMetadata(null, 0, 0, "No chunks produced"));
            } else if (embeddedChunks == 0) {
                document.setStatus("FAILED");
                document.setMetadata(buildMetadata(null, savedChunks, failedEmbeddings, "All embeddings failed"));
            } else if (failedEmbeddings > 0) {
                document.setStatus("PARTIAL");
                document.setMetadata(buildMetadata(null, savedChunks, failedEmbeddings, "Some embeddings failed"));
            } else {
                document.setStatus("COMPLETED");
                document.setMetadata(buildMetadata(null, savedChunks, 0, null));
            }
            documentRepository.save(document);

            log.info("Document processing finished: {} chunks saved, {} embedded, {} failed -> status {}",
                savedChunks, embeddedChunks, failedEmbeddings, document.getStatus());
            return document;

        } catch (Exception e) {
            log.error("❌ Error processing document: {}", e.getMessage(), e);
            document.setStatus("FAILED");
            document.setMetadata(buildMetadata(e.getClass().getSimpleName(), null, null, "Processing error"));
            documentRepository.save(document);
            throw e;
        }
    }

    /**
     * Delete document and all associated chunks
     */
    @Transactional
    public void deleteDocument(UUID documentId, Long tenantId) {
        log.info("Deleting document: {} for tenant: {}", documentId, tenantId);
        
        PennyKnowledgeDocument document = getDocument(documentId, tenantId);
        if (document == null) {
            throw new IllegalArgumentException("Document not found: " + documentId);
        }
        
        // Chunks will be deleted automatically due to CASCADE
        documentRepository.deleteById(documentId);
        
        log.info("✅ Document deleted: {}", documentId);
    }

    /**
     * Delete all documents for a bot
     */
    @Transactional
    public void deleteDocumentsByBot(UUID botId, Long tenantId) {
        log.info("Deleting all documents for bot: {} tenant: {}", botId, tenantId);
        
        validateBotBelongsToTenant(botId, tenantId);
        
        List<PennyKnowledgeDocument> documents = documentRepository.findByBotIdAndTenantId(botId, tenantId);
        for (PennyKnowledgeDocument document : documents) {
            deleteDocument(document.getId(), tenantId);
        }
        
        log.info("✅ Deleted {} documents for bot: {}", documents.size(), botId);
    }

    /**
     * Get document by ID scoped to tenant. Returns null when the document
     * does not exist or belongs to another tenant (no existence leak).
     */
    public PennyKnowledgeDocument getDocument(UUID documentId, Long tenantId) {
        return documentRepository.findById(documentId)
            .filter(doc -> tenantId != null && tenantId.equals(doc.getTenantId()))
            .orElse(null);
    }

    /**
     * Get all documents for a bot and tenant
     */
    public List<PennyKnowledgeDocument> getDocumentsByBotAndTenant(UUID botId, Long tenantId) {
        return documentRepository.findByBotIdAndTenantId(botId, tenantId);
    }

    /**
     * Re-process document (delete old chunks and regenerate)
     */
    public PennyKnowledgeDocument reprocessDocument(UUID documentId, MultipartFile file, Long tenantId) throws Exception {
        PennyKnowledgeDocument document = getDocument(documentId, tenantId);
        if (document == null) {
            throw new IllegalArgumentException("Document not found: " + documentId);
        }

        log.info("Re-processing document: {}", documentId);

        validateFile(file);

        // Delete existing chunks, then re-run pipeline on the SAME document record
        chunkRepository.deleteByDocumentId(documentId);

        return runProcessingPipeline(file, document, document.getBotId(), document.getTenantId());
    }

    /**
     * Validate uploaded file: extension, size, and detected content type.
     * Rejects files whose actual content does not match an allowed document type.
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        if (file.getSize() > maxFileSizeBytes) {
            throw new IllegalArgumentException(
                "File too large: " + file.getSize() + " bytes (max " + maxFileSizeBytes + ")");
        }

        String fileName = file.getOriginalFilename();
        String ext = getFileType(fileName);
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException(
                "Unsupported file type: " + ext + ". Allowed: PDF, DOCX, XLSX");
        }

        // Verify actual content via magic bytes / Tika detection
        try (InputStream is = file.getInputStream()) {
            String detected = tika.detect(is);
            if (detected == null || !ALLOWED_MIME_TYPES.contains(detected.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException(
                    "File content does not match an allowed document type (detected: " + detected + ")");
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not validate file content", e);
        }
    }

    /**
     * Ensure the bot exists and belongs to the given tenant.
     */
    private void validateBotBelongsToTenant(UUID botId, Long tenantId) {
        PennyBot bot = pennyBotRepository.findByIdAndTenantId(botId, tenantId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Bot not found for tenant " + tenantId + ": " + botId));
        if (!bot.isActive()) {
            throw new IllegalStateException("Bot is not active: " + botId);
        }
    }

    /**
     * Build a small JSON metadata summary. Stores only error type/counts —
     * never raw exception messages or document content.
     */
    private String buildMetadata(String errorType, Integer totalChunks, Integer failedEmbeddings, String note) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        if (errorType != null) {
            sb.append("\"errorType\":\"").append(errorType).append("\"");
            first = false;
        }
        if (totalChunks != null) {
            if (!first) sb.append(",");
            sb.append("\"totalChunks\":").append(totalChunks);
            first = false;
        }
        if (failedEmbeddings != null) {
            if (!first) sb.append(",");
            sb.append("\"failedEmbeddings\":").append(failedEmbeddings);
            first = false;
        }
        if (note != null) {
            if (!first) sb.append(",");
            sb.append("\"note\":\"").append(note.replace("\"", "'")).append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Get file type from filename
     */
    private String getFileType(String fileName) {
        if (fileName == null) {
            return "UNKNOWN";
        }
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            return fileName.substring(lastDot + 1).toUpperCase();
        }
        return "UNKNOWN";
    }

    /**
     * Convert float array to string format for pgvector storage
     */
    private String floatArrayToString(float[] array) {
        if (array == null || array.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < array.length; i++) {
            sb.append(array[i]);
            if (i < array.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
