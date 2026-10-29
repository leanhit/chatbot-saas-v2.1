Agent Conversation Statistics Dashboard
Thêm tính năng thống kê conversation chi tiết cho từng Agent — tham khảo các app chuyên nghiệp như Intercom, Zendesk, Freshdesk, Crisp.

Mô tả
Hiện tại AgentManagement.vue chỉ hiển thị currentLoad và thông tin cơ bản. Cần bổ sung dashboard thống kê mạnh mẽ cho từng agent với các chỉ số nghề nghiệp (KPI) quan trọng.

Dữ liệu đã sẵn có trong DB:

conversations.agent_assigned_id → biết agent nào xử lý conversation nào
conversations.resolution_status → resolved / unresolved / pending
conversations.user_satisfaction_rating → CSAT score 1–5
conversations.is_closed_by_agent → agent đã đóng
conversations.first_agent_response_time → thời gian phản hồi đầu tiên
conversations.sla_breach_count → số lần vi phạm SLA
Các KPI sẽ hiển thị (tham khảo Intercom / Zendesk)
Chỉ số	Mô tả	Nguồn dữ liệu
Total Conversations	Tổng số cuộc hội thoại được assign	conversations WHERE agent_assigned_id = ?
Resolved	Đã giải quyết thành công	resolution_status = 'resolved'
Resolution Rate	% đã giải quyết	resolved / total * 100
Avg First Response Time	Thời gian phản hồi trung bình đầu tiên	AVG(first_agent_response_time - created_at)
CSAT Score	Đánh giá hài lòng trung bình	AVG(user_satisfaction_rating)
SLA Breaches	Số lần vi phạm SLA	SUM(sla_breach_count)
Active Conversations	Đang xử lý hiện tại	currentLoad (đã có)
Proposed Changes
Backend — AgentConversationStatsService + Endpoint
[NEW] AgentConversationStatsDTO.java
DTO mới chứa tất cả KPI trên
Fields: totalConversations, resolvedCount, resolutionRate, avgFirstResponseTimeSeconds, csatScore, slaBreachCount, activeConversations, agentId, agentName, period
[MODIFY] ConversationRepository.java
Thêm các native queries:

sql
-- Đếm total conversations theo agentId và tenant, trong khoảng thời gian
-- Đếm resolved conversations
-- Tính avg first_response_time
-- Tính avg satisfaction_rating
-- Sum sla_breach_count
[MODIFY] AgentController.java
Thêm 2 endpoint mới:

GET /api/agents/{id}/stats?period=7d|30d|90d → thống kê của 1 agent
GET /api/agents/leaderboard?period=7d → bảng xếp hạng tất cả agents (Zendesk style)
[MODIFY] agentApi.js
Thêm:

js
getAgentConversationStats(id, period)
getAgentLeaderboard(period)
Frontend — AgentStatsModal + Leaderboard Panel
[MODIFY] AgentManagement.vue
Thêm 2 tính năng mới:

"Xem thống kê" button trên mỗi agent card → mở modal chi tiết
Agent Leaderboard panel ở đầu trang (bên dưới summary cards) → bảng xếp hạng top agents theo Resolution Rate & CSAT
[NEW] AgentStatsModal.vue
Modal thống kê chi tiết từng agent với:

KPI Cards: 6 metric cards (Total, Resolved, Rate, Response Time, CSAT, SLA Breaches)
Time period selector: 7d / 30d / 90d
Progress bar cho Resolution Rate và CSAT Score
Star rating display cho CSAT
Design: glassmorphism dark, gradient accent (tham khảo Intercom Agent Report)
UI Design Reference
Tham khảo pattern từ:

Intercom: Agent Performance Report — card-based KPIs với color-coded status
Zendesk Explore: Leaderboard với rank, avatar, CSAT score, resolution rate
Freshdesk: Individual agent stats với donut chart cho resolution rate
Layout AgentStatsModal
┌─────────────────────────────────────────────┐
│ 🧑 Agent: Nguyễn Văn A    [7d][30d][90d]   │
├──────────┬──────────┬──────────┬────────────┤
│ 🗨 Total │ ✅ Res'd │ 📊 Rate │ ⏱ Response│
│    124   │   108    │  87.1%  │   2m 34s   │
├──────────┴──────────┴──────────┴────────────┤
│ ⭐ CSAT Score          🚨 SLA Breaches      │
│  ★★★★☆ 4.2/5            3 breaches         │
├─────────────────────────────────────────────┤
│ Resolution Rate: [████████████░░░░] 87.1%  │
│ CSAT Progress:   [██████████████░░] 84%    │
└─────────────────────────────────────────────┘
Layout Leaderboard (trong AgentManagement)
┌─────────────────────────────────────────────────┐
│ 🏆 Agent Leaderboard       [7d][30d][90d]       │
├────┬────────────┬──────────┬──────────┬─────────┤
│ #  │ Agent      │ Resolved │ Rate     │ CSAT    │
├────┼────────────┼──────────┼──────────┼─────────┤
│ 🥇 │ Agent A   │ 108      │ 87.1%    │ ⭐4.2   │
│ 🥈 │ Agent B   │ 94       │ 82.5%    │ ⭐4.0   │
└────┴────────────┴──────────┴──────────┴─────────┘
Verification Plan
Backend
Build: ./gradlew compileJava — phải pass không lỗi
Các JPQL query phải compatible với PostgreSQL
Frontend
Mở AgentManagement → thấy leaderboard panel
Click "Xem thống kê" trên 1 agent → modal mở với KPI cards
Đổi period 7d → 30d → data refresh
Open Questions
IMPORTANT

Dữ liệu thực tế: Hiện nay conversations có agent_assigned_id chưa? Nếu chưa có data, các chỉ số sẽ hiện 0 — vẫn render đẹp nhưng cần một vài conversations được assign để test thực.

NOTE

Leaderboard: Mặc định sort theo resolvedCount DESC. Bạn có muốn có thêm tùy chọn sort theo CSAT hoặc Response Time không?

===================================================


Agent Conversation Statistics — Implementation Plan (v2)
Thêm dashboard thống kê conversation chuyên nghiệp cho từng Agent, chỉ OWNER mới được xem, với biểu đồ trực quan dùng ApexCharts (đã có sẵn trong project).

Thay đổi so với v1
IMPORTANT

Hai bổ sung mới:

Phân quyền OWNER-only — cả backend guard và frontend ẩn UI với non-OWNER
Biểu đồ trực quan — ApexCharts RadialBar, Bar, Donut (thư viện đã có sẵn: vue3-apexcharts ^1.4.1)
KPI & Data Model
Chỉ số	Nguồn DB	Loại biểu đồ
Total Conversations	COUNT(*) WHERE agent_assigned_id = ?	Số đơn
Resolved	COUNT WHERE resolution_status = 'resolved'	Số đơn
Resolution Rate	resolved / total * 100	🔵 RadialBar
Avg First Response Time	AVG(first_agent_response_time - created_at)	Số đơn
CSAT Score	AVG(user_satisfaction_rating)	🟡 RadialBar
SLA Breaches	SUM(sla_breach_count)	Số đơn
Daily Conversations	Group by day, last 7/30d	📊 Bar Chart
Status Distribution	resolved/unresolved/pending count	🍩 Donut Chart
Proposed Changes
1. Backend
[NEW] AgentConversationStatsDTO.java
com/chatbot/core/message/store/dto/AgentConversationStatsDTO.java

java
@Data
@Builder
public class AgentConversationStatsDTO {
    private Long agentId;
    private String agentName;
    private String period;                    // "7d" | "30d" | "90d"
    
    // KPI metrics
    private Long totalConversations;
    private Long resolvedCount;
    private Long unresolvedCount;
    private Long pendingCount;
    private Double resolutionRate;            // 0–100
    private Double avgFirstResponseSeconds;   // seconds
    private Double csatScore;                 // 1.0–5.0
    private Long slaBreachCount;
    private Integer activeConversations;      // currentLoad
    
    // Chart data
    private List<DailyStatPoint> dailyStats;  // Bar chart data
    
    @Data @Builder
    public static class DailyStatPoint {
        private String date;       // "2026-10-22"
        private Long total;
        private Long resolved;
    }
}
[MODIFY] ConversationRepository.java
Thêm các JPQL/native queries sau:

java
// Tổng conversations được assign cho agent trong khoảng thời gian
@Query("SELECT COUNT(c) FROM Conversation c WHERE c.tenantId = :tenantId AND c.agentAssignedId = :agentId AND c.createdAt >= :since")
Long countByAgentAndPeriod(@Param("tenantId") Long tenantId, @Param("agentId") Long agentId, @Param("since") LocalDateTime since);
// Đếm theo resolutionStatus
@Query("SELECT COUNT(c) FROM Conversation c WHERE c.tenantId = :tenantId AND c.agentAssignedId = :agentId AND c.resolutionStatus = :status AND c.createdAt >= :since")
Long countByAgentAndStatusAndPeriod(...);
// Avg CSAT của agent
@Query("SELECT AVG(c.userSatisfactionRating) FROM Conversation c WHERE c.tenantId = :tenantId AND c.agentAssignedId = :agentId AND c.userSatisfactionRating IS NOT NULL AND c.createdAt >= :since")
Double getAvgCsatByAgentAndPeriod(...);
// Avg first response time (seconds)
@Query(value = "SELECT AVG(EXTRACT(EPOCH FROM (first_agent_response_time - created_at))) FROM conversations WHERE tenant_id = :tenantId AND agent_assigned_id = :agentId AND first_agent_response_time IS NOT NULL AND created_at >= :since", nativeQuery = true)
Double getAvgFirstResponseSecondsByAgent(...);
// Sum SLA breaches
@Query("SELECT COALESCE(SUM(c.slaBreachCount), 0) FROM Conversation c WHERE c.tenantId = :tenantId AND c.agentAssignedId = :agentId AND c.createdAt >= :since")
Long sumSlaBreachesByAgent(...);
// Daily stats cho bar chart (native SQL)
@Query(value = "SELECT DATE(created_at) as date, COUNT(*) as total, SUM(CASE WHEN resolution_status='resolved' THEN 1 ELSE 0 END) as resolved FROM conversations WHERE tenant_id = :tenantId AND agent_assigned_id = :agentId AND created_at >= :since GROUP BY DATE(created_at) ORDER BY date", nativeQuery = true)
List<Object[]> getDailyStatsByAgent(...);
[MODIFY] AgentController.java
Thêm 2 endpoint OWNER-only:

java
// Thống kê chi tiết 1 agent (OWNER only)
@GetMapping("/{id}/conversation-stats")
@Operation(summary = "Get conversation stats for a specific agent (OWNER only)")
public ResponseEntity<AgentConversationStatsDTO> getAgentConversationStats(
        @PathVariable Long id,
        @RequestParam(defaultValue = "7d") String period) {
    Long tenantId = requireTenantId();
    if (!isOwner(tenantId)) return forbidden("Only OWNER can view agent stats");
    return ResponseEntity.ok(agentStatsService.getAgentStats(id, tenantId, period));
}
// Leaderboard tất cả agents (OWNER only)
@GetMapping("/leaderboard")
@Operation(summary = "Get agent leaderboard (OWNER only)")
public ResponseEntity<List<AgentConversationStatsDTO>> getLeaderboard(
        @RequestParam(defaultValue = "7d") String period) {
    Long tenantId = requireTenantId();
    if (!isOwner(tenantId)) return forbidden("Only OWNER can view leaderboard");
    return ResponseEntity.ok(agentStatsService.getLeaderboard(tenantId, period));
}
[NEW] AgentConversationStatsService.java
Service mới tách riêng để tránh làm AgentService quá lớn:

getAgentStats(agentId, tenantId, period) — tính toán đầy đủ KPI + daily chart data
getLeaderboard(tenantId, period) — lấy tất cả agents, tính KPI, sort theo resolvedCount DESC
2. Frontend
[MODIFY] agentApi.js
js
getAgentConversationStats(id, period = '7d') {
  return api.get(`/agents/${id}/conversation-stats`, { params: { period } })
},
getAgentLeaderboard(period = '7d') {
  return api.get('/agents/leaderboard', { params: { period } })
}
[NEW] AgentStatsModal.vue
Modal full-featured với 2 tabs:

Tab 1 — Overview (KPI Cards)

┌─────────────────────────────────────────────────┐
│ 🧑 Nguyễn Văn A · AGENT         [×] Đóng        │
│ ───────────────────────── [7d] [30d] [90d]       │
├───────────┬───────────┬───────────┬─────────────┤
│🗨 Total   │✅ Resolved│📊 Rate    │⏱ Response   │
│   124     │   108     │  87.1%    │  2m 34s     │
├───────────┴───────────┴───────────┴─────────────┤
│ ⭐ CSAT Score 4.2 / 5.0    🚨 SLA Breaches: 3  │
│ ★ ★ ★ ★ ☆                                        │
├─────────────────────────────────────────────────┤
│ Resolution Rate Progress  [██████████░░░] 87%   │
│ CSAT Progress             [████████████░] 84%   │
└─────────────────────────────────────────────────┘
Tab 2 — Charts

┌──────────────────┬──────────────────────────────┐
│  RadialBar       │  Bar Chart (7 ngày gần nhất)  │
│  Resolution Rate │  ██  ██        ██  ██  ██     │
│     87.1%        │  ██  ██  ██   ██  ██  ██     │
│  (màu xanh)      │  T2  T3  T4   T5  T6  T7  T8 │
├──────────────────┴──────────────────────────────┤
│  Donut Chart — Phân phối trạng thái             │
│  ● Resolved (87%) ● Unresolved (8%) ● Pending   │
└─────────────────────────────────────────────────┘
Sử dụng vue3-apexcharts (đã có trong project):

ApexCharts RadialBar → Resolution Rate & CSAT
ApexCharts Bar → Daily Conversations (total vs resolved)
ApexCharts Donut → Status Distribution
[MODIFY] AgentManagement.vue
Phân quyền UI (frontend guard):

html
<!-- Chỉ hiện leaderboard và nút stats khi là OWNER -->
<div v-if="isOwner">
  <!-- Leaderboard Panel -->
  <!-- Nút "Xem thống kê" trên mỗi agent card -->
</div>
Leaderboard Panel (chỉ OWNER thấy):

┌─────────────────────────────────────────────────────┐
│ 🏆 Agent Leaderboard                [7d][30d][90d]  │
├─────┬────────────────┬──────────┬──────────┬────────┤
│ #   │ Agent          │ Resolved │ Rate     │ CSAT   │
├─────┼────────────────┼──────────┼──────────┼────────┤
│ 🥇  │ Nguyễn Văn A  │   108    │  87.1%   │ ⭐4.2  │
│ 🥈  │ Trần Thị B    │    94    │  82.5%   │ ⭐4.0  │
│ 🥉  │ Lê Văn C      │    76    │  75.0%   │ ⭐3.8  │
└─────┴────────────────┴──────────┴──────────┴────────┘
Agent card (OWNER thấy nút 📊 bên cạnh Edit/Delete):

html
<button v-if="isOwner" @click="viewStats(agent)" title="Xem thống kê">
  <Icon icon="mdi:chart-bar" />
</button>
Luồng phân quyền tổng thể
Frontend: isOwner computed → v-if ẩn leaderboard + nút stats
    ↓ (nếu OWNER gọi API)
Backend: if (!isOwner(tenantId)) return 403
    ↓ (nếu pass)
Service: tính KPI từ DB → trả về DTO + chart data
Verification Plan
Backend
./gradlew compileJava — no errors
Test endpoint: GET /api/agents/1/conversation-stats?period=7d với OWNER token → 200 OK
Test với non-OWNER token → 403 Forbidden
Test leaderboard endpoint
Frontend
Login với OWNER → thấy Leaderboard panel + nút 📊 trên agent cards
Login với non-OWNER (AGENT role) → không thấy Leaderboard, không thấy nút 📊
Click nút 📊 → modal mở, 2 tabs hoạt động
Đổi period 7d/30d/90d → charts re-render
Open Questions
NOTE

Cấp độ xem của chính agent: Bạn có muốn agent thấy được thống kê của chính mình không (tự xem, không xem người khác)? Hoặc chỉ OWNER mới thấy tất cả?

NOTE

Leaderboard sort mặc định: Sort theo resolvedCount DESC (nhiều đơn thành công nhất lên đầu). Bạn có muốn thêm tùy chọn sort theo CSAT hoặc Response Time?

Designing Agent Conversation Analytics
với mỗi agent tôi nhĩ cần thống kê conversation với các chỉ số như số conversation, số đơn thành công ...? tham khảo các app chuyên nghiệp
Đây là kế hoạch cho tính năng thống kê Agent! Tôi đã phân tích toàn bộ codebase và thiết kế dựa trên dữ liệu đã có sẵn trong DB.

Tóm tắt kế hoạch
Hệ thống đã có đủ dữ liệu để tính toán — chỉ cần thêm queries và UI:

Backend (3 thay đổi)
File	Thay đổi
[NEW] AgentConversationStatsDTO	DTO chứa tất cả KPI
[MODIFY] ConversationRepository	Thêm 5 native SQL queries aggregate theo agent_assigned_id
[MODIFY] AgentController	2 endpoint mới: /agents/{id}/stats và /agents/leaderboard
Frontend (2 thay đổi)
File	Thay đổi
[NEW] AgentStatsModal.vue	Modal chi tiết từng agent với 6 KPI cards, progress bars, star rating
[MODIFY] AgentManagement.vue	Thêm nút "Xem thống kê" + Agent Leaderboard panel kiểu Zendesk
6 chỉ số KPI (tham khảo Intercom/Zendesk/Freshdesk)
🗨️ Total Conversations — tổng cuộc hội thoại được assign
✅ Resolved — số đã giải quyết thành công
📊 Resolution Rate — tỷ lệ % giải quyết
⏱️ Avg First Response Time — thời gian phản hồi đầu tiên trung bình
⭐ CSAT Score — điểm hài lòng 1–5 sao
🚨 SLA Breaches — số lần vi phạm SLA
Bạn duyệt kế hoạch này không? Tôi sẽ bắt đầu implement ngay.

Implementation Plan
10:40 AM
kế hoạch tốt, chúng ta có nên thêm phân quyền chỉ owner mới được xem, thêm hiển thị thông tin dạng biểu đồ trực quan để dễ hiểu?
Implementation Plan
10:58 AM
