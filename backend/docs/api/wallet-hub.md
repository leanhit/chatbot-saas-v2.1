# Wallet Hub API Documentation

## Overview
The Wallet Hub manages digital wallets, transactions, and ledger operations for the chatbot SaaS platform.

## Base URL
```
http://localhost:8080/api/v1/wallets
```

## Authentication
All endpoints require JWT token in Authorization header:
```
Authorization: Bearer <jwt-token>
```

## Endpoints

### Wallet Management

#### GET /wallets
Get current user's wallets.

**Response:**
```json
{
  "wallets": [
    {
      "id": "wallet_123",
      "userId": "user_456",
      "tenantId": "tenant_789",
      "currency": "USD",
      "balance": 150.75,
      "status": "ACTIVE",
      "type": "PERSONAL",
      "createdAt": "2024-01-01T00:00:00Z",
      "updatedAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

#### POST /wallets
Create a new wallet.

**Request Body:**
```json
{
  "currency": "EUR",
  "type": "BUSINESS"
}
```

**Response:**
```json
{
  "id": "wallet_456",
  "userId": "user_456",
  "tenantId": "tenant_789",
  "currency": "EUR",
  "balance": 0.00,
  "status": "ACTIVE",
  "type": "BUSINESS",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

#### GET /wallets/{walletId}
Get wallet details.

**Response:**
```json
{
  "id": "wallet_123",
  "userId": "user_456",
  "tenantId": "tenant_789",
  "currency": "USD",
  "balance": 150.75,
  "availableBalance": 125.50,
  "frozenBalance": 25.25,
  "status": "ACTIVE",
  "type": "PERSONAL",
  "metadata": {
    "autoTopup": true,
    "topupThreshold": 50.00,
    "topupAmount": 100.00
  },
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

#### PUT /wallets/{walletId}
Update wallet settings.

**Request Body:**
```json
{
  "metadata": {
    "autoTopup": false,
    "topupThreshold": 25.00,
    "topupAmount": 50.00
  }
}
```

#### POST /wallets/{walletId}/freeze
Freeze wallet balance.

**Request Body:**
```json
{
  "amount": 25.00,
  "reason": "Pending transaction"
}
```

#### POST /wallets/{walletId}/unfreeze
Unfreeze wallet balance.

**Request Body:**
```json
{
  "amount": 25.00,
  "reason": "Transaction completed"
}
```

### Transactions

#### GET /wallets/{walletId}/transactions
Get wallet transaction history.

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `type`: Transaction type filter (TOPUP, PURCHASE, REFUND)
- `status`: Status filter (PENDING, COMPLETED, FAILED)
- `fromDate`: Start date filter (optional)
- `toDate`: End date filter (optional)

**Response:**
```json
{
  "content": [
    {
      "id": "txn_123",
      "walletId": "wallet_123",
      "type": "TOPUP",
      "amount": 100.00,
      "currency": "USD",
      "status": "COMPLETED",
      "description": "Wallet topup via credit card",
      "metadata": {
        "paymentMethod": "CREDIT_CARD",
        "paymentId": "pay_456",
        "cardLast4": "1234"
      },
      "createdAt": "2024-01-01T10:00:00Z",
      "completedAt": "2024-01-01T10:01:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 50,
  "totalPages": 3
}
```

#### POST /wallets/{walletId}/topup
Top up wallet balance.

**Request Body:**
```json
{
  "amount": 50.00,
  "paymentMethod": "CREDIT_CARD",
  "paymentDetails": {
    "cardToken": "tok_123",
    "saveCard": true
  }
}
```

**Response:**
```json
{
  "transactionId": "txn_456",
  "walletId": "wallet_123",
  "amount": 50.00,
  "currency": "USD",
  "status": "PENDING",
  "description": "Wallet topup via credit card",
  "createdAt": "2024-01-01T10:00:00Z"
}
```

#### POST /wallets/{walletId}/purchase
Make a purchase from wallet.

**Request Body:**
```json
{
  "amount": 25.50,
  "description": "Premium subscription",
  "metadata": {
    "productId": "premium_monthly",
    "tenantId": "tenant_789"
  }
}
```

**Response:**
```json
{
  "transactionId": "txn_789",
  "walletId": "wallet_123",
  "amount": 25.50,
  "currency": "USD",
  "status": "COMPLETED",
  "description": "Premium subscription",
  "newBalance": 125.25,
  "createdAt": "2024-01-01T10:00:00Z"
}
```

#### POST /wallets/{walletId}/refund
Refund a transaction.

**Request Body:**
```json
{
  "originalTransactionId": "txn_789",
  "amount": 25.50,
  "reason": "Customer request"
}
```

#### GET /transactions/{transactionId}
Get transaction details.

**Response:**
```json
{
  "id": "txn_123",
  "walletId": "wallet_123",
  "type": "TOPUP",
  "amount": 100.00,
  "currency": "USD",
  "status": "COMPLETED",
  "description": "Wallet topup via credit card",
  "fee": 2.50,
  "netAmount": 97.50,
  "metadata": {
    "paymentMethod": "CREDIT_CARD",
    "paymentId": "pay_456",
    "cardLast4": "1234",
    "processor": "STRIPE"
  },
  "relatedTransactions": [
    {
      "id": "txn_124",
      "type": "FEE",
      "amount": -2.50,
      "description": "Processing fee"
    }
  ],
  "createdAt": "2024-01-01T10:00:00Z",
  "completedAt": "2024-01-01T10:01:00Z"
}
```

### Ledger

#### GET /wallets/{walletId}/ledger
Get wallet ledger entries.

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 50)
- `accountType`: Filter by account type (ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE)
- `fromDate`: Start date filter (optional)
- `toDate`: End date filter (optional)

**Response:**
```json
{
  "content": [
    {
      "id": "ledger_123",
      "walletId": "wallet_123",
      "transactionId": "txn_123",
      "accountType": "ASSET",
      "accountCode": "CASH",
      "debitAmount": 100.00,
      "creditAmount": 0.00,
      "balance": 100.00,
      "description": "Wallet topup",
      "createdAt": "2024-01-01T10:00:00Z"
    },
    {
      "id": "ledger_124",
      "walletId": "wallet_123",
      "transactionId": "txn_123",
      "accountType": "EXPENSE",
      "accountCode": "PROCESSING_FEE",
      "debitAmount": 0.00,
      "creditAmount": 2.50,
      "balance": -2.50,
      "description": "Processing fee",
      "createdAt": "2024-01-01T10:00:00Z"
    }
  ],
  "page": 0,
  "size": 50,
  "totalElements": 150,
  "totalPages": 3
}
```

#### GET /wallets/{walletId}/balance
Get current wallet balance with breakdown.

**Response:**
```json
{
  "walletId": "wallet_123",
  "currency": "USD",
  "totalBalance": 150.75,
  "availableBalance": 125.50,
  "frozenBalance": 25.25,
  "pendingCredits": 0.00,
  "pendingDebits": 0.00,
  "breakdown": {
    "topups": 200.00,
    "purchases": -49.25,
    "fees": -2.50,
    "refunds": 2.50
  },
  "lastUpdated": "2024-01-01T10:00:00Z"
}
```

### Payment Methods

#### GET /payment-methods
Get saved payment methods.

**Response:**
```json
{
  "paymentMethods": [
    {
      "id": "pm_123",
      "type": "CREDIT_CARD",
      "cardBrand": "VISA",
      "cardLast4": "1234",
      "expiryMonth": 12,
      "expiryYear": 2025,
      "isDefault": true,
      "status": "ACTIVE",
      "createdAt": "2024-01-01T00:00:00Z"
    }
  ]
}
```

#### POST /payment-methods
Add new payment method.

**Request Body:**
```json
{
  "type": "CREDIT_CARD",
  "cardToken": "tok_456",
  "isDefault": false
}
```

#### DELETE /payment-methods/{paymentMethodId}
Remove payment method.

## Error Responses

All endpoints return consistent error format:

```json
{
  "error": "ERROR_CODE",
  "message": "Human readable error message",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

Common error codes:
- `UNAUTHORIZED` (401): Invalid or missing token
- `FORBIDDEN` (403): Insufficient permissions
- `NOT_FOUND` (404): Wallet or transaction not found
- `INSUFFICIENT_BALANCE` (402): Insufficient wallet balance
- `INVALID_AMOUNT` (400): Invalid transaction amount
- `PAYMENT_FAILED` (402): Payment processing failed
- `WALLET_FROZEN` (423): Wallet is frozen
- `TRANSACTION_FAILED` (402): Transaction could not be processed
- `DUPLICATE_TRANSACTION` (409): Duplicate transaction detected

## gRPC Services

### WalletService

- `GetWalletById`: Get wallet by ID
- `CreateWallet`: Create new wallet
- `UpdateWallet`: Update wallet settings
- `FreezeBalance`: Freeze wallet balance
- `UnfreezeBalance`: Unfreeze wallet balance

### TransactionService

- `ProcessTopup`: Process wallet topup
- `ProcessPurchase`: Process wallet purchase
- `ProcessRefund`: Process refund
- `GetTransactionById`: Get transaction by ID
- `ValidateTransaction`: Validate transaction

### LedgerService

- `CreateLedgerEntry`: Create ledger entry
- `GetLedgerEntries`: Get ledger entries
- `CalculateBalance`: Calculate wallet balance
- `ValidateDoubleEntry`: Validate double-entry accounting

## Rate Limiting

API endpoints are rate-limited:
- Wallet operations: 100 requests per minute
- Transaction operations: 200 requests per minute
- Ledger queries: 50 requests per minute
- Payment method operations: 30 requests per minute

## Security

- All financial data encrypted at rest
- PCI DSS compliance for payment processing
- Double-entry accounting for all transactions
- Audit trails for all financial operations
- Fraud detection and prevention
- Multi-signature required for large transactions
