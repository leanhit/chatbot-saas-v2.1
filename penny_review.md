# 🔍 Báo cáo Audit: Penny Bot & Rule Features

> **Phạm vi**: Backend (`BotRule`, `BotRuleController`, `BotRuleManager`, `PennyBotCrudService`) · Frontend (`BotManagement.vue`, `Rules.vue`, `PennyRuleModal.vue`)
> **Mục tiêu**: Kiểm tra độ "professional" theo tiêu chuẩn SaaS hiện đại

---

## ✅ Điểm Mạnh (Đã Đạt Chuẩn)

### Backend

| Hạng mục | Chi tiết |
|---|---|
| **Kiến trúc tầng** | Controller → Manager → Repository tách biệt rõ ràng |
| **Multi-tenancy** | `TenantContext` được kiểm tra ở tất cả write operations |
| **Permission guard** | `OWNER/EDITOR` được validate trước mọi thao tác tạo/sửa/xóa |
| **Null-safe replaceVariables** | Dùng `Objects.toString(x, "")` — đã fix bug từ lần trước |
| **Webhook action** | Gọi HTTP POST thực tế, hỗ trợ Bearer token, extract response |
| **Script action** | SpEL Evaluator với context variables — khá mạnh |
| **Rule Engine** | Priority-based evaluation, execution count tracking |
| **CSV Import/Export** | Đầy đủ với error reporting theo từng dòng |
| **Bot deletion guard** | Kiểm tra active connections & recent conversations trước khi xóa |
| **Input sanitization** | `InputSanitizer` kiểm tra độ dài và malicious content |
| **Logging** | Structured emoji logging nhất quán |

### Frontend

| Hạng mục | Chi tiết |
|---|---|
| **State management** | Pinia stores (`pennyRuleStore`, `pennyBotStore`) tách biệt |
| **Skeleton loading** | Hiệu ứng pulse loading khi fetch data |
| **Empty states** | Màn hình "No bots yet" và "No rules yet" đầy đủ |
| **Onboarding Tour** | `driver.js` hướng dẫn người dùng mới |
| **Rule Templates** | Component `RuleTemplates` để áp dụng mẫu nhanh |
| **Regex Tester** | Component riêng test regex trực tiếp |
| **Condition Builder** | Visual builder cho condition rules |
| **JSON Editor** | Editor riêng với validation cho Webhook action |
| **i18n** | Đa ngôn ngữ đầy đủ qua `vue-i18n` |
| **Dark mode** | Hỗ trợ Tailwind dark: class |

---

## 🔴 Vấn Đề Nghiêm Trọng (Critical Issues)

### 1. 🔒 Backend: BotRuleController — Thiếu Tenant Isolation cho GET endpoints

**File**: `BotRuleController.java` — Line 159-186

```java
// ❌ HIỆN TẠI: Bất kỳ authenticated user nào cũng đọc được rule của bot khác tenant
@GetMapping
public ResponseEntity<List<Map<String, Object>>> getRules(@PathVariable String botId) {
    UUID botUuid = UUID.fromString(botId);
    List<BotRule> rules = botRuleManager.getAllRulesForBot(botUuid);
    // ...
}
```

**Vấn đề**: Không validate `TenantContext` → user của tenant A có thể đọc rule của tenant B nếu biết `botId`.

```java
// ✅ FIX: Phải thêm tenant ownership check
@GetMapping
public ResponseEntity<List<Map<String, Object>>> getRules(@PathVariable String botId) {
    Long tenantId = getValidatedTenantId();            // thêm
    UUID botUuid = UUID.fromString(botId);
    validateBotBelongsToTenant(botUuid, tenantId);     // thêm
    List<BotRule> rules = botRuleManager.getAllRulesForBot(botUuid);
    // ...
}
```

Tương tự cho: `getRulesByType()`, `testRule()`, `getRuleStatistics()`, `exportRulesToCSV()`

---

### 2. 🔒 Backend: SpEL SCRIPT action — Security Risk

**File**: `BotRuleManager.java` — Line 329-363

```java
// ❌ NGUY HIỂM: SpEL có thể bị khai thác để chạy code tùy ý
Object result = spelParser.parseExpression(script).getValue(evalContext);
```

**Vấn đề**: `StandardEvaluationContext` cho phép truy cập class loaders, file system, network. Đây là **Remote Code Execution risk** cực kỳ nguy hiểm trong SaaS multi-tenant.

```java
// ✅ FIX: Dùng SimpleEvaluationContext (read-only, giới hạn)
EvaluationContext evalContext = SimpleEvaluationContext
    .forReadOnlyDataBinding()
    .withInstanceMethods()
    .build();
evalContext.setVariable("userId", context.getUserId());
// ...
```

---

### 3. 🔒 Backend: BotRule.evaluateCondition() — Stub Function Sai Lệch

**File**: `BotRule.java` — Line 134-138

```java
// ❌ Logic sai: Luôn trả về true nếu condition != null — KHÔNG đúng ý nghĩa semantic
private boolean evaluateCondition(Map<String, Object> context) {
    // This would be implemented with a proper expression evaluator
    // For now, return true if condition exists
    return condition != null && !condition.trim().isEmpty();
}
```

**Vấn đề**: Mọi rule có `TriggerType.CONDITION` đều match ngay cả khi điều kiện sai. Đây là bug nghiêm trọng về logic nghiệp vụ.

```java
// ✅ FIX: Throw hoặc log warning rõ ràng
private boolean evaluateCondition(Map<String, Object> context) {
    log.warn("⚠️ CONDITION trigger type is not fully implemented for ruleId={}. Defaulting to false.", id);
    return false; // safe default — chờ implementation thực sự
}
```

---

### 4. 💸 Backend: BotRuleManager.deleteRule() — Hard Delete không có Soft Delete

**File**: `BotRuleManager.java` — Line 126-137

```java
// ❌ Hard delete mất hoàn toàn lịch sử
botRuleRepository.deleteById(ruleId);
```

**Vấn đề**: SaaS chuyên nghiệp cần soft delete để:
- Audit trail / changelog tracking
- Recovery khi xóa nhầm
- Phân tích execution history

```java
// ✅ FIX: Soft delete bằng cách set isActive = false và ghi timestamp
rule.setIsActive(false);
rule.setDeletedAt(LocalDateTime.now());
rule.setDeletedBy(deletedBy);
botRuleRepository.save(rule);
```

---

### 5. 🧪 Frontend: PennyRuleModal — Test New Rule tạo + xóa tạm (Anti-pattern)

**File**: `PennyRuleModal.vue` — Line 594-608

```javascript
// ❌ Bad pattern: Test một rule bằng cách CREATE → TEST → DELETE trong production DB
const createdRule = await pennyRuleApi.createRule(props.bot.id, tempRuleData)
response = await pennyRuleApi.testRule(props.bot.id, createdRule.data.ruleId, testData)
await pennyRuleApi.deleteRule(props.bot.id, createdRule.data.ruleId)
```

**Vấn đề**: 
- Tạo race condition nếu delete fail
- Gây noise trong audit log
- Không atomic (nếu test crash, rule tồn tại vĩnh viễn trong DB)

```java
// ✅ FIX: Tạo endpoint riêng để test rule "in memory"
// Backend: POST /api/penny/bots/{botId}/rules/test-preview
// Không persist vào DB, chỉ evaluate logic và trả về kết quả
```

---

## 🟡 Vấn Đề Trung Bình (Medium Issues)

### 6. 🔧 Backend: Controller dùng `Map<String, Object>` thay DTO

**File**: `BotRuleController.java` — Line 50-103

```java
// ❌ Không có validation, không có type-safety, không có OpenAPI docs
@PostMapping
public ResponseEntity<Map<String, Object>> createRule(
        @PathVariable String botId,
        @RequestBody Map<String, Object> request,  // ← quá lỏng lẻo
        Principal principal) {
```

**Thiếu**: `@Valid`, `@NotNull`, Bean Validation, OpenAPI `@Schema`.

```java
// ✅ Chuẩn chuyên nghiệp
public record CreateBotRuleRequest(
    @NotBlank String ruleName,
    @NotBlank String action,
    @NotNull RuleType ruleType,
    @NotNull TriggerType triggerType,
    String triggerValue,
    String condition,
    @Min(0) @Max(100) Integer priority
) {}

@PostMapping
public ResponseEntity<BotRuleResponse> createRule(
        @PathVariable String botId,
        @Valid @RequestBody CreateBotRuleRequest request,
        Principal principal) {
```

---

### 7. 🚀 Backend: `evaluateRules()` không có caching

**File**: `BotRuleManager.java` — Line 163

```java
// ❌ Mỗi message đến đều query DB để lấy rules
List<BotRule> rules = getRulesForBot(botId);
```

Với traffic cao (1000 msg/giây), đây là bottleneck nghiêm trọng.

```java
// ✅ Cache rules với TTL ngắn
@Cacheable(value = "botRules", key = "#botId", unless = "#result.isEmpty()")
public List<BotRule> getRulesForBot(UUID botId) {
    return botRuleRepository.findByBotIdAndIsActiveTrueOrderByPriorityDesc(botId);
}

@CacheEvict(value = "botRules", key = "#rule.botId")
public BotRule createRule(...) { ... }
```

---

### 8. ⚡ Backend: Webhook action dùng `.block()` — Blocking I/O

**File**: `BotRuleManager.java` — Line 292-303

```java
// ❌ block() trong reactive context gây thread starvation
Map<String, Object> webhookResponse = webClient.post()
    .uri(webhookUrl)
    // ...
    .block();  // ← BLOCKING!
```

```java
// ✅ Dùng async với timeout
webClient.post()
    .uri(webhookUrl)
    // ...
    .retrieve()
    .bodyToMono(Map.class)
    .timeout(Duration.ofSeconds(5))
    .onErrorReturn(Map.of("error", "timeout"))
    .subscribe(resp -> handleWebhookResponse(resp, rule));
```

---

### 9. 🖥️ Frontend: `confirm()` thay vì Modal xác nhận

**File**: `Rules.vue` — Line 518 / `BotManagement.vue` — Line 461

```javascript
// ❌ Browser native confirm() — trông rất amateur, không có UX tốt
if (confirm(`Are you sure you want to delete "${rule.name}"?`)) {
```

```vue
<!-- ✅ Chuẩn chuyên nghiệp: Dùng confirmation modal riêng -->
<ConfirmModal
  v-if="showDeleteConfirm"
  :title="`Delete rule '${ruleToDelete?.name}'?`"
  description="This action cannot be undone."
  confirmText="Delete"
  danger
  @confirm="executeDelete"
  @cancel="showDeleteConfirm = false"
/>
```

---

### 10. 🖥️ Frontend: Rules.vue — `aiRules` computed property sai

**File**: `Rules.vue` — Line 416-418

```javascript
// ❌ Backend không có RuleType.AI nhưng frontend filter theo 'AI'
const aiRules = computed(() => 
  rules.value.filter(rule => rule.type === 'AI')  // sẽ luôn = 0
)
```

Backend `RuleType` enum chỉ có: `RESPONSE, REDIRECT, WEBHOOK, SCRIPT`. Không có `AI`.

---

### 11. 🖥️ Frontend: BotManagement.vue — Empty onBotSelectionChange watchers

**File**: `PennyRuleModal.vue` — Line 416-417, 659-664

```javascript
// ❌ Watchers và handlers trống — dead code làm rối logic
const onBotSelectionChange = (event) => {
  }  // ← thân hàm trống

watch(() => formData.value.botId, (newBotId, oldBotId) => {
  }, { immediate: true })  // ← thân hàm trống
```

---

## 🟢 Gợi Ý Cải Thiện UX (Enhancement)

### 12. Frontend: Thiếu Pagination cho Rules Grid

**File**: `Rules.vue` — Line 239-341

Hiện tại load toàn bộ rules một lần (`v-for` không phân trang). Với 100+ rules sẽ gây chậm UI.

```vue
<!-- Thêm pagination -->
<div class="pagination-bar">
  <button @click="currentPage--" :disabled="currentPage === 1">←</button>
  <span>{{ currentPage }} / {{ totalPages }}</span>
  <button @click="currentPage++" :disabled="currentPage === totalPages">→</button>
</div>
```

---

### 13. Backend: Thiếu Bulk Operations API

Các app chuyên nghiệp (Botpress, Dialogflow) có:
- `POST /rules/bulk-enable` — batch activate
- `POST /rules/bulk-disable` — batch deactivate  
- `DELETE /rules/bulk` — batch delete
- `POST /rules/reorder` — drag-and-drop priority reordering

---

### 14. Frontend: Thiếu Rule Execution Analytics trên Rule Card

Hiện tại card rule chỉ hiện `executionCount` dạng text. Nên thêm mini chart hoặc badge "Triggered 1.2k times" để trực quan hơn.

---

## 📊 Tổng Kết Đánh Giá

| Tiêu chí | Điểm | Nhận xét |
|---|---|---|
| **Security** | 6/10 | Thiếu tenant isolation GET, SpEL RCE risk |
| **Architecture** | 8/10 | Tầng tốt, nhưng cần DTO thay Map |
| **Code Quality** | 7/10 | Còn stub function và dead code |
| **UX/Frontend** | 7.5/10 | Tour guide, templates tốt; cần confirm modal & pagination |
| **Performance** | 6/10 | Chưa cache rules, blocking webhook |
| **Professional Standards** | 7/10 | Cần soft delete, bulk ops, request validation |

> **Tổng thể: 6.9/10** — Nền tảng tốt, cần fix 2 lỗ hổng bảo mật nghiêm trọng (#1, #2) trước khi production.

---

## 🛠️ Thứ Tự Ưu Tiên Fix

| # | Vấn đề | Mức độ | Effort |
|---|---|---|---|
| 1 | Tenant isolation cho GET endpoints | 🔴 Critical | 1h |
| 2 | SpEL `StandardEvaluationContext` → `SimpleEvaluationContext` | 🔴 Critical | 30m |
| 3 | CONDITION trigger luôn trả true | 🔴 Critical | 15m |
| 4 | Soft delete thay hard delete | 🟡 Medium | 2h |
| 5 | DTO thay Map trong Controller | 🟡 Medium | 3h |
| 6 | Cache rules trong `evaluateRules()` | 🟡 Medium | 1h |
| 7 | Confirm modal thay `confirm()` | 🟡 Medium | 1h |
| 8 | Fix `aiRules` computed property | 🟢 Low | 15m |
| 9 | Async webhook (remove `.block()`) | 🟡 Medium | 1h |
| 10 | Pagination cho rules grid | 🟢 Low | 2h |
