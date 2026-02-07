package com.chatbot.modules.tenant.membership.model;

public enum MembershipStatus {
    PENDING,    // User gửi yêu cầu tham gia, chờ Admin duyệt
    INVITED,    // Admin mời, chờ User đồng ý
    ACTIVE,     // Thành viên chính thức
    REJECTED,   // Yêu cầu bị từ chối
    BLOCKED     // Bị khóa tài khoản trong Tenant này
}