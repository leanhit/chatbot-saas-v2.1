package com.chatbot.modules.tenant.core.dto;

public enum TenantMembershipStatus {
    NONE,      // Chưa yêu cầu, chưa tham gia
    PENDING,   // Đang chờ duyệt
    APPROVED,  // Đã là thành viên
    REJECTED   // Bị từ chối (nếu hệ thống của bạn có lưu lịch sử từ chối)
}