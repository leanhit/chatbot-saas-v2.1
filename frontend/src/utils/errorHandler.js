import i18n from '@/locales/index'

/**
 * Get error message from error code using i18n
 * @param {string} errorCode - The error code from server
 * @param {string} fallbackMessage - Fallback message if code not found
 * @returns {string} Localized error message
 */
export function getErrorMessage(errorCode, fallbackMessage = null) {
  const t = i18n.global.t
  
  if (!errorCode) {
    return fallbackMessage || t('errors.INTERNAL_ERROR')
  }
  
  // Try to get localized message for the error code
  const message = t(`errors.${errorCode}`)
  
  // If the translation key doesn't exist, i18n will return the key itself
  // In that case, use fallback or the server message
  if (message === `errors.${errorCode}`) {
    return fallbackMessage || message
  }
  
  return message
}

/**
 * Extract error code from axios error response
 * @param {Error} error - Axios error object
 * @returns {string|null} Error code or null
 */
export function extractErrorCode(error) {
  if (!error || !error.response) {
    return 'NETWORK_ERROR';
  }

  if (error?.response?.data?.code) {
    return error.response.data.code
  }
  
  // Check for errorCode field (used by some handlers)
  if (error?.response?.data?.errorCode) {
    return error.response.data.errorCode
  }
  
  // Map HTTP status codes to error codes
  if (error?.response?.status) {
    const statusMap = {
      400: 'BAD_REQUEST',
      401: 'UNAUTHORIZED',
      403: 'FORBIDDEN',
      404: 'NOT_FOUND',
      409: 'CONFLICT',
      413: 'PAYLOAD_TOO_LARGE',
      415: 'UNSUPPORTED_MEDIA_TYPE',
      405: 'METHOD_NOT_ALLOWED',
      408: 'TIMEOUT',
      429: 'RATE_LIMIT_EXCEEDED',
      500: 'INTERNAL_ERROR',
      502: 'SERVICE_UNAVAILABLE',
      503: 'SERVICE_UNAVAILABLE',
      504: 'TIMEOUT'
    }
    return statusMap[error.response.status] || null
  }
  
  return null
}

/**
 * Get user-friendly error message from axios error
 * @param {Error} error - Axios error object
 * @param {string} fallbackMessage - Fallback message
 * @returns {string} Localized error message
 */
export function getErrorFromResponse(error, fallbackMessage = null) {
  const errorCode = extractErrorCode(error)
  const serverMessage = error?.response?.data?.message
  
  // Use server message as fallback if available
  const finalFallback = fallbackMessage || serverMessage || null
  
  return getErrorMessage(errorCode, finalFallback)
}

/**
 * Handle API error and return appropriate message
 * This is a convenience function for use in components
 * @param {Error} error - Error object
 * @param {Object} options - Options
 * @param {string} options.fallback - Fallback message
 * @param {boolean} options.logError - Whether to log error to console
 * @returns {string} Error message
 */
export function handleApiError(error, options = {}) {
  const { fallback = null, logError = true } = options
  
  if (logError && error) {
    console.error('API Error:', error)
  }
  
  return getErrorFromResponse(error, fallback)
}

/**
 * Check if error is a specific error code
 * @param {Error} error - Error object
 * @param {string} errorCode - Error code to check
 * @returns {boolean}
 */
export function isErrorCode(error, errorCode) {
  return extractErrorCode(error) === errorCode
}

/**
 * Check if error is authentication related
 * @param {Error} error - Error object
 * @returns {boolean}
 */
export function isAuthError(error) {
  const code = extractErrorCode(error)
  return code === 'UNAUTHORIZED' || 
         code === 'BAD_CREDENTIALS' || 
         code === 'AUTHENTICATION_FAILED' ||
         code === 'INVALID_TOKEN' ||
         code === 'TOKEN_INVALID_OR_EXPIRED' ||
         code === 'REFRESH_TOKEN_EXPIRED' ||
         code === 'USER_NOT_AUTHENTICATED'
}

/**
 * Check if error is validation error
 * @param {Error} error - Error object
 * @returns {boolean}
 */
export function isValidationError(error) {
  return extractErrorCode(error) === 'VALIDATION_ERROR'
}

/**
 * Get validation errors from response
 * @param {Error} error - Error object
 * @returns {Array<string>} Array of validation error messages
 */
export function getValidationErrors(error) {
  if (error?.response?.data?.errors) {
    return error.response.data.errors
  }
  
  if (error?.response?.data?.message && typeof error.response.data.message === 'object') {
    // Handle nested validation errors
    return Object.values(error.response.data.message).flat()
  }
  
  return []
}

/**
 * Check if error is file validation error
 * @param {Error} error - Error object
 * @returns {boolean}
 */
export function isFileValidationError(error) {
  const code = extractErrorCode(error)
  return code === 'INVALID_FILE_TYPE' ||
         code === 'FILE_TOO_LARGE' ||
         code === 'FILE_EMPTY' ||
         code === 'FILE_NULL'
}

/**
 * Check if error is agent validation error
 * @param {Error} error - Error object
 * @returns {boolean}
 */
export function isAgentValidationError(error) {
  const code = extractErrorCode(error)
  return code === 'INVALID_AGENT_NAME' ||
         code === 'INVALID_AGENT_EMAIL' ||
         code === 'INVALID_AGENT_ROLE' ||
         code === 'INVALID_AGENT_MAX_CONCURRENT' ||
         code === 'INVALID_AGENT_CURRENT_LOAD' ||
         code === 'AGENT_TENANT_ID_REQUIRED'
}

/**
 * Check if error is webhook validation error
 * @param {Error} error - Error object
 * @returns {boolean}
 */
export function isWebhookValidationError(error) {
  const code = extractErrorCode(error)
  return code === 'WEBHOOK_URL_EXISTS' ||
         code === 'INVALID_WEBHOOK_URL' ||
         code === 'WEBHOOK_SIGNATURE_ERROR' ||
         code === 'WEBHOOK_TEST_FAILED'
}

/**
 * Check if error is package validation error
 * @param {Error} error - Error object
 * @returns {boolean}
 */
export function isPackageValidationError(error) {
  const code = extractErrorCode(error)
  return code === 'PACKAGE_ID_EXISTS' ||
         code === 'PACKAGE_NOT_ACTIVE'
}

/**
 * Check if error is discount validation error
 * @param {Error} error - Error object
 * @returns {boolean}
 */
export function isDiscountValidationError(error) {
  return extractErrorCode(error) === 'DISCOUNT_CODE_EXISTS'
}

/**
 * Check if error is tenant-related error
 * @param {Error} error - Error object
 * @returns {boolean}
 */
export function isTenantError(error) {
  const code = extractErrorCode(error)
  return code === 'TENANT_NOT_FOUND' ||
         code === 'TENANT_INACTIVE' ||
         code === 'TENANT_CONTEXT_MISSING' ||
         code === 'NOT_TENANT_MEMBER' ||
         code === 'CANNOT_ACCESS_TENANT'
}

/**
 * Check if error is payment-related error
 * @param {Error} error - Error object
 * @returns {boolean}
 */
export function isPaymentError(error) {
  const code = extractErrorCode(error)
  return code === 'PAYMENT_ERROR' ||
         code === 'PAYMENT_NOT_FOUND' ||
         code === 'PAYMENT_EXPIRED' ||
         code === 'INVALID_PAYMENT_AMOUNT' ||
         code === 'INSUFFICIENT_BALANCE' ||
         code === 'BANK_API_ERROR'
}

/**
 * Get file error details for user-friendly message
 * @param {Error} error - Error object
 * @returns {Object|null} File error details or null
 */
export function getFileErrorDetails(error) {
  if (!isFileValidationError(error)) return null
  
  const code = extractErrorCode(error)
  const details = error?.response?.data?.details || {}
  
  switch (code) {
    case 'INVALID_FILE_TYPE':
      return {
        type: 'invalid_type',
        allowedTypes: details.allowedTypes || 'image/*',
        message: getErrorMessage(code, 'Invalid file type')
      }
    case 'FILE_TOO_LARGE':
      return {
        type: 'too_large',
        maxSize: details.maxSize || 10485760,
        message: getErrorMessage(code, 'File size exceeds limit')
      }
    case 'FILE_EMPTY':
      return {
        type: 'empty',
        message: getErrorMessage(code, 'File is empty')
      }
    case 'FILE_NULL':
      return {
        type: 'null',
        message: getErrorMessage(code, 'File cannot be null')
      }
    default:
      return null
  }
}

/**
 * Log error with correlation ID for debugging
 * @param {Error} error - Error object
 * @param {string} context - Additional context
 */
export function logErrorWithCorrelation(error, context = '') {
  const correlationId = error?.response?.data?.correlationId
  const requestId = error?.response?.data?.requestId
  const code = extractErrorCode(error)
  
  console.error('API Error:', {
    context,
    code,
    correlationId,
    requestId,
    message: error?.response?.data?.message,
    timestamp: error?.response?.data?.timestamp
  })
}
