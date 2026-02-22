# Database Schema Design

## Overview

The chatbot SaaS platform uses a multi-database architecture with each hub having its own isolated database. This design ensures data isolation, independent scaling, and clear domain boundaries.

## Database Architecture

### Database Isolation Strategy

```
┌─────────────────────────────────────────────────────────────┐
│                    Database Layer                          │
├─────────────────────────────────────────────────────────────┤
│  chatbot_identity_db  │  chatbot_user_db  │  chatbot_tenant_db│
│  chatbot_app_db      │  chatbot_billing_db│  chatbot_wallet_db│
│  chatbot_config_db   │  chatbot_message_db │  chatbot_spokes_db│
└─────────────────────────────────────────────────────────────┘
```

Each database serves a specific domain:
- **Identity DB**: Authentication and user identity
- **User DB**: User profiles and activities  
- **Tenant DB**: Multi-tenancy and workspace data
- **App DB**: Application registry and subscriptions
- **Billing DB**: Billing and entitlement data (read-only)
- **Wallet DB**: Financial transactions and ledger
- **Config DB**: Runtime configuration
- **Message DB**: Message routing and storage
- **Spokes DB**: External integration data

## Schema Definitions

### Identity Hub Database (`chatbot_identity_db`)

#### Users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    role VARCHAR(50) DEFAULT 'USER',
    status VARCHAR(50) DEFAULT 'ACTIVE',
    email_verified BOOLEAN DEFAULT FALSE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_status ON users(status);
```

#### User Sessions Table
```sql
CREATE TABLE user_sessions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    ip_address INET,
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_sessions_user_id ON user_sessions(user_id);
CREATE INDEX idx_sessions_token_hash ON user_sessions(token_hash);
CREATE INDEX idx_sessions_expires_at ON user_sessions(expires_at);
```

### User Hub Database (`chatbot_user_db`)

#### User Profiles Table
```sql
CREATE TABLE user_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    phone VARCHAR(20),
    avatar_url TEXT,
    timezone VARCHAR(50) DEFAULT 'UTC',
    language VARCHAR(10) DEFAULT 'en',
    bio TEXT,
    date_of_birth DATE,
    gender VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_profiles_user_id ON user_profiles(user_id);
```

#### User Preferences Table
```sql
CREATE TABLE user_preferences (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE NOT NULL,
    email_notifications BOOLEAN DEFAULT TRUE,
    sms_notifications BOOLEAN DEFAULT FALSE,
    push_notifications BOOLEAN DEFAULT TRUE,
    theme VARCHAR(20) DEFAULT 'light',
    currency VARCHAR(10) DEFAULT 'USD',
    privacy_profile_visibility VARCHAR(20) DEFAULT 'PUBLIC',
    privacy_activity_tracking BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_preferences_user_id ON user_preferences(user_id);
```

#### User Activities Table
```sql
CREATE TABLE user_activities (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    activity_type VARCHAR(50) NOT NULL,
    description TEXT,
    ip_address INET,
    user_agent TEXT,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_activities_user_id ON user_activities(user_id);
CREATE INDEX idx_activities_type ON user_activities(activity_type);
CREATE INDEX idx_activities_created_at ON user_activities(created_at);
```

#### User Addresses Table
```sql
CREATE TABLE user_addresses (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    address_type VARCHAR(20) NOT NULL, -- HOME, WORK, BILLING
    street_address TEXT,
    city VARCHAR(100),
    state_province VARCHAR(100),
    postal_code VARCHAR(20),
    country VARCHAR(100),
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_addresses_user_id ON user_addresses(user_id);
CREATE INDEX idx_addresses_type ON user_addresses(address_type);
```

### Tenant Hub Database (`chatbot_tenant_db`)

#### Tenants Table
```sql
CREATE TABLE tenants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    visibility VARCHAR(50) DEFAULT 'PRIVATE',
    owner_id UUID NOT NULL,
    industry VARCHAR(100),
    company_size VARCHAR(50),
    website TEXT,
    logo_url TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tenants_owner_id ON tenants(owner_id);
CREATE INDEX idx_tenants_status ON tenants(status);
CREATE INDEX idx_tenants_visibility ON tenants(visibility);
```

#### Tenant Members Table
```sql
CREATE TABLE tenant_members (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenants(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL, -- OWNER, ADMIN, MEMBER, VIEWER
    status VARCHAR(50) DEFAULT 'ACTIVE',
    invited_by UUID,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, user_id)
);

CREATE INDEX idx_members_tenant_id ON tenant_members(tenant_id);
CREATE INDEX idx_members_user_id ON tenant_members(user_id);
CREATE INDEX idx_members_role ON tenant_members(role);
```

#### Tenant Profiles Table
```sql
CREATE TABLE tenant_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID UNIQUE NOT NULL,
    industry VARCHAR(100),
    company_size VARCHAR(50),
    website TEXT,
    logo_url TEXT,
    timezone VARCHAR(50) DEFAULT 'UTC',
    currency VARCHAR(10) DEFAULT 'USD',
    language VARCHAR(10) DEFAULT 'en',
    business_hours JSONB,
    settings JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tenant_profiles_tenant_id ON tenant_profiles(tenant_id);
```

### App Hub Database (`chatbot_app_db`)

#### App Registry Table
```sql
CREATE TABLE app_registry (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100),
    app_type VARCHAR(50) DEFAULT 'INTEGRATION',
    status VARCHAR(50) DEFAULT 'ACTIVE',
    version VARCHAR(50),
    developer VARCHAR(255),
    icon_url TEXT,
    screenshots JSONB,
    features JSONB,
    requirements JSONB,
    pricing JSONB,
    configuration_schema JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_app_registry_category ON app_registry(category);
CREATE INDEX idx_app_registry_status ON app_registry(status);
CREATE INDEX idx_app_registry_type ON app_registry(app_type);
```

#### App Subscriptions Table
```sql
CREATE TABLE app_subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    app_id UUID NOT NULL REFERENCES app_registry(id),
    tenant_id UUID NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    plan VARCHAR(50),
    configuration JSONB,
    usage_stats JSONB,
    billing_info JSONB,
    subscribed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(app_id, tenant_id)
);

CREATE INDEX idx_subscriptions_app_id ON app_subscriptions(app_id);
CREATE INDEX idx_subscriptions_tenant_id ON app_subscriptions(tenant_id);
CREATE INDEX idx_subscriptions_status ON app_subscriptions(status);
```

#### App Guards Table
```sql
CREATE TABLE app_guards (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    app_id UUID NOT NULL REFERENCES app_registry(id),
    tenant_id UUID NOT NULL,
    guard_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    rules JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_guards_app_id ON app_guards(app_id);
CREATE INDEX idx_guards_tenant_id ON app_guards(tenant_id);
CREATE INDEX idx_guards_type ON app_guards(guard_type);
```

### Wallet Hub Database (`chatbot_wallet_db`)

#### Wallets Table
```sql
CREATE TABLE wallets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    tenant_id UUID,
    currency VARCHAR(10) NOT NULL DEFAULT 'USD',
    balance DECIMAL(19,4) DEFAULT 0.00,
    available_balance DECIMAL(19,4) DEFAULT 0.00,
    frozen_balance DECIMAL(19,4) DEFAULT 0.00,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    wallet_type VARCHAR(50) DEFAULT 'PERSONAL',
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_wallets_user_id ON wallets(user_id);
CREATE INDEX idx_wallets_tenant_id ON wallets(tenant_id);
CREATE INDEX idx_wallets_currency ON wallets(currency);
CREATE INDEX idx_wallets_status ON wallets(status);
```

#### Transactions Table
```sql
CREATE TABLE transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id UUID NOT NULL REFERENCES wallets(id),
    transaction_type VARCHAR(50) NOT NULL, -- TOPUP, PURCHASE, REFUND, FEE
    amount DECIMAL(19,4) NOT NULL,
    currency VARCHAR(10) NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    description TEXT,
    fee DECIMAL(19,4) DEFAULT 0.00,
    net_amount DECIMAL(19,4),
    metadata JSONB,
    external_transaction_id TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP
);

CREATE INDEX idx_transactions_wallet_id ON transactions(wallet_id);
CREATE INDEX idx_transactions_type ON transactions(transaction_type);
CREATE INDEX idx_transactions_status ON transactions(status);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
```

#### Ledger Table
```sql
CREATE TABLE ledger (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id UUID NOT NULL REFERENCES wallets(id),
    transaction_id UUID REFERENCES transactions(id),
    account_type VARCHAR(50) NOT NULL, -- ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE
    account_code VARCHAR(100) NOT NULL,
    debit_amount DECIMAL(19,4) DEFAULT 0.00,
    credit_amount DECIMAL(19,4) DEFAULT 0.00,
    balance DECIMAL(19,4),
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_ledger_wallet_id ON ledger(wallet_id);
CREATE INDEX idx_ledger_transaction_id ON ledger(transaction_id);
CREATE INDEX idx_ledger_account_type ON ledger(account_type);
CREATE INDEX idx_ledger_account_code ON ledger(account_code);
```

### Message Hub Database (`chatbot_message_db`)

#### Messages Table
```sql
CREATE TABLE messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL,
    message_type VARCHAR(50) DEFAULT 'TEXT',
    content TEXT,
    sender_id UUID NOT NULL,
    sender_type VARCHAR(50) NOT NULL, -- USER, BOT, AGENT
    recipient_id UUID,
    recipient_type VARCHAR(50),
    channel VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'SENT',
    metadata JSONB,
    external_message_id TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP,
    read_at TIMESTAMP
);

CREATE INDEX idx_messages_conversation_id ON messages(conversation_id);
CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_messages_channel ON messages(channel);
CREATE INDEX idx_messages_status ON messages(status);
CREATE INDEX idx_messages_created_at ON messages(created_at);
```

#### Conversations Table
```sql
CREATE TABLE conversations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    participant_id UUID NOT NULL,
    participant_type VARCHAR(50) NOT NULL,
    channel VARCHAR(50) NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    last_message_at TIMESTAMP,
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_conversations_participant_id ON conversations(participant_id);
CREATE INDEX idx_conversations_channel ON conversations(channel);
CREATE INDEX idx_conversations_status ON conversations(status);
CREATE INDEX idx_conversations_last_message_at ON conversations(last_message_at);
```

#### Routing Rules Table
```sql
CREATE TABLE routing_rules (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID,
    name VARCHAR(255) NOT NULL,
    priority INTEGER NOT NULL DEFAULT 1,
    conditions JSONB NOT NULL,
    actions JSONB NOT NULL,
    status VARCHAR(50) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_routing_rules_tenant_id ON routing_rules(tenant_id);
CREATE INDEX idx_routing_rules_priority ON routing_rules(priority);
CREATE INDEX idx_routing_rules_status ON routing_rules(status);
```

### Config Hub Database (`chatbot_config_db`)

#### Runtime Config Table
```sql
CREATE TABLE runtime_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    config_key VARCHAR(255) UNIQUE NOT NULL,
    config_value TEXT,
    config_type VARCHAR(50) DEFAULT 'STRING',
    scope VARCHAR(50) NOT NULL, -- GLOBAL, TENANT, USER
    tenant_id UUID,
    user_id UUID,
    category VARCHAR(100),
    description TEXT,
    is_encrypted BOOLEAN DEFAULT FALSE,
    validation_rules JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_configs_key ON runtime_configs(config_key);
CREATE INDEX idx_configs_scope ON runtime_configs(scope);
CREATE INDEX idx_configs_tenant_id ON runtime_configs(tenant_id);
CREATE INDEX idx_configs_category ON runtime_configs(category);
```

#### Environment Config Table
```sql
CREATE TABLE environment_configs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    environment VARCHAR(50) NOT NULL,
    config_key VARCHAR(255) NOT NULL,
    config_value TEXT,
    config_type VARCHAR(50) DEFAULT 'STRING',
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(environment, config_key)
);

CREATE INDEX idx_env_configs_environment ON environment_configs(environment);
CREATE INDEX idx_env_configs_key ON environment_configs(config_key);
```

### Spokes Database (`chatbot_spokes_db`)

#### Facebook Connections Table
```sql
CREATE TABLE facebook_connections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    page_id VARCHAR(100) NOT NULL,
    page_name VARCHAR(255),
    page_access_token TEXT,
    webhook_secret TEXT,
    connection_status VARCHAR(50) DEFAULT 'ACTIVE',
    metadata JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_fb_connections_tenant_id ON facebook_connections(tenant_id);
CREATE INDEX idx_fb_connections_page_id ON facebook_connections(page_id);
CREATE INDEX idx_fb_connections_status ON facebook_connections(connection_status);
```

#### Facebook Users Table
```sql
CREATE TABLE facebook_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    facebook_user_id VARCHAR(100) UNIQUE NOT NULL,
    user_id UUID,
    tenant_id UUID,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    profile_pic_url TEXT,
    locale VARCHAR(10),
    timezone INTEGER,
    last_interaction_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_fb_users_facebook_id ON facebook_users(facebook_user_id);
CREATE INDEX idx_fb_users_user_id ON facebook_users(user_id);
CREATE INDEX idx_fb_users_tenant_id ON facebook_users(tenant_id);
```

#### Botpress User Mappings Table
```sql
CREATE TABLE botpress_user_mappings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL,
    platform_user_id VARCHAR(255) NOT NULL,
    platform_type VARCHAR(50) NOT NULL,
    botpress_user_id VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(tenant_id, platform_user_id, platform_type)
);

CREATE INDEX idx_bp_mappings_tenant_id ON botpress_user_mappings(tenant_id);
CREATE INDEX idx_bp_mappings_platform_user_id ON botpress_user_mappings(platform_user_id);
CREATE INDEX idx_bp_mappings_botpress_user_id ON botpress_user_mappings(botpress_user_id);
```

## Data Relationships

### Cross-Database Relationships

Since databases are isolated, cross-database relationships are maintained through:

1. **Shared IDs**: User IDs and Tenant IDs are consistent across databases
2. **gRPC Services**: For real-time data validation
3. **Event Sourcing**: For maintaining eventual consistency
4. **Reference Data**: Cached in multiple hubs when needed

### Data Flow Patterns

```
Identity Hub (users.id) → User Hub (user_profiles.user_id)
Identity Hub (users.id) → Tenant Hub (tenants.owner_id)
Identity Hub (users.id) → Wallet Hub (wallets.user_id)
Tenant Hub (tenants.id) → App Hub (app_subscriptions.tenant_id)
Tenant Hub (tenants.id) → Message Hub (routing_rules.tenant_id)
```

## Migration Strategy

### Version Control
- All schema changes managed through Flyway migrations
- Each database has its own migration history
- Migration files named: `V{version}__{description}.sql`

### Migration Process
1. **Development**: Local database migrations
2. **Testing**: Migration testing on staging databases
3. **Production**: Zero-downtime migrations
4. **Rollback**: Rollback scripts for critical changes

### Data Consistency
- **Referential Integrity**: Within database boundaries
- **Eventual Consistency**: Across database boundaries
- **Compensation**: Saga pattern for distributed transactions

## Performance Optimization

### Indexing Strategy
- **Primary Keys**: UUID for all tables
- **Foreign Keys**: Indexed for join performance
- **Query Patterns**: Indexes based on common query patterns
- **Composite Indexes**: For multi-column queries

### Partitioning
- **Time-based Partitioning**: For large time-series tables
- **Tenant-based Partitioning**: For multi-tenant tables
- **Geographic Partitioning**: For regional data distribution

### Caching Strategy
- **Application Level**: Redis for frequently accessed data
- **Database Level**: Query result caching
- **CDN**: For static assets and configuration data

## Security Considerations

### Data Encryption
- **At Rest**: Database-level encryption
- **In Transit**: TLS for all connections
- **Sensitive Fields**: Column-level encryption

### Access Control
- **Database Users**: Least privilege principle
- **Connection Security**: IP whitelisting
- **Audit Logging**: All data access logged

### Data Privacy
- **PII Masking**: Sensitive data masked in logs
- **Data Retention**: Automatic cleanup policies
- **GDPR Compliance**: Right to be forgotten implementation

This database schema design provides a solid foundation for the chatbot SaaS platform with proper isolation, scalability, and security considerations.
