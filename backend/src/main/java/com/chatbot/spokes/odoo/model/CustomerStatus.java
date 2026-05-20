package com.chatbot.spokes.odoo.model;

public enum CustomerStatus {
    /** Đang thu thập thông tin, chưa đủ data cần thiết. */
    PENDING, 
    
    /** Đã thu thập đủ thông tin. */
    COMPLETED, 
    
    /** Thu thập hoặc xử lý thất bại. */
    FAILED
}


// public enum CustomerStatus {
//     NEW,               // Vừa tạo record, chưa có thông tin
//     COLLECTING,        // Đang thu thập thông tin (chưa đủ name/phone)
//     PENDING_SYNC,      // Đã đủ nhưng lỗi khi sync Odoo
//     SYNCED_WITH_ODOO,  // Đã tạo thành công bên Odoo
//     UPDATED,           // Đã đồng bộ thêm thông tin mới
//     COMPLETED          // Đầy đủ, không cần cập nhật nữa
// }