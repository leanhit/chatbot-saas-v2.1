// package com.chatbot.odoo.client;

// import com.chatbot.odoo.client.OdooApiClient;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.context.TestPropertySource;

// import java.util.HashMap;
// import java.util.Map;

// @SpringBootTest
// @TestPropertySource(properties = {
//     "odoo.url=",
//     "odoo.db=",
//     "odoo.username=",
//     "odoo.password="
// })
// class OdooApiIntegrationTests {
    
//     @Autowired
//     private OdooApiClient odooApiClient; // Spring sẽ tự động inject Bean OdooApiClient

//     @Test
//     void testCreateAndUpdatePartnerFlow() throws Exception {
//         // --- 1. Dữ liệu Test ---
//         // Sử dụng một PSID duy nhất để đảm bảo luồng tạo mới luôn chạy
//         String testPsid = "PSID_TEST_" + System.currentTimeMillis(); 
        
//         // Dữ liệu BAN ĐẦU (Tạo mới)
//         Map<String, Object> initialData = new HashMap<>();
//         // ⭐ ĐÃ SỬA: Đổi 'email' thành 'email_from' (tên trường chuẩn của Odoo Lead)
//         initialData.put("name", "Khách Hàng Ban Đầu");
//         initialData.put("email_from", "test_initial_" + testPsid + "@example.com"); 

//         // Dữ liệu MỚI (Cập nhật)
//         Map<String, Object> updateData = new HashMap<>();
//         updateData.put("name", "Khách Hàng Đã Cập Nhật");
//         updateData.put("phone", "0987654321"); // Thêm trường Phone mới

//         // --- 2. Thực thi Tạo mới (Create) ---
//         Integer initialId = odooApiClient.createOrUpdateLead(testPsid, initialData);
//         System.out.println("Tạo mới thành công Partner ID: " + initialId);
        
//         // --- 3. Thực thi Cập nhật (Update) ---
//         Integer updatedId = odooApiClient.createOrUpdateLead(testPsid, updateData);
//         System.out.println("Cập nhật thành công Partner ID: " + updatedId);

//         // Cần thêm bước xác minh (Assertion) nếu muốn test tự động
//         // Ví dụ: assert initialId.equals(updatedId); 
//     }
// }
