package com.chatbot.shared.penny.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VietnameseTextNormalizerTest {

    @Test
    public void testNormalize() {
        // Test basic lowercase and trim
        assertEquals("tiếng việt", VietnameseTextNormalizer.normalize("  Tiếng Việt  "));
        
        // Test old style tone mark to new style tone mark
        assertEquals("hòa", VietnameseTextNormalizer.normalize("hoà"));
        assertEquals("hóa", VietnameseTextNormalizer.normalize("hoá"));
        assertEquals("hủy", VietnameseTextNormalizer.normalize("huỷ"));
        assertEquals("tủy", VietnameseTextNormalizer.normalize("tuỷ"));
        assertEquals("khỏe", VietnameseTextNormalizer.normalize("khoẻ"));
    }

    @Test
    public void testRemoveAccents() {
        // Test removing accents
        assertEquals("tieng viet rat dep!", VietnameseTextNormalizer.removeAccents("Tiếng Việt rất đẹp!"));
        assertEquals("kiem tra don hang", VietnameseTextNormalizer.removeAccents("Kiểm tra đơn hàng"));
        assertEquals("nho go khong dau", VietnameseTextNormalizer.removeAccents("Nhớ gõ không dấu"));
        assertEquals("hoa qua son tra", VietnameseTextNormalizer.removeAccents("hoà quả sơn trà"));
    }

    @Test
    public void testNullInput() {
        assertNull(VietnameseTextNormalizer.normalize(null));
        assertNull(VietnameseTextNormalizer.removeAccents(null));
    }
}
