import api from './api'

export const currencyApi = {
  // Get supported currencies
  getSupportedCurrencies() {
    return api.get('/billing/currency/supported')
  },

  // Get exchange rate
  getExchangeRate(tenantKey, fromCurrency, toCurrency) {
    return api.get(`/billing/currency/rate`, {
      params: {
        tenantKey,
        from: fromCurrency,
        to: toCurrency
      }
    })
  },

  // Convert currency
  convertCurrency(tenantKey, amount, fromCurrency, toCurrency) {
    return api.post('/billing/currency/convert', null, {
      params: {
        tenantKey,
        amount,
        from: fromCurrency,
        to: toCurrency
      }
    })
  },

  // Get user currency settings
  getUserCurrencySettings(tenantKey) {
    return api.get('/billing/currency/settings', {
      params: { tenantKey }
    })
  },

  // Update user currency settings
  updateUserCurrencySettings(tenantKey, settings) {
    return api.put('/billing/currency/settings', settings, {
      params: { tenantKey }
    })
  },

  // Get all exchange rates (admin only)
  getAllExchangeRates() {
    return api.get('/billing/currency/rates')
  },

  // Update exchange rate (admin only)
  updateExchangeRate(fromCurrency, toCurrency, rate, source = 'MANUAL') {
    return api.post('/billing/currency/rates', null, {
      params: {
        from: fromCurrency,
        to: toCurrency,
        rate,
        source
      }
    })
  }
}
