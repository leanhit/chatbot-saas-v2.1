package com.chatbot.shared.penny.service;

import com.chatbot.shared.penny.context.ConversationContext;
import com.chatbot.shared.penny.dto.request.MiddlewareRequest;
import com.chatbot.shared.penny.routing.dto.IntentAnalysisResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for IntentAnalyzer
 */
@ExtendWith(MockitoExtension.class)
class IntentAnalyzerTest {

    @InjectMocks
    private IntentAnalyzer intentAnalyzer;

    private MiddlewareRequest request;
    private ConversationContext context;

    @BeforeEach
    void setUp() {
        request = MiddlewareRequest.builder()
            .requestId("test-request")
            .userId("test-user")
            .message("xin chào")
            .platform("facebook")
            .botId(UUID.randomUUID().toString())
            .timestamp(Instant.now())
            .build();

        context = ConversationContext.builder()
            .botId(UUID.randomUUID().toString())
            .userId("test-user")
            .platform("facebook")
            .createdAt(Instant.now())
            .lastUpdated(Instant.now())
            .messageCount(0)
            .build();
    }

    @Test
    @DisplayName("Should detect greeting intent")
    void shouldDetectGreetingIntent() {
        request.setMessage("xin chào");
        IntentAnalysisResult result = intentAnalyzer.analyze(request, context);
        
        assertEquals("greeting", result.getPrimaryIntent());
        assertTrue(result.getConfidence() > 0.5);
    }

    @Test
    @DisplayName("Should detect order inquiry intent")
    void shouldDetectOrderInquiryIntent() {
        request.setMessage("kiểm tra tình trạng đơn hàng của tôi");
        IntentAnalysisResult result = intentAnalyzer.analyze(request, context);
        
        assertEquals("order_inquiry", result.getPrimaryIntent());
    }

    @Test
    @DisplayName("Should detect price inquiry intent")
    void shouldDetectPriceInquiryIntent() {
        request.setMessage("sản phẩm này giá bao nhiêu");
        IntentAnalysisResult result = intentAnalyzer.analyze(request, context);
        
        assertEquals("price_inquiry", result.getPrimaryIntent());
    }

    @Test
    @DisplayName("Should extract phone number entity")
    void shouldExtractPhoneNumberEntity() {
        request.setMessage("số điện thoại của tôi là 0901234567");
        IntentAnalysisResult result = intentAnalyzer.analyze(request, context);
        
        assertTrue(result.getEntities().containsKey("phone_number"));
        assertEquals("0901234567", result.getEntities().get("phone_number"));
    }

    @Test
    @DisplayName("Should extract email entity")
    void shouldExtractEmailEntity() {
        request.setMessage("email của tôi là test@example.com");
        IntentAnalysisResult result = intentAnalyzer.analyze(request, context);
        
        assertTrue(result.getEntities().containsKey("email"));
        assertEquals("test@example.com", result.getEntities().get("email"));
    }

    @Test
    @DisplayName("Should extract price entity")
    void shouldExtractPriceEntity() {
        request.setMessage("giá sản phẩm là 500000 vnđ");
        IntentAnalysisResult result = intentAnalyzer.analyze(request, context);
        
        assertTrue(result.getEntities().containsKey("price"));
    }

    @Test
    @DisplayName("Should detect Vietnamese language")
    void shouldDetectVietnameseLanguage() {
        request.setMessage("xin chào bạn");
        IntentAnalysisResult result = intentAnalyzer.analyze(request, context);
        
        assertEquals("vi", result.getLanguage());
    }

    @Test
    @DisplayName("Should calculate complexity correctly")
    void shouldCalculateComplexityCorrectly() {
        request.setMessage("a".repeat(100)); // Long message
        IntentAnalysisResult result = intentAnalyzer.analyze(request, context);
        
        assertEquals("high", result.getComplexity());
    }

    @Test
    @DisplayName("Should handle empty message")
    void shouldHandleEmptyMessage() {
        request.setMessage("");
        IntentAnalysisResult result = intentAnalyzer.analyze(request, context);
        
        assertEquals("general_chat", result.getPrimaryIntent());
    }

    @Test
    @DisplayName("Should detect follow-up intent based on context")
    void shouldDetectFollowUpIntent() {
        context.setLastIntent("order_inquiry");
        request.setMessage("đúng");
        IntentAnalysisResult result = intentAnalyzer.analyze(request, context);
        
        assertEquals("order_inquiry_followup", result.getPrimaryIntent());
    }

    @Test
    @DisplayName("Should extract order ID entity")
    void shouldExtractOrderIdEntity() {
        request.setMessage("kiểm tra đơn hàng #ABC12345");
        IntentAnalysisResult result = intentAnalyzer.analyze(request, context);
        
        assertTrue(result.getEntities().containsKey("order_id"));
    }

    @Test
    @DisplayName("Should detect customer support intent")
    void shouldDetectCustomerSupportIntent() {
        request.setMessage("tôi cần hỗ trợ");
        IntentAnalysisResult result = intentAnalyzer.analyze(request, context);
        
        assertEquals("customer_support", result.getPrimaryIntent());
    }

    @Test
    @DisplayName("Should detect gratitude intent")
    void shouldDetectGratitudeIntent() {
        request.setMessage("cảm ơn bạn rất nhiều");
        IntentAnalysisResult result = intentAnalyzer.analyze(request, context);
        
        assertEquals("gratitude", result.getPrimaryIntent());
    }

    @Test
    @DisplayName("Should handle unaccented Vietnamese")
    void shouldHandleUnaccentedVietnamese() {
        request.setMessage("xin chao ban");
        IntentAnalysisResult result = intentAnalyzer.analyze(request, context);
        
        assertEquals("greeting", result.getPrimaryIntent());
    }
}
