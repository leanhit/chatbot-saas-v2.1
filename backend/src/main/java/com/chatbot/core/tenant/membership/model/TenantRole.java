package com.chatbot.core.tenant.membership.model;

public enum TenantRole {
    OWNER,   // 🏆 Chủ tenant - Full quyền
    EDITOR,  // ✏️ Editor - Chỉnh sửa nội dung & bots (CRUD)
    MEMBER,  // 👤 Member - Member cơ bản (read-only)
    NONE     // ❌ Không có quyền
}
