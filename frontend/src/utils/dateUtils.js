/**
 * Date utility functions for consistent date formatting across the application
 * Matches backend DateUtils constants
 */

// Backend constants from DateUtils.java
export const STANDARD_JSON_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS"
export const STANDARD_TIMEZONE = "Asia/Ho_Chi_Minh"

/**
 * Format date for display in UI
 * @param {string|Date|Array} dateString - Date string, Date object, or array from gRPC
 * @param {Object} options - Formatting options
 * @returns {string} Formatted date string
 */
export function formatDate(dateString, options = {}) {
  if (!dateString) return options.fallback || 'N/A'
  
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
    
    if (isNaN(date.getTime())) return options.fallback || 'Invalid Date'
    
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
  if (!dateString) return options.fallback || 'N/A'
  
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
    
    if (isNaN(date.getTime())) return options.fallback || 'Invalid Date'
    
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
 * Format date for datetime-local input
 * @param {string|Date|Array} dateString - Date string, Date object, or array from gRPC
 * @returns {string} Date in YYYY-MM-DDTHH:mm format for datetime-local input
 */
export function formatDateTimeLocal(dateString) {
  if (!dateString) return ''
  
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
    
    if (isNaN(date.getTime())) return ''
    
    // Convert to local time and format as YYYY-MM-DDTHH:mm
    const localDate = new Date(date.getTime() - (date.getTimezoneOffset() * 60000))
    return localDate.toISOString().slice(0, 16)
  } catch (e) {
    console.warn('DateTime-local formatting error:', dateString, e)
    return ''
  }
}

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
    
    return date.toISOString()
  } catch (e) {
    console.warn('DateTime-local to ISO conversion error:', dateTimeLocalValue, e)
    return null
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
    const date = new Date(dateString)
    if (isNaN(date.getTime())) return 'Invalid Date'
    
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
