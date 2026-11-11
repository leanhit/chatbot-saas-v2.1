# RBAC Matrix — Chatbot SaaS

Cập nhật: 2026-11-11. Nguồn: `@PreAuthorize` annotations + `TenantPermissionValidator` + `TenantRole` enum.

## 1. Hai lớp phân quyền

| Lớp | Cơ chế | Giá trị |
|---|---|---|
| **Spring Security role** | `@PreAuthorize("hasRole/hasAnyRole")` trên controller | `ADMIN`, `MANAGER`, `SUPERVISOR`, `AGENT`, `USER` |
| **Tenant role** | `TenantPermissionValidator.isOwnerOrEditor()` / `isAdminOrOwner()` | `OWNER`, `EDITOR`, `MEMBER`, `NONE` |

Quy tắc: cả hai lớp đều phải pass — Spring role kiểm soát chức năng, tenant role kiểm soát phạm vi tenant.

## 2. Tenant role (`com.chatbot.core.membership.model.TenantRole`)

| Role | Mô tả | Quyền |
|---|---|---|
| `OWNER` | Chủ tenant | Full: members, billing, settings, GDPR delete, mọi mutation |
| `EDITOR` | Editor | CRUD nội dung/bots/customers/training; không xóa tenant, không quản lý billing |
| `MEMBER` | Member cơ bản | Read-only + thao tác agent (chat, training của chính mình) |
| `NONE` | Không quyền | Chỉ auth, không truy cập tenant data |

## 3. Ma trận module × role

| Module | Endpoint prefix | ADMIN | MANAGER | SUPERVISOR | AGENT | OWNER/EDITOR | MEMBER |
|---|---|---|---|---|---|---|---|
| Auth/profile | `/api/auth/**` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |
| Tenant settings/members | `/api/tenant/**` | ✅ | một phần | ❌ | ❌ | ✅ (owner only cho delete/GDPR) | read |
| GDPR export/delete | `/api/tenant/export`, `/deletion-requests` | ✅ | ❌ | ❌ | ❌ | ❌ | ❌ |
| Customer profiles | `/api/customer/profiles` | ✅ | ✅ | ✅ | ✅ | ✅ + delete/anonymize (owner/editor) | read |
| Feedback | `/api/feedback` | ✅ | ✅ | ✅ | ✅ | ✅ | read |
| Training materials | `/api/training/**` | ✅ | ✅ | ✅ | own progress | ✅ manage | own progress |
| Onboarding approve/reject | `/api/onboarding/progress/approve|reject` | ✅ | ✅ | ✅ | ❌ | ✅ (owner/editor) | ❌ |
| Certifications award | `/api/training/certifications` | ✅ | ✅ | ✅ | view own | ✅ | view own |
| Routing rules | `/api/routing/rules` | ✅ | ✅ | ✅ | ❌ | ✅ | read |
| SLA config | `/api/routing/sla` | ✅ | ✅ | ✅ | ❌ | ✅ | read |
| Escalation tiers | `/api/routing/escalation-tiers` | ✅ | ✅ | ✅ | ❌ | ✅ | read |
| Supervisor sessions | `/api/routing/supervisor-sessions` | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ |
| Whisper | `POST .../supervisor-sessions/{id}/whisper` | ✅ | ✅ | ✅ | ❌ | ✅ | ❌ |
| Conversation takeover | `/api/takeover/**` | ✅ | ✅ | ✅ | ✅ (own conv) | ✅ | ❌ |
| AI suggestions | `/api/ai/suggestions` | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| Sentiment | `/api/ai/sentiment` | ✅ | ✅ | ✅ | ✅ | ✅ | ❌ |
| Analytics/reports | `/api/analytics`, `/api/reports` | ✅ | ✅ | ✅ | ❌ | ✅ | read |
| Audit log | `/api/tenant/audit-log` | ✅ | ✅ | ❌ | ❌ | ✅ | ❌ |
| Payment/billing | `/api/payment/**` | ✅ | ❌ | ❌ | ❌ | ✅ (owner) | ❌ |
| Webhooks (payment) | `/api/payment/webhooks` | ✅ | ❌ | ❌ | ❌ | ✅ | ❌ |
| 2FA | `/api/auth/2fa` | ✅ | ✅ | ✅ | ✅ | ✅ | ✅ |

## 4. Audit log actions hiện có

| Action | Service | Trigger |
|---|---|---|
| `CREATE_TENANT`, `UPDATE_*`, member/invitation events | `TenantService`, `TenantMemberService`, `TenantInvitationService`, `TenantJoinRequestService`, `TenantSelfService` | tenant/membership mutations |
| `DATA_DELETION_*` | `DataDeletionService` | GDPR tenant deletion requests |
| `CUSTOMER_PROFILE_DELETED`, `CUSTOMER_PROFILE_ANONYMIZED` | `CustomerProfileService` | GDPR customer erasure (2026-11-11) |
| `ROUTING_RULE_CREATED/UPDATED/DELETED` | `RoutingRuleService` | routing config changes (2026-11-11) |

## 5. Known gaps

- Audit log chưa phủ: training material CRUD, escalation tier CRUD, SLA config CRUD, agent management, package/billing changes (một phần đã có qua `TenantPackageService`).
- `MEMBER` role hiện chủ yếu read-only — chưa có per-resource permission granularity.
- Frontend route guard chỉ check `requiresAuth`, chưa check role per-route.
