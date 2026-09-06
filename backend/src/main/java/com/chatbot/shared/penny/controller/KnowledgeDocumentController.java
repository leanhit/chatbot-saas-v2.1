package com.chatbot.shared.penny.controller;

import com.chatbot.shared.penny.model.PennyKnowledgeDocument;
import com.chatbot.shared.penny.service.DocumentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

/**
 * KnowledgeDocumentController — REST API for knowledge document management
 *
 * Provides endpoints for uploading, managing, and deleting knowledge documents
 * for RAG (Retrieval Augmented Generation).
 */
@RestController
@RequestMapping("/api/v1/penny/knowledge-base")
@RequiredArgsConstructor
@Slf4j
public class KnowledgeDocumentController {

    private final DocumentProcessingService documentProcessingService;

    /**
     * Upload and process a knowledge document
     * POST /api/v1/penny/knowledge-base/upload
     */
    @PostMapping("/upload")
    @PreAuthorize("hasAuthority('TENANT_ADMIN') or hasAuthority('BOT_OWNER')")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("botId") UUID botId,
            @RequestParam("tenantId") Long tenantId,
            @RequestParam("documentName") String documentName,
            @RequestParam(value = "uploadedBy", required = false) String uploadedBy) {

        try {
            log.info("Upload request: document={}, bot={}, tenant={}", documentName, botId, tenantId);

            // Validate file type
            String fileName = file.getOriginalFilename();
            if (fileName == null || (!fileName.toLowerCase().endsWith(".pdf") 
                    && !fileName.toLowerCase().endsWith(".docx") 
                    && !fileName.toLowerCase().endsWith(".xlsx"))) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("Invalid file type. Only PDF, DOCX, and XLSX are supported.")
                );
            }

            // Validate file size (max 50MB)
            if (file.getSize() > 50 * 1024 * 1024) {
                return ResponseEntity.badRequest().body(
                    new ErrorResponse("File size exceeds 50MB limit.")
                );
            }

            PennyKnowledgeDocument document = documentProcessingService.processDocument(
                file, botId, tenantId, documentName, uploadedBy);

            return ResponseEntity.ok(new UploadResponse(
                document.getId(),
                document.getDocumentName(),
                document.getStatus(),
                document.getTotalPages(),
                document.getTotalChunks(),
                "Document uploaded and processed successfully"
            ));

        } catch (Exception e) {
            log.error("Error uploading document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to process document: " + e.getMessage()));
        }
    }

    /**
     * Get all documents for a bot
     * GET /api/v1/penny/knowledge-base/documents?botId={botId}&tenantId={tenantId}
     */
    @GetMapping("/documents")
    @PreAuthorize("hasAuthority('TENANT_ADMIN') or hasAuthority('BOT_OWNER')")
    public ResponseEntity<?> getDocuments(
            @RequestParam UUID botId,
            @RequestParam Long tenantId) {

        try {
            List<PennyKnowledgeDocument> documents = documentProcessingService
                .getDocumentsByBotAndTenant(botId, tenantId);
            return ResponseEntity.ok(documents);
        } catch (Exception e) {
            log.error("Error fetching documents: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to fetch documents: " + e.getMessage()));
        }
    }

    /**
     * Get a specific document by ID
     * GET /api/v1/penny/knowledge-base/documents/{documentId}
     */
    @GetMapping("/documents/{documentId}")
    @PreAuthorize("hasAuthority('TENANT_ADMIN') or hasAuthority('BOT_OWNER')")
    public ResponseEntity<?> getDocument(@PathVariable UUID documentId) {
        try {
            PennyKnowledgeDocument document = documentProcessingService.getDocument(documentId);
            if (document == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            log.error("Error fetching document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to fetch document: " + e.getMessage()));
        }
    }

    /**
     * Delete a document
     * DELETE /api/v1/penny/knowledge-base/documents/{documentId}
     */
    @DeleteMapping("/documents/{documentId}")
    @PreAuthorize("hasAuthority('TENANT_ADMIN') or hasAuthority('BOT_OWNER')")
    public ResponseEntity<?> deleteDocument(@PathVariable UUID documentId) {
        try {
            documentProcessingService.deleteDocument(documentId);
            return ResponseEntity.ok(new SuccessResponse("Document deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to delete document: " + e.getMessage()));
        }
    }

    /**
     * Delete all documents for a bot
     * DELETE /api/v1/penny/knowledge-base/documents/bot/{botId}
     */
    @DeleteMapping("/documents/bot/{botId}")
    @PreAuthorize("hasAuthority('TENANT_ADMIN') or hasAuthority('BOT_OWNER')")
    public ResponseEntity<?> deleteDocumentsByBot(@PathVariable UUID botId) {
        try {
            documentProcessingService.deleteDocumentsByBot(botId);
            return ResponseEntity.ok(new SuccessResponse("All documents for bot deleted successfully"));
        } catch (Exception e) {
            log.error("Error deleting documents for bot: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to delete documents: " + e.getMessage()));
        }
    }

    /**
     * Re-process a document
     * POST /api/v1/penny/knowledge-base/documents/{documentId}/reprocess
     */
    @PostMapping("/documents/{documentId}/reprocess")
    @PreAuthorize("hasAuthority('TENANT_ADMIN') or hasAuthority('BOT_OWNER')")
    public ResponseEntity<?> reprocessDocument(
            @PathVariable UUID documentId,
            @RequestParam("file") MultipartFile file) {

        try {
            PennyKnowledgeDocument document = documentProcessingService.reprocessDocument(documentId, file);
            return ResponseEntity.ok(new UploadResponse(
                document.getId(),
                document.getDocumentName(),
                document.getStatus(),
                document.getTotalPages(),
                document.getTotalChunks(),
                "Document reprocessed successfully"
            ));
        } catch (Exception e) {
            log.error("Error reprocessing document: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Failed to reprocess document: " + e.getMessage()));
        }
    }

    // Response DTOs

    public record ErrorResponse(String error) {}
    public record SuccessResponse(String message) {}
    public record UploadResponse(
        UUID documentId,
        String documentName,
        String status,
        Integer totalPages,
        Integer totalChunks,
        String message
    ) {}
}
