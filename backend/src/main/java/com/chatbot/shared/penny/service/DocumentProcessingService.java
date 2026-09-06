package com.chatbot.shared.penny.service;

import com.chatbot.shared.penny.kb.EmbeddingService;
import com.chatbot.shared.penny.model.PennyKnowledgeChunk;
import com.chatbot.shared.penny.model.PennyKnowledgeDocument;
import com.chatbot.shared.penny.repository.PennyKnowledgeChunkRepository;
import com.chatbot.shared.penny.repository.PennyKnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
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

    /**
     * Process uploaded document through the complete RAG pipeline
     * 
     * @param file Uploaded file
     * @param botId Bot UUID
     * @param tenantId Tenant ID
     * @param documentName Display name for the document
     * @param uploadedBy User who uploaded the document
     * @return Processed document entity
     */
    @Transactional
    public PennyKnowledgeDocument processDocument(
            MultipartFile file,
            UUID botId,
            Long tenantId,
            String documentName,
            String uploadedBy) throws Exception {

        log.info("Starting document processing: {} for bot {}", documentName, botId);

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

        try {
            // Step 1: Parse document
            DocumentParsingService.DocumentParseResult parseResult = documentParsingService.parseDocument(file);
            document.setTotalPages(parseResult.getTotalPages());
            documentRepository.save(document);

            log.info("Parsed document: {} pages", parseResult.getTotalPages());

            // Step 2: Chunk text
            List<TextChunkerService.TextChunk> chunks = textChunkerService.chunkDocumentPages(parseResult.getPages());
            log.info("Created {} chunks from document", chunks.size());

            // Step 3: Generate embeddings and save chunks
            int savedChunks = 0;
            for (TextChunkerService.TextChunk chunk : chunks) {
                float[] embedding = embeddingService.generateEmbedding(chunk.getText());
                
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

            // Update document status
            document.setTotalChunks(savedChunks);
            document.setStatus("COMPLETED");
            document.setProcessedAt(LocalDateTime.now());
            documentRepository.save(document);

            log.info("✅ Document processing completed: {} chunks saved", savedChunks);
            return document;

        } catch (Exception e) {
            log.error("❌ Error processing document: {}", e.getMessage(), e);
            document.setStatus("FAILED");
            documentRepository.save(document);
            throw e;
        }
    }

    /**
     * Delete document and all associated chunks
     */
    @Transactional
    public void deleteDocument(UUID documentId) {
        log.info("Deleting document: {}", documentId);
        
        // Chunks will be deleted automatically due to CASCADE
        documentRepository.deleteById(documentId);
        
        log.info("✅ Document deleted: {}", documentId);
    }

    /**
     * Delete all documents for a bot
     */
    @Transactional
    public void deleteDocumentsByBot(UUID botId) {
        log.info("Deleting all documents for bot: {}", botId);
        
        List<PennyKnowledgeDocument> documents = documentRepository.findByBotId(botId);
        for (PennyKnowledgeDocument document : documents) {
            deleteDocument(document.getId());
        }
        
        log.info("✅ Deleted {} documents for bot: {}", documents.size(), botId);
    }

    /**
     * Get document by ID
     */
    public PennyKnowledgeDocument getDocument(UUID documentId) {
        return documentRepository.findById(documentId).orElse(null);
    }

    /**
     * Get all documents for a bot
     */
    public List<PennyKnowledgeDocument> getDocumentsByBot(UUID botId) {
        return documentRepository.findByBotId(botId);
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
    @Transactional
    public PennyKnowledgeDocument reprocessDocument(UUID documentId, MultipartFile file) throws Exception {
        PennyKnowledgeDocument document = getDocument(documentId);
        if (document == null) {
            throw new IllegalArgumentException("Document not found: " + documentId);
        }

        log.info("Re-processing document: {}", documentId);

        // Delete existing chunks
        chunkRepository.deleteByDocumentId(documentId);

        // Re-process with new file
        return processDocument(file, document.getBotId(), document.getTenantId(), 
            document.getDocumentName(), document.getUploadedBy());
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
