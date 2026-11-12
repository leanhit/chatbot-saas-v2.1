package com.chatbot.core.penny.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for chunking text into smaller segments for RAG
 * Chunk size: 500-800 tokens with 100 token overlap
 */
@Slf4j
@Service
public class TextChunkerService {

    private static final int MIN_CHUNK_SIZE = 500;
    private static final int MAX_CHUNK_SIZE = 800;
    private static final int OVERLAP_SIZE = 100;
    // Conservative fallback; estimateTokens() uses language-aware heuristic.
    private static final int APPROX_CHARS_PER_TOKEN_ASCII = 4;
    private static final int APPROX_CHARS_PER_TOKEN_VIETNAMESE = 3; // used for overlap sizing

    /**
     * Chunk text with metadata
     */
    public List<TextChunk> chunkText(String text, int pageNumber, String sheetName) {
        List<TextChunk> chunks = new ArrayList<>();
        
        if (text == null || text.trim().isEmpty()) {
            return chunks;
        }

        // Split text into paragraphs first to maintain context
        String[] paragraphs = text.split("\\n\\s*\\n");
        StringBuilder currentChunk = new StringBuilder();
        int currentTokens = 0;
        int chunkIndex = 0;

        for (String paragraph : paragraphs) {
            String trimmedParagraph = paragraph.trim();
            if (trimmedParagraph.isEmpty()) {
                continue;
            }

            int paragraphTokens = estimateTokens(trimmedParagraph);

            // If adding this paragraph exceeds max chunk size, save current chunk and start new
            if (currentTokens + paragraphTokens > MAX_CHUNK_SIZE && currentTokens > MIN_CHUNK_SIZE) {
                chunks.add(createChunk(currentChunk.toString(), chunkIndex++, pageNumber, sheetName, currentTokens));
                
                // Start new chunk with overlap
                String overlapText = getOverlapText(currentChunk.toString());
                currentChunk = new StringBuilder(overlapText);
                currentTokens = estimateTokens(overlapText);
            }

            // Add paragraph to current chunk
            if (currentChunk.length() > 0) {
                currentChunk.append("\n\n");
            }
            currentChunk.append(trimmedParagraph);
            currentTokens += paragraphTokens;
        }

        // Add remaining text as final chunk
        if (currentChunk.length() > 0) {
            chunks.add(createChunk(currentChunk.toString(), chunkIndex, pageNumber, sheetName, currentTokens));
        }

        log.info("Chunked text into {} chunks (page: {}, sheet: {})", chunks.size(), pageNumber, sheetName);
        return chunks;
    }

    /**
     * Chunk text from parsed document pages
     */
    public List<TextChunk> chunkDocumentPages(List<DocumentParsingService.PageContent> pages) {
        List<TextChunk> allChunks = new ArrayList();
        int globalChunkIndex = 0;

        for (DocumentParsingService.PageContent page : pages) {
            List<TextChunk> pageChunks = chunkText(page.getText(), page.getPageNumber(), page.getSheetName());
            
            for (TextChunk chunk : pageChunks) {
                chunk.setChunkIndex(globalChunkIndex++);
            }
            
            allChunks.addAll(pageChunks);
        }

        log.info("Total chunks created from {} pages: {}", pages.size(), allChunks.size());
        return allChunks;
    }

    /**
     * Create a TextChunk object
     */
    private TextChunk createChunk(String text, int index, int pageNumber, String sheetName, int tokens) {
        TextChunk chunk = new TextChunk();
        chunk.setChunkIndex(index);
        chunk.setText(text);
        chunk.setTokens(tokens);
        chunk.setPageNumber(pageNumber);
        chunk.setSheetName(sheetName);
        return chunk;
    }

    /**
     * Get overlap text from the end of current chunk
     */
    private String getOverlapText(String text) {
        // Use the Vietnamese-aware chars-per-token estimate for overlap sizing
        // so we don't under-overlap on Vietnamese documents.
        int overlapChars = OVERLAP_SIZE * APPROX_CHARS_PER_TOKEN_VIETNAMESE;
        if (text.length() <= overlapChars) {
            return text;
        }
        return text.substring(text.length() - overlapChars);
    }

    /**
     * Estimate token count with language-aware heuristic.
     *
     * <p>Vietnamese text has many diacritic characters (each represented as a
     * multi-byte Unicode code point) so the byte-to-token ratio is lower than
     * plain ASCII English.
     *
     * <ul>
     *   <li>Vietnamese (high non-ASCII ratio): ~2.5 chars/token</li>
     *   <li>English / ASCII:                   ~4.0 chars/token</li>
     *   <li>Mixed (default):                   ~3.0 chars/token</li>
     * </ul>
     *
     * Note: this is still an approximation. For exact counts per model use a
     * proper tokenizer such as <a href="https://github.com/knuddelsgmbh/jtokkit">jtokkit</a>.
     */
    private int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        long nonAsciiCount = text.chars()
                .filter(c -> c > 127)
                .count();
        double nonAsciiRatio = (double) nonAsciiCount / text.length();

        // Pick chars-per-token based on detected language mix
        double charsPerToken;
        if (nonAsciiRatio > 0.4) {
            charsPerToken = 2.5; // predominantly Vietnamese
        } else if (nonAsciiRatio > 0.15) {
            charsPerToken = 3.0; // mixed Vietnamese/English
        } else {
            charsPerToken = 4.0; // predominantly English / ASCII
        }
        return (int) Math.ceil(text.length() / charsPerToken) + 1;
    }

    /**
     * Text chunk with metadata
     */
    public static class TextChunk {
        private int chunkIndex;
        private String text;
        private int tokens;
        private Integer pageNumber;
        private String sheetName;

        public int getChunkIndex() {
            return chunkIndex;
        }

        public void setChunkIndex(int chunkIndex) {
            this.chunkIndex = chunkIndex;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public int getTokens() {
            return tokens;
        }

        public void setTokens(int tokens) {
            this.tokens = tokens;
        }

        public Integer getPageNumber() {
            return pageNumber;
        }

        public void setPageNumber(Integer pageNumber) {
            this.pageNumber = pageNumber;
        }

        public String getSheetName() {
            return sheetName;
        }

        public void setSheetName(String sheetName) {
            this.sheetName = sheetName;
        }
    }
}
