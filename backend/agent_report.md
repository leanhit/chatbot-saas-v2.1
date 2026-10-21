# Fix Bug + Viết Test Đầy Đủ Cho Gói Penny

## Mô tả

Thực hiện song song 2 mục tiêu:
1. **Fix 2 bug đang xảy ra trong production** (đã xác định từ log)
2. **Viết test cho các class quan trọng chưa được covered**

---

## Bug Cần Fix

### Bug 1 — `NullPointerException` trong `BotRuleManager.replaceVariables`

**File:** [BotRuleManager.java](file:///root/chatbot-saas-v2.1/backend/src/main/java/com/chatbot/core/penny/rules/BotRuleManager.java#L367)  
**Root cause:** `String.replace()` ném NPE khi `context.getUserId()`, `context.getBotId()`, `context.getPlatform()` là `null`.

```java
// HIỆN TẠI (lỗi)
result = result.replace("{{user_name}}", context.getUserId());   // NPE nếu null

// SAU FIX
result = result.replace("{{user_name}}", context.getUserId() != null ? context.getUserId() : "");
```

### Bug 2 — Rule action lưu plain text thay vì JSON

Rule `46d85399-88d7-4ac0-9723-d36a95c2c362` có `action = "xin chào"` (plain text).  
Fallback hiện tại tại `BotRuleManager.java:236` gọi `replaceVariables(rule.getAction(), context)` → NPE.

**Fix DB:** Cập nhật record trong `postgres_app` DB:
```sql
UPDATE penny_bot_rules 
SET action = '{"text": "xin chào"}' 
WHERE id = '46d85399-88d7-4ac0-9723-d36a95c2c362';
```

---

## Proposed Changes

### Component 1 — Bug Fix

---

#### [MODIFY] [BotRuleManager.java](file:///root/chatbot-saas-v2.1/backend/src/main/java/com/chatbot/core/penny/rules/BotRuleManager.java)

Null-safe toàn bộ `replaceVariables()` method (lines 367–384): thay `context.getX()` bằng `Objects.toString(context.getX(), "")`.

---

### Component 2 — New Test Files

---

#### [MODIFY] [BotRuleManagerTest.java](file:///root/chatbot-saas-v2.1/backend/src/test/java/com/chatbot/core/penny/rules/BotRuleManagerTest.java)

Thêm `@Nested class NullSafeReplaceVariablesTests` với các test:
- `shouldHandleNullUserId` — context.userId = null, không ném NPE
- `shouldHandleNullBotId` — context.botId = null
- `shouldHandleNullPlatform` — context.platform = null
- `shouldHandleAllNullContext` — tất cả fields null
- `shouldFallbackPlainTextActionWithNullContext` — action = "xin chào" (plain text), không bị NPE (reproduce bug thực tế)

---

#### [NEW] `CustomLogicEngineTest.java`

**Path:** `backend/src/test/java/com/chatbot/core/penny/rules/CustomLogicEngineTest.java`

Test cases:
- `processWithCustomLogic_ruleMatched_returnsCustomRuleResponse` — rule match → trả về CUSTOM_RULE response
- `processWithCustomLogic_noRuleMatch_templateMatched_returnsTemplateResponse` — rule không match → dùng template
- `processWithCustomLogic_noRuleNoTemplate_returnsNull` — không có gì match → trả null (để fallback sang provider)
- `processWithCustomLogic_botIdNull_returnsNull` — botId null → bắt exception, trả null
- `processWithCustomLogic_ruleMatchException_returnsNull` — rule manager ném exception → trả null (graceful)
- `hasCustomLogic_withRulesAndTemplates_returnsTrue`
- `hasCustomLogic_emptyBot_returnsFalse`
- `getCustomLogicStatistics_returnsCorrectCounts`
- `toggleCustomLogic_enablesAllRulesAndTemplates`

---

#### [NEW] `ErrorHandlerTest.java`

**Path:** `backend/src/test/java/com/chatbot/core/penny/error/ErrorHandlerTest.java`

Test cases:
- `handleError_withIllegalArgumentException_returnsValidationError`
- `handleError_withTimeoutException_returnsTimeoutError`
- `handleError_withConnectionException_returnsConnectionError`
- `handleError_withFallbackEnabled_returnsFallbackForPriceQuery`
- `handleError_withFallbackEnabled_returnsFallbackForOrderQuery`
- `handleError_withFallbackEnabled_returnsFallbackForGreeting`
- `handleProviderError_returnsProviderFallbackResponse`
- `circuitBreaker_opensAfterFailureThreshold`
- `circuitBreaker_resetsAfterSuccess`
- `circuitBreaker_halfOpenAfterRecoveryDuration`
- `executeWithRetry_succeedsOnFirstAttempt`
- `executeWithRetry_succeedsAfterRetries`
- `executeWithRetry_exhaustsRetriesAndThrows`
- `isCircuitBreakerOpen_whenDisabledAlwaysFalse`

---

#### [NEW] `ResponseTemplateManagerTest.java`

**Path:** `backend/src/test/java/com/chatbot/core/penny/rules/ResponseTemplateManagerTest.java`

Test cases:
- `createTemplate_savesWithCorrectFields`
- `createTemplate_duplicateName_throwsIllegalArgument`
- `updateTemplate_updatesOnlyProvidedFields`
- `getBestTemplate_returnsHighestPriorityForIntentAndLanguage`
- `getBestTemplate_fallsBackToVietnamese_whenLanguageNotFound`
- `getBestTemplate_returnsNull_whenNoTemplatesExist`
- `processTemplate_replacesVariablesInTemplateText`
- `processTemplate_incrementsUsageCount`
- `processTemplate_inactiveTemplate_throwsException`
- `getTemplateStatistics_returnsCorrectCounts`

---

#### [NEW] `ConversationContextTest.java`

**Path:** `backend/src/test/java/com/chatbot/core/penny/context/ConversationContextTest.java`

Test cases:
- `incrementMessageCount_startsFromNullAndIncrementsCorrectly`
- `addIntentToHistory_keepsLast50`
- `addProviderToHistory_keepsLast20`
- `isExpired_returnsTrueWhenExpiresAtInPast`
- `isExpired_returnsFalseWhenExpiresAtNull`
- `isActive_requiresStatusActiveAndNotExpired`
- `isIdle_returnsTrueAfter30Minutes`
- `addMessageToHistory_storesUserAndBotMessages`
- `getAgeInMinutes_calculatesCorrectly`
- `needsEscalation_returnsTrueWhenFlagOrLevel`

---

## Verification Plan

### Automated Tests
```bash
# Chạy toàn bộ test gói penny
cd /root/chatbot-saas-v2.1/backend
./mvnw test -pl . -Dtest="com.chatbot.core.penny.**" -q

# Chạy từng test class mới
./mvnw test -Dtest="BotRuleManagerTest,CustomLogicEngineTest,ErrorHandlerTest,ResponseTemplateManagerTest,ConversationContextTest"
```

### Manual Verification
- Rebuild Docker image → xác nhận log không còn NPE từ `BotRuleManager.replaceVariables`
- Test gửi tin nhắn qua Facebook Messenger → confirm rule matching hoạt động đúng

======================

Kết quả Review — Agent Module
Nhìn chung: 7/10 — Khung tốt, một số điểm cần fix trước khi production.

✅ Làm tốt
Event-driven provisioning (@TransactionalEventListener(AFTER_COMMIT)) — đúng chuẩn, idempotent, có endpoint repair
Tenant isolation — unique constraint (email, tenant_id) và (user_id, tenant_id) đúng
Security phân quyền — READ/WRITE/STATUS rõ ràng, validate ownership trong service layer
Skills-based routing — score composite (skill match + load penalty), tốt hơn round-robin
🔴 Bug nghiêm trọng cần fix ngay
AgentReassignmentService — Scheduler không bao giờ chạy:

java
@Scheduled(fixedRate = 300000)
public void checkAndReassignOfflineAgents() {
    Long tenantId = TenantContext.getTenantId(); // ← LUÔN NULL trong scheduled thread!
    if (tenantId == null) {
        return; // ← Bỏ qua mọi lần chạy!
    }
TenantContext là ThreadLocal — scheduled thread không có HTTP context. Cần query tất cả tenant IDs từ DB và loop qua từng tenant.

⚠️ Cần cải thiện
#	Vấn đề	Ưu tiên
1	Controller trả Agent entity trực tiếp, thiếu DTO/Response layer	🔴 Cao
2	Thiếu @Valid + request DTO cho updateAgent (caller có thể inject tenantId bất kỳ)	🟡
3	findByUserId(userId) không có tenant scope → có thể trả agent sai tenant	🟡
4	getAgents() trả toàn bộ list — thiếu pagination	🟡
5	new ObjectMapper() trong AgentAssignmentService thay vì inject bean	🟠
6	Email broadcast chỉ gửi về fromEmail, không gửi đến admin của tenant	🟠
Bạn muốn tôi fix luôn không? Có thể bắt đầu với 2 vấn đề quan trọng nhất: fix scheduler + thêm DTO layer.

Agent Review
4:57 PM
