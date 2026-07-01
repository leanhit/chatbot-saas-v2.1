# Kế Hoạch Cải Tiến Hệ Thống Conversation Routing

## Tổng quan

Tài liệu này lập kế hoạch cải tiến hệ thống xử lý tin nhắn và conversation routing cho chatbot-saas, dựa trên phân tích các platform SaaS hàng đầu (Intercom, Drift, Zendesk, Freshchat, ManyChat, Botpress) và đánh giá tính phù hợp với kiến trúc hiện tại.

---

## Cấu trúc hệ thống hiện tại

### Đã có (Existing)
- ✅ Conversation entity với `isTakenOverByAgent`, `agentAssignedId`, `status`
- ✅ TakeoverService cho bot ↔ agent handoff
- ✅ ConversationService với `takeoverConversation()`, `releaseConversation()`
- ✅ Facebook webhook consumer với Kafka, dedup
- ✅ PennyBotManager cho bot processing
- ✅ WebSocket cho real-time updates
- ✅ Redis cho message history
- ✅ Tenant context
- ✅ Channel enum (FACEBOOK, ZALO, INSTAGRAM, WHATSAPP, INTERNAL)
- ✅ Multi-channel support (Facebook đã implement)
- ✅ Notification system (WebSocket + SSE)

### Chưa có (Missing)
- ❌ Agent entity/management system
- ❌ Skills/attributes cho agents
- ❌ SLA tracking
- ❌ Routing rules engine
- ❌ Workflow builder
- ❌ AI-based escalation decision
- ❌ Multi-tier escalation logic
- ❌ Bot inbox isolation

---

## Lộ trình cải tiến

### Phase 1: Cải tiến cơ bản (1-2 tuần)

#### 1.1 Bot Inbox Isolation
**Mục tiêu:** Tách biệt conversations giữa bot và agent

**Tasks:**
- [ ] Thêm filter methods vào ConversationService
  - `getBotInboxConversations()` - conversations đang được bot xử lý
  - `getAgentInboxConversations()` - conversations đang được agent xử lý
- [ ] Cập nhật frontend để hiển thị 2 inbox riêng biệt
- [ ] Thêm UI filter để chuyển giữa bot/agent inbox
- [ ] Implement auto-assign rule cho bot inbox

**Files cần thay đổi:**
- `ConversationService.java` - Thêm filter methods
- `ConversationRepository.java` - Thêm query methods
- Frontend: Conversation list component

**Độ khó:** Thấp

---

#### 1.2 Cải thiện Direct Notifications
**Mục tiêu:** Tăng hiệu quả thông báo cho agents

**Tasks:**
- [ ] Thêm individual agent notification (chứ không phải broadcast toàn tenant)
- [ ] Thêm priority-based notification (urgent, high, medium, low)
- [ ] Thêm notification channels (Slack, email)
- [ ] Implement notification acknowledgment
- [ ] Thêm notification history tracking

**Files cần thay đổi:**
- `NotificationWebSocketHandler.java` - Thêm individual notification
- `MessagePublisher.java` - Thêm priority channels
- `notificationStore.js` - Thêm priority handling

**Độ khó:** Thấp

---

#### 1.3 Attribute-based Routing
**Mục tiêu:** Route conversations dựa trên customer attributes

**Tasks:**
- [ ] Thêm fields vào Conversation entity
  - `customerTier` (VIP, Enterprise, Standard)
  - `language` (en, vi, etc.)
  - `customAttributes` (Map<String, Object>)
- [ ] Implement routing rules trong ConversationService
  - VIP customers → Priority queue
  - Language-based routing → Language-specific agents
- [ ] Thêm UI để quản lý routing rules
- [ ] Integrate với Facebook user info để auto-fill attributes

**Files cần thay đổi:**
- `Conversation.java` - Thêm attribute fields
- `ConversationService.java` - Thêm routing logic
- `FacebookEventConsumer.java` - Extract attributes từ user info
- Frontend: Routing rules management UI

**Độ khó:** Thấp

---

### Phase 2: Cải tiến trung cấp (1 tháng)

#### 2.1 SLA Monitoring
**Mục tiêu:** Track và enforce response time SLA

**Tasks:**
- [ ] Thêm tracking fields vào Conversation entity
  - `firstAgentResponseTime`
  - `firstBotResponseTime`
  - `slaBreachCount`
  - `expectedResponseTime`
- [ ] Implement SLAMonitorService
  - Track response times
  - Check SLA breaches
  - Trigger escalation khi SLA exceeded
- [ ] Thêm scheduled job để check SLA breaches
- [ ] Thêm UI để hiển thị SLA metrics
- [ ] Thêm alerts cho SLA breaches

**Files cần thay đổi:**
- `Conversation.java` - Thêm SLA fields
- `SLAMonitorService.java` - New service
- `ConversationService.java` - Update response time tracking
- Frontend: SLA dashboard

**Độ khó:** Trung bình

---

#### 2.2 Multi-tier Escalation
**Mục tiêu:** Implement escalation chain với multiple tiers

**Tasks:**
- [ ] Define escalation tiers
  - Tier 1: Agent (5 minutes)
  - Tier 2: Team Lead (15 minutes)
  - Tier 3: Supervisor (30 minutes)
- [ ] Implement EscalationService
  - Track current escalation tier
  - Escalate to next tier khi timeout
  - Notify appropriate person at each tier
- [ ] Thêm escalation rules engine
- [ ] Thêm escalation history tracking
- [ ] Thêm UI để quản lý escalation rules

**Files cần thay đổi:**
- `EscalationService.java` - New service
- `EscalationTier.java` - New entity
- `Conversation.java` - Thêm escalation tracking
- Frontend: Escalation management UI

**Độ khó:** Trung bình

---

#### 2.3 Special Workflows
**Mục tiêu:** Handle error và timeout scenarios

**Tasks:**
- [ ] Implement ErrorWorkflow
  - Log errors
  - Notify admin
  - Send fallback message to user
  - Create escalation ticket if needed
- [ ] Implement TimeoutWorkflow
  - Detect inactive conversations
  - Send timeout message
  - Auto-close or reassign
- [ ] Implement ConversationEndWorkflow
  - Summarize conversation
  - Offer follow-up actions
  - Update customer data
- [ ] Integrate workflows vào FacebookEventConsumer

**Files cần thay đổi:**
- `ErrorWorkflow.java` - New service
- `TimeoutWorkflow.java` - New service
- `ConversationEndWorkflow.java` - New service
- `FacebookEventConsumer.java` - Integrate workflows

**Độ khó:** Trung bình

---

### Phase 3: Cải tiến nâng cao (2-3 tháng)

#### 3.1 Agent Management System
**Mục tiêu:** Xây dựng hệ thống quản lý agents

**Tasks:**
- [ ] Create Agent entity
  - Basic info (name, email, role)
  - Skills (billing, technical, sales)
  - Availability status (online, offline, away)
  - Current load (number of active conversations)
  - Assignment preferences
- [ ] Implement AgentRepository
- [ ] Implement AgentService
  - CRUD operations
  - Availability tracking
  - Load balancing
- [ ] Thêm UI để quản lý agents
- [ ] Integrate với authentication system

**Files cần thay đổi:**
- `Agent.java` - New entity
- `AgentRepository.java` - New repository
- `AgentService.java` - New service
- `AgentController.java` - New controller
- Frontend: Agent management UI

**Độ khó:** Cao

---

#### 3.2 Skills-based Routing
**Mục tiêu:** Match agents với conversations dựa trên skills

**Tasks:**
- [ ] Define skill taxonomy
  - Technical skills
  - Language skills
  - Product knowledge
- [ ] Implement AgentAssignmentService
  - Match skills với conversation tags
  - Consider availability
  - Consider load balancing
  - Consider priority
- [ ] Implement skill matching algorithm
- [ ] Thêm UI để quản lý skills
- [ ] Add skill-based routing rules

**Files cần thay đổi:**
- `AgentAssignmentService.java` - New service
- `Skill.java` - New entity
- `Agent.java` - Thêm skills field
- `Conversation.java` - Thêm requiredSkills field
- Frontend: Skills management UI

**Độ khó:** Cao

---

#### 3.3 AI-based Escalation
**Mục tiêu:** Sử dụng LLM để quyết định khi nào nên escalate

**Tasks:**
- [ ] Integrate LLM (OpenAI, Anthropic, hoặc local model)
- [ ] Implement AIEscalationService
  - Analyze message sentiment
  - Detect complexity
  - Identify escalation triggers
  - Make escalation decision
- [ ] Define escalation criteria
  - Sentiment threshold
  - Complexity score
  - Intent classification
- [ ] Implement prompt engineering
- [ ] Add fallback logic khi LLM unavailable

**Files cần thay đổi:**
- `AIEscalationService.java` - New service
- `LLMClient.java` - New service
- `FacebookEventConsumer.java` - Integrate AI escalation
- Configuration: LLM API keys

**Độ khó:** Cao

---

### Phase 4: Cải tiến chuyên sâu (3-6 tháng)

#### 4.1 Workflow Engine
**Mục tiêu:** Xây dựng engine để execute workflows

**Tasks:**
- [ ] Design workflow architecture
  - Nodes (start, end, action, condition, branching)
  - Transitions
  - Variables
  - Context
- [ ] Implement WorkflowEngine
  - Parse workflow definitions
  - Execute nodes sequentially
  - Handle branching logic
  - Manage state
- [ ] Implement workflow storage
- [ ] Add workflow versioning
- [ ] Implement workflow testing/debugging

**Files cần thay đổi:**
- `WorkflowEngine.java` - New service
- `Workflow.java` - New entity
- `WorkflowNode.java` - New entity
- `WorkflowTransition.java` - New entity
- `WorkflowExecution.java` - New entity

**Độ khó:** Rất cao

---

#### 4.2 Visual Workflow Builder
**Mục tiêu:** Xây dựng UI để tạo workflows visually

**Tasks:**
- [ ] Design UI architecture
  - Canvas for drag-drop
  - Node palette
  - Connection editor
  - Property inspector
- [ ] Implement frontend components
  - WorkflowCanvas component
  - Node components
  - Connection components
  - Property editor
- [ ] Integrate với WorkflowEngine
- [ ] Add workflow validation
- [ ] Add workflow preview/testing

**Files cần thay đổi:**
- Frontend: Workflow builder UI (new module)
- Backend: Workflow API endpoints
- Integration: Frontend ↔ Backend communication

**Độ khó:** Rất cao

---

#### 4.3 PennyBot Integration
**Mục tiêu:** Tích hợp workflow engine với PennyBot

**Tasks:**
- [ ] Design integration architecture
- [ ] Implement PennyBot adapter
- [ ] Map PennyBot flows to workflows
- [ ] Handle PennyBot events
- [ ] Sync state giữa workflows và PennyBot
- [ ] Add fallback khi PennyBot unavailable

**Files cần thay đổi:**
- `PennyBotAdapter.java` - New service
- `WorkflowEngine.java` - Thêm PennyBot integration
- `PennyBotManager.java` - Update for workflow sync

**Độ khó:** Rất cao

---

## Priority Matrix

| Feature | Impact | Effort | Priority | Phase |
|---------|--------|--------|----------|-------|
| Bot Inbox | High | Low | **P0** | 1 |
| Direct Notifications | High | Low | **P0** | 1 |
| Attribute-based Routing | High | Low | **P0** | 1 |
| SLA Monitoring | High | Medium | **P1** | 2 |
| Multi-tier Escalation | High | Medium | **P1** | 2 |
| Special Workflows | Medium | Medium | **P1** | 2 |
| Agent Management | High | High | **P2** | 3 |
| Skills-based Routing | High | High | **P2** | 3 |
| AI-based Escalation | Medium | High | **P2** | 3 |
| Workflow Engine | High | Very High | **P3** | 4 |
| Visual Builder | Medium | Very High | **P3** | 4 |
| PennyBot Integration | Medium | Very High | **P3** | 4 |

---

## Success Metrics

### Phase 1 Metrics
- **Response Time:** Giảm 30% thời gian response trung bình
- **Assignment Efficiency:** Tăng 50% conversations được assign đúng ngay lần đầu
- **Notification Effectiveness:** Tăng 40% agents nhận thông báo và phản hồi trong 5 phút

### Phase 2 Metrics
- **SLA Compliance:** Tăng 80% conversations đạt SLA
- **Escalation Rate:** Giảm 50% escalation không cần thiết
- **Error Recovery:** Tăng 60% error cases được xử lý tự động

### Phase 3 Metrics
- **Skill Match Rate:** Tăng 70% conversations được assign agent có skill phù hợp
- **Agent Utilization:** Tối ưu hóa load balancing (variance < 20%)
- **AI Escalation Accuracy:** Đạt 85% accuracy trong escalation decisions

### Phase 4 Metrics
- **Workflow Adoption:** 60% conversations sử dụng workflows
- **Workflow Efficiency:** Giảm 40% thời gian setup conversation flows
- **Flexibility:** Tăng 50% khả năng customize conversation logic

---

## Risks & Mitigations

### Technical Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Performance degradation với nhiều features | High | Implement caching, optimize queries, load testing |
| LLM API costs quá cao | Medium | Implement rate limiting, use local models when possible |
| Workflow engine quá phức tạp | High | Start simple, iterate, thorough testing |
| Agent management không scalable | Medium | Design for horizontal scaling from start |

### Business Risks

| Risk | Impact | Mitigation |
|------|--------|------------|
| Users không adopt new features | Medium | Provide training, gradual rollout, gather feedback |
| Maintenance cost tăng | Medium | Automate monitoring, alerting, self-healing |
| Competitors có features tốt hơn | High | Focus on unique value propositions, continuous innovation |

---

## Resource Requirements

### Phase 1 (1-2 tuần)
- **Backend Developer:** 1 full-time
- **Frontend Developer:** 0.5 full-time
- **QA:** 0.25 full-time

### Phase 2 (1 tháng)
- **Backend Developer:** 1 full-time
- **Frontend Developer:** 0.75 full-time
- **QA:** 0.5 full-time

### Phase 3 (2-3 tháng)
- **Backend Developer:** 1.5 full-time
- **Frontend Developer:** 1 full-time
- **DevOps:** 0.25 full-time
- **QA:** 0.75 full-time

### Phase 4 (3-6 tháng)
- **Backend Developer:** 2 full-time
- **Frontend Developer:** 1.5 full-time
- **DevOps:** 0.5 full-time
- **QA:** 1 full-time
- **UX Designer:** 0.5 full-time

---

## Dependencies

### External Dependencies
- **LLM API:** OpenAI/Anthropic cho AI-based escalation
- **Slack API:** Cho Slack notifications
- **Email Service:** SendGrid/Mailgun cho email notifications

### Internal Dependencies
- **Authentication System:** Cho agent management
- **Permission System:** Cho routing rules
- **Monitoring System:** Cho SLA tracking
- **Database:** Schema migrations cho new entities

---

## Timeline Summary

```
Phase 1: 1-2 tuần
├── Bot Inbox
├── Direct Notifications
└── Attribute-based Routing

Phase 2: 1 tháng
├── SLA Monitoring
├── Multi-tier Escalation
└── Special Workflows

Phase 3: 2-3 tháng
├── Agent Management System
├── Skills-based Routing
└── AI-based Escalation

Phase 4: 3-6 tháng
├── Workflow Engine
├── Visual Workflow Builder
└── PennyBot Integration
```

**Total Estimated Time:** 6-10 tháng

---

## Next Steps

1. **Review & Approve:** Stakeholders review và approve kế hoạch
2. **Resource Allocation:** Allocate team members theo timeline
3. **Setup Phase 1:** Bắt đầu với Phase 1 tasks
4. **Weekly Reviews:** Regular progress reviews
5. **Adjust as Needed:** Adapt plan dựa trên feedback và metrics

---

## References

- Intercom: https://www.intercom.com/help/en/articles/9630589-route-customer-conversations-to-the-right-team
- Zendesk: https://support.zendesk.com/hc/en-us/articles/5746068733338-Designing-your-conversational-messaging-workflow
- Botpress: https://botpress.com/docs/studio/guides/advanced/kitchen-sink-advanced-starter-template
- ManyChat: https://community.manychat.com/general-q-a-43/human-handover-4122
- Drift: https://gethelp.drift.com/hc/en-us/articles/360019516693-Bot-Skill-Route-Conversation
