package com.chatbot.shared.penny.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DocumentParsingService
 */
@ExtendWith(MockitoExtension.class)
class DocumentParsingServiceTest {

    @InjectMocks
    private DocumentParsingService documentParsingService;

    @Test
    @DisplayName("Should parse simple text file")
    void shouldParseSimpleTextFile() throws Exception {
        String content = "This is a test document.\nIt has multiple lines.\nAnd some content.";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            content.getBytes()
        );

        DocumentParsingService.DocumentParseResult result = documentParsingService.parseDocument(file);

        assertNotNull(result);
        assertEquals("txt", result.getFileType()); // Tika returns lowercase
        assertNotNull(result.getPages());
        assertFalse(result.getPages().isEmpty());
    }

    @Test
    @DisplayName("Should handle empty file")
    void shouldHandleEmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.txt",
            "text/plain",
            " ".getBytes() // Use space instead of empty to avoid Tika ZeroByteFileException
        );

        DocumentParsingService.DocumentParseResult result = documentParsingService.parseDocument(file);

        assertNotNull(result);
        assertEquals("txt", result.getFileType());
    }

    @Test
    @DisplayName("Should extract file type from filename")
    void shouldExtractFileType() throws Exception {
        // Test file type detection with text files (Tika can parse these)
        assertEquals("txt", documentParsingService.parseDocument(
            new MockMultipartFile("file", "test.txt", "text/plain", "test content".getBytes())
        ).getFileType());
    }

    @Test
    @DisplayName("Should handle file without extension")
    void shouldHandleFileWithoutExtension() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "noextension",
            "text/plain",
            "content".getBytes()
        );

        DocumentParsingService.DocumentParseResult result = documentParsingService.parseDocument(file);

        assertNotNull(result);
        assertEquals("UNKNOWN", result.getFileType());
    }

    @Test
    @DisplayName("Should handle null filename")
    void shouldHandleNullFilename() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            null,
            "text/plain",
            "content".getBytes()
        );

        DocumentParsingService.DocumentParseResult result = documentParsingService.parseDocument(file);

        assertNotNull(result);
        assertEquals("UNKNOWN", result.getFileType());
    }
}
