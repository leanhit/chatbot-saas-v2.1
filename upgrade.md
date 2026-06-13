# Hướng Dẫn Nâng Cấp Hệ Thống Chatbot SaaS

Tài liệu này cung cấp hướng dẫn kỹ thuật chi tiết để triển khai các tính năng nâng cấp quan trọng nhằm tối ưu hóa bảo mật, tự động hóa quy trình vận hành, nâng cao trải nghiệm người dùng và tích hợp AI cho hệ thống Chatbot SaaS.

---

## 1. Sửa Lỗi Bảo Mật Rò Rỉ Dữ Liệu Multi-Tenant

### Vấn đề hiện tại
Trong `MessageUsageController` và `PackageUpgradeController`, mã nguồn đang bị hardcode `Long tenantId = 1L;`. Điều này dẫn đến việc người dùng ở bất kỳ workspace nào cũng có thể xem và làm ảnh hưởng đến dữ liệu giới hạn tin nhắn/lịch sử giao dịch của Tenant 1.

### Giải pháp nâng cấp

#### Bước 1: Thay thế logic lấy Tenant ID trong Controller
Chuyển đổi từ mock sang lấy động từ `TenantContext` (được thiết lập tự động bởi `TenantContextInterceptor` qua Header `X-Tenant-Key`).

**File**: `com/chatbot/core/message/usage/controller/MessageUsageController.java`
Thay thế toàn bộ các block lấy `Long tenantId = 1L;` bằng:
```java
Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
if (tenantId == null) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(com.chatbot.shared.dto.ApiResponse.error("Không tìm thấy thông tin Tenant hiện tại. Vui lòng kiểm tra Header X-Tenant-Key."));
}
```

**File**: `com/chatbot/core/simplepayment/controller/PackageUpgradeController.java`
Tương tự, thay thế dòng lấy `tenantId` trong API `/history`:
```java
Long tenantId = com.chatbot.core.tenant.infra.TenantContext.getTenantId();
if (tenantId == null) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(com.chatbot.shared.dto.ApiResponse.error("Không tìm thấy thông tin Tenant hiện tại. Vui lòng kiểm tra Header X-Tenant-Key."));
}
```

---

## 2. Hệ Thống Tự Động Hóa Quét & Hạ Cấp Gói Hết Hạn

### Vấn đề hiện tại
Hệ thống lưu trữ hạn sử dụng gói (`expires_at`) trong bảng `tenants`, nhưng không tự động hạ cấp Tenant về gói `free` khi hết hạn.

### Giải pháp nâng cấp

#### Bước 1: Bổ sung method truy vấn trong Repository
**File**: `com/chatbot/core/tenant/repository/TenantRepository.java`
Thêm query để tìm tất cả các Tenant đã hết hạn sử dụng nhưng chưa được chuyển về gói `free`:
```java
@Query("SELECT t FROM Tenant t WHERE t.expiresAt IS NOT NULL AND t.expiresAt <= :now AND t.currentPackageId != 'free'")
List<Tenant> findExpiredTenants(@Param("now") LocalDateTime now);
```

#### Bước 2: Viết Scheduler (Cron Job) quét định kỳ
Tạo một Class Scheduler chạy ngầm để kiểm tra tự động hàng giờ.

**File mới**: `com/chatbot/core/tenant/scheduler/TenantSubscriptionScheduler.java`
```java
package com.chatbot.core.tenant.scheduler;

import com.chatbot.core.tenant.model.Tenant;
import com.chatbot.core.tenant.repository.TenantRepository;
import com.chatbot.core.tenant.service.TenantPackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class TenantSubscriptionScheduler {

    private final TenantRepository tenantRepository;
    private final TenantPackageService tenantPackageService;

    // Chạy vào phút thứ 0 của mỗi giờ
    @Scheduled(cron = "0 0 * * * *")
    @Transactional("tenantTransactionManager")
    public void downgradeExpiredTenants() {
        log.info("⏰ Bắt đầu quét các Workspace đã hết hạn gói dịch vụ...");
        LocalDateTime now = LocalDateTime.now();
        List<Tenant> expiredTenants = tenantRepository.findExpiredTenants(now);

        if (expiredTenants.isEmpty()) {
            log.info("✅ Không phát hiện Workspace nào quá hạn sử dụng.");
            return;
        }

        for (Tenant tenant : expiredTenants) {
            try {
                log.warn("🚨 Phát hiện Workspace '{}' (ID: {}) đã hết hạn lúc {}. Tiến hành hạ cấp về gói Free.",
                        tenant.getName(), tenant.getId(), tenant.getExpiresAt());
                
                // Sử dụng hàm nâng cấp có sẵn để hạ cấp về gói free
                tenantPackageService.upgradeTenantPackage(tenant.getId(), "free");
                
            } catch (Exception e) {
                log.error("❌ Lỗi khi tự động hạ cấp Workspace ID {}: {}", tenant.getId(), e.getMessage());
            }
        }
    }
}
```

---

## 3. Cập Nhật Trạng Thái Giao Dịch Thời Gian Thực (Real-Time SSE)

### Vấn đề hiện tại
Khách hàng nạp tiền xong phải tự bấm nút kiểm tra hoặc frontend phải liên tục gửi request thăm dò (polling) gây quá tải server và giảm trải nghiệm người dùng.

### Giải pháp nâng cấp

#### Bước 1: Tạo SseEmitter Registry để quản lý kết nối
**File mới**: `com/chatbot/core/simplepayment/service/PaymentNotificationService.java`
```java
package com.chatbot.core.simplepayment.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PaymentNotificationService {

    // Quản lý các kết nối SSE đang chờ thanh toán theo referenceCode
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String referenceCode) {
        SseEmitter emitter = new SseEmitter(600000L); // Timeout 10 phút
        emitters.put(referenceCode, emitter);

        emitter.onCompletion(() -> emitters.remove(referenceCode));
        emitter.onTimeout(() -> emitters.remove(referenceCode));
        emitter.onError((e) -> emitters.remove(referenceCode));

        return emitter;
    }

    public void notifyPaymentSuccess(String referenceCode, Object paymentInfo) {
        SseEmitter emitter = emitters.get(referenceCode);
        if (emitter != null) {
            try {
                emitter.send(SseEmitter.event()
                    .name("payment_completed")
                    .data(paymentInfo));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            } finally {
                emitters.remove(referenceCode);
            }
        }
    }
}
```

#### Bước 2: Tích hợp vào quy trình xử lý giao dịch thành công
Khi giao dịch được Bank API ghi nhận thành công, gửi tín hiệu SSE.

**File**: `com/chatbot/core/simplepayment/service/SimplePaymentService.java`
Inject `PaymentNotificationService` và thêm dòng kích hoạt sau khi giao dịch thành công:
```java
paymentNotificationService.notifyPaymentSuccess(payment.getReferenceCode(), responseDto);
```

#### Bước 3: Frontend lắng nghe sự kiện
Trong màn hình nạp tiền của Vue, khởi tạo EventSource để tự chuyển hướng khi có tín hiệu.

**File**: `frontend/src/views/payment/Deposit.vue`
```javascript
// Khi QR Code hiển thị, tiến hành lắng nghe SSE
let eventSource = null;

const startListeningPayment = (referenceCode) => {
  if (eventSource) eventSource.close();
  
  eventSource = new EventSource(`/api/public/simple-payment/events/${referenceCode}`);
  
  eventSource.addEventListener('payment_completed', (event) => {
    const paymentData = JSON.parse(event.data);
    // Cập nhật trạng thái Store để giao diện tự chuyển sang màn hình thành công
    paymentStore.currentPayment = paymentData;
    eventSource.close();
  });
};
```

---

## 4. Tích Hợp Chatbot Thông Minh (Generative AI & RAG)

### Động lực nâng cấp
Chuyển đổi chatbot từ phản hồi rập khuôn (Rules) sang đàm thoại thông minh bằng AI (OpenAI GPT-4o / Google Gemini 1.5 Pro) có khả năng tự động tra cứu tài liệu doanh nghiệp.

### Các thành phần cần bổ sung

#### Bước 1: Cấu hình Vector Database (PGVector)
Sử dụng Postgres có sẵn và kích hoạt extension pgvector để lưu trữ Embedding của tài liệu:
```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

#### Bước 2: Viết Document Processor & Chunking
Tạo endpoint cho phép admin upload tài liệu (PDF/TXT), chia nhỏ văn bản (Chunking) khoảng 500 ký tự và convert sang Vector Embeddings.

**File mới**: `com/chatbot/shared/penny/service/EmbeddingService.java`
```java
// Gọi OpenAI/Gemini Embeddings API để chuyển văn bản thành vector [float] 1536 chiều
```

#### Bước 3: Đấu nối AI vào luồng định tuyến tin nhắn (Message Router)
Khi nhận tin nhắn của khách hàng:
1. Tra cứu xem có Rule/Response Template nào khớp hoàn toàn không (ưu tiên Rule cứng của chủ shop).
2. Nếu không khớp Rule, thực hiện tìm kiếm ngữ cảnh (Vector Search) trong tài liệu đã upload của Tenant:
   ```sql
   SELECT content FROM document_chunks 
   WHERE tenant_id = :tenantId 
   ORDER BY embedding <=> :userPromptEmbedding LIMIT 3;
   ```
3. Gửi prompt kèm theo context tìm được lên OpenAI/Gemini API để tạo câu trả lời và phản hồi lại khách hàng.
