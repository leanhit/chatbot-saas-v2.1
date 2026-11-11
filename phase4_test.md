
Dưới đây là checklist test còn thiếu chi tiết cho từng file, theo đúng kịch bản Phase 4. Mỗi file nên được tạo riêng để test một lớp và dễ maintain.

## ✅ TRẠNG THÁI TEST PHASE 4 (Updated: Nov 11, 2026)

### Unit Tests (Mockito) - ✅ HOÀN THÀNH (62/62 tests passed)
- ✅ RoutingServiceMockitoTest.java - 8 tests passed
- ✅ EscalationServiceMockitoTest.java - 12 tests passed
- ✅ IntelligentRoutingServiceTest.java - 18 tests passed (gồm 2 test tenant-isolation mới cho `assignConversationToAgent`)
- ✅ IntelligentEscalationServiceTest.java - 9 tests passed
- ✅ ConditionEvaluationTest.java - 15 tests passed
- ✅ Đã xóa 3 test file obsolete tham chiếu `SupervisorSessionService` (service đã deprecated/xóa)

### Integration Tests (Testcontainers) - ✅ HOÀN THÀNH (chạy được sau khi fix test JVM spawn)
- ✅ TenantIsolationTest.java - 5 tests passed
- ✅ RoutingRepositoryIntegrationTest.java - 8 tests passed
- ✅ EscalationRepositoryIntegrationTest.java - 8 tests passed
- ✅ SupervisorRepositoryIntegrationTest.java - 8 tests passed
- ✅ TenantIsolationIntegrationTest.java (tenant) - 7 tests passed
- ✅ Controller integration tests (Routing/Escalation/Supervisor) - 22 tests passed

### Smoke Tests (Spring Context) - ✅ HOÀN THÀNH
- ✅ RoutingSmokeTest.java - 8 tests passed
- ✅ EscalationSmokeTest.java - 9 tests passed

### Test infra fix (2026-11-11)
- Root cause: JDK spawn-helper failure khi Gradle fork test JVM → fix bằng `systemProp.jdk.lang.Process.launchMechanism=VFORK` trong gradle.properties + `jvmArgs '--enable-preview'` + `maxHeapSize 2g` trong test task.
- Tổng: ~120 integration/smoke tests pass, 0 failures.

### Docker Environment Status
- ✅ Docker test services running (PostgreSQL, Redis, RabbitMQ, Kafka, Zookeeper)
- ✅ App deployed and running successfully (http://localhost:8080)
- ✅ Unit tests pass in Docker environment
- ✅ Integration/Smoke/Controller tests pass (Testcontainers + Spring context)

---

## CHI TIẾT TEST CHECKLIST

backend/src/test/java/com/chatbot/core/routing/integration/RoutingRepositoryIntegrationTest.java
Mục tiêu:

Verify repository layer với database thật
Check query correctness theo tenant, status, priority, target type
Checklist:

 Test save routing rule thành công

setup tenantId = 1
create RoutingRule với name, priority, targetType, ruleType
save
assert id not null
assert tenantId đúng
 Test findByTenantIdAndIsActiveTrueOrderByPriorityAsc

create 3 rules cùng tenant với priority 10, 20, 5
assert order = 5, 10, 20
 Test findByTenantIdAndRuleTypeAndIsActiveTrue

create SKILL_BASED and PRIORITY_BASED rules
query ruleType SKILL_BASED
assert chỉ trả về SKILL_BASED
 Test findByTenantIdAndTargetTypeAndIsActiveTrue

create AGENT, TEAM, SKILL rule
query TEAM
assert đúng 1 entity
 Test findByTenantIdAndIsActiveTrueAndIsDefaultTrue

create default rule true, false
query default rules
assert only default true
 Test findFirstByTenantIdAndIsActiveTrueOrderByPriorityAsc

create rules with different priorities
assert lowest priority returned
 Test query active only

create isActive true + false
query active rules
assert no inactive returned
 Test query by tenant isolation

tenant 1 rule + tenant 2 rule
query tenant 1
assert không thấy tenant 2
backend/src/test/java/com/chatbot/core/routing/integration/EscalationRepositoryIntegrationTest.java
Mục tiêu:

Verify escalation rules repository và DB mapping
Checklist:

 Test save escalation rule thành công

create EscalationRule với triggerType, escalationAction, priority
save
assert id not null
 Test query by trigger type

create WAIT_TIME_EXCEEDED and NO_RESPONSE
query WAIT_TIME_EXCEEDED
assert đúng item
 Test query active escalation rules only

create 2 active + 1 inactive
query active
assert exclude inactive
 Test query by escalation action

create ASSIGN_TO_AGENT and ASSIGN_TO_SUPERVISOR
query ASSIGN_TO_SUPERVISOR
assert đúng
 Test escalateToSupervisor flag

set escalateToSupervisor = true
query
assert boolean true
 Test escalateToTeam flag

set escalate to team
assert team id stored
 Test priority ordering

create priorities 1, 5, 3
query order ascending or descending depending requirement
assert đúng order
 Test tenant isolation on escalation rules

tenant 1 + tenant 2
query tenant 1
assert no tenant 2 data
backend/src/test/java/com/chatbot/core/routing/integration/SupervisorRepositoryIntegrationTest.java
Mục tiêu:

Verify supervisor session repository, lifecycle, unique constraints
Checklist:

 Test save supervisor session

session with conversationId, supervisorId, status, startedAt
save
assert id not null
 Test query by supervisor ID

create 2 sessions same supervisor, one different
query supervisorId = x
assert only those records
 Test query by conversation ID

same conversation with multiple sessions
assert exact match
 Test query active sessions only

create active and closed sessions
query active
assert only active
 Test query by session type or mode

if exists sessionType enum
query one type
assert match
 Test count active sessions

3 active sessions
count active
assert 3
 Test unique active session constraint

same supervisor + conversation + active
assert duplicate not allowed or service rejects
 Test tenant isolation

tenant 1 session + tenant 2 session
query tenant 1
assert only tenant 1
backend/src/test/java/com/chatbot/core/routing/integration/TenantIsolationTest.java
Mục tiêu:

Verify multi-tenant safety end-to-end at repository/service boundary
Checklist:

 Test tenant A cannot see tenant B routing rules

create routing rules in tenant 1 and 2
query tenant 1
assert no tenant 2 items returned
 Test tenant A cannot see tenant B escalation rules

same as above
 Test tenant A cannot see tenant B agent workload

create workload with tenant 1 and 2
query tenant 1
assert only tenant 1
 Test tenant A cannot see tenant B supervisor sessions

create sessions separate tenant
query tenant 1
assert only tenant 1
 Test routeConversation uses only tenant data

set tenant context to tenant 1
create agent workload tenant 1 and tenant 2
route conversation
assert assigned agent belongs to tenant 1 only
 Test default rule not leaking across tenants

tenant 1 default rule
tenant 2 default rule
query default for tenant 1
assert tenant 1 default only
 Test no cross-tenant data leak in history

conversation from tenant 1 and 2
query history by tenant 1
assert no tenant 2 record
backend/src/test/java/com/chatbot/core/routing/smoke/RoutingSmokeTest.java
Mục tiêu:

Verify end-to-end routing flow với data thật và service context
Checklist:

 Test priority-based routing end-to-end

create 2 available agents:
agent A priorityScore high
agent B lower
route conversation with priority rule
assert agent A selected
 Test skill-based routing end-to-end

create agent with required skill
create conversation with skill metadata
route
assert matched skill agent selected
 Test team-based routing end-to-end

create agents in same team
route with targetTeamId
assert only team members selected
 Test round-robin fairness

3 agents available, same team
route 3 conversations sequentially
assert assignments rotate in order
 Test least-busy routing end-to-end

agents with activeConversations = 3, 1, 5
route conversation
assert lowest busy agent selected
 Test no-match scenario fallback to default

no rule matches
default rule exists
assert default route selected
 Test no agents available scenario

all agents busy or offline
route conversation
assert assigned = false
 Test history saved after route

route conversation
query history by conversationId
assert 1 record with assigned agent and strategy
backend/src/test/java/com/chatbot/core/routing/smoke/EscalationSmokeTest.java
Mục tiêu:

Verify escalation flow thực tế
Checklist:

 Test wait time exceeded trigger

conversation waited over threshold
trigger rule active
assert escalation created / alert triggered
 Test no response trigger

no response after time threshold
assert escalation executed
 Test low customer satisfaction trigger

rating below threshold
assert escalation executed
 Test high priority customer trigger

priority high
assert escalation executed
 Test escalation to agent

action = ASSIGN_TO_AGENT
assert assignment target agent set
 Test escalation to team

action = ASSIGN_TO_TEAM
assert team assigned
 Test escalation to supervisor

action = ASSIGN_TO_SUPERVISOR
assert supervisor notified or session created
 Test no trigger scenario

conditions not met
assert no escalation created
 Test history saved after escalation

assert escalation record persisted
backend/src/test/java/com/chatbot/core/routing/smoke/SupervisorSmokeTest.java
Mục tiêu:

Verify supervisor session lifecycle thực tế
Checklist:

 Test start supervisor session

create conversation
start session
assert session created active=true
 Test supervisor intervention

active session exists
call intervene
assert intervention count increments
 Test end supervisor session

active session
end
assert status = CLOSED and endedAt not null
 Test interrupt session

if service supports interrupt or takeover
assert status changed and ended/flag set
 Test query sessions by supervisor

multiple sessions
query by supervisorId
assert proper subset
 Test query sessions by conversation

query by conversationId
assert matching session(s)
 Test count active sessions

2 active + 1 closed
count active
assert 2
 Test duplicate active session rejected

same supervisor + same conversation + active already exists
assert rejected
backend/src/test/java/com/chatbot/core/routing/controller/RoutingControllerIntegrationTest.java
Mục tiêu:

Verify HTTP layer and request validation
Checklist:

 Test POST /api/routing/rules

valid payload
assert 200 and created object
 Test GET /api/routing/rules

list rules
assert data list
 Test GET /api/routing/rules/{id}

get one rule
assert fields match
 Test PUT /api/routing/rules/{id}

update rule
assert updated values
 Test DELETE /api/routing/rules/{id}

delete
assert 204 no content
 Test POST /api/routing/rules/route/{conversationId}

valid conversation context
assert result assigned or false
 Test GET /api/routing/rules/history/{conversationId}

assert list of history
backend/src/test/java/com/chatbot/core/routing/controller/EscalationControllerIntegrationTest.java
Checklist:

 Test POST /api/escalation/rules
 Test GET /api/escalation/rules
 Test PUT /api/escalation/rules/{id}
 Test DELETE /api/escalation/rules/{id}
 Test trigger check escalation
 Test execute escalation
 Test no trigger scenario
backend/src/test/java/com/chatbot/core/routing/controller/SupervisorControllerIntegrationTest.java
Checklist:

 Test POST /api/supervisor/start-session
 Test POST /api/supervisor/intervene
 Test POST /api/supervisor/end-session
 Test GET /api/supervisor/sessions/supervisor/{supervisorId}
 Test GET /api/supervisor/sessions/conversation/{conversationId}
 Test GET /api/supervisor/active-count
 Test invalid session start rejected
Priority order

Highest priority:

TenantIsolationTest
RoutingRepositoryIntegrationTest
RoutingSmokeTest
EscalationSmokeTest
SupervisorSmokeTest
Second priority:

EscalationRepositoryIntegrationTest
SupervisorRepositoryIntegrationTest
ControllerIntegration tests
Definition of done cho Phase 4

 Unit tests pass
 Integration tests pass
 Smoke tests pass
 Tenant isolation pass
 No cross-tenant data leak
 End-to-end route/escalation/supervisor flow validated