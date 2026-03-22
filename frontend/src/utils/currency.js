/**
 * Safe currency formatting utility
 * @param {number} amount - The amount to format
 * @param {string} currency - The currency code (default: 'USD')
 * @returns {string} Formatted currency string
 */
export const formatCurrency = (amount, currency = 'USD') => {
  if (typeof amount !== 'number' || isNaN(amount)) {
    amount = 0
  }
  
  if (!currency || typeof currency !== 'string') {
    currency = 'USD'
  }
  
  try {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: currency === 'JPY' || currency === 'VND' ? 0 : 2,
      maximumFractionDigits: currency === 'JPY' || currency === 'VND' ? 0 : 2
    }).format(amount)
  } catch (error) {
    console.warn('Currency formatting error:', error, { amount, currency })
    // Fallback formatting
    return `${currency} ${amount.toFixed(2)}`
  }
}

/**
 * Get currency symbol
 * @param {string} currency - The currency code
 * @returns {string} Currency symbol
 */
export const getCurrencySymbol = (currency = 'USD') => {
  if (!currency || typeof currency !== 'string') {
    currency = 'USD'
  }
  
  try {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency
    }).formatToParts(0).find(part => part.type === 'currency')?.value || currency
  } catch (error) {
    console.warn('Currency symbol error:', error, { currency })
    return currency
  }
}

/**
 * Validate currency code
 * @param {string} currency - The currency code to validate
 * @returns {boolean} True if valid
 */
export const isValidCurrency = (currency) => {
  if (!currency || typeof currency !== 'string') {
    return false
  }
  
  try {
    new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency
    })
    return true
  } catch (error) {
    return false
  }
}
