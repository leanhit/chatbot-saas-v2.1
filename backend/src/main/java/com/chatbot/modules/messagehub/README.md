# Message Hub MVP - Business Core Giai Đoạn 0

## 1. Mục tiêu của Message Hub MVP

### Tại sao module này tồn tại
Message Hub MVP được tạo để giải quyết **vấn đề cốt lõi** trong kiến trúc hiện tại:
- **Thiếu điểm trung tâm điều phối message**: Adapter (Facebook, chatbot-app) đang xử lý trực tiếp, không có hub
- **Không có quản lý context hội thoại**: Mỗi message là độc lập, không có trạng thái hội thoại
- **Không có decision engine**: Không có logic để quyết định bot hay human xử lý

### Vấn đề kiến trúc giải quyết
- **Tạo ra business-core foundation**: Bước đầu tiên từ foundation-core sang business-core
- **Thiết lập Hub & Spoke pattern cho message**: Message Hub là trung tâm, adapters là spokes
- **Cung cấp decision point duy nhất**: Mọi quyết định routing đi qua Message Hub

## 2. Phạm vi (Scope)

### Message Hub MVP LÀM
- **Nhận message từ tất cả adapters** qua MessageGateway (điểm vào duy nhất)
- **Lưu trữ context hội thoại tối thiểu** (conversation_id, user_id, tenant_id, last_intent, asked_price_count, handler_type)
- **Ra quyết định routing** với rule engine đơn giản (if/else)
- **Cung cấp REST API** cho adapters và monitoring

### Message Hub MVP KHÔNG LÀM
- **Event-driven architecture**: Không publish/subscribe events
- **Workflow engine**: Không có flow phức tạp
- **AI/ML integration**: Không có NLP, intent detection phức tạp
- **UI/Dashboard**: Không có giao diện quản trị
- **Multi-channel routing**: Chỉ focus decision logic, không implement channel-specific logic

## 3. Vị trí trong kiến trúc tổng thể

### Quan hệ với Foundation-Core
```
Foundation Core (Identity, Tenant, Billing, App, Config, Wallet)
                    ↓
            Business Core - Message Hub MVP
                    ↓
        Application Layer (Chatbot App, Facebook Adapter)
```

- **Foundation Core**: Cung cấp authentication, multi-tenancy, billing guard
- **Message Hub**: Sử dụng foundation services để ra quyết định business
- **Application Layer**: Gửi message đến Message Hub, nhận decision và thực thi

### Quan hệ với Chatbot App / Facebook Adapter
- **Adapter → Message Hub**: Gửi MessageRequest
- **Message Hub → Adapter**: Trả về MessageResponse với decision
- **Adapter KHÔNG được xử lý trực tiếp**: Phải qua MessageGateway

## 4. Context MVP

### Danh sách field
| Field | Type | Business Meaning |
|-------|------|------------------|
| conversation_id | String | Unique identifier for conversation thread |
| user_id | UUID | User who is participating in conversation |
| tenant_id | UUID | Tenant/workspace context |
| last_intent | String | Last detected intent (e.g., "ask_price", "greeting") |
| asked_price_count | Integer | How many times customer asked about price |
| handler_type | Enum | Current handler: BOT or HUMAN |

### Ý nghĩa business
- **conversation_id**: Track conversation across multiple messages
- **asked_price_count**: Business rule trigger - sau 2 lần hỏi giá → chuyển human
- **handler_type**: Trạng thái hiện tại của conversation - ai đang xử lý
- **last_intent**: Input cho decision engine - quyết định dựa trên intent

## 5. Rule Engine Giai Đoạn 0

### Các rules đang có
1. **Human Handler Rule**: Nếu `handler_type = HUMAN` → không cho bot xử lý
2. **Price Inquiry Counting**: Nếu `intent = ask_price` → tăng `asked_price_count`
3. **Price Threshold Rule**: Nếu `asked_price_count >= 2` → chuyển `handler_type = HUMAN`
4. **Default Bot Rule**: Nếu không match rule nào → bot xử lý

### Vì sao chỉ cần từng đó
- **MVP focus**: Chỉ giải quyết vấn đề cốt lõi - quyết định bot vs human
- **Business value**: Rule "hỏi giá 2 lần → human" là use case thực tế
- **Simplicity**: If/else đủ cho giai đoạn 0, không cần complex rule engine
- **Extensibility**: Design cho phép thêm rule sau này

## 6. Tiêu chí Hoàn thành Giai đoạn 0

### Dấu hiệu nhận biết Business-Core cơ bản đã hình thành
- ✅ **MessageGateway nhận message từ adapters**: Không có adapter xử lý trực tiếp
- ✅ **ContextStore lưu và truy xuất context**: Có trạng thái hội thoại
- ✅ **DecisionService ra quyết định**: Có business logic routing
- ✅ **REST API hoạt động**: Adapter có thể gọi Message Hub
- ✅ **Database schema sẵn sàng**: Conversation context được persist

### Điều kiện để bước sang giai đoạn tiếp theo
- **Stable decision logic**: Rules hoạt động đúng trong production
- **Performance acceptable**: Message processing latency < 100ms
- **Integration proven**: Ít nhất 1 adapter (Facebook hoặc chatbot-app) tích hợp thành công
- **Monitoring ready**: Có logs và metrics cơ bản

### Success metrics
- **100% message flow through Message Hub**: Không có adapter bypass
- **Context persistence accuracy**: Context được lưu và đọc đúng
- **Decision consistency**: Cùng input → cùng decision
- **API reliability**: 99%+ uptime, < 1% error rate

---

**Tóm tắt**: Message Hub MVP tạo ra business-core foundation đầu tiên, giải quyết vấn đề central message routing và context management với rule engine đơn giản. Đây là bước nền tảng để xây dựng business logic phức tạp hơn trong tương lai.
