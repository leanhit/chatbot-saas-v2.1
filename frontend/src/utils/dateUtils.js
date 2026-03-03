/**
 * Date utility functions for consistent date formatting across the application
 * Matches backend DateUtils constants - ISO 8601 UTC Standard
 */

// Backend constants from DateUtils.java - Updated for consistency
export const API_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"
export const API_TIMEZONE = "UTC"
export const ISO_DATETIME_WITH_ZONE = "yyyy-MM-dd'T'HH:mm:ssXXX"
export const ISO_DATETIME_WITH_MILLIS = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX"

/**
 * Convert datetime-local input value to ISO string for API
 * @param {string} dateTimeLocalValue - DateTime-local input value (YYYY-MM-DDTHH:mm)
 * @returns {string} ISO string for API or null if invalid
 */
export function dateTimeLocalToIso(dateTimeLocalValue) {
  if (!dateTimeLocalValue) return null
  
  try {
    const date = new Date(dateTimeLocalValue)
    if (isNaN(date.getTime())) return null
    
    // Return ISO 8601 with timezone (Z for UTC) - consistent with backend
    return date.toISOString()
  } catch (e) {
    console.warn('DateTime-local to ISO conversion error:', dateTimeLocalValue, e)
    return null
  }
}

/**
 * Format backend ISO datetime to local datetime input
 * @param {string|Date|Array} dateString - Date string, Date object, or array from gRPC
 * @returns {string} Date in YYYY-MM-DDTHH:mm format for datetime-local input
 */
export function formatDateTimeLocal(dateString) {
  console.log('formatDateTimeLocal input:', dateString, typeof dateString);
  if (!dateString && dateString !== 0) return ''
  
  try {
    let date
    
    // Handle gRPC array format: [year, month, day, hour, minute, second, nanosecond]
    if (Array.isArray(dateString)) {
      const [year, month, day, hour, minute, second] = dateString
      // gRPC months are 1-based, JS months are 0-based
      date = new Date(year, month - 1, day, hour || 0, minute || 0, second || 0)
    } else if (typeof dateString === 'number') {
      // Handle Unix timestamp (could be seconds or milliseconds)
      // If timestamp is in seconds (less than year 2000), convert to milliseconds
      const timestamp = dateString < 10000000000 ? dateString * 1000 : dateString
      console.log('timestamp after conversion:', timestamp);
      date = new Date(timestamp)
    } else {
      // Parse ISO 8601 from backend (should be UTC)
      date = new Date(dateString)
    }
    
    console.log('date object:', date, 'isValid:', !isNaN(date.getTime()));
    
    if (isNaN(date.getTime())) return ''
    
    // Handle special case for timestamp 0 (no expiry)
    if (date.getTime() === 0) {
      console.log('timestamp is 0, returning empty string');
      return ''
    }
    
    // Convert to local time and format as YYYY-MM-DDTHH:mm
    const localDate = new Date(date.getTime() - (date.getTimezoneOffset() * 60000))
    const result = localDate.toISOString().slice(0, 16)
    console.log('formatDateTimeLocal result:', result);
    return result
  } catch (e) {
    console.warn('DateTime-local formatting error:', dateString, e)
    return ''
  }
}

/**
 * Parse backend datetime response to local Date object
 * @param {string} isoDateTime - ISO datetime string from backend
 * @returns {Date} Local Date object
 */
export function parseBackendDateTime(isoDateTime) {
  if (!isoDateTime) return null
  
  try {
    // Backend should send ISO 8601 UTC format
    return new Date(isoDateTime)
  } catch (e) {
    console.warn('Backend datetime parsing error:', isoDateTime, e)
    return null
  }
}

/**
 * Format local Date to backend ISO datetime
 * @param {Date} localDate - Local Date object
 * @returns {string} ISO datetime string for backend
 */
export function formatToBackendDateTime(localDate) {
  if (!localDate) return null
  
  try {
    // Convert to ISO 8601 UTC for backend
    return localDate.toISOString()
  } catch (e) {
    console.warn('Backend datetime formatting error:', localDate, e)
    return null
  }
}

/**
 * Format date for display in UI
 * @param {string|Date|Array} dateString - Date string, Date object, or array from gRPC
 * @param {Object} options - Formatting options
 * @returns {string} Formatted date string
 */
export function formatDate(dateString, options = {}) {
  if (!dateString && dateString !== 0) return options.fallback || 'N/A'
  
  try {
    let date
    
    // Handle gRPC array format: [year, month, day, hour, minute, second, nanosecond]
    if (Array.isArray(dateString)) {
      const [year, month, day, hour, minute, second] = dateString
      // gRPC months are 1-based, JS months are 0-based
      date = new Date(year, month - 1, day, hour || 0, minute || 0, second || 0)
    } else if (typeof dateString === 'number') {
      // Handle Unix timestamp (could be seconds or milliseconds)
      // If timestamp is in seconds (less than year 2000), convert to milliseconds
      const timestamp = dateString < 10000000000 ? dateString * 1000 : dateString
      date = new Date(timestamp)
    } else {
      date = new Date(dateString)
    }
    
    if (isNaN(date.getTime())) return options.fallback || 'Invalid Date'
    
    // Handle special case for timestamp 0 (no expiry)
    if (date.getTime() === 0) {
      return options.fallback || 'No expiry'
    }
    
    const defaultOptions = {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    }
    
    return date.toLocaleDateString('en-US', { ...defaultOptions, ...options })
  } catch (e) {
    console.warn('Date formatting error:', dateString, e)
    return options.fallback || 'Invalid Date'
  }
}

/**
 * Format date with time
 * @param {string|Date|Array} dateString - Date string, Date object, or array from gRPC
 * @param {Object} options - Formatting options
 * @returns {string} Formatted date-time string
 */
export function formatDateTime(dateString, options = {}) {
  if (!dateString && dateString !== 0) return options.fallback || 'N/A'
  
  try {
    let date
    
    // Handle gRPC array format: [year, month, day, hour, minute, second, nanosecond]
    if (Array.isArray(dateString)) {
      const [year, month, day, hour, minute, second] = dateString
      // gRPC months are 1-based, JS months are 0-based
      date = new Date(year, month - 1, day, hour || 0, minute || 0, second || 0)
    } else if (typeof dateString === 'number') {
      // Handle Unix timestamp (could be seconds or milliseconds)
      // If timestamp is in seconds (less than year 2000), convert to milliseconds
      const timestamp = dateString < 10000000000 ? dateString * 1000 : dateString
      date = new Date(timestamp)
    } else {
      date = new Date(dateString)
    }
    
    if (isNaN(date.getTime())) return options.fallback || 'Invalid Date'
    
    // Handle special case for timestamp 0 (no expiry)
    if (date.getTime() === 0) {
      return options.fallback || 'No expiry'
    }
    
    const defaultOptions = {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    }
    
    return date.toLocaleString('en-US', { ...defaultOptions, ...options })
  } catch (e) {
    console.warn('Date-time formatting error:', dateString, e)
    return options.fallback || 'Invalid Date'
  }
}

/**
 * Check if date is valid
 * @param {string|Date|Array} dateString - Date string, Date object, or array from gRPC
 * @returns {boolean} True if date is valid
 */
export function isValidDate(dateString) {
  if (!dateString) return false
  
  try {
    let date
    
    // Handle gRPC array format: [year, month, day, hour, minute, second, nanosecond]
    if (Array.isArray(dateString)) {
      const [year, month, day, hour, minute, second] = dateString
      // gRPC months are 1-based, JS months are 0-based
      date = new Date(year, month - 1, day, hour || 0, minute || 0, second || 0)
    } else {
      date = new Date(dateString)
    }
    
    return !isNaN(date.getTime())
  } catch (e) {
    return false
  }
}

/**
 * Get relative time (e.g., "2 hours ago", "3 days ago")
 * @param {string|Date} dateString - Date string or Date object
 * @returns {string} Relative time string
 */
export function getRelativeTime(dateString) {
  if (!dateString) return 'N/A'
  
  try {
    let date
    
    if (typeof dateString === 'number') {
      // Handle Unix timestamp (could be seconds or milliseconds)
      // If timestamp is in seconds (less than year 2000), convert to milliseconds
      const timestamp = dateString < 10000000000 ? dateString * 1000 : dateString
      date = new Date(timestamp)
    } else {
      date = new Date(dateString)
    }
    
    if (isNaN(date.getTime())) return 'Invalid Date'
    
    // Handle special case for timestamp 0 (no expiry)
    if (date.getTime() === 0) {
      return 'Never expires'
    }
    
    const now = new Date()
    const diffMs = now - date
    const diffMins = Math.floor(diffMs / 60000)
    const diffHours = Math.floor(diffMins / 60)
    const diffDays = Math.floor(diffHours / 24)
    
    if (diffMins < 1) return 'Just now'
    if (diffMins < 60) return `${diffMins} minute${diffMins > 1 ? 's' : ''} ago`
    if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`
    if (diffDays < 7) return `${diffDays} day${diffDays > 1 ? 's' : ''} ago`
    
    return formatDate(dateString)
  } catch (e) {
    console.warn('Relative time error:', dateString, e)
    return 'Invalid Date'
  }
}

/**
 * Format currency amount
 * @param {number} amount - Amount to format
 * @param {string} currency - Currency code (default: USD)
 * @returns {string} Formatted currency string
 */
export function formatCurrency(amount, currency = 'USD') {
  if (typeof amount !== 'number') return 'N/A'
  
  try {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency
    }).format(amount)
  } catch (e) {
    console.warn('Currency formatting error:', amount, e)
    return amount.toString()
  }
}

/**
 * Check if date is expiring soon (within 3 days)
 * @param {string|Date} dateString - Date string or Date object
 * @returns {boolean} True if expiring within 3 days
 */
export function isExpiringSoon(dateString) {
  if (!dateString) return false
  
  try {
    const expiryDate = new Date(dateString)
    const now = new Date()
    const diffTime = expiryDate - now
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
    return diffDays <= 3 && diffDays > 0
  } catch (e) {
    console.warn('Expiry check error:', dateString, e)
    return false
  }
}

/**
 * Format time as HH:MM format
 * @param {string|Date|number} timestamp - Timestamp
 * @returns {string} Formatted time string
 */
export function formatTime(timestamp) {
  if (!timestamp) return ''
  
  try {
    return new Date(timestamp).toLocaleTimeString([], {
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (e) {
    console.warn('Time formatting error:', timestamp, e)
    return ''
  }
}
