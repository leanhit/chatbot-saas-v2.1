# Facebook Adapter Integration Phase 0

## 1. Mục tiêu Phase 0

**Chỉ nối dây, không sửa não** - Kết nối Facebook Adapter vào Message Hub MVP đã tồn tại mà không thay đổi logic của Message Hub.

### Vấn đề giải quyết
- Facebook Adapter hiện đang BYPASS Message Hub
- 100% Facebook messages phải đi qua Message Hub
- Botpress chỉ nhận message khi Message Hub quyết định

## 2. Sơ đồ luồng Phase 0

```
Facebook Page → Facebook Webhook → FacebookWebhookController
                                            ↓
                                    FacebookWebhookService
                                            ↓
                                    MessageRequest (parse)
                                            ↓
                            POST /api/message-hub/message
                                            ↓
                                    MessageGateway
                                            ↓
                            DecisionService (existing rules)
                                            ↓
                            ContextStore (existing context)
                                            ↓
                                    MessageResponse
                                            ↓
        ┌─────────────────┴─────────────────┐
        ↓                                   ↓
BOT_PROCESS → forwardToBotpress()    HUMAN_REQUIRED → log only
        ↓                                   ↓
Botpress API                      Human Inbox (TODO)
```

## 3. Trách nhiệm từng thành phần

### FacebookWebhookController
- **Nhận**: Facebook webhook verification & events
- **Không xử lý**: Business logic, routing decisions
- **Chỉ forward**: Gửi webhook payload cho service

### FacebookWebhookService
- **Parse**: Facebook payload → MessageRequest
- **Gọi**: MessageGateway.processMessage()
- **Route**: Theo MessageResponse decision
- **Không quyết định**: Không tự ra quyết định routing

### Message Hub (existing)
- **MessageGateway**: Entry point trung tâm
- **DecisionService**: Apply existing rules (ask_price → human)
- **ContextStore**: Persist conversation state
- **Không thay đổi**: Giữ nguyên logic Phase 0

### Botpress Integration
- **Chỉ khi**: MessageResponse.decision = "BOT_PROCESS"
- **Không khi**: MessageResponse.decision = "HUMAN_REQUIRED"
- **STUB**: Placeholder cho Phase 0

## 4. Checklist "Chỉ nối – không sửa"

- [x] Facebook webhook endpoint nhận messages
- [x] Parse Facebook payload → MessageRequest
- [x] Gọi Message Hub API (không bypass)
- [x] Route theo Message Hub decision
- [x] Botpress chỉ nhận khi BOT_PROCESS
- [x] Log khi HUMAN_REQUIRED
- [ ] Không thêm rule mới
- [ ] Không sửa DecisionService
- [ ] Không thêm context field
- [ ] Không tối ưu flow

## 5. Những việc TUYỆT ĐỐI KHÔNG LÀM ở Phase 0

### ❌ KHÔNG thay đổi Message Hub
- Không sửa DecisionService rules
- Không thêm context fields
- Không thay đổi MessageResponse format
- Không optimize performance

### ❌ KHÔNG mở rộng scope
- Không implement AI/NLP
- Không thêm event-driven architecture
- Không implement human inbox UI
- Không add monitoring/analytics

### ❌ KHÔNG refactor
- Không thay đổi existing code structure
- Không optimize database queries
- Không add caching layer
- Không implement error handling phức tạp

## 6. Tiêu chí xác nhận hoàn thành

### ✅ Functional criteria
- [ ] Facebook webhook nhận messages thành công
- [ ] 100% messages đi qua Message Hub
- [ ] Botpress chỉ nhận khi decision = BOT_PROCESS
- [ ] Messages bị block khi decision = HUMAN_REQUIRED
- [ ] Context được lưu/truy xuất đúng

### ✅ Integration criteria
- [ ] Tắt Message Hub → Facebook adapter không hoạt động
- [ ] Thay rule trong DecisionService → hành vi thay đổi
- [ ] Facebook → Message Hub → Botpress flow hoạt động

### ✅ Architecture criteria
- [ ] Không có direct Facebook → Botpress
- [ ] Message Hub là single decision point
- [ ] Adapters không tự ra quyết định

### ✅ Code criteria
- [ ] Không sửa existing Message Hub code
- [ ] Chỉ thêm integration layer
- [ ] STUB cho Botpress API call
- [ ] Log đầy đủ cho debugging

---

**Phase 0 Success**: Facebook Adapter đã được kết nối vào Message Hub, tuân thủ kiến trúc Hub & Spoke, sẵn sàng cho Phase 1.
