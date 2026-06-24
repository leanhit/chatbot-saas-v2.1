# Error Handling Guide

## Overview

Hệ thống error handling được thiết kế để server trả về error code và frontend hiển thị message theo ngôn ngữ (vi/en) tương ứng.

## Architecture

### Backend
- Tất cả exception handlers sử dụng `ErrorResponse` từ `com.chatbot.shared.dto.ErrorResponse`
- Mỗi error có `code` (ví dụ: `NOT_FOUND`, `VALIDATION_ERROR`, `UNAUTHORIZED`)
- Message từ server chỉ dùng cho debug, không hiển thị trực tiếp cho user

### Frontend
- Error codes được mapping trong `src/locales/vi.json` và `src/locales/en.json` (section `errors`)
- Utility `src/utils/errorHandler.js` cung cấp các function để xử lý error
- Axios interceptor tự động gắn `localizedMessage` và `errorCode` vào error object

## Error Codes Reference

### Common Errors
- `NOT_FOUND` - Resource không tồn tại
- `VALIDATION_ERROR` - Dữ liệu đầu vào không hợp lệ
- `UNAUTHORIZED` - Không có quyền truy cập
- `FORBIDDEN` - Truy cập bị từ chối
- `BAD_REQUEST` - Yêu cầu không hợp lệ
- `CONFLICT` - Xung đột dữ liệu
- `INTERNAL_ERROR` - Lỗi hệ thống

### Authentication Errors
- `BAD_CREDENTIALS` - Email hoặc mật khẩu không chính xác
- `AUTHENTICATION_FAILED` - Xác thực thất bại
- `INVALID_TOKEN` - Token không hợp lệ

### User Errors
- `EMAIL_ALREADY_EXISTS` - Email đã tồn tại
- `USER_NOT_FOUND` - Không tìm thấy người dùng

### Tenant Errors
- `TENANT_NOT_FOUND` - Không tìm thấy không gian làm việc
- `INVALID_TENANT_KEY` - Tenant key không hợp lệ
- `INSUFFICIENT_PERMISSION` - Quyền hạn không đủ

### License Errors
- `LICENSE_NOT_FOUND` - Không tìm thấy license
- `LICENSE_EXPIRED` - License đã hết hạn

### Payment Errors
- `PAYMENT_NOT_FOUND` - Không tìm thấy thanh toán
- `PAYMENT_EXPIRED` - Thanh toán đã hết hạn
- `INVALID_PAYMENT_AMOUNT` - Số tiền thanh toán không hợp lệ
- `BANK_API_ERROR` - Lỗi API ngân hàng

## Usage Examples

### Frontend - Trong Vue Component

```javascript
import { handleApiError, isAuthError, isValidationError } from '@/utils/errorHandler';

// Cách 1: Sử dụng error object từ axios (đã có localizedMessage)
try {
  await apiCall();
} catch (error) {
  // Hiển thị message đã được localize
  toast.error(error.localizedMessage || 'Có lỗi xảy ra');
  
  // Kiểm tra loại error
  if (isAuthError(error)) {
    router.push('/login');
  }
}

// Cách 2: Sử dụng utility function
try {
  await apiCall();
} catch (error) {
  const message = handleApiError(error, { 
    fallback: 'Không thể thực hiện thao tác',
    logError: true 
  });
  toast.error(message);
}

// Cách 3: Xử lý validation error riêng
try {
  await apiCall();
} catch (error) {
  if (isValidationError(error)) {
    const errors = getValidationErrors(error);
    // Hiển thị từng field error
    errors.forEach(err => {
      toast.error(err);
    });
  } else {
    toast.error(error.localizedMessage);
  }
}
```

### Frontend - Trong Action/Store

```javascript
import { handleApiError } from '@/utils/errorHandler';

async function fetchUserData() {
  try {
    const response = await axios.get('/api/users/me');
    return response.data;
  } catch (error) {
    const message = handleApiError(error);
    throw new Error(message);
  }
}
```

### Backend - Tạo Custom Exception

```java
package com.chatbot.core.yourmodule.exception;

public class YourCustomException extends YourBaseException {
    
    public YourCustomException(String errorCode, String message) {
        super(errorCode, message);
    }
    
    public YourCustomException(String errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
```

### Backend - Exception Handler

```java
@RestControllerAdvice
public class YourExceptionHandler {

    @ExceptionHandler(YourCustomException.class)
    public ResponseEntity<ErrorResponse> handleYourException(
            YourCustomException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getErrorCode(), ex.getMessage())
                .withPath(request.getDescription(false).replace("uri=", ""))
                .withTimestamp(LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
```

## Adding New Error Codes

### 1. Backend
Định nghĩa error code trong exception class:
```java
public class YourException extends BaseException {
    public YourException() {
        super("YOUR_ERROR_CODE", "Technical message for logging");
    }
}
```

### 2. Frontend - Add to locale files

**vi.json:**
```json
{
  "errors": {
    "YOUR_ERROR_CODE": "Thông báo lỗi bằng tiếng Việt"
  }
}
```

**en.json:**
```json
{
  "errors": {
    "YOUR_ERROR_CODE": "Error message in English"
  }
}
```

## Utility Functions Reference

### `getErrorMessage(errorCode, fallbackMessage)`
Lấy message localized từ error code.

### `extractErrorCode(error)`
Trích xuất error code từ axios error response.

### `getErrorFromResponse(error, fallbackMessage)`
Lấy message localized từ axios error object.

### `handleApiError(error, options)`
Convenience function để handle API error.
- `options.fallback`: Fallback message
- `options.logError`: Có log error không (default: true)

### `isErrorCode(error, errorCode)`
Kiểm tra error có phải là code cụ thể không.

### `isAuthError(error)`
Kiểm tra có phải authentication error không.

### `isValidationError(error)`
Kiểm tra có phải validation error không.

### `getValidationErrors(error)`
Lấy danh sách validation error messages.

## Flow Summary

1. **Backend**: Exception xảy ra → Handler trả về `ErrorResponse` với `code`
2. **Frontend**: Axios nhận error → Extract error code → Map với locale file → Attach `localizedMessage`
3. **Component**: Hiển thị `error.localizedMessage` cho user

## Benefits

- ✅ Tách biệt logic error handling
- ✅ Dễ dàng đa ngôn ngữ
- ✅ Dễ maintain và update messages
- ✅ Consistent error response format
- ✅ Type-safe error handling
