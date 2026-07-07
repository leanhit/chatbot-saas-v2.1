package com.chatbot.core.message.store.service;

import com.chatbot.core.message.store.model.Conversation;
import com.chatbot.core.message.store.dto.ConversationStatisticsDTO;
import com.chatbot.core.message.store.dto.ChartDataPointDTO;
import com.chatbot.core.message.store.dto.ActivityDTO;
import com.chatbot.core.message.store.repository.ConversationRepository;
import com.chatbot.core.message.store.repository.MessageRepository;
import com.chatbot.spokes.facebook.connection.repository.FacebookConnectionRepository;
import com.chatbot.core.tenant.infra.TenantContext;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationStatisticsService {

    private final ConversationRepository conversationRepo;
    private final FacebookConnectionRepository facebookConnectionRepo;
    private final MessageRepository messageRepo;

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
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startDate;
            String dateFormat;
            
            switch (period.toLowerCase()) {
                case "7d":
                    startDate = now.minusDays(7);
                    dateFormat = "EEE";
                    return generateDailyChartData(tenantId, startDate, now, dateFormat);
                    
                case "1m":
                    startDate = now.minusMonths(1);
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
    
    private List<ChartDataPointDTO> generateDailyChartData(Long tenantId, LocalDateTime startDate, LocalDateTime endDate, String dateFormat) {
        List<ChartDataPointDTO> chartData = new ArrayList<>();
        
        List<Object[]> results = conversationRepo.getDailyChartStats(tenantId, startDate, endDate);
        
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
    
    private List<ChartDataPointDTO> generateWeeklyChartData(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        List<ChartDataPointDTO> chartData = new ArrayList<>();
        
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
    
    private List<ChartDataPointDTO> generateMonthlyChartData(Long tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        List<ChartDataPointDTO> chartData = new ArrayList<>();
        
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
