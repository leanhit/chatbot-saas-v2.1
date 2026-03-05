package com.chatbot.core.tenant.membership.model;

public enum TenantRole {
    OWNER,   // 🏆 Chủ tenant - Full quyền
    ADMIN,    // 🔧 Admin tenant - Quản lý member & settings  
    EDITOR,   // ✏️ Editor - Chỉnh sửa nội dung & bots
    MEMBER,   // 👤 Member - Member cơ bản (read-only)
    NONE      // ❌ Không có quyền
}
