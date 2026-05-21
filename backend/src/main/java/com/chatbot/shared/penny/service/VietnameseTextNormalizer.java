package com.chatbot.shared.penny.service;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Tiện ích chuẩn hóa và xử lý văn bản Tiếng Việt
 */
public class VietnameseTextNormalizer {

    private static final Pattern DIACRITICAL_MARKS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    
    // Bản đồ chuẩn hóa dấu thanh kiểu cũ sang kiểu mới (Chuẩn hóa nhất quán về kiểu mới)
    private static final Map<String, String> TONE_MARK_MAP = new HashMap<>();
    
    static {
        // oà -> òa, oá -> óa, oả -> ỏa, oã -> õa, oạ -> ọa
        TONE_MARK_MAP.put("oà", "òa");
        TONE_MARK_MAP.put("oá", "óa");
        TONE_MARK_MAP.put("oả", "ỏa");
        TONE_MARK_MAP.put("oã", "õa");
        TONE_MARK_MAP.put("oạ", "ọa");
        
        // uý -> úy, uỷ -> ủy, uỹ -> ũy, uỵ -> uy (ụy)
        TONE_MARK_MAP.put("uý", "úy");
        TONE_MARK_MAP.put("uỷ", "ủy");
        TONE_MARK_MAP.put("uỹ", "ũy");
        TONE_MARK_MAP.put("uỵ", "ụy");
        
        // oè -> òe, oé -> óe, oẻ -> ỏe, oẽ -> õe, oẹ -> ọe
        TONE_MARK_MAP.put("oè", "òe");
        TONE_MARK_MAP.put("oé", "óe");
        TONE_MARK_MAP.put("oẻ", "ỏe");
        TONE_MARK_MAP.put("oẽ", "õe");
        TONE_MARK_MAP.put("oẹ", "ọe");
    }

    /**
     * Chuẩn hóa văn bản Tiếng Việt:
     * 1. Đưa về dạng Unicode chuẩn NFC.
     * 2. Chuyển thành chữ thường và trim khoảng trắng thừa.
     * 3. Chuẩn hóa vị trí đặt dấu thanh (ví dụ: oà -> òa).
     */
    public static String normalize(String text) {
        if (text == null) {
            return null;
        }
        
        // 1. Đưa về dạng Unicode NFC
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFC);
        
        // 2. Chuyển sang chữ thường và xóa khoảng trắng thừa ở hai đầu
        normalized = normalized.toLowerCase().trim();
        
        // 3. Chuẩn hóa dấu thanh
        for (Map.Entry<String, String> entry : TONE_MARK_MAP.entrySet()) {
            normalized = normalized.replace(entry.getKey(), entry.getValue());
        }
        
        return normalized;
    }

    /**
     * Loại bỏ toàn bộ dấu Tiếng Việt (chuyển thành chữ không dấu).
     * Ví dụ: "Tiếng Việt rất đẹp!" -> "tieng viet rat dep!"
     */
    public static String removeAccents(String text) {
        if (text == null) {
            return null;
        }
        
        // Chuẩn hóa trước
        String normalized = normalize(text);
        
        // Phân tách Unicode thành dạng NFD để tách các ký tự dấu Combining Diacritical Marks
        String nfdNormalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        
        // Loại bỏ các combining marks
        String withoutAccents = DIACRITICAL_MARKS.matcher(nfdNormalized).replaceAll("");
        
        // Thay thế chữ đ/Đ vì chữ đ không dùng combining mark thông thường
        return withoutAccents
            .replace('đ', 'd')
            .replace('Đ', 'D')
            .replace('\u0111', 'd')
            .replace('\u0110', 'D');
    }
}
