package com.chatbot.shared.penny.service;

import com.chatbot.shared.penny.context.ConversationContext;
import com.chatbot.shared.penny.dto.request.MiddlewareRequest;
import com.chatbot.shared.penny.routing.dto.IntentAnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class IntentAnalyzerVnTest {

    private IntentAnalyzer intentAnalyzer;
    private ConversationContext context;

    @BeforeEach
    public void setUp() {
        intentAnalyzer = new IntentAnalyzer();
        context = ConversationContext.builder()
            .contextId("test-context-id")
            .userId("test-user-id")
            .platform("test-platform")
            .build();
    }

    @Test
    public void testAccentedVietnameseIntent() {
        // Given
        MiddlewareRequest request = MiddlewareRequest.builder()
            .message("Kiểm tra đơn hàng giúp mình với")
            .language("vi")
            .build();

        // When
        IntentAnalysisResult result = intentAnalyzer.analyze(request, context);

        // Then
        assertEquals("order_inquiry", result.getPrimaryIntent());
        assertTrue(result.getConfidence() >= 0.7);
    }

    @Test
    public void testUnaccentedVietnameseIntent() {
        // Given - gõ không dấu
        MiddlewareRequest request = MiddlewareRequest.builder()
            .message("kiem tra don hang giup minh voi")
            .language("vi")
            .build();

        // When
        IntentAnalysisResult result = intentAnalyzer.analyze(request, context);

        // Then
        assertEquals("order_inquiry", result.getPrimaryIntent());
        assertTrue(result.getConfidence() >= 0.7);
    }

    @Test
    public void testOldStyleAccentsIntent() {
        // Given - gõ đặt dấu kiểu cũ (hoà quả vs hòa quả)
        MiddlewareRequest request = MiddlewareRequest.builder()
            .message("cần hỗ trợ kiểm tra đơn hàng gấp")
            .language("vi")
            .build();

        // When
        IntentAnalysisResult result = intentAnalyzer.analyze(request, context);

        // Then
        assertTrue(result.getAllIntents().contains("order_inquiry") || result.getAllIntents().contains("customer_support"));
    }

    @Test
    public void testPriceEntityExtractionAccentedAndUnaccented() {
        // Given - accented price
        MiddlewareRequest requestAccented = MiddlewareRequest.builder()
            .message("Sản phẩm này có giá 500 nghìn đúng không?")
            .build();

        // When
        IntentAnalysisResult resultAccented = intentAnalyzer.analyze(requestAccented, context);

        // Then
        assertTrue(resultAccented.getEntities().containsKey("price"));
        assertEquals("price_inquiry", resultAccented.getPrimaryIntent());

        // Given - unaccented price
        MiddlewareRequest requestUnaccented = MiddlewareRequest.builder()
            .message("san pham nay co gia 500 nghin dung khong?")
            .build();

        // When
        IntentAnalysisResult resultUnaccented = intentAnalyzer.analyze(requestUnaccented, context);

        // Then
        assertTrue(resultUnaccented.getEntities().containsKey("price"));
        assertEquals("price_inquiry", resultUnaccented.getPrimaryIntent());
    }

    @Test
    public void testLocationEntityExtraction() {
        // Given - accented location
        MiddlewareRequest request1 = MiddlewareRequest.builder()
            .message("địa chỉ cửa hàng ở đâu thế?")
            .build();

        // When
        IntentAnalysisResult result1 = intentAnalyzer.analyze(request1, context);

        // Then
        assertTrue(result1.getEntities().containsKey("location"));

        // Given - unaccented location
        MiddlewareRequest request2 = MiddlewareRequest.builder()
            .message("dia chi cua hang o dau the?")
            .build();

        // When
        IntentAnalysisResult result2 = intentAnalyzer.analyze(request2, context);

        // Then
        assertTrue(result2.getEntities().containsKey("location"));
    }
}
