package com.chatbot.shared.penny.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for parsing documents (PDF, DOCX, XLSX) using Apache Tika, PDFBox, and POI
 */
@Slf4j
@Service
public class DocumentParsingService {

    private final Tika tika = new Tika();

    /**
     * Parse document and extract text with metadata
     */
    public DocumentParseResult parseDocument(MultipartFile file) throws IOException, TikaException {
        String fileName = file.getOriginalFilename();
        String fileType = getFileType(fileName);

        log.info("Parsing document: {}, type: {}", fileName, fileType);

        switch (fileType.toUpperCase()) {
            case "PDF":
                return parsePdf(file);
            case "DOCX":
                return parseDocx(file);
            case "XLSX":
                return parseXlsx(file);
            default:
                // Fallback to Tika for other formats
                return parseWithTika(file);
        }
    }

    /**
     * Parse PDF document using Apache PDFBox
     */
    private DocumentParseResult parsePdf(MultipartFile file) throws IOException {
        DocumentParseResult result = new DocumentParseResult();
        result.setFileType("PDF");

        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {

            int totalPages = document.getNumberOfPages();
            result.setTotalPages(totalPages);

            PDFTextStripper stripper = new PDFTextStripper();
            List<PageContent> pages = new ArrayList<>();

            for (int pageNum = 1; pageNum <= totalPages; pageNum++) {
                stripper.setStartPage(pageNum);
                stripper.setEndPage(pageNum);
                String pageText = stripper.getText(document).trim();

                if (!pageText.isEmpty()) {
                    PageContent page = new PageContent();
                    page.setPageNumber(pageNum);
                    page.setText(pageText);
                    pages.add(page);
                }
            }

            result.setPages(pages);
            log.info("Parsed PDF with {} pages, extracted {} pages with content", totalPages, pages.size());
        }

        return result;
    }

    /**
     * Parse DOCX document using Apache POI
     */
    private DocumentParseResult parseDocx(MultipartFile file) throws IOException {
        DocumentParseResult result = new DocumentParseResult();
        result.setFileType("DOCX");

        try (InputStream inputStream = file.getInputStream();
             XWPFDocument document = new XWPFDocument(inputStream)) {

            List<PageContent> pages = new ArrayList<>();
            StringBuilder fullText = new StringBuilder();

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText().trim();
                if (!text.isEmpty()) {
                    fullText.append(text).append("\n");
                }
            }

            // Treat entire document as single page for DOCX
            PageContent page = new PageContent();
            page.setPageNumber(1);
            page.setText(fullText.toString());
            pages.add(page);

            result.setPages(pages);
            result.setTotalPages(1);
            log.info("Parsed DOCX with {} characters", fullText.length());
        }

        return result;
    }

    /**
     * Parse XLSX document using Apache POI
     */
    private DocumentParseResult parseXlsx(MultipartFile file) throws IOException {
        DocumentParseResult result = new DocumentParseResult();
        result.setFileType("XLSX");

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(inputStream)) {

            List<PageContent> pages = new ArrayList<>();
            int totalSheets = workbook.getNumberOfSheets();

            for (int sheetIndex = 0; sheetIndex < totalSheets; sheetIndex++) {
                Sheet sheet = workbook.getSheetAt(sheetIndex);
                StringBuilder sheetText = new StringBuilder();
                sheetText.append("Sheet: ").append(sheet.getSheetName()).append("\n");

                for (Row row : sheet) {
                    StringBuilder rowText = new StringBuilder();
                    for (Cell cell : row) {
                        String cellValue = getCellValueAsString(cell);
                        if (cellValue != null && !cellValue.isEmpty()) {
                            rowText.append(cellValue).append(" | ");
                        }
                    }
                    if (rowText.length() > 0) {
                        sheetText.append(rowText.toString()).append("\n");
                    }
                }

                PageContent page = new PageContent();
                page.setPageNumber(sheetIndex + 1);
                page.setSheetName(sheet.getSheetName());
                page.setText(sheetText.toString());
                pages.add(page);
            }

            result.setPages(pages);
            result.setTotalPages(totalSheets);
            log.info("Parsed XLSX with {} sheets", totalSheets);
        }

        return result;
    }

    /**
     * Fallback parser using Apache Tika
     */
    private DocumentParseResult parseWithTika(MultipartFile file) throws IOException, TikaException {
        DocumentParseResult result = new DocumentParseResult();
        result.setFileType(getFileType(file.getOriginalFilename()));

        String text = tika.parseToString(file.getInputStream());
        
        PageContent page = new PageContent();
        page.setPageNumber(1);
        page.setText(text);
        
        List<PageContent> pages = new ArrayList<>();
        pages.add(page);
        
        result.setPages(pages);
        result.setTotalPages(1);
        
        log.info("Parsed document with Tika, extracted {} characters", text.length());
        return result;
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
            return fileName.substring(lastDot + 1);
        }
        return "UNKNOWN";
    }

    /**
     * Get cell value as string
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                } else {
                    yield String.valueOf(cell.getNumericCellValue());
                }
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getCellFormula();
            default -> "";
        };
    }

    /**
     * Result class for document parsing
     */
    public static class DocumentParseResult {
        private String fileType;
        private int totalPages;
        private List<PageContent> pages;

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        public List<PageContent> getPages() {
            return pages;
        }

        public void setPages(List<PageContent> pages) {
            this.pages = pages;
        }
    }

    /**
     * Page content with metadata
     */
    public static class PageContent {
        private int pageNumber;
        private String sheetName;
        private String text;

        public int getPageNumber() {
            return pageNumber;
        }

        public void setPageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
        }

        public String getSheetName() {
            return sheetName;
        }

        public void setSheetName(String sheetName) {
            this.sheetName = sheetName;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }
}
