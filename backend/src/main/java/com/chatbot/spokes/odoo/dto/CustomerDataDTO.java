package com.chatbot.spokes.odoo.dto;

import com.chatbot.spokes.odoo.model.CustomerStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.chatbot.shared.utils.DateUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Customer Data DTO - Gộp thông tin từ 3 bảng:
 * - fb_customer_staging
 * - facebook_users  
 * - fb_captured_phone
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDataDTO {

    // ===== FROM FB_CUSTOMER_STAGING =====
    private String psid;
    private String ownerId;
    private String pageId;
    
    /**
     * Danh sách số điện thoại đã capture từ tin nhắn
     */
    private Set<String> phones;
    
    /**
     * Data JSON trích xuất từ tin nhắn
     */
    private String dataJson;
    
    /**
     * Trạng thái xử lý
     */
    private CustomerStatus status;
    
    /**
     * ID sau khi sync với Odoo
     */
    private Integer odooId;
    
    /**
     * Thời gian tạo record
     */
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime createdAt;
    
    /**
     * Thời gian cập nhật
     */
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime updatedAt;

    // ===== FROM FACEBOOK_USERS =====
    /**
     * Tên người dùng từ Facebook
     */
    private String facebookName;
    
    /**
     * Avatar URL từ Facebook
     */
    private String facebookAvatar;
    
    /**
     * ID partner trong Odoo (nếu có)
     */
    private Integer odooPartnerId;
    
    /**
     * Lần cuối tương tác
     */
    @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
    private LocalDateTime lastInteraction;

    // ===== FROM FB_CAPTURED_PHONE =====
    /**
     * Danh sách tất cả SĐT đã được capture cho owner này
     */
    private List<CapturedPhoneInfo> capturedPhones;

    // ===== DERIVED FIELDS =====
    /**
     * Tên hiển thị (ưu tiên facebook name, fallback psid)
     */
    private String displayName;
    
    /**
     * Avatar hiển thị (ưu tiên facebook avatar)
     */
    private String displayAvatar;
    
    /**
     * Tổng số điện thoại đã capture
     */
    private Integer totalPhones;
    
    /**
     * Số điện thoại chính (đầu tiên trong danh sách)
     */
    private String primaryPhone;

    // ===== INNER CLASS FOR CAPTURED PHONES =====
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CapturedPhoneInfo {
        /**
         * Số điện thoại
         */
        private String phoneNumber;
        
        /**
         * Thời gian capture
         */
        @JsonFormat(pattern = DateUtils.STANDARD_JSON_FORMAT, timezone = DateUtils.STANDARD_TIMEZONE)
        private LocalDateTime capturedAt;
        
        /**
         * Owner của SĐT này
         */
        private String ownerId;
    }

    // ===== HELPER METHODS =====
    
    /**
     * Kiểm tra có thông tin Facebook không
     */
    public boolean hasFacebookInfo() {
        return facebookName != null || facebookAvatar != null;
    }
    
    /**
     * Kiểm tra có số điện thoại không
     */
    public boolean hasPhoneNumbers() {
        return phones != null && !phones.isEmpty();
    }
    
    /**
     * Kiểm tra đã sync với Odoo chưa
     */
    public boolean isSyncedWithOdoo() {
        return odooId != null || odooPartnerId != null;
    }
    
    /**
     * Lấy trạng thái hiển thị
     */
    public String getDisplayStatus() {
        if (status == null) return "UNKNOWN";
        return status.name();
    }
    
    /**
     * Auto-generate display name
     */
    public String getDisplayName() {
        if (facebookName != null && !facebookName.trim().isEmpty()) {
            return facebookName;
        }
        if (psid != null && !psid.trim().isEmpty()) {
            return "User " + psid.substring(0, Math.min(psid.length(), 8));
        }
        return "Unknown User";
    }
    
    /**
     * Auto-generate display avatar
     */
    public String getDisplayAvatar() {
        if (facebookAvatar != null && !facebookAvatar.trim().isEmpty()) {
            return facebookAvatar;
        }
        return null; // Could return default avatar URL
    }
    
    /**
     * Auto-generate primary phone
     */
    public String getPrimaryPhone() {
        if (phones != null && !phones.isEmpty()) {
            return phones.iterator().next();
        }
        return null;
    }
    
    /**
     * Auto-generate total phones
     */
    public Integer getTotalPhones() {
        if (phones != null) {
            return phones.size();
        }
        return 0;
    }
}
