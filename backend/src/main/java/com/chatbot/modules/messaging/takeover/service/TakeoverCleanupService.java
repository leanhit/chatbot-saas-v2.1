package com.chatbot.modules.messaging.takeover.service;
import lombok.extern.slf4j.Slf4j;

import com.chatbot.modules.messaging.messStore.model.Conversation;
import com.chatbot.modules.messaging.messStore.service.ConversationService;
import com.chatbot.modules.messaging.messStore.repository.ConversationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TakeoverCleanupService {

    private final ConversationRepository conversationRepository; // Dùng Repository để query nhanh hơn
    private final ConversationService conversationService;      // Dùng Service để gọi logic Release

    // 2 phút = 120,000 milliseconds
    private static final long IDLE_TIMEOUT_MS = 2 * 60 * 1000; 

    /**
     * Chạy định kỳ mỗi 30 giây để kiểm tra và tự động giải phóng (release) 
     * các cuộc hội thoại bị Agent bỏ quên cho TẤT CẢ các tenant.
     */
    @Scheduled(fixedRate = 300000000) // Chạy mỗi 30 giây
    public void autoReleaseIdleConversations() {
        log.info("⏰ [Scheduler] Bắt đầu kiểm tra các cuộc hội thoại đang bị Agent tiếp quản...");
        
        // Lấy tất cả các conversation đang được Agent tiếp quản từ tất cả các tenant
        // Repository sẽ xử lý multi-tenant filtering tự động
        List<Conversation> takenOverConversations = conversationRepository.findAllByIsTakenOverByAgent(true);

        long currentTimeMillis = System.currentTimeMillis();
        int processedCount = 0;
        int releasedCount = 0;

        for (Conversation conversation : takenOverConversations) {
            processedCount++;
            
            // Lấy thời gian cập nhật cuối cùng (updatedAt) dưới dạng epoch millis
            // Giả định updatedAt là LocalDateTime (như trong Entity của bạn)
            long lastUpdateTimeMillis = conversation.getUpdatedAt()
                                                    .atZone(ZoneOffset.ofHours(7)) // Chuyển đổi sang múi giờ thích hợp
                                                    .toInstant()
                                                    .toEpochMilli();
            
            long idleDuration = currentTimeMillis - lastUpdateTimeMillis;

            if (idleDuration >= IDLE_TIMEOUT_MS) {
                log.info("🚨 Auto-Handback: Conversation " + conversation.getId() + 
                                   " (Tenant: " + conversation.getTenantId() + ") đã nhàn rỗi " + (idleDuration / 1000) + " giây. Đang giải phóng...");
                
                try {
                    // 2. Gọi logic giải phóng (release)
                    // Hàm này sẽ đặt isTakenOverByAgent = false và status = "open"
                    conversationService.releaseConversation(conversation.getId());
                    releasedCount++;
                    log.info("✅ Conversation " + conversation.getId() + " đã được chuyển giao lại cho Botpress.");
                    
                    // TODO: OPTIONAL: Gửi một tin nhắn thông báo (Internal System Message)
                    // tới người dùng hoặc Agent về việc chuyển giao.
                    
                } catch (Exception e) {
                    log.error("❌ Lỗi khi tự động giải phóng Conversation " + conversation.getId() + ": " + e.getMessage());
                }
            }
        }
        
        log.info("⏰ [Scheduler] Hoàn thành: Đã xử lý " + processedCount + 
                          " conversation, giải phóng " + releasedCount + " conversation.");
    }
}