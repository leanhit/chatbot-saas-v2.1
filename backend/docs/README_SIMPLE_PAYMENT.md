# Simple Payment System

## Overview

Simple bank transfer payment system for Chatbot SaaS v2.1. Replaces complex Billing & Wallet with a streamlined solution.

## Features

- ✅ **QR Code Generation** - Simple bank transfer QR codes
- ✅ **Auto-confirmation** - Bank API polling every 10 seconds
- ✅ **Real-time Updates** - Instant balance updates
- ✅ **Simple API** - Clean, minimal endpoints
- ✅ **Multi-tenant Support** - Works with existing tenant system

## API Endpoints

### Create Deposit Request
```http
POST /api/simple-payment/deposit
Content-Type: application/json

{
  "walletId": 1,
  "amount": 100000,
  "currency": "VND",
  "description": "Nạp tiền vào ví"
}
```

**Response:**
```json
{
  "id": 1,
  "referenceCode": "NAPABC12345",
  "amount": 100000,
  "currency": "VND",
  "status": "PENDING",
  "qrContent": "00020101021238630010...",
  "expiresAt": "2024-03-29T10:24:00",
  "createdAt": "2024-03-28T10:24:00",
  "bankAccountNumber": "1234567890",
  "bankAccountName": "CHATBOT SaaS",
  "bankName": "Vietcombank"
}
```

### Check Payment Status
```http
GET /api/simple-payment/status/NAPABC12345
```

### Get Payment History
```http
GET /api/simple-payment/history
```

### Get Bank Information
```http
GET /api/simple-payment/bank-info
```

### Test Simulate Payment
```http
POST /api/simple-payment/test/simulate-payment
Content-Type: application/json

{
  "referenceCode": "NAPABC12345",
  "amount": 100000
}
```

## Payment Flow

1. **User Request Deposit**
   - Call `/api/simple-payment/deposit`
   - System creates pending payment with unique reference
   - Generate QR code for bank transfer

2. **User Transfers Money**
   - Scan QR code with banking app
   - Transfer money with reference code in description
   - Bank processes transaction

3. **Auto-confirmation**
   - Background job checks bank API every 10 seconds
   - When transaction found, marks payment as completed
   - Updates user balance immediately

4. **Real-time Update**
   - User balance updated instantly
   - Payment status changed to COMPLETED
   - Transaction history updated

## Database Schema

### Users Table (Updated)
```sql
ALTER TABLE users ADD COLUMN balance DECIMAL(15,2) DEFAULT 0.00;
```

### Simple Payments Table
```sql
CREATE TABLE simple_payments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    reference_code VARCHAR(50) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    bank_transaction_id VARCHAR(100),
    description TEXT,
    qr_content TEXT,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    completed_at TIMESTAMP
);
```

## Configuration

### Application Properties
```properties
# Simple Payment Configuration
payment.bank.name=Vietcombank
payment.bank.account_number=1234567890
payment.bank.account_name=CHATBOT SaaS
payment.qr.expire_hours=24
payment.check.interval_seconds=10
```

## Bank Integration

### Mock Implementation (Current)
- Simulates bank API responses
- 30% chance of finding transaction
- For testing and development

### Real Bank API (Future)
- Replace `BankApiService` with real bank integration
- Support for multiple banks (Vietcombank, Techcombank, VCB)
- Real-time transaction checking

## Security

- ✅ Reference codes are unique and non-guessable
- ✅ Amount validation prevents overpayment
- ✅ QR codes expire after 24 hours
- ✅ Bank transaction validation
- ✅ Admin-only manual completion endpoint

## Testing

### Test Flow
1. Create deposit request
2. Use simulate payment endpoint
3. Check payment status
4. Verify balance update

### Test Commands
```bash
# Create deposit
curl -X POST http://localhost:8080/api/simple-payment/deposit \
  -H "Content-Type: application/json" \
  -d '{"walletId":1,"amount":100000}'

# Simulate payment
curl -X POST http://localhost:8080/api/simple-payment/test/simulate-payment \
  -H "Content-Type: application/json" \
  -d '{"referenceCode":"NAPABC12345","amount":100000}'

# Check status
curl http://localhost:8080/api/simple-payment/status/NAPABC12345
```

## Migration from Billing & Wallet

### Advantages
- ✅ Simpler architecture
- ✅ Faster implementation
- ✅ Easier maintenance
- ✅ Better performance

### Disadvantages
- ❌ Less features (no subscriptions, no complex accounting)
- ❌ Manual migration required
- ❌ Limited reporting capabilities

## Future Enhancements

1. **Multiple Bank Support**
2. **Real Bank API Integration**
3. **Payment Notifications**
4. **Advanced Reporting**
5. **Refund Management**
6. **Subscription Support**

## File Structure

```
/core/simplepayment/
├── controller/
│   └── SimplePaymentController.java
├── service/
│   ├── SimplePaymentService.java
│   ├── BankApiService.java
│   └── QRCodeService.java
├── model/
│   ├── SimplePayment.java
│   └── PaymentStatus.java
├── repository/
│   └── SimplePaymentRepository.java
├── dto/
│   ├── DepositRequest.java
│   ├── DepositResponse.java
│   └── PaymentStatusResponse.java
├── scheduler/
│   └── PaymentCheckScheduler.java
└── config/
    └── SimplePaymentConfig.java
```
