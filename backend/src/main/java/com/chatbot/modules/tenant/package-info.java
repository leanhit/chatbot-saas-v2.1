package com.chatbot.modules.tenant;

//  1️⃣ Tenant Core
//  | Chức năng                 | Method | URL                                      | BE Controller      | Quyền  | Ghi chú              |
// | ------------------------- | ------ | ---------------------------------------- | ------------------ | ------ | -------------------- |
// | Tạo tenant                | POST   | `/tenants`                               | `TenantController` | USER   | Tạo tenant mới       |
// | Lấy tenant theo id        | GET    | `/tenants/{tenantId}`                    | `TenantController` | MEMBER | Phải thuộc tenant    |
// | Switch tenant             | POST   | `/tenants/{tenantId}/switch`             | `TenantController` | MEMBER | Đổi tenant hiện tại  |
// | Danh sách tenant của user | GET    | `/tenants/me`                            | `TenantController` | USER   | Tenant đang tham gia |
// | Update tenant             | PUT    | `/tenants/{tenantId}`                    | `TenantController` | OWNER  | Update name, info    |
// | Suspend tenant            | POST   | `/tenants/{tenantId}/suspend`            | `TenantController` | OWNER  | Disable tenant       |
// | Activate tenant           | POST   | `/tenants/{tenantId}/activate`           | `TenantController` | OWNER  | Enable tenant        |
// | Transfer ownership        | POST   | `/tenants/{tenantId}/transfer-ownership` | `TenantController` | OWNER  | Chuyển OWNER         |
// | Search tenant             | GET    | `/tenants/search`                        | `TenantController` | USER   | Public search        |

// 2️⃣ Join Requests (User → Tenant)
// | Chức năng                            | Method | URL                                                     | BE Controller            | Quyền         | Ghi chú                |
// | ------------------------------------ | ------ | ------------------------------------------------------- | ------------------------ | ------------- | ---------------------- |
// | Gửi request join tenant              | POST   | `/tenants/{tenantId}/members/join-requests`             | `TenantMemberController` | USER          | Join tenant            |
// | Danh sách join request của tenant    | GET    | `/tenants/{tenantId}/members/join-requests`             | `TenantMemberController` | ADMIN / OWNER | Pending only           |
// | Duyệt join request                   | PATCH  | `/tenants/{tenantId}/members/join-requests/{requestId}` | `TenantMemberController` | ADMIN / OWNER | `{ status: APPROVED }` |
// | Từ chối join request                 | PATCH  | `/tenants/{tenantId}/members/join-requests/{requestId}` | `TenantMemberController` | ADMIN / OWNER | `{ status: REJECTED }` |
// | Danh sách tenant user đang chờ duyệt | GET    | `/tenants/members/pending-tenants`                      | `TenantMemberController` | USER          | My pending             |

// 3️⃣ Tenant Members (Admin management)
// | Chức năng             | Method | URL                                         | BE Controller            | Quyền         | Ghi chú               |
// | --------------------- | ------ | ------------------------------------------- | ------------------------ | ------------- | --------------------- |
// | Danh sách member      | GET    | `/tenants/{tenantId}/members`               | `TenantMemberController` | ADMIN / OWNER | Pagination            |
// | Lấy info 1 member     | GET    | `/tenants/{tenantId}/members/{userId}`      | `TenantMemberController` | SELF / ADMIN  |                       |
// | Invite member (email) | POST   | `/tenants/{tenantId}/members/invite`        | `TenantMemberController` | ADMIN / OWNER | Invite                |
// | Update role member    | PUT    | `/tenants/{tenantId}/members/{userId}/role` | `TenantMemberController` | OWNER         | Không downgrade OWNER |
// | Remove member         | DELETE | `/tenants/{tenantId}/members/{userId}`      | `TenantMemberController` | OWNER         | Không remove OWNER    |

// 4️⃣ Member Self-Service
// | Chức năng                           | Method | URL                              | BE Controller            | Quyền  | Ghi chú                |
// | ----------------------------------- | ------ | -------------------------------- | ------------------------ | ------ | ---------------------- |
// | Lấy thông tin của mình trong tenant | GET    | `/tenants/{tenantId}/members/me` | `TenantMemberController` | MEMBER | FE dùng nhiều          |
// | Rời tenant                          | DELETE | `/tenants/{tenantId}/members/me` | `TenantMemberController` | MEMBER | OWNER không được leave |

// 5️⃣ Invite Management (Optional nhưng nên có)
// | Chức năng                | Method | URL                                                     | BE Controller            | Quyền         | Ghi chú |
// | ------------------------ | ------ | ------------------------------------------------------- | ------------------------ | ------------- | ------- |
// | Danh sách invite pending | GET    | `/tenants/{tenantId}/members/invites`                   | `TenantMemberController` | ADMIN / OWNER |         |
// | Resend invite            | POST   | `/tenants/{tenantId}/members/invites/{inviteId}/resend` | `TenantMemberController` | ADMIN / OWNER |         |
// | Revoke invite            | DELETE | `/tenants/{tenantId}/members/invites/{inviteId}`        | `TenantMemberController` | ADMIN / OWNER |         |

// 6️⃣ System / Utility (Recommended)
// | Chức năng      | Method | URL                | Ghi chú    |
// | -------------- | ------ | ------------------ | ---------- |
// | Health check   | GET    | `/health`          | Monitoring |
// | Current user   | GET    | `/me`              | Auth info  |
// | Current tenant | GET    | `/tenants/current` | FE boot    |
