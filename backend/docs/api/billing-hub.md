# 💰 Billing Hub API Documentation

## Overview

The Billing Hub provides comprehensive billing, subscription, and entitlement management for the multi-SaaS platform. It follows a READ-ONLY design pattern as specified in the architecture.

## Features

### 📊 Account Management
- Billing account creation and management
- Credit limit monitoring
- Account suspension and reactivation
- Multi-currency support

### 📋 Subscription Management
- Multiple subscription plans (Free, Starter, Professional, Enterprise)
- Trial period management
- Automatic renewal processing
- Subscription lifecycle management

### 🔐 Entitlement & Usage Control
- Feature-based access control
- Usage limit enforcement
- Real-time usage tracking
- Overage handling

## API Endpoints

### Billing Accounts

#### Create Billing Account
```http
POST /api/billing/accounts
```
**Permissions:** ADMIN, TENANT_OWNER

**Request Body:**
```json
{
  "accountName": "Company Account",
  "companyName": "ACME Corp",
  "email": "billing@acme.com",
  "billingType": "SUBSCRIPTION",
  "currency": "USD",
  "creditLimit": 1000.00
}
```

#### Get Billing Account by Tenant
```http
GET /api/billing/accounts/tenant/{tenantId}
```
**Permissions:** ADMIN, Tenant Member

#### Update Billing Account
```http
PUT /api/billing/accounts/{id}
```
**Permissions:** ADMIN, Account Manager

#### Suspend Billing Account
```http
POST /api/billing/accounts/{id}/suspend?reason=Non-payment
```
**Permissions:** ADMIN, Account Manager

### Subscriptions

#### Create Subscription
```http
POST /api/billing/subscriptions
```
**Permissions:** ADMIN, TENANT_OWNER

**Request Body:**
```json
{
  "plan": "PROFESSIONAL",
  "planName": "Professional Plan",
  "billingCycle": "MONTHLY",
  "price": 99.99,
  "enableTrial": true,
  "trialDays": 14
}
```

#### Get Subscription by Tenant
```http
GET /api/billing/subscriptions/tenant/{tenantId}
```

#### Cancel Subscription
```http
POST /api/billing/subscriptions/{id}/cancel?reason=Downgrading
```

### Entitlements & Usage

#### Validate Feature Access
```http
GET /api/billing/entitlements/tenant/{tenantId}/feature/{feature}/validate
```

**Response:**
```json
{
  "allowed": true,
  "message": "Access granted"
}
```

#### Consume Usage
```http
POST /api/billing/entitlements/tenant/{tenantId}/feature/{feature}/consume?amount=1
```

#### Get Usage Summary
```http
GET /api/billing/entitlements/tenant/{tenantId}/status
```

## Data Models

### BillingAccount
- `accountNumber`: Unique account identifier
- `billingType`: PREPAID, POSTPAID, SUBSCRIPTION, USAGE_BASED
- `creditLimit`: Maximum credit allowed
- `currentBalance`: Current outstanding balance
- `autoPayment`: Automatic payment enabled

### Subscription
- `plan`: FREE, STARTER, PROFESSIONAL, ENTERPRISE, CUSTOM
- `status`: PENDING, ACTIVE, SUSPENDED, CANCELLED, EXPIRED, TRIAL
- `billingCycle`: MONTHLY, YEARLY
- `autoRenew`: Automatic renewal setting

### Entitlement
- `feature`: Specific feature (e.g., CHATBOT_CREATION, API_ACCESS)
- `limitValue`: Maximum allowed usage
- `currentUsage`: Current consumption
- `resetPeriod`: Usage reset frequency

## Features Available

### Core Features
- `USER_MANAGEMENT`: User management capabilities
- `TENANT_MANAGEMENT`: Tenant administration
- `API_ACCESS`: API access rights

### Bot Features
- `CHATBOT_CREATION`: Create chatbots
- `MULTILINGUAL_SUPPORT`: Multiple language support
- `AI_TRAINING`: Custom AI training

### Integration Features
- `FACEBOOK_INTEGRATION`: Facebook platform integration
- `WEBSITE_WIDGET`: Website chat widget
- `WHATSAPP_INTEGRATION`: WhatsApp integration

### Usage Limits
- `MAX_USERS`: Maximum user count
- `MAX_BOTS`: Maximum bot count
- `MAX_MESSAGES_PER_MONTH`: Monthly message limit
- `MAX_STORAGE_MB`: Storage limit in MB

## Usage Examples

### Check if user can create bot
```javascript
const response = await fetch('/api/billing/entitlements/tenant/123/feature/CHATBOT_CREATION/validate');
const result = await response.json();

if (result.allowed) {
  // Allow bot creation
} else {
  // Show upgrade message
  console.log(result.message);
}
```

### Track API usage
```javascript
const response = await fetch('/api/billing/entitlements/tenant/123/feature/API_ACCESS/consume?amount=1');
const result = await response.json();

if (!result.success) {
  // Handle limit exceeded
  console.log(result.message);
}
```

## Configuration

### Environment Variables
- `BILLING_DB_PASSWORD`: Database password
- `STRIPE_PUBLISHABLE_KEY`: Stripe public key
- `STRIPE_SECRET_KEY`: Stripe secret key
- `BILLING_WEBHOOK_SECRET`: Webhook verification secret

### Plan Configuration
Plans are configured in `application-billing.properties`:

```properties
billing.plans.professional.max-users=20
billing.plans.professional.max-bots=10
billing.plans.professional.max-messages-per-month=10000
```

## gRPC Services

The Billing Hub exposes gRPC services on port 50055:

### ValidateBillingAccount
```protobuf
rpc ValidateBillingAccount(ValidateBillingAccountRequest) returns (ValidateBillingAccountResponse);
```

### CheckSubscriptionStatus
```protobuf
rpc CheckSubscriptionStatus(CheckSubscriptionStatusRequest) returns (CheckSubscriptionStatusResponse);
```

### ValidateFeatureAccess
```protobuf
rpc ValidateFeatureAccess(ValidateFeatureAccessRequest) returns (ValidateFeatureAccessResponse);
```

## Security

### Authentication
- JWT token required for all endpoints
- Role-based access control (RBAC)
- Tenant-level isolation

### Data Protection
- Payment data encryption
- Audit logging enabled
- SSL enforcement for payment endpoints

## Monitoring

### Health Checks
- Account status monitoring
- Subscription expiry alerts
- Usage limit warnings
- Payment failure notifications

### Metrics
- Monthly Recurring Revenue (MRR)
- Annual Recurring Revenue (ARR)
- Customer churn rate
- Usage statistics by feature

## Integration Points

### Payment Providers
- Stripe (default)
- PayPal (planned)
- VNPay (planned)

### Notification Services
- Email notifications
- In-app alerts
- Webhook callbacks

### External Systems
- Identity Hub (user validation)
- Tenant Hub (tenant information)
- Wallet Hub (payment processing)
