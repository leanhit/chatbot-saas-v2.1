# Operations Runbook — Chatbot SaaS v2.1

Phase 6: Scale, compliance & integration — operational guide for monitoring, alerts, and incident handling.

## 1. Health Checks

| Endpoint | Purpose |
|----------|---------|
| `GET /actuator/health` | Overall app health (db, redis, rabbitmq, LLM, botpress) |
| `GET /health` (via nginx) | Load balancer health check |

**Expected**: `status: UP`. Components `LLM` and `botpress` may be `DOWN` if providers are not configured/running — this does not block core messaging.

## 2. Monitoring & Alerting

### MetricsAlertService
- Scheduled check every minute (`checkCriticalMetrics`)
- Monitors: message processing errors, webhook failures, DLQ depth, response times
- Alerts broadcast via `NotificationWebSocketHandler` to tenant admins

### Key metrics to watch
- **DLQ depth** — messages in `chatbot.dlx` queues indicate repeated failures
- **Message limit rejections** — `MessageLimitExceededException` in logs = tenant hit package quota
- **Facebook webhook 400/401** — invalid page token or signature failures
- **Hibernate exceptions** — any `InvalidDataAccessApiUsageException` = entity mapping bug (see ConversationStatus converter)

## 3. Incident Handling

### 3.1 Messages not being delivered (Facebook)
1. Check `docker logs chatbot_saas_app | grep Facebook`
2. Verify page access token validity: `FacebookConnectionService` health checks run periodically
3. Check DLQ: `DLQManagementService` — inspect `chatbot.queue.*.dlq` queues in RabbitMQ management UI (`localhost:15672`)
4. Replay DLQ messages after fixing root cause

### 3.2 App fails to start
1. `docker logs chatbot_saas_app` — check Flyway migration errors first
2. Verify all 6 databases reachable (postgres containers on port 5440)
3. Check Redis (6379), RabbitMQ, Kafka are running

### 3.3 Tenant quota exceeded
- `MessageUsageService.validateAndIncrementMessageCount` throws `MessageLimitExceededException`
- User messages are blocked; agent/bot messages still flow
- Resolution: upgrade tenant package or wait for next billing period reset

### 3.4 Scheduled tasks not running
- ShedLock requires `shedlock` table in DB — verify it exists
- Check logs for `SchedulerLock` errors
- Tasks: SLA monitor (1min), escalation check (1min), timeout (5min), auto-assign (30s), analytics aggregation (1:30AM daily)

## 4. Data Compliance (GDPR)

### Data export (portability)
```
GET /api/tenant/export/data
Authorization: Bearer <admin-jwt>
```
Returns JSON with all tenant data: conversations, messages, customer profiles, feedback. Admin-only. Capped at 5000 conversations / 1000 messages per conversation for safety.

### Data deletion
- `DELETE /api/tenant/key/{tenantKey}` — full tenant deletion via `TenantCleanupService`
- Message archiving: `MessageArchivingService` moves messages older than retention period to archive table (scheduled)

### Audit trail
- `TenantAuditLog` records sensitive operations (member changes, package upgrades, deletions)
- `AuditService` (identity) records auth events

## 5. Security

- **Webhook signatures**: Facebook webhooks verified via `X-Hub-Signature-256` (HMAC-SHA256 with app secret)
- **Payment webhooks**: `WebhookSignatureService` verification
- **RBAC**: `@PreAuthorize` on all controllers — ADMIN/MANAGER/SUPERVISOR/AGENT roles
- **Secrets**: LLM API keys encrypted via `ApiKeyManager` (AES, `penny.api-key.encryption.secret`)
- **Rate limiting**: `RateLimitingFilter` + `ApiRateLimiterConfig` per-tenant/IP

## 6. Deployment

```bash
cd app-deploy && ./deploy.sh   # build + restart container
```

Verify: `docker ps` → `chatbot_saas_app` healthy; `curl http://localhost/health`

## 7. Escalation

1. Check logs first: `docker logs chatbot_saas_app --since 1h | grep ERROR`
2. Check DLQ depth in RabbitMQ management UI
3. Check `/actuator/health` for component status
4. For DB issues: `psql -h localhost -p 5440 -U chatbot_user -d chatbot_message_db`
