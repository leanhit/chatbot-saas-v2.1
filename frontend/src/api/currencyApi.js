import axios from '@/plugins/axios'

export const currencyApi = {
  // Get supported currencies
  getSupportedCurrencies() {
    return axios.get('/billing/currency/supported')
  },

  // Get exchange rate
  getExchangeRate(tenantKey, fromCurrency, toCurrency) {
    return axios.get(`/billing/currency/rate`, {
      params: {
        tenantKey,
        from: fromCurrency,
        to: toCurrency
      }
    })
  },

  // Convert currency
  convertCurrency(tenantKey, amount, fromCurrency, toCurrency) {
    return axios.post('/billing/currency/convert', null, {
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
    return axios.get('/billing/currency/settings', {
      params: { tenantKey }
    })
  },

  // Update user currency settings
  updateUserCurrencySettings(tenantKey, settings) {
    return axios.put('/billing/currency/settings', settings, {
      params: { tenantKey }
    })
  },

  // Get all exchange rates (admin only)
  getAllExchangeRates() {
    return axios.get('/billing/currency/rates')
  },

  // Update exchange rate (admin only)
  updateExchangeRate(fromCurrency, toCurrency, rate, source = 'MANUAL') {
    return axios.post('/billing/currency/rates', null, {
      params: {
        from: fromCurrency,
        to: toCurrency,
        rate,
        source
      }
    })
  }
}
