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

    // SỬA PHONE_PATTERN: Đơn giản hóa. Tìm (0 hoặc +84) theo sau là 9 đến 15 ký tự là số, khoảng trắng, chấm hoặc gạch ngang.
    // \d tương đương [0-9]. Dùng \b để bắt chuỗi số độc lập.
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "\\b(0|\\+84)[\\d\\s.-]{9,15}\\b" 
    );
    
    /**
     * 🎯 Chỉ trích xuất SỐ ĐIỆN THOẠI.
     */
    public CustomerInfo extractInfo(String text) {
        if (text == null || text.isBlank()) return new CustomerInfo();
        
        // Chuẩn hóa văn bản đầu vào cho an toàn
        String lowerCaseText = text.toLowerCase(Locale.ROOT);
        
        // 1. TRÍCH XUẤT PHONE
        String phone = extractPhone(lowerCaseText); 
        
        // Trả về CustomerInfo (name, phone, email) với name và email là null
        return new CustomerInfo(null, phone, null); 
    }
    
    /**
     * Trích xuất SĐT và chuẩn hóa nó (chỉ giữ lại số và dấu +)
     */
    private String extractPhone(String text) {
        log.info("📞 Extract message='{}'", text);

        if (text == null || text.isBlank()) return null;
        String cleaned = text.replaceAll("[^0-9+]", ""); // chỉ giữ số và dấu +
        Matcher matcher = PHONE_PATTERN.matcher(text);
        String rawPhone = null;

        // Ưu tiên khớp theo pattern chính
        if (matcher.find()) {
            rawPhone = matcher.group().replaceAll("[^0-9+]", "");
        } else if (cleaned.matches("^(0|\\+84)[0-9]{8,12}$")) {
            rawPhone = cleaned;
        }

        // Chuẩn hóa: thay +84 thành 0 cho đồng nhất (tùy bạn)
        if (rawPhone != null) {
            rawPhone = rawPhone.replaceFirst("^\\+84", "0");
            if (rawPhone.length() >= 9 && rawPhone.length() <= 11) {
                log.info("📞 Extracted phone={} from message='{}'", rawPhone, text);
                return rawPhone;
            }
        }
        log.info("📞 No phone to extract");

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