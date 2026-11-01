# Phase 4 Intelligent Routing Rollback Plan

## Overview
This document outlines the rollback procedures for Phase 4 Intelligent Routing deployment. The rollback plan ensures minimal downtime and data integrity in case of deployment issues.

## Deployment Information
- **Phase**: Phase 4 - Intelligent Routing
- **Components**: Backend routing service, database schema changes, API endpoints
- **Deployment Date**: TBD
- **Rollback Trigger**: Critical bugs, performance degradation, data corruption

---

## Pre-Deployment Checklist

### Database Backup
- [ ] Backup `chatbot_message_db` database
- [ ] Backup `chatbot_user_db` database
- [ ] Verify backup integrity
- [ ] Store backup in secure location with retention policy

### Code Backup
- [ ] Tag current stable release: `git tag pre-phase4-stable`
- [ ] Create feature branch: `git checkout -b phase4-deployment`
- [ ] Verify all tests passing on stable branch
- [ ] Document current commit SHA

### Configuration Backup
- [ ] Backup `application.properties`
- [ ] Backup environment variables
- [ ] Document current Flyway migration version
- [ ] Backup routing rules (if any exist)

### Monitoring Setup
- [ ] Configure application monitoring (logs, metrics)
- [ ] Set up alerting for critical errors
- [ ] Prepare rollback notification channels
- [ ] Document rollback decision criteria

---

## Rollback Triggers

### Critical Triggers (Immediate Rollback)
- Database migration failure
- API endpoints returning 500 errors > 5% rate
- Data corruption detected
- Authentication/authorization bypass
- Performance degradation > 50% response time increase
- Tenant isolation breach

### Major Triggers (Rollback within 1 hour)
- Unit tests failing in production
- Routing logic producing incorrect assignments
- Agent workload tracking errors
- Supervisor session failures
- Rate limiting not working

### Minor Triggers (Rollback within 24 hours)
- UI inconsistencies
- Non-critical bugs
- Performance degradation < 50%
- Minor logging issues

---

## Rollback Procedures

### Option 1: Code Rollback (Recommended for Code Issues)

#### When to Use
- Logic bugs in routing service
- API endpoint errors
- Performance issues
- Unit test failures

#### Steps
1. **Stop Application**
   ```bash
   # Stop backend service
   systemctl stop chatbot-backend
   # Or kill process
   pkill -f chatbot-backend
   ```

2. **Revert Codebase**
   ```bash
   cd /root/chatbot-saas-v2.1/backend
   git checkout pre-phase4-stable
   git pull origin main
   ```

3. **Rebuild Application**
   ```bash
   ./gradlew clean build -x test
   ```

4. **Restart Application**
   ```bash
   systemctl start chatbot-backend
   # Or start manually
   java -jar build/libs/chatbot-backend.jar
   ```

5. **Verify Health**
   ```bash
   curl -H "X-Tenant-Key: <tenant-key>" https://truyenthongviet.vn/api/health
   ```

6. **Monitor Logs**
   ```bash
   tail -f /var/log/chatbot-backend/application.log
   ```

**Estimated Time**: 5-10 minutes

---

### Option 2: Database Rollback (Recommended for Schema Issues)

#### When to Use
- Database migration failure
- Schema corruption
- Data integrity issues
- Foreign key constraint violations

#### Steps

1. **Stop Application**
   ```bash
   systemctl stop chatbot-backend
   ```

2. **Identify Failed Migration**
   ```bash
   # Check Flyway schema history
   psql -U postgres -d chatbot_message_db -c "SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;"
   ```

3. **Restore Database Backup**
   ```bash
   # Stop PostgreSQL
   systemctl stop postgresql
   
   # Restore from backup
   pg_restore -U postgres -d chatbot_message_db /backups/chatbot_message_db_pre-phase4.backup
   
   # Start PostgreSQL
   systemctl start postgresql
   ```

4. **Verify Schema**
   ```bash
   # Check routing tables exist
   psql -U postgres -d chatbot_message_db -c "\dt agent_workload"
   psql -U postgres -d chatbot_message_db -c "\dt team_workload"
   psql -U postgres -d chatbot_message_db -c "\dt routing_history"
   ```

5. **Revert Code** (follow Option 1 steps 2-6)

**Estimated Time**: 15-30 minutes

---

### Option 3: Feature Flag Rollback (Recommended for Business Logic Issues)

#### When to Use
- Routing logic producing incorrect assignments
- Skill matching errors
- Fallback logic issues
- Condition evaluation bugs

#### Steps

1. **Disable Phase 4 Features via Configuration**
   ```properties
   # In application.properties
   routing.phase4.enabled=false
   routing.use-intelligent-routing=false
   ```

2. **Restart Application**
   ```bash
   systemctl restart chatbot-backend
   ```

3. **Verify Legacy Routing Active**
   ```bash
   # Check logs for "Using legacy routing"
   tail -f /var/log/chatbot-backend/application.log | grep "legacy routing"
   ```

4. **Monitor System**
   - Verify conversations are routed correctly
   - Check agent assignments
   - Monitor error rates

**Estimated Time**: 2-5 minutes

---

### Option 4: Full Rollback (Last Resort)

#### When to Use
- Multiple rollback options failed
- Severe data corruption
- System-wide failure
- Security breach

#### Steps

1. **Stop All Services**
   ```bash
   systemctl stop chatbot-backend
   systemctl stop postgresql
   ```

2. **Restore Full System Backup**
   ```bash
   # Restore database
   pg_restore -U postgres -d chatbot_message_db /backups/chatbot_message_db_pre-phase4.backup
   pg_restore -U postgres -d chatbot_user_db /backups/chatbot_user_db_pre-phase4.backup
   
   # Restore application
   cd /root/chatbot-saas-v2.1
   git checkout pre-phase4-stable
   ```

3. **Rebuild and Deploy**
   ```bash
   cd backend
   ./gradlew clean build -x test
   ```

4. **Start Services**
   ```bash
   systemctl start postgresql
   systemctl start chatbot-backend
   ```

5. **Full System Verification**
   - Run smoke tests
   - Verify all tenants operational
   - Check API endpoints
   - Verify data integrity

**Estimated Time**: 30-60 minutes

---

## Post-Rollback Verification

### Database Verification
```sql
-- Verify routing tables don't exist (if full rollback)
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('agent_workload', 'team_workload', 'routing_history');

-- Verify no data corruption
SELECT COUNT(*) FROM conversations WHERE agent_assigned_id IS NOT NULL;

-- Verify foreign key constraints
SELECT COUNT(*) FROM information_schema.table_constraints 
WHERE constraint_type = 'FOREIGN KEY';
```

### Application Verification
```bash
# Health check
curl -H "X-Tenant-Key: <tenant-key>" https://truyenthongviet.vn/api/health

# API endpoint check
curl -H "X-Tenant-Key: <tenant-key>" https://truyenthongviet.vn/api/routing/routing-rules

# Log check for errors
tail -100 /var/log/chatbot-backend/application.log | grep ERROR
```

### Functional Verification
- [ ] Conversations are routed correctly
- [ ] Agent assignments work
- [ ] Supervisor sessions function
- [ ] Multi-tenant isolation verified
- [ ] No data loss detected
- [ ] Performance metrics normal

---

## Communication Plan

### Rollback Notification
- **Immediate**: Notify DevOps team via Slack
- **Within 5 minutes**: Notify stakeholders via email
- **Within 15 minutes**: Post status update to incident channel
- **Within 1 hour**: Provide detailed incident report

### Stakeholder Communication
- **DevOps Team**: Technical details, rollback status
- **Product Team**: Feature impact, timeline
- **Support Team**: Customer impact, known issues
- **Management**: Business impact, resolution timeline

---

## Rollback Testing

### Pre-Deployment Rollback Test
1. Deploy Phase 4 to staging environment
2. Simulate rollback scenario
3. Verify rollback procedures work
4. Document any issues found
5. Update rollback plan if needed

### Rollback Test Scenarios
- [ ] Code rollback test
- [ ] Database rollback test
- [ ] Feature flag rollback test
- [ ] Full system rollback test

---

## Lessons Learned

### Post-Rollback Review
1. Document root cause of rollback
2. Identify gaps in rollback procedures
3. Update rollback plan based on lessons
4. Improve monitoring and alerting
5. Update deployment checklist

### Continuous Improvement
- Review rollback plan after each deployment
- Update based on new features
- Incorporate feedback from team
- Maintain rollback procedure documentation

---

## Emergency Contacts

| Role | Name | Contact |
|------|------|---------|
| DevOps Lead | TBD | TBD |
| Backend Lead | TBD | TBD |
| Database Admin | TBD | TBD |
| Product Manager | TBD | TBD |

---

## Appendix

### Useful Commands

```bash
# Check Git status
git status
git log --oneline -10

# Check database migrations
psql -U postgres -d chatbot_message_db -c "SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC;"

# Check application logs
tail -f /var/log/chatbot-backend/application.log

# Check service status
systemctl status chatbot-backend
systemctl status postgresql

# Monitor system resources
top
htop
df -h
```

### Rollback Decision Tree
```
Issue Detected
├── Code Issue?
│   ├── Yes → Option 1: Code Rollback
│   └── No → Continue
├── Database Issue?
│   ├── Yes → Option 2: Database Rollback
│   └── No → Continue
├── Business Logic Issue?
│   ├── Yes → Option 3: Feature Flag Rollback
│   └── No → Continue
└── Critical System Failure?
    ├── Yes → Option 4: Full Rollback
    └── No → Investigate further
```

---

**Document Version**: 1.0
**Last Updated**: 2024-01-15
**Next Review**: After Phase 4 deployment
