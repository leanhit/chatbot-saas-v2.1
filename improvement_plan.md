# Improvement Plan — Chatbot SaaS v2.1

> **Mục đích**: kế hoạch nâng cấp backend (Java Spring Boot) + frontend (Vue 3) theo hướng data-first, chia phase rõ ràng, mỗi phase có Definition of Done (DoD) và known gaps đối chiếu với code hiện tại.
>
> **Cập nhật**: 2026-11-11 — đã đối chiếu với codebase thực tế.

---

## 0. Nguyên tắc chung

- **Data-first**: domain model ổn định trước, UI sau. Domain chính: `Tenant`, `Agent`, `Customer`, `Conversation`, `ConversationMessage`, `ConversationEvent`, `RoutingRule`, `EscalationTier`, `TrainingMaterial`, `AgentSkill`, `Feedback`, `AiSuggestion`.
- **Tenant isolation là bắt buộc**: mọi query/service phải qua `TenantContext` + `tenantId`; mutation endpoint phải check `TenantPermissionValidator`.
- **Module khép kín**: mỗi module = backend (entity + repository + service + controller + DTO) + frontend (route + store + views). Không build UI trước khi API ổn.
- **Definition of Done (DoD) chung cho mọi phase**:
  1. API chạy đúng, tenant-scoped, có `@PreAuthorize`/permission check phù hợp.
  2. Frontend page dùng được end-to-end (không mock data).
  3. Có test tối thiểu (unit cho service rule, integration cho luồng chính).
  4. Không còn `TODO`/stub trong luồng chính của phase.
  5. Cập nhật file này: đánh dấu phase done + ghi known gaps còn lại.

---

## Phase 1 — Foundation & Tenant/Security (Giai đoạn 1)

### Scope
- Tenant context, RBAC (`TenantPermissionValidator`), audit log (`TenantAuditLogService`).
- Base entity: `createdAt/updatedAt`, soft delete (`isDeleted`), DTO + mapper.
- Centralized error handling, API versioning, health check.

### Trạng thái code hiện tại
- TenantContext, TenantPermissionValidator, TenantAuditLogService, soft delete trên nhiều entity.
- `@PreAuthorize` trên `AnalyticsController`, permission check trong `TrainingController`/`OnboardingController`.

### DoD
- [x] Tenant isolation trên các module chính.
- [~] Audit log được ghi cho **mọi** mutation nhạy cảm — đã bổ sung `CUSTOMER_PROFILE_DELETED/ANONYMIZED` + `ROUTING_RULE_*` (2026-11-11); còn thiếu training/escalation/SLA/agent/package mutations.
- [x] RBAC matrix rõ ràng — đã tạo `docs/rbac-matrix.md` (2026-11-11).

### Known gaps
- Audit log coverage chưa đồng đều giữa các controller (một phần đã fix).
- ~~Chưa có RBAC matrix document~~ → đã có `docs/rbac-matrix.md`.

---

## Phase 2 — Agent Enablement (Giai đoạn 3)

### Scope
- Training materials/categories, agent training progress, onboarding tasks/progress, skill assessment, certification levels.
- Agent conversation stats (KPI, leaderboard).

### Trạng thái code hiện tại
- Backend đầy đủ: `TrainingService`, `SkillAssessmentService`, `CertificationService`, `OnboardingService`, `AgentConversationStatsService`.
- API: `/api/training/**`, `/api/onboarding/**`.
- Frontend: có `admin/SkillsManagement.vue`; **cần verify** `TrainingLibrary`, `OnboardingWizard`, `SkillMatrix`, `Certification` views đã có chưa.

### DoD
- [x] Agent hoàn thành training → skill score cập nhật → certification auto-award: `TrainingService.completeTraining` → `updateSkillScore` → `checkAndAutoAwardCertifications` (verify 2026-11-11).
- [x] Onboarding checklist chặn agent "đi online": `AgentService.updateAgentStatus(ONLINE)` → `assertOnboardingComplete` throw; `PresenceWebSocketHandler` cũng check (verify 2026-11-11).
- [x] Frontend: Training Library + Onboarding Wizard + Skill Matrix + Certification Overview đã có views + routes (verify 2026-11-11).
- [x] Supervisor approve/reject onboarding task: đã expose `POST /api/onboarding/progress/approve|reject` (fix 2026-11-11).

### Known gaps
- ~~Certification award chỉ manual~~ → đã có auto-award qua `checkAndAutoAwardCertifications`.
- ~~`approveTask/rejectTask` chưa expose~~ → đã thêm endpoints.

---

## Phase 3 — Customer Experience (Giai đoạn 4)

### Scope
- Customer profile (tier, block, tags, custom attributes), feedback/CSAT/NPS, VIP routing, segments.

### Trạng thái code hiện tại
- `CustomerProfileService`, `FeedbackService`/`FeedbackController` đầy đủ filter (status/type/date-range/recent).
- Frontend: `customer/CustomerProfiles.vue`, `CustomerProfileDetail.vue`, `FeedbackSurvey.vue`, `NPSPanel.vue`, `VIPQueue.vue`, `SegmentOverview.vue`, `CustomerAnalytics.vue`.
- VIP tier đã được `RoutingRuleService.createDefaultRoutingRules` dùng (vip_queue/enterprise_queue).

### DoD
- [x] Feedback gắn với conversation + cập nhật `satisfactionScore`: `createFeedback` tự gọi `updateCustomerSatisfactionScore` + `recalculateTier` (verify 2026-11-11).
- [x] Customer timeline/history hiển thị đầy đủ trên `CustomerProfileDetail` — tab Timeline dùng `CustomerTimelineService` unified events (verify 2026-11-11).
- [x] VIP customer ưu tiên routing: default rules `vip_queue`/`enterprise_queue` + `handleRouteToQueue` giờ try assign ngay (fix 2026-11-11).

### Known gaps
- ~~`handleRouteToQueue` chỉ lưu `queueName`, không có consumer~~ → đã fix (try assign + retry).
- Customer segment: có `SegmentOverview.vue` nhưng cần verify backend `CustomerSegmentService`/`CustomerSegmentationService` — `FeedbackService` đã inject `CustomerSegmentationService` nên backend có tồn tại.

---

## Phase 4 — Automation & Routing (Giai đoạn 2)

### Scope
- Attribute-based routing rules, skill-based + workload balancing, multi-tier escalation, SLA monitoring, supervisor takeover/whisper.

### Trạng thái code hiện tại
- `RoutingRuleService` (conditions + actions: assign/route/escalate/block/custom).
- `EscalationService` (tier theo thời gian, scheduled 1 phút, notify WebSocket+email+Slack).
- `SLAMonitorService` (SLA config per tier + package fallback, breach count, scheduled).
- `AIEscalationService` (LLM sentiment/complexity → auto-escalate).
- `IntelligentSupervisorService` takeover/release/session đã wire vào `Conversation` + WebSocket (fix 2026-11-11).
- Frontend: `admin/RoutingRules.vue`, `EscalationTiers.vue`, `SLAMonitoring.vue`.

### DoD
- [x] **SLA breach → escalation thực sự xảy ra**: `SLAMonitorService.triggerEscalation()` đã gọi `EscalationService.escalateConversation()` + broadcast `sla_escalation` qua WebSocket (fix 2026-11-11).
- [x] **Supervisor takeover khép kín**: `IntelligentSupervisorService.takeoverConversation` mark `isTakenOverByAgent`/`ACTIVE_AGENT` khi sessionType=TAKEOVER; `releaseConversation` restore state; broadcast `SUPERVISOR_EVENT` qua WebSocket (fix 2026-11-11).
- [x] Skill-based routing: routing rule action hỗ trợ `requiredSkills` → `AgentAssignmentService.assignConversationToAgent` match `Agent.skills` + load balancing (fix 2026-11-11). **Bridge đã có**: `AgentSkill` score ≥ 60 (INTERMEDIATE+) tự sync sang `Agent.skills` qua `SkillAssessmentService`/`TrainingService` → `addSkillToAgent` (skill string = category lowercase, vd "technical", "billing") (fix 2026-11-11).
- [x] Workload balancing: `AgentAssignmentService.assignByLoadBalancing`/`assignBySkillMatchAndLoad` đã cân bằng theo `currentLoad`/`maxConcurrentConversations` + filter ONLINE.
- [x] Frontend supervisor dashboard: `SupervisorDashboard.vue` có whisper button + modal gọi `POST /{id}/whisper` (fix 2026-11-11).

### Known gaps (đã xử lý 2026-11-11)
1. ~~`SLAMonitorService.triggerEscalation()` là stub~~ → đã wire `EscalationService` + WebSocket `sla_escalation`.
2. ~~`IntelligentSupervisorService.takeoverConversation/releaseConversation` thiếu wiring~~ → đã mark conversation state + broadcast `SUPERVISOR_EVENT`.
3. ~~`handleRouteToQueue` không có consumer~~ → đã thêm `tryAssignFromQueue` (skill-aware → load-balance fallback → `reassignConversation`); retry mỗi 30s qua `BotInboxAutoAssignService`.
4. ~~`SupervisorSessionService` deprecated duplicate~~ → đã xóa file, không còn reference.
5. ~~Whisper: `isSilent`/`canIntervene` có field nhưng chưa có message path riêng~~ → backend `sendWhisper()` + endpoint + frontend whisper modal trong `SupervisorDashboard.vue` (fix 2026-11-11).

---

## Phase 5 — AI & Analytics (Giai đoạn 5)

### Scope
- Daily aggregation, customer journey, funnel, custom reports, AI suggestions + feedback, sentiment.

### Trạng thái code hiện tại
- `AnalyticsController` (`/api/analytics/summary`, `/journey/{id}`, `/funnel` + stages CRUD).
- `AnalyticsAggregationService`, `CustomerJourneyService`, `FunnelService`, `ReportService` (5 report types).
- `AiSuggestionService` (generate, confidence, PENDING status), `AIEscalationService`.
- Frontend: `analytics/CustomReports.vue`, `CustomerJourney.vue`, `FunnelAnalysis.vue`.

### DoD
- [x] Aggregation chạy scheduled: `AnalyticsAggregationService.aggregateDailyScheduled` cron 01:30 hằng ngày, ShedLock, per-tenant `TenantContext` (verify 2026-11-11).
- [x] AI suggestion accept/reject feedback loop: `AiSuggestionController` có `/{id}/accept` + `/{id}/reject` → status ACCEPTED/REJECTED → đếm trong `aggregateForDate` (`aiSuggestionsAccepted/Rejected`, `aiAcceptanceRate`) (verify 2026-11-11).
- [x] Report scheduling: `ReportSchedule` + `ReportScheduleService` + `ReportScheduler` (poll mỗi 60s, cron expression per schedule, ShedLock) (verify 2026-11-11 — đánh giá trước đó sai, đã tồn tại).
- [ ] Caching cho query báo cáo dài — chưa verify Redis cache layer (minor).

### Known gaps
- ~~`ReportSchedule` chưa implement~~ → đã có đầy đủ.
- ~~Sentiment realtime~~ → `SentimentController`: `POST /api/ai/sentiment/score/{conversationId}` (on-demand scoring) + `GET /conversation/{id}` latest — đủ cho use case realtime per-conversation.
- Word cloud / heatmap charts — cần verify frontend analytics views có đủ chart types.

---

## Phase 6 — Scale, Compliance & Integrations (Giai đoạn 6)

### Scope
- Webhooks, third-party integrations, GDPR export/delete, 2FA, data retention, API key management, rate limit/quota, DLQ.

### Trạng thái code hiện tại
- Webhook backend **đã có** cho payment domain: `payment/gateway/WebhookService` + `WebhookSignatureService` + `WebhookDeadLetter` (DLQ) + `MerchantWebhookDispatcher`; frontend `WebhookManagement.vue` gọi `paymentAPI` — khớp nhau (verify 2026-11-11).
- Webhook cho **conversation/bot events** (outbound tenant webhooks) — chưa làm, cần quyết định scope.
- GDPR: tenant-level export/delete đã có (`DataExportController`, `DataDeletionController` 30-day grace); **customer-level vừa thêm** `POST /api/customer/profiles/{id}/anonymize` + `GET /{id}/export` (fix 2026-11-11).
- 2FA: `TwoFactorService` + `TwoFactorController` đã có (verify 2026-11-11).
- Rate limit: `TenantRateLimitFilter` per-tenant 600 req/min Redis + auth/payment/penny limiters (verify 2026-11-11).
- API key: `MerchantApiKeyController` cho payment merchant; tenant API keys chưa thấy.
- Data retention: soft delete (`isDeleted`) có sẵn; chưa có retention policy job.

### DoD
- [x] Webhook CRUD + delivery + retry — payment domain backend + frontend khớp nhau.
- [x] GDPR: tenant export/delete + **customer-level anonymize/export** (fix 2026-11-11).
- [x] Rate limit/quota per tenant — `TenantRateLimitFilter` Redis sliding window.
- [x] 2FA — `TwoFactorService`/`TwoFactorController` đã tồn tại.
- [ ] Data retention policy job (purge soft-deleted records theo TTL) — chưa có.
- [ ] Tenant API keys (nếu cần public API) — chưa có.

### Known gaps
- Conversation-events outbound webhook — chưa làm (cần nếu tích hợp CRM).
- Data retention auto-purge — chưa có scheduled job.
- ~~Frontend GDPR UI~~ → đã thêm Export (download JSON) + Anonymize (confirm) buttons vào `CustomerProfileDetail.vue` (fix 2026-11-11).

---

## Ma trận tích hợp giữa các module (điểm dễ gãy nhất)

| Flow | Trạng thái |
|---|---|
| SLA breach → Escalation → Notification | Xong — `triggerEscalation()` gọi `EscalationService` + broadcast `sla_escalation` |
| Escalation tier → notify agent/supervisor | Xong — WebSocket + email + Slack |
| AI sentiment → auto-escalate | Xong — `AIEscalationService` (async) |
| Routing rule → assign/queue/escalate | Xong — `handleRouteToQueue` giờ try assign ngay + retry qua auto-assign job |
| Supervisor takeover → conversation state | Xong — TAKEOVER mark `isTakenOverByAgent`/`ACTIVE_AGENT`, release restore + `SUPERVISOR_EVENT` broadcast |
| Feedback submit → customer satisfactionScore | Xong — `createFeedback` auto update score + recalculate tier |
| Training complete → skill score → certification | Xong — `completeTraining` chain auto-award |
| Analytics ← conversation events | Xong — nightly aggregation cron + ReportScheduler + sentiment/AI metrics |

**Nguyên tắc**: trước khi thêm feature mới, ưu tiên đóng các dòng chưa xong trong bảng trên — đây là nơi "có code nhưng chưa tạo giá trị".

---

## Thứ tự đề xuất thực hiện tiếp theo

1. ~~Hotfix integration gaps Phase 4~~ — **xong** (2026-11-11): SLA→escalation, supervisor takeover, queue consumer, skill-based routing, whisper backend.
2. ~~Verify & hoàn thiện Phase 2~~ — **xong** (2026-11-11): auto-certification, onboarding enforcement, frontend training views, approve/reject endpoints.
3. ~~Phase 5 hardening~~ — **xong** (2026-11-11, verify): scheduled aggregation, ReportSchedule/ReportScheduler, AI suggestion feedback loop, sentiment endpoints đều đã tồn tại.
4. ~~Phase 6~~ — **phần lớn xong** (2026-11-11): payment webhooks, GDPR tenant+customer, 2FA, rate-limit đều đã có; vừa thêm customer anonymize/export. Còn lại: data retention purge job, conversation-events webhook, tenant API keys — theo nhu cầu.
5. Dọn nợ kỹ thuật còn lại: ~~audit log coverage~~ (một phần — xem Phase 1), ~~RBAC matrix doc~~ (xong), ~~bridge `AgentSkill`↔`Agent.skills`~~ (xong — score ≥60 auto-sync). Frontend whisper panel + GDPR buttons đã xong (2026-11-11).
