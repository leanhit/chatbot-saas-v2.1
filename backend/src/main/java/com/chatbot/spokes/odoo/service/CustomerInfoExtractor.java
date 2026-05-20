package com.chatbot.spokes.odoo.service;

import com.chatbot.spokes.odoo.model.CustomerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CustomerInfoExtractor {

    // Regex tìm số điện thoại Việt Nam (0... hoặc +84...) có khoảng trắng, chấm, gạch ngang
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "(?:\\+84|0)(?:\\s*\\d){9,10}\\b"
    );

    // Regex tìm email chuẩn
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );
    
    /**
     * 🎯 Trích xuất SỐ ĐIỆN THOẠI và EMAIL từ tin nhắn.
     */
    public CustomerInfo extractInfo(String text) {
        if (text == null || text.isBlank()) return new CustomerInfo();
        
        String phone = extractPhone(text); 
        String email = extractEmail(text);
        
        return new CustomerInfo(null, phone, email); 
    }
    
    /**
     * Trích xuất SĐT và chuẩn hóa nó
     */
    private String extractPhone(String text) {
        log.debug("🔍 Extract phone from text='{}'", text);
        if (text == null || text.isBlank()) return null;

        // Chuẩn hóa văn bản bằng cách xóa các ký tự gạch nối, chấm để gom chuỗi số lại gần nhau
        String normalizedText = text.replaceAll("[-.]", " ");
        Matcher matcher = PHONE_PATTERN.matcher(normalizedText);
        if (matcher.find()) {
            String rawPhone = matcher.group().replaceAll("\\s+", "");
            if (rawPhone.startsWith("+84")) {
                rawPhone = "0" + rawPhone.substring(3);
            }
            if (rawPhone.length() >= 10 && rawPhone.length() <= 11) {
                log.info("📞 Extracted phone={} from text='{}'", rawPhone, text);
                return rawPhone;
            }
        }

        // Dự phòng (fallback) nếu đầu vào chỉ chứa mỗi chuỗi số sạch
        String cleaned = text.replaceAll("[^0-9+]", "");
        if (cleaned.matches("^(0|\\+84)[0-9]{9,10}$")) {
            String rawPhone = cleaned.startsWith("+84") ? "0" + cleaned.substring(3) : cleaned;
            log.info("📞 Extracted phone (fallback)={} from text='{}'", rawPhone, text);
            return rawPhone;
        }

        return null;
    }

    /**
     * Trích xuất Email
     */
    private String extractEmail(String text) {
        log.debug("🔍 Extract email from text='{}'", text);
        if (text == null || text.isBlank()) return null;

        Matcher matcher = EMAIL_PATTERN.matcher(text);
        if (matcher.find()) {
            String email = matcher.group().toLowerCase(Locale.ROOT).trim();
            log.info("📧 Extracted email={} from text='{}'", email, text);
            return email;
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // HÀM TIỆN ÍCH CHUNG (Giữ nguyên)
    // -------------------------------------------------------------------------

    public String toTitleCase(String input) {
        if (input == null || input.isBlank()) return null;
        return Arrays.stream(input.trim().split("\\s+"))
                .map(s -> {
                    if (s.isEmpty()) return "";
                    if (s.length() <= 3) return s.toUpperCase(Locale.ROOT); 
                    return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1).toLowerCase(Locale.ROOT);
                })
                .collect(Collectors.joining(" "));
    }
}