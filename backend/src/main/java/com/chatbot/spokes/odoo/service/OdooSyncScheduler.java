package com.chatbot.spokes.odoo.service;
import lombok.extern.slf4j.Slf4j;

import com.chatbot.spokes.odoo.client.OdooApiClient;
import com.chatbot.spokes.odoo.model.FbCustomerStaging;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduler dùng để đồng bộ khách hàng có trạng thái COMPLETED từ Postgres lên Odoo.
 */
@Component
@Slf4j
public class OdooSyncScheduler {

    private final CustomerDataService customerDataService;
    private final OdooApiClient odooClient;

    @Autowired
    public OdooSyncScheduler(CustomerDataService customerDataService, OdooApiClient odooClient) {
        this.customerDataService = customerDataService;
        this.odooClient = odooClient;
    }

    /**
     * Chạy định kỳ để đồng bộ hóa các khách hàng đã đồng bộ thất bại (Failed) lên Odoo.
     * Ví dụ: Chạy mỗi 5 phút (300000 ms).
     * Có thể cấu hình lại trong file application.yml:
     * 
     * scheduler:
     *   odoo-sync-failed-rate: 300000
     */
    @Scheduled(fixedRateString = "${scheduler.odoo-sync-failed-rate:300000000}")
    public void syncFailedToOdoo() {
        log.info("⏳ Bắt đầu tiến trình đồng bộ hóa Odoo...");

        List<FbCustomerStaging> customersToSync = customerDataService.getFailedCustomers();

        if (customersToSync.isEmpty()) {
            log.info("✅ Không có khách hàng nào cần đồng bộ.");
            return;
        }

        log.info("📦 Tìm thấy {} khách hàng cần đồng bộ.", customersToSync.size());

        int successCount = 0;
        int failCount = 0;

        for (FbCustomerStaging customer : customersToSync) {
            try {
                customerDataService.syncCustomerToOdoo(customer);
                successCount++;
            } catch (Exception e) {
                failCount++;
                log.error("❌ Lỗi khi đồng bộ khách hàng ID {}: {}", customer.getPsid(), e.getMessage(), e);
            }
        }

        log.info("🏁 Kết thúc tiến trình đồng bộ hóa Odoo. Thành công: {}, Lỗi: {}.", successCount, failCount);
    }
}
