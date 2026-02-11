package com.chatbot.core.tenant.membership.model;

public enum InvitationStatus {
    PENDING,   // Đang chờ xử lý
    ACCEPTED,  // Đã chấp nhận
    REJECTED,  // Đã từ chối (Thêm mới)
    EXPIRED,   // Đã hết hạn
    REVOKED    // Admin đã thu hồi
}