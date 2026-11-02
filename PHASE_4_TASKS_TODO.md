# Phase 4 Intelligent Routing - 5 Critical Tasks to Complete

## 🎯 Objective
Complete Phase 4 by implementing missing integration, smoke, and API tests to make it truly merge-ready for production.

---

## Task 1: Repository Integration Tests (Tenant Isolation)

**Priority:** HIGH  
**Estimated Time:** 2-3 hours

### Files to Create:
1. `backend/src/test/java/com/chatbot/core/routing/integration/RoutingRepositoryIntegrationTest.java`
2. `backend/src/test/java/com/chatbot/core/routing/integration/EscalationRepositoryIntegrationTest.java`
3. `backend/src/test/java/com/chatbot/core/routing/integration/SupervisorRepositoryIntegrationTest.java`
4. `backend/src/test/java/com/chatbot/core/routing/integration/TenantIsolationTest.java`

### Test Requirements:
- Use `@DataJpaTest` for repository layer testing
- Use H2 in-memory database for test isolation
- Test save and query operations for each repository
- Test tenant filtering (tenantId queries)
- Test multi-tenant data isolation (no cross-tenant data leak)
- Test active/inactive status filtering
- Test priority ordering

### Test Cases:
**RoutingRepositoryIntegrationTest:**
- [ ] Save and query routing rules
- [ ] Query by rule type (SKILL_BASED, PRIORITY_BASED, etc.)
- [ ] Query by target type (AGENT, TEAM, SKILL)
- [ ] Query default rules
- [ ] Query active rules only
- [ ] Test priority ordering

**EscalationRepositoryIntegrationTest:**
- [ ] Save and query escalation rules
- [ ] Query by trigger type (WAIT_TIME_EXCEEDED, NO_RESPONSE, etc.)
- [ ] Query by escalation action
- [ ] Test escalate to supervisor flag
- [ ] Test escalate to team flag
- [ ] Test active rules only

**SupervisorRepositoryIntegrationTest:**
- [ ] Save and query supervisor sessions
- [ ] Query by supervisor ID
- [ ] Query by conversation ID
- [ ] Query active sessions
- [ ] Query by session type
- [ ] Count active sessions
- [ ] Test unique active session constraint

**TenantIsolationTest:**
- [ ] Test tenant isolation for routing rules
- [ ] Test tenant isolation for escalation rules
- [ ] Test tenant isolation for agent workload
- [ ] Test tenant isolation for supervisor sessions
- [ ] Verify no cross-tenant data leak

### Success Criteria:
- All tests pass with H2 in-memory database
- Tenant isolation verified for all entities
- Repository queries return correct tenant-specific data

---

## Task 2: Smoke Tests (Service Layer)

**Priority:** HIGH  
**Estimated Time:** 3-4 hours

### Files to Create:
1. `backend/src/test/java/com/chatbot/core/routing/smoke/RoutingSmokeTest.java`
2. `backend/src/test/java/com/chatbot/core/routing/smoke/EscalationSmokeTest.java`
3. `backend/src/test/java/com/chatbot/core/routing/smoke/SupervisorSmokeTest.java`

### Test Requirements:
- Use `@SpringBootTest` for full Spring context
- Mock external dependencies (Redis, Kafka, etc.)
- Test end-to-end service flows
- Test service methods with real database operations
- Test error handling and edge cases

### Test Cases:
**RoutingSmokeTest:**
- [ ] Test priority-based routing end-to-end
- [ ] Test skill-based routing end-to-end
- [ ] Test team-based routing end-to-end
- [ ] Test round-robin routing end-to-end
- [ ] Test least-busy routing end-to-end
- [ ] Test no-match scenario (fallback to default)
- [ ] Test no agents available scenario
- [ ] Test routing history save

**EscalationSmokeTest:**
- [ ] Test wait time exceeded trigger
- [ ] Test no response trigger
- [ ] Test low customer satisfaction trigger
- [ ] Test high priority customer trigger
- [ ] Test escalation to agent
- [ ] Test escalation to team
- [ ] Test escalation to supervisor
- [ ] Test no trigger scenario
- [ ] Test escalation history save

**SupervisorSmokeTest:**
- [ ] Test start supervisor session
- [ ] Test supervisor intervention
- [ ] Test end supervisor session
- [ ] Test interrupt supervisor session
- [ ] Test query sessions by supervisor
- [ ] Test query sessions by conversation
- [ ] Test count active sessions
- [ ] Test unique active session constraint

### Success Criteria:
- All smoke tests pass with mocked external dependencies
- End-to-end service flows work correctly
- Error handling works as expected

---

## Task 3: API Integration Tests (Controller Layer)

**Priority:** MEDIUM  
**Estimated Time:** 3-4 hours

### Files to Create:
1. `backend/src/test/java/com/chatbot/core/routing/controller/RoutingControllerIntegrationTest.java`
2. `backend/src/test/java/com/chatbot/core/routing/controller/EscalationControllerIntegrationTest.java`
3. `backend/src/test/java/com/chatbot/core/routing/controller/SupervisorControllerIntegrationTest.java`

### Test Requirements:
- Use `@WebMvcTest` or `@SpringBootTest` with TestRestTemplate
- Mock service layer dependencies
- Test HTTP endpoints
- Test request/response validation
- Test error responses

### Test Cases:
**RoutingControllerIntegrationTest:**
- [ ] Test POST /api/routing/route-conversation
- [ ] Test GET /api/routing/rules
- [ ] Test POST /api/routing/rules
- [ ] Test PUT /api/routing/rules/{id}
- [ ] Test DELETE /api/routing/rules/{id}
- [ ] Test validation errors
- [ ] Test authentication/authorization

**EscalationControllerIntegrationTest:**
- [ ] Test POST /api/escalation/check-escalation
- [ ] Test POST /api/escalation/execute-escalation
- [ ] Test GET /api/escalation/rules
- [ ] Test POST /api/escalation/rules
- [ ] Test PUT /api/escalation/rules/{id}
- [ ] Test DELETE /api/escalation/rules/{id}
- [ ] Test validation errors
- [ ] Test authentication/authorization

**SupervisorControllerIntegrationTest:**
- [ ] Test POST /api/supervisor/start-session
- [ ] Test POST /api/supervisor/intervene
- [ ] Test POST /api/supervisor/end-session
- [ ] Test GET /api/supervisor/sessions/supervisor/{supervisorId}
- [ ] Test GET /api/supervisor/sessions/conversation/{conversationId}
- [ ] Test GET /api/supervisor/active-count
- [ ] Test validation errors
- [ ] Test authentication/authorization

### Success Criteria:
- All API tests pass with mocked services
- HTTP endpoints return correct responses
- Validation and authentication work correctly

---

## Task 4: Verify Migration SQL Execution

**Priority:** HIGH  
**Estimated Time:** 1-2 hours

### Requirements:
- Run Flyway migrations on test database
- Verify all tables are created correctly
- Verify constraints and indexes
- Verify data types match entities

### Verification Steps:
1. **Verify V6__create_routing_tables.sql (app DB):**
   - [ ] conversation_routing table created
   - [ ] routing_rules table created
   - [ ] escalation_rules table created
   - [ ] supervisor_sessions table created
   - [ ] All constraints (FK, PK, UNIQUE) applied
   - [ ] All indexes created

2. **Verify V24__create_routing_tables.sql (message DB):**
   - [ ] agent_workload table created
   - [ ] team_workload table created
   - [ ] routing_history table created
   - [ ] All constraints applied
   - [ ] All indexes created

3. **Verify V28__create_escalation_history_table.sql (message DB):**
   - [ ] escalation_history table created
   - [ ] All constraints applied
   - [ ] All indexes created

4. **Verify Entity-Table Mapping:**
   - [ ] RoutingRule entity matches routing_rules table
   - [ ] EscalationRule entity matches escalation_rules table
   - [ ] SupervisorSession entity matches supervisor_sessions table
   - [ ] AgentWorkload entity matches agent_workload table
   - [ ] All column names and types match

### Success Criteria:
- All migrations run without errors
- All tables created with correct schema
- Entity mappings verified

---

## Task 5: Full Test Suite Run

**Priority:** HIGH  
**Estimated Time:** 1 hour

### Requirements:
- Run all tests (unit + integration + smoke + API)
- Verify 100% pass rate
- Generate test coverage report

### Test Execution:
1. **Unit Tests:**
   - [ ] Run ConditionEvaluationTest (15 tests)
   - [ ] Run IntelligentRoutingServiceTest (16 tests)
   - [ ] Run IntelligentEscalationServiceTest (9 tests)
   - [ ] Run SupervisorSessionServiceTest (11 tests)

2. **Integration Tests:**
   - [ ] Run RoutingRepositoryIntegrationTest
   - [ ] Run EscalationRepositoryIntegrationTest
   - [ ] Run SupervisorRepositoryIntegrationTest
   - [ ] Run TenantIsolationTest

3. **Smoke Tests:**
   - [ ] Run RoutingSmokeTest
   - [ ] Run EscalationSmokeTest
   - [ ] Run SupervisorSmokeTest

4. **API Tests:**
   - [ ] Run RoutingControllerIntegrationTest
   - [ ] Run EscalationControllerIntegrationTest
   - [ ] Run SupervisorControllerIntegrationTest

### Success Criteria:
- All tests pass (100% pass rate)
- Test coverage > 80% for routing module
- No test failures or errors

---

## 📊 Summary

| Task | Priority | Est. Time | Dependencies |
|------|----------|-----------|--------------|
| Task 1: Repository Integration Tests | HIGH | 2-3 hours | None |
| Task 2: Smoke Tests | HIGH | 3-4 hours | Task 1 |
| Task 3: API Integration Tests | MEDIUM | 3-4 hours | Task 1, Task 2 |
| Task 4: Verify Migration SQL Execution | HIGH | 1-2 hours | None |
| Task 5: Full Test Suite Run | HIGH | 1 hour | Task 1, 2, 3, 4 |

**Total Estimated Time:** 10-14 hours

**Recommended Order:**
1. Task 4 (Verify migrations - can be done in parallel)
2. Task 1 (Repository integration tests - foundation)
3. Task 2 (Smoke tests - depends on Task 1)
4. Task 3 (API tests - depends on Task 1, 2)
5. Task 5 (Full test run - final verification)

---

## 🎯 Completion Criteria

Phase 4 is truly complete when:
- ✅ All 5 tasks above are completed
- ✅ All tests pass (unit + integration + smoke + API)
- ✅ Migration SQL verified and executed successfully
- ✅ Test coverage > 80%
- ✅ Code is ready for production deployment
