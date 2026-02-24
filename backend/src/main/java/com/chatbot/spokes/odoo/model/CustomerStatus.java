package com.chatbot.spokes.odoo.model;

public enum CustomerStatus {
    /** Đang thu thập thông tin, chưa đủ data cần thiết. */
    PENDING, 
    
    /** Đã thu thập đủ thông tin, sẵn sàng để đẩy vào Odoo. */
    COMPLETED, 
    
    /** Đã đẩy thành công vào Odoo. */
    PUSHED_TO_ODOO, 
    
    /** Đã thử đẩy nhưng thất bại (lỗi API, data không hợp lệ, v.v.). */
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