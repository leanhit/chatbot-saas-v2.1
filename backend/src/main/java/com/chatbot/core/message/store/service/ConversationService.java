package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.model.Message;
import com.chatbot.core.message.store.dto.ConversationStatisticsDTO;
import com.chatbot.core.message.store.dto.ChartDataPointDTO;
import com.chatbot.core.message.store.dto.ActivityDTO;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.spokes.facebook.connection.model.FacebookConnection;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.spokes.facebook.user.service.FacebookUserService;
import com.chatbot.core.message.store.model.Channel;
import com.chatbot.core.tenant.infra.TenantContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationService {

    private final ConversationRepository conversationRepo;
    private final FacebookConnectionRepository facebookConnectionRepo;
    private final MessageRepository messageRepo;
    private final FacebookUserService facebookUserService;

    /**
     * Tìm kiếm Conversation hiện có hoặc tạo mới nếu chưa tồn tại.
     * Tự động lấy ownerId từ Connection và gán vào Conversation mới.
     * @param channel Kênh tin nhắn (ví dụ: "facebook_messenger")
     */
    public Conversation findOrCreate(UUID connectionId, String externalUserId, Channel channel) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        
        return conversationRepo
                .findByConnectionIdAndExternalUserIdAndTenantId(connectionId, externalUserId, tenantId)
                .orElseGet(() -> {
                    // Lấy ownerId từ Connection.
                    String ownerId = facebookConnectionRepo.findById(connectionId)
                        .map(FacebookConnection::getOwnerId)
                        .orElseThrow(() -> new RuntimeException("Connection not found with ID: " + connectionId));

                    // Tạo conversation mới
                    Conversation c = Conversation.builder()
                            .connectionId(connectionId)
                            .externalUserId(externalUserId)
                            .status("open")
                            .channel(channel) 
                            .isClosedByAgent(false)
                            .isTakenOverByAgent(false)
                            .ownerId(ownerId)
                            .build();
                    
                    // Nếu là kênh Facebook, lấy thông tin người dùng
                    if (channel == Channel.FACEBOOK) {
                        try {
                            // Lấy thông tin kết nối để lấy pageId thực tế
                            FacebookConnection fbConnection = facebookConnectionRepo.findById(connectionId)
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy kết nối với ID: " + connectionId));
                            
                            String pageId = fbConnection.getPageId();
                            log.info("🔄 Đang lấy thông tin người dùng Facebook - PSID: {}, Page ID: {}", 
                                externalUserId, pageId);
                                
                            var userInfo = facebookUserService.getUserInfo(externalUserId, pageId);
                            
                            if (userInfo != null) {
                                log.info("✅ Đã lấy được thông tin người dùng - Tên: {}, Avatar: {}", 
                                    userInfo.getName(), 
                                    userInfo.getProfilePic() != null ? "[Có ảnh đại diện]" : "[Không có ảnh]");
                                    
                                c.setUserName(userInfo.getName());
                                c.setUserAvatar(userInfo.getProfilePic());
                            } else {
                                log.warn("⚠️ Không lấy được thông tin người dùng từ Facebook cho PSID: {}", externalUserId);
                            }
                        } catch (Exception e) {
                            log.error("❌ Lỗi khi lấy thông tin người dùng từ Facebook - PSID: {}, Lỗi: {}", 
                                externalUserId, e.getMessage(), e);
                        }
                    }
                    
                    return conversationRepo.save(c);
                });
    }

    /**
     * Lấy danh sách Conversations TỔNG THỂ (không lọc theo Owner/Connection).
     * Sắp xếp theo updatedAt để Conversation có tin nhắn mới nhất lên đầu.
     */
    public Page<Conversation> getConversations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        return conversationRepo.findAllByTenantIdOrderByUpdatedAtDesc(tenantId, pageable);
    }
    
    /**
     * Lấy danh sách Conversations, LỌC theo Owner ID.
     * Sắp xếp theo updatedAt để Conversation có tin nhắn mới nhất lên đầu.
     * @param ownerId ID của chủ sở hữu (Owner) để lọc
     */
    public Page<Conversation> getConversationsByOwnerId(String ownerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        return conversationRepo.findByOwnerIdAndTenantIdOrderByUpdatedAtDesc(ownerId, tenantId, pageable);
    }
    
    /**
     * Thêm phương thức để đóng Conversation
     */
    public Conversation closeConversation(Long conversationId) {
        return conversationRepo.findById(conversationId)
            .map(c -> {
                c.setIsClosedByAgent(true);
                c.setStatus("closed");
                // Khi đóng, luôn trả quyền điều khiển về Botpress (mặc dù cuộc hội thoại đã đóng)
                c.setIsTakenOverByAgent(false); 
                return conversationRepo.save(c);
            })
            .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }

    // =========================================================================
    // PHƯƠNG THỨC MỚI: XỬ LÝ HANDOVER/TAKEOVER
    // =========================================================================

    /**
     * Agent tiếp quản (Takeover) Conversation.
     * Botpress sẽ bị ngắt (isTakenOverByAgent = true).
     * @param conversationId ID của Conversation
     * @param agentAssignedId ID của Agent tiếp quản
     * @return Conversation đã được cập nhật
     */
    public Conversation takeoverConversation(Long conversationId, Long agentAssignedId) {
        return conversationRepo.findById(conversationId)
            .map(c -> {
                if (c.getIsClosedByAgent()) {
                    // Không tiếp quản conversation đã đóng
                    throw new IllegalStateException("Cannot takeover a closed conversation.");
                }
                c.setIsTakenOverByAgent(true); // Ngắt Botpress
                c.setAgentAssignedId(agentAssignedId); // Gán Agent
                c.setStatus("active_agent"); // Cập nhật trạng thái
                return conversationRepo.save(c);
            })
            .orElseThrow(() -> new RuntimeException("Conversation not found for takeover"));
    }

    /**
     * Agent giải phóng (Release) Conversation.
     * Botpress sẽ được kích hoạt lại (isTakenOverByAgent = false).
     * @param conversationId ID của Conversation
     * @return Conversation đã được cập nhật
     */
    public Conversation releaseConversation(Long conversationId) {
        return conversationRepo.findById(conversationId)
            .map(c -> {
                if (c.getIsClosedByAgent()) {
                    // Không release conversation đã đóng (mặc dù hành động close đã làm điều này)
                    throw new IllegalStateException("Cannot release a closed conversation.");
                }
                c.setIsTakenOverByAgent(false); // Kích hoạt lại Botpress
                c.setAgentAssignedId(null); // Bỏ gán Agent
                c.setStatus("open"); // Quay về trạng thái mở để bot xử lý
                return conversationRepo.save(c);
            })
            .orElseThrow(() -> new RuntimeException("Conversation not found for release"));
    }
    
    /**
     * Xóa vĩnh viễn một conversation khỏi hệ thống
     * @param conversationId ID của conversation cần xóa
     * @throws RuntimeException nếu không tìm thấy conversation
     */
    /**
     * Xóa một conversation và tất cả tin nhắn liên quan với kiểm tra quyền sở hữu
     * @param conversationId ID của conversation cần xóa
     * @param ownerId ID của chủ sở hữu
     * @throws RuntimeException nếu không tìm thấy conversation hoặc không có quyền
     */
    @Transactional
    public void deleteConversation(Long conversationId, String ownerId) {
        Conversation conversation = conversationRepo.findById(conversationId)
            .orElseThrow(() -> new RuntimeException("Conversation not found with id: " + conversationId));
            
        if (!ownerId.equals(conversation.getOwnerId())) {
            throw new RuntimeException("You don't have permission to delete this conversation");
        }
        
        // Xóa tất cả tin nhắn liên quan
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        messageRepo.deleteByConversationIdAndTenantId(conversationId, tenantId);
        
        // Xóa conversation
        conversationRepo.delete(conversation);
    }
    
    /**
     * Xóa nhiều conversations cùng lúc với kiểm tra quyền sở hữu
     * @param conversationIds Danh sách ID của các conversation cần xóa
     * @param ownerId ID của chủ sở hữu
     * @return Số lượng conversation đã xóa
     */
    @Transactional
    public int deleteConversations(List<Long> conversationIds, String ownerId) {
        if (conversationIds == null || conversationIds.isEmpty()) {
            return 0;
        }
        
        // Lấy danh sách các conversation thuộc owner
        List<Conversation> conversationsToDelete = conversationRepo
            .findAllById(conversationIds)
            .stream()
            .filter(c -> ownerId.equals(c.getOwnerId()))
            .collect(Collectors.toList());
            
        if (!conversationsToDelete.isEmpty()) {
            // Lấy danh sách ID conversation để xóa tin nhắn
            List<Long> idsToDelete = conversationsToDelete.stream()
                .map(Conversation::getId)
                .collect(Collectors.toList());
                
            // Xóa tất cả tin nhắn của các conversation này
            Long tenantId = TenantContext.getTenantId();
            if (tenantId == null) {
                throw new RuntimeException("Không tìm thấy tenant ID trong context");
            }
            messageRepo.deleteAllByConversationIdInAndTenantId(idsToDelete, tenantId);
            
            // Xóa các conversation
            conversationRepo.deleteAll(conversationsToDelete);
        }
        
        return conversationsToDelete.size();
    }
    
    /**
     * Cập nhật trạng thái isTakenOverByAgent của một conversation
     */
    @Transactional
    public Conversation updateTakenOverStatus(Long conversationId, Boolean isTakenOverByAgent, Long agentAssignedId, String ownerId) {

    log.info(
        "Request to update takeover status. ConversationId={}, isTakenOver={}, agentAssignedId={}, CallerOwnerId={}",
        conversationId, isTakenOverByAgent, agentAssignedId, ownerId
    );

    return conversationRepo.findById(conversationId)
        .map(conversation -> {

            // Log trạng thái hiện tại
            log.debug(
                "Before update → ID={}, OwnerId={}, isTakenOver={}, Status={}, AgentAssignedId={}",
                conversation.getId(),
                conversation.getOwnerId(),
                conversation.getIsTakenOverByAgent(),
                conversation.getStatus(),
                conversation.getAgentAssignedId()
            );

            // Kiểm tra quyền
            if (!ownerId.equals(conversation.getOwnerId())) {
                log.warn(
                    "Permission denied. CallerOwnerId={} != ConversationOwnerId={}, ConversationId={}",
                    ownerId, conversation.getOwnerId(), conversationId
                );
                throw new RuntimeException("You don't have permission to update this conversation");
            }

            // Không cho phép update nếu đã đóng
            if (conversation.getIsClosedByAgent()) {
                log.warn("Conversation {} is closed, cannot update takeover status.", conversationId);
                throw new IllegalStateException("Cannot update a closed conversation.");
            }

            // Cập nhật trạng thái takeover
            conversation.setIsTakenOverByAgent(isTakenOverByAgent);

            if (isTakenOverByAgent) {
                // Agent takeover → bot stop
                conversation.setStatus("active_agent");  // Giữ đồng bộ với hàm takeoverConversation()
                conversation.setAgentAssignedId(agentAssignedId);
                log.debug("Conversation {} takeover → status=active_agent, agentAssignedId={}", conversationId, agentAssignedId);
            } else {
                // Release → trả lại bot
                conversation.setStatus("open");
                conversation.setAgentAssignedId(null); // Giải phóng Agent
                log.debug("Conversation {} released → status=open, agentAssignedId reset", conversationId);
            }

            Conversation updated = conversationRepo.save(conversation);

            // Log sau khi lưu
            log.info(
                "Conversation {} updated. Final: isTakenOver={}, Status={}, AgentAssignedId={}",
                updated.getId(),
                updated.getIsTakenOverByAgent(),
                updated.getStatus(),
                updated.getAgentAssignedId()
            );

            return updated;
        })
        .orElseThrow(() -> {
            log.error("Conversation not found with id {}", conversationId);
            return new RuntimeException("Conversation not found with id: " + conversationId);
        });
}

        
    /**
     * @deprecated Sử dụng phiên bản mới có kiểm tra ownerId
     */
    @Deprecated
    public void deleteConversation(Long conversationId) {
        deleteConversation(conversationId, ""); // Gọi phương thức mới với ownerId rỗng
    }
    
    // =========================================================================
    // END PHƯƠNG THỨC HANDOVER
    // =========================================================================
    
    /**
     * Tìm kiếm Conversation hiện có. Rất cần thiết khi Bot/Agent phản hồi, 
     * để lấy Conversation ID cho việc lưu trữ tin nhắn đi.
     */
    public Optional<Conversation> findByConnectionIdAndExternalUserId(UUID connectionId, String externalUserId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        return conversationRepo.findByConnectionIdAndExternalUserIdAndTenantId(connectionId, externalUserId, tenantId);
    }


    /**
     * Lấy danh sách Conversations, có hỗ trợ LỌC theo connectionId.
     * Sắp xếp theo updatedAt để Conversation có tin nhắn mới nhất lên đầu.
     * @param connectionId UUID của Connection để lọc (có thể là null)
     * @param ownerId String của user để lọc (có thể là null)
     */

    public Page<Conversation> getConversationsByOwnerIdAndConnectionId(
            String ownerId,
            UUID connectionId,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        
        return conversationRepo.findByOwnerIdAndConnectionIdAndTenantIdOrderByUpdatedAtDesc(
                ownerId,
                connectionId,
                tenantId,
                pageable
        );
    }

    // --------------------------------------------------------------------------
    // MISSING METHODS FOR FRONTEND API
    // --------------------------------------------------------------------------

    /**
     * Get conversation by ID
     */
    public Conversation getConversationById(Long conversationId) {
        Long tenantId = TenantContext.getTenantId();
        return conversationRepo.findByIdAndTenantId(conversationId, tenantId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }

    /**
     * Get conversation by ID - trả về Optional
     */
    public Optional<Conversation> findConversationById(Long conversationId) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        return conversationRepo.findByIdAndTenantId(conversationId, tenantId);
    }

    /**
     * Create new conversation
     */
    @Transactional
    public Conversation createConversation(Conversation conversation) {
        Long tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("Không tìm thấy tenant ID trong context");
        }
        
        // Set tenant ID
        conversation.setTenantId(tenantId);
        
        // Set default values
        if (conversation.getStatus() == null) {
            conversation.setStatus("open");
        }
        if (conversation.getIsTakenOverByAgent() == null) {
            conversation.setIsTakenOverByAgent(false);
        }
        if (conversation.getIsClosedByAgent() == null) {
            conversation.setIsClosedByAgent(false);
        }
        
        return conversationRepo.save(conversation);
    }

    /**
     * Update conversation
     */
    public Conversation updateConversation(Long conversationId, Object conversationDTO, String ownerId) {
        Conversation conversation = getConversationById(conversationId);
        if (!ownerId.equals(conversation.getOwnerId())) {
            throw new RuntimeException("You don't have permission to update this conversation");
        }
        // Status update if DTO contains status field (handled via PATCH endpoints)
        return conversationRepo.save(conversation);
    }

    /**
     * Search conversations with real filtering
     */
    public Page<Conversation> searchConversations(String ownerId, String query, String channel, String dateRange, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Long tenantId = TenantContext.getTenantId();
        if (query != null && !query.isBlank()) {
            return conversationRepo.searchByOwnerIdAndTenantId(ownerId, tenantId, query.trim(), pageable);
        }
        return conversationRepo.findByOwnerIdAndTenantIdOrderByUpdatedAtDesc(ownerId, tenantId, pageable);
    }

    /**
     * Get conversation statistics
     */
    public ConversationStatisticsDTO getConversationStatistics(String ownerId) {
        Long tenantId = TenantContext.getTenantId();

        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime todayStart = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime last24h = now.minusHours(24);
            LocalDateTime lastMonth = now.minusMonths(1);
            LocalDateTime prev2Month = now.minusMonths(2);

            // --- Conversation counts ---
            Long totalConversations = conversationRepo.countByTenantId(tenantId);
            Long todayConversations = conversationRepo.countByTenantIdAndCreatedAtAfter(tenantId, todayStart);
            Long activeTakeovers = conversationRepo.countByTenantIdAndIsTakenOverByAgent(tenantId, true);
            Long totalMessages = messageRepo.countByConversationTenantId(tenantId);
            Long todayMessages = messageRepo.countByConversationTenantIdAndCreatedAtAfter(tenantId, todayStart);

            // --- Growth rate: (hôm nay / tổng) * 100 ---
            Double growthRate = totalConversations > 0 ? (todayConversations * 100.0 / totalConversations) : 0.0;

            // --- Active Users: số external user distinct hoạt động trong 24h ---
            Long activeUsers = conversationRepo.countDistinctActiveUsers(tenantId, last24h);

            // --- Bot Responses: tin nhắn có sender = 'bot' ---
            Long botResponses = messageRepo.countBySenderAndTenantId("bot", tenantId);

            // --- Response Rate: botResponses / totalMessages * 100 ---
            Double responseRate = totalMessages > 0 ? (botResponses * 100.0 / totalMessages) : 0.0;

            // --- Active Connections ---
            Long activeConnections = facebookConnectionRepo.countByTenantIdAndIsActiveTrue(tenantId);

            // --- User Growth: so sánh last month vs prev month ---
            Long lastMonthUsers = conversationRepo.countDistinctActiveUsers(tenantId, lastMonth);
            Long prev2MonthUsers = conversationRepo.countDistinctActiveUsers(tenantId, prev2Month);
            Double userGrowth = prev2MonthUsers > 0
                ? ((lastMonthUsers - prev2MonthUsers) * 100.0 / prev2MonthUsers)
                : 0.0;

            ConversationStatisticsDTO statistics = new ConversationStatisticsDTO();
            statistics.setTotalConversations(totalConversations);
            statistics.setActiveTakeovers(activeTakeovers);
            statistics.setPendingMessages(todayMessages);
            statistics.setTodayMessages(todayMessages);
            statistics.setGrowthRate(growthRate);
            statistics.setActiveUsers(activeUsers);
            statistics.setUserGrowth(userGrowth);
            statistics.setBotResponses(botResponses);
            statistics.setResponseRate(responseRate);
            statistics.setActiveConnections(activeConnections);
            statistics.setTotalMessages(totalMessages);

            return statistics;

        } catch (Exception e) {
            log.error("Error calculating statistics for tenant {}: {}", tenantId, e.getMessage(), e);
            ConversationStatisticsDTO defaultStats = new ConversationStatisticsDTO();
            defaultStats.setTotalConversations(0L);
            defaultStats.setActiveTakeovers(0L);
            defaultStats.setPendingMessages(0L);
            defaultStats.setTodayMessages(0L);
            defaultStats.setGrowthRate(0.0);
            defaultStats.setActiveUsers(0L);
            defaultStats.setUserGrowth(0.0);
            defaultStats.setBotResponses(0L);
            defaultStats.setResponseRate(0.0);
            defaultStats.setActiveConnections(0L);
            defaultStats.setTotalMessages(0L);
            return defaultStats;
        }
    }

    /**
     * Get conversation chart data based on time period
     */
    public List<ChartDataPointDTO> getConversationChartData(String ownerId, String period) {
        Long tenantId = TenantContext.getTenantId();
        
        try {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime startDate;
            String dateFormat;
            
            switch (period.toLowerCase()) {
                case "7d":
                    startDate = now.minusDays(7);
                    dateFormat = "EEE";
                    return generateDailyChartData(tenantId, startDate, now, dateFormat);
                    
                case "1m":
                    startDate = now.minusMonths(1);
                    dateFormat = "Week %d";
                    return generateWeeklyChartData(tenantId, startDate, now);
                    
                case "3m":
                    startDate = now.minusMonths(3);
                    return generateMonthlyChartData(tenantId, startDate, now);
                    
                case "1y":
                    startDate = now.minusYears(1);
                    return generateQuarterlyChartData(tenantId, startDate, now);
                    
                default:
                    // Default to 7 days
                    startDate = now.minusDays(7);
                    dateFormat = "EEE";
                    return generateDailyChartData(tenantId, startDate, now, dateFormat);
            }
            
        } catch (Exception e) {
            log.error("Error generating chart data for tenant {} with period {}: {}", tenantId, period, e.getMessage(), e);
            
            // Return empty list on error
            return java.util.Collections.emptyList();
        }
    }
    
    private List<ChartDataPointDTO> generateDailyChartData(Long tenantId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate, String dateFormat) {
        List<ChartDataPointDTO> chartData = new java.util.ArrayList<>();
        
        // ✅ OPTIMIZED: Single batch query instead of N+1
        List<Object[]> results = conversationRepo.getDailyChartStats(tenantId, startDate, endDate);
        
        // Convert results to ChartDataPointDTO
        for (Object[] result : results) {
            java.sql.Timestamp date = (java.sql.Timestamp) result[0];
            Long count = ((Number) result[1]).longValue();
            
            ChartDataPointDTO dataPoint = new ChartDataPointDTO();
            dataPoint.setLabel(date.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern(dateFormat)));
            dataPoint.setValue(count);
            dataPoint.setDate(date.toLocalDateTime().toString());
            
            chartData.add(dataPoint);
        }
        
        return chartData;
    }
    
    private List<ChartDataPointDTO> generateWeeklyChartData(Long tenantId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        List<ChartDataPointDTO> chartData = new java.util.ArrayList<>();
        
        // ✅ OPTIMIZED: Single batch query
        List<Object[]> results = conversationRepo.getWeeklyChartStats(tenantId, startDate, endDate);
        
        int weekNum = 1;
        for (Object[] result : results) {
            java.sql.Timestamp date = (java.sql.Timestamp) result[0];
            Long count = ((Number) result[1]).longValue();
            
            ChartDataPointDTO dataPoint = new ChartDataPointDTO();
            dataPoint.setLabel(String.format("Week %d", weekNum++));
            dataPoint.setValue(count);
            dataPoint.setDate(date.toLocalDateTime().toString());
            
            chartData.add(dataPoint);
        }
        
        return chartData;
    }
    
    private List<ChartDataPointDTO> generateMonthlyChartData(Long tenantId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate) {
        List<ChartDataPointDTO> chartData = new java.util.ArrayList<>();
        
        // ✅ OPTIMIZED: Single batch query
        List<Object[]> results = conversationRepo.getMonthlyChartStats(tenantId, startDate, endDate);
        
        for (Object[] result : results) {
            java.sql.Timestamp date = (java.sql.Timestamp) result[0];
            Long count = ((Number) result[1]).longValue();
            
            ChartDataPointDTO dataPoint = new ChartDataPointDTO();
            dataPoint.setLabel(date.toLocalDateTime().format(java.time.format.DateTimeFormatter.ofPattern("MMM")));
            dataPoint.setValue(count);
            dataPoint.setDate(date.toLocalDateTime().toString());
            
            chartData.add(dataPoint);
        }
        
        return chartData;
    }
    
    private List<ChartDataPointDTO> generateQuarterlyChartData(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        List<ChartDataPointDTO> chartData = new ArrayList<>();

        java.time.LocalDate current = startDate.toLocalDate()
                .withMonth(((startDate.getMonthValue() - 1) / 3) * 3 + 1)
                .withDayOfMonth(1);
        java.time.LocalDate end = endDate.toLocalDate();

        int quarterNum = 1;
        while (!current.isAfter(end)) {
            LocalDateTime quarterStart = current.atStartOfDay();
            LocalDateTime quarterEnd = current.plusMonths(2)
                    .withDayOfMonth(current.plusMonths(2).lengthOfMonth())
                    .atTime(23, 59, 59);

            Long count = conversationRepo.countByTenantIdAndCreatedAtBetween(tenantId, quarterStart, quarterEnd);

            ChartDataPointDTO dataPoint = new ChartDataPointDTO();
            dataPoint.setLabel(String.format("Q%d", quarterNum++));
            dataPoint.setValue(count);
            dataPoint.setDate(current.toString());

            chartData.add(dataPoint);
            current = current.plusMonths(3);
            if (quarterNum > 4) quarterNum = 1;
        }

        return chartData;
    }

    // =========================================================================
    // RECENT ACTIVITY — dùng cho Dashboard
    // =========================================================================

    /**
     * Lấy danh sách activity gần đây từ conversations thực trong DB.
     * Map trạng thái conversation sang loại activity.
     */
    public List<ActivityDTO> getRecentActivity(String ownerId, Long tenantId, Pageable pageable) {
        Page<Conversation> recentConversations = conversationRepo
                .findByOwnerIdAndTenantIdOrderByUpdatedAtDesc(ownerId, tenantId, pageable);

        return recentConversations.getContent().stream()
                .map(c -> {
                    ActivityDTO activity = new ActivityDTO();
                    activity.setId("conv_" + c.getId());
                    activity.setTimestamp(c.getUpdatedAt());

                    String userName = (c.getUserName() != null && !c.getUserName().isBlank())
                            ? c.getUserName() : "User " + c.getExternalUserId();

                    if (Boolean.TRUE.equals(c.getIsTakenOverByAgent())) {
                        activity.setType("takeover");
                        activity.setTitle("Agent took over conversation");
                        activity.setDescription("Agent assumed control of conversation with " + userName);
                    } else if (Boolean.TRUE.equals(c.getIsClosedByAgent())) {
                        activity.setType("closed");
                        activity.setTitle("Conversation closed");
                        activity.setDescription("Conversation with " + userName + " was closed");
                    } else if ("open".equals(c.getStatus())) {
                        activity.setType("conversation");
                        activity.setTitle("New conversation started");
                        activity.setDescription(userName + " started a new chat via " +
                                (c.getChannel() != null ? c.getChannel().name() : "Unknown"));
                    } else {
                        activity.setType("bot_response");
                        activity.setTitle("Bot responded");
                        activity.setDescription("Bot replied to " + userName);
                    }

                    return activity;
                })
                .collect(Collectors.toList());
    }
}