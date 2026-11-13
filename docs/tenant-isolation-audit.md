# Tenant Isolation Audit — P1-A

Date: 2026-11-13
Method: automated scan of all `*Repository.java` under `backend/src/main` for finder/delete/count methods whose signature lacks `tenantId`, cross-referenced with entities that declare a `tenantId`/`tenant_id` column, then manual review of controller-facing callers.

## Tenant context mechanism

- `TenantContext` (ThreadLocal) is populated by `TenantContextInterceptor` from `X-Tenant-Key` header (validated + status-checked via `TenantService.getTenantInfoByKey`), with fallback resolution from the authenticated user.
- Rule: **tenant ID must come from `TenantContext.getTenantId()`**, never from client-supplied path/body params without equality check.

## Fixed in this pass

### payment/invoice — CONFIRMED cross-tenant read/write vulnerability

`InvoiceController` exposed tenant data with no tenant check:

- `GET /api/payment/invoices/{invoiceNumber}` — any authenticated user could read any tenant's invoice.
- `GET /api/payment/invoices/user/{userId}` — listed invoices of arbitrary user across tenants.
- `GET /api/payment/invoices/tenant/{tenantId}` — accepted arbitrary tenantId from path.
- `DELETE /{invoiceNumber}` — admin endpoint now tenant-scoped when tenant context exists.

Fix applied:
- `InvoiceRepository`: added `findByInvoiceNumberAndTenantId`, `findByUserIdAndTenantIdOrderByCreatedAtDesc`, `deleteByInvoiceNumberAndTenantId`.
- `InvoiceService`: added `getInvoiceByNumberAndTenant`, `getUserInvoices(userId, tenantId)`, `deleteInvoice(invoiceNumber, tenantId)`; unscoped variants kept for internal/admin use only.
- `InvoiceController`: all user-facing endpoints now use `TenantContext.getTenantId()`; `/tenant/{tenantId}` returns 403 when path tenant ≠ context tenant.

### payment/transaction — CRITICAL: simulate-payment endpoint unauthenticated

`SecurityConfig` line 148 allowed `permitAll` on `/api/payment/test/**` and `/api/payment/simulate-payment` → any unauthenticated user could forge payment completion by calling `completePayment` with any referenceCode.

Fix applied:
- `SecurityConfig`: removed test/simulate paths from `permitAll`.
- `SimplePaymentController` + `PublicSimplePaymentController`: added `@PreAuthorize("hasRole('SYSTEM_ADMIN')")` to simulate-payment endpoints.

### payment/transaction — referenceCode lookup unscoped

`OrderLookupService.searchOrderContextIfRequested` used `findByReferenceCode` without tenant filter despite having `tenantId` param.

Fix applied:
- `SimplePaymentRepository`: added `findByReferenceCodeAndTenantId`.
- `OrderLookupService`: changed to use `findByReferenceCodeAndTenantId(orderCode, tenantId)`.

### payment/merchant — TenantContext not set in MerchantAuthFilter

`MerchantAuthFilter` set `tenantId` into request attribute but not into `TenantContext`, causing services that rely on `TenantContext.getTenantId()` to see null.

Fix applied:
- `MerchantAuthFilter`: added `TenantContext.setTenantId(merchantKey.getTenantId())` after API key validation.

### payment/merchant — session lookup unscoped

`MerchantPaymentService.getSessionStatus`, `cancelSession`, `completeMerchantPayment` used `findBySessionId` without tenant filter.

Fix applied:
- `MerchantPaymentSessionRepository`: added `findBySessionIdAndTenantId`, `deleteBySessionIdAndTenantId`.
- `MerchantPaymentService`: scoped all session lookup methods to `TenantContext.getTenantId()`.

### app/subscription — user/app lookup unscoped

`AppSubscriptionService.getSubscriptionsByUser`, `getSubscriptionsByApp` used `findByUserId`, `findByAppId` without tenant filter.

Fix applied:
- `AppSubscriptionRepository`: added `findByUserIdAndTenantId`, `findAllByAppIdAndTenantId`, `findExpiredSubscriptionsByTenant`, `findExpiredTrialsByTenant`.
- `AppSubscriptionService`: scoped `getSubscriptionById`, `getSubscriptionsByUser`, `getSubscriptionsByApp` to `TenantContext.getTenantId()`. Scheduler methods (`getExpiredSubscriptions`, `getExpiredTrials`) kept unscoped for global processing; added tenant-scoped variants.

### app/guard — appId lookup unscoped

`AppGuardService.getGuardsByAppId`, `getActiveGuardsByAppId` used `findByAppId`, `findByAppIdAndIsActive` without tenant filter. `AppGuard` has tenantId via base class.

Fix applied:
- `AppGuardRepository`: added `findByAppIdAndTenantId`.
- `AppGuardService`: scoped `getGuardById`, `getGuardsByAppId`, `getActiveGuardsByAppId` to `TenantContext.getTenantId()`.

### payment/plan — PackageUpgradeAudit lookup unscoped

`PackageUpgradeAuditRepository` had `findByPaymentReferenceCode`, `findByPaymentReferenceCodeAndUpgradeStatus`, `findByUpgradeStatusAndProcessedAtBefore`, `findByUserIdOrderByCreatedAtDesc` without tenant filter.

Fix applied:
- `PackageUpgradeAuditRepository`: added `findByPaymentReferenceCodeAndTenantId`, `findByPaymentReferenceCodeAndUpgradeStatusAndTenantId`, `findByUpgradeStatusAndProcessedAtBeforeAndTenantId`, `findByUserIdAndTenantIdOrderByCreatedAtDesc`.
- `PaymentPackageUpgradeService.getUpgradeHistory` already scoped by tenantId parameter; internal methods are event-driven and safe.

### payment/merchant — deleteByMerchantId unscoped

`MerchantPaymentSessionRepository.deleteByMerchantId` had no tenant filter, allowing deletion of sessions across tenants.

Fix applied:
- `MerchantPaymentSessionRepository`: added `deleteByMerchantIdAndTenantId`.

### payment/transaction — analytics finders unscoped

`SimplePaymentRepository` had `existsByReferenceCode`, `findByCreatedAtBetween`, `findByStatusAndCreatedAtAfter`, `countByStatus` without tenant filter.

Fix applied:
- `SimplePaymentRepository`: added `existsByReferenceCodeAndTenantId`, `findByCreatedAtBetweenAndTenantId`, `findByStatusAndCreatedAtAfterAndTenantId`, `countByStatusAndTenantId`.

### config/environment — EnvironmentConfig lookup unscoped

`EnvironmentConfigRepository` had no tenant-scoped finder methods despite entity extending `BaseTenantEntity`.

Fix applied:
- `EnvironmentConfigRepository`: added tenant-scoped variants for all finder methods (`findByEnvironmentAndConfigKeyAndTenantIdAndIsActive`, `findByEnvironmentAndTenantIdAndIsActiveOrderByConfigKey`, `findByConfigKeyAndTenantIdAndIsActiveOrderByEnvironment`, `findActiveConfigsByEnvironmentAndTenantId`, `findActiveConfigsByKeyAndTenantId`, `countActiveConfigsByEnvironmentAndTenantId`).

### config/runtime — RuntimeConfig design OK

`RuntimeConfigRepository` uses `ConfigScope` (GLOBAL/TENANT/USER) design with tenant-aware queries (`findEffectiveConfigs`, `findEffectiveConfigByKey`). No changes needed.

### spokes/facebook — FacebookConnectionRepository OK

Repository has many tenant-scoped methods. Unscoped methods (`findByPageIdForWebhook`, `findAllByPageId`, `findByPageIdAndIsActiveTrue`, `findByIsActiveTrue`, `findTokensExpiringBefore`, `findByBotIdAndIsActiveTrue`, `findAllByBotIdAndIsActiveTrue`) are legitimate for webhook lookup, scheduler, and agent messages.

### message/store — AgentRepository OK

Repository has `findByUserIdAndTenantId`, `findByIdAndTenantId`, `deleteByTenantId`. `findByUserId` is deprecated with tenant-scoped alternative. `findByEmail` is not used in `AgentService`.

### payment/merchant — MerchantAuthFilter OK

Filter sets `TenantContext.setTenantId()` after API key validation (line 80).

## Remaining findings — requires follow-up (ordered by risk)

### HIGH — entity has tenantId, repository methods unscoped, reachable from controllers/services

| Domain | Repository | Risky methods | Notes |
|---|---|---|---|
| payment/transaction | `SimplePaymentRepository` | `findByReferenceCode`, `findByStatusAndExpiresAtBefore`, `findPendingPaymentsSince`, `findActivePendingPayments`, `existsByReferenceCode`, `findByCreatedAtBetween`, `findByStatusAndCreatedAtAfter`, `countByStatus` | `SimplePayment` has tenantId. Callers: `SimplePaymentService`, `PaymentCancellationService`, `PaymentRefundService`, `PaymentRetryService`, `PaymentExpirationService`, `OrderLookupService`, `PaymentAnalyticsController`, `BankApiService`. Some are scheduler/webhook-legit; controller-facing ones need scoping. |
| payment/merchant | `MerchantPaymentSessionRepository` | `findBySessionId`, `findByMerchantOrderId`, `findExpiredSessions`, `findPendingWebhooks`, `findFailedWebhooksForRetry`, `findByPaymentReferenceCode`, `existsBySessionId`, `existsByMerchantOrderId`, `deleteBySessionId`, `deleteByMerchantId` | Entity has tenantId. Webhook/session lookup by sessionId must verify tenant or merchant ownership. |
| payment/merchant | `MerchantApiKeyRepository` | `findByApiKey`, `findActiveApiKeys`, `existsByApiKey`, `deleteByApiKey` | API-key lookup is the auth mechanism itself — verify tenant binding after lookup. |
| payment/plan | `PackageUpgradeAuditRepository` | `findByPaymentReferenceCode`, `findByPaymentReferenceCodeAndUpgradeStatus`, `findByUpgradeStatusAndProcessedAtBefore`, `findByUserIdOrderByCreatedAtDesc` | Entity has tenantId. |
| app/subscription | `AppSubscriptionRepository` | all finders (`findByUserId`, `findByAppId`, `findBySubscriptionStatus`, …) | Entity has tenantId; no tenant-scoped variant exists. |
| app/guard | `AppGuardRepository`, `GuardRuleRepository` | `findByAppId*`, `findByGuardType`, `findByAppGuardId*` | `AppGuard` has tenantId; scoped only via appId → app→tenant chain must be verified. |
| app/registry | `AppRegistryRepository` | `findByName`, `findByAppType`, `findByStatus`, `findByIsActive`, `findByIsPublic`, `findByCreatedBy`, `existsByName` | Entity has tenantId — verify whether registry is meant to be global catalog or tenant-scoped. |
| config/environment | `EnvironmentConfigRepository` | all finders | Entity has tenantId. |
| config/runtime | `RuntimeConfigRepository` | `findByConfigKeyAndConfigScope`, `findByUserIdOrderByConfigKey`, `findByConfigScopeOrderByConfigKey`, `findEffectiveConfigByKey`, `countByKeyAndScope` | Entity has tenantId; `findEffectiveConfigByKey` is the dangerous one. |
| shared/address | `AddressRepository` | `findByOwnerTypeAndOwnerId`, `findByOwnerIdAndOwnerType` | Entity has tenantId; ownerId may be user/tenant — verify. |
| spokes/facebook | `FacebookConnectionRepository` | `findAllByPageId`, `findByPageIdAndIsActiveTrue`, `findByIsActiveTrue`, `findTokensExpiringBefore`, `findByBotIdAndIsActiveTrue`, `findAllByBotIdAndIsActiveTrue` | `findByPageIdForWebhook` is webhook-legit. Others need tenant check. |
| message/store | `ConversationRepository` | `findByExternalUserIdAndConnectionId`, `findByExternalUserId`, `findByStatus`, `findAllByIsTakenOverByAgent`, `existsByConnectionIdInAndUpdatedAtAfter` | Mostly scheduler/internal; `findByExternalUserIdAndConnectionId` used by `PennyBotProviderService` for agent messages — verify tenant check at call site. |
| message/store | `AgentRepository` | `findByUserId`, `findByEmail` | `Agent` has tenantId. |
| routing | `ConversationRoutingRepository` | `findByConversationId` | Verify `ConversationRouting` tenant linkage via conversation. |

### MEDIUM — scoped via parent (botId/agentId/merchantId/userId) — verify parent→tenant chain

- `core/penny/rules/*` (`BotRule`, `ResponseTemplate`, `ImportExportChangelog`, `BotWebhookDeadLetter`) — scoped by `botId`; Penny bot ownership was fixed in Penny P0/P1, verify callers still validate.
- `core/penny/repository/*` (`PennyKnowledgeDocument`, `PennyKnowledgeChunk`, `PennyBotApiKey`, `PennyConversationContext`, `EscalationTicket`) — same botId/agentId chain.
- `core/penny/analytics/AnalyticsEventRepository` — `findByEventType`, `deleteByTimestampBefore` unscoped.
- `core/training/repository/*` (`AgentSkill`, `AgentTrainingProgress`, `AgentOnboardingProgress`, `AgentCertification`, `TrainingMaterial`) — scoped via agentId/categoryId.
- `core/analytics/repository/ReportScheduleRepository` — `findByEnabledTrueAndNextRunAtBeforeAndIsDeletedFalse` (scheduler-legit).
- `core/payment/gateway/repository/Webhook*` — platform-level webhook infra; verify tenant binding on delivery.

### LOW / legitimately global (no tenantId on entity or global by design)

- `UserRepository`, `AuthRepository`, `UserProfileRepository`, `RefreshTokenRepository` — identity domain, users are global.
- `PackageRepository`, `DiscountRepository` — global catalog.
- `LicenseRepository`, `DeviceBindingRepository` — scoped by userId (user→tenant membership check needed at service layer).
- `TenantRepository`, `TenantMemberRepository`, `TenantApiKeyRepository`, `TenantAuditLogRepository`, `TenantWebhook*Repository` — tenant domain itself.
- `BaseRepository` default methods (`findAllActive`, `findByIdAndActive`) — generic helpers; audit usages.
- `DataDeletionRequestRepository.findByStatusAndScheduledAtBefore` — scheduler-legit.
- `AiSuggestionRepository.findPendingOlderThan` — scheduler-legit.

## Recommended next steps

1. For each HIGH row: add `tenantId` param to repository methods and pass `TenantContext.getTenantId()` from services; for webhook/scheduler paths keep unscoped variants but document why.
2. Add cross-tenant regression tests per domain: tenant A cannot read/update/delete tenant B data (pattern: set `TenantContext` to tenant A, request tenant B resource → expect empty/403).
3. Consider a shared `TenantScopedRepository` fragment or JPA `@Filter` to enforce tenant scoping declaratively.
