import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { billingApi } from '@/api/billingApi'

export const useCurrencyStore = defineStore('currency', () => {
  // State
  const displayCurrency = ref('USD')
  const paymentCurrency = ref('USD')
  const autoConvert = ref(true)
  const showOriginalPrice = ref(true)
  const exchangeRates = ref({})
  const supportedCurrencies = ref([])
  const loading = ref(false)
  const error = ref(null)

  // Getters
  const currentDisplayCurrency = computed(() => ({
    code: displayCurrency.value,
    symbol: getCurrencySymbol(displayCurrency.value),
    name: getCurrencyName(displayCurrency.value)
  }))

  const currentPaymentCurrency = computed(() => ({
    code: paymentCurrency.value,
    symbol: getCurrencySymbol(paymentCurrency.value),
    name: getCurrencyName(paymentCurrency.value)
  }))

  const exchangeRate = computed(() => (from, to) => {
    const key = `${from}_${to}`
    return exchangeRates.value[key] || 1
  })

  // Actions
  const fetchCurrencySettings = async () => {
    loading.value = true
    error.value = null
    
    try {
      // TODO: Get tenant key from localStorage
      const tenantKey = localStorage.getItem('ACTIVE_TENANT_ID')
      if (!tenantKey) {
        throw new Error('No tenant selected')
      }

      const response = await billingApi.getCurrencySettings(tenantKey)
      const settings = response.data

      displayCurrency.value = settings.displayCurrency || 'USD'
      paymentCurrency.value = settings.paymentCurrency || 'USD'
      autoConvert.value = settings.autoConvert !== false
      showOriginalPrice.value = settings.showOriginalPrice !== false

      // Fetch exchange rates after getting settings
      await fetchExchangeRates()
    } catch (err) {
      error.value = err.message || 'Failed to fetch currency settings'
      console.error('Error fetching currency settings:', err)
    } finally {
      loading.value = false
    }
  }

  const updateCurrencySettings = async (settings) => {
    loading.value = true
    error.value = null

    try {
      const tenantKey = localStorage.getItem('ACTIVE_TENANT_ID')
      if (!tenantKey) {
        throw new Error('No tenant selected')
      }

      await billingApi.updateCurrencySettings(tenantKey, settings)

      // Update local state
      displayCurrency.value = settings.displayCurrency || displayCurrency.value
      paymentCurrency.value = settings.paymentCurrency || paymentCurrency.value
      autoConvert.value = settings.autoConvert !== undefined ? settings.autoConvert : autoConvert.value
      showOriginalPrice.value = settings.showOriginalPrice !== undefined ? settings.showOriginalPrice : showOriginalPrice.value

    } catch (err) {
      error.value = err.message || 'Failed to update currency settings'
      console.error('Error updating currency settings:', err)
      throw err
    } finally {
      loading.value = false
    }
  }

  const fetchExchangeRates = async () => {
    try {
      const tenantKey = localStorage.getItem('ACTIVE_TENANT_ID')
      if (!tenantKey) return

      // Fetch USD to VND rate
      const usdVndResponse = await billingApi.getExchangeRate(tenantKey, 'USD', 'VND')
      exchangeRates.value['USD_VND'] = usdVndResponse.data.rate

      // Fetch VND to USD rate
      const vndUsdResponse = await billingApi.getExchangeRate(tenantKey, 'VND', 'USD')
      exchangeRates.value['VND_USD'] = vndUsdResponse.data.rate

      // Fetch other major pairs
      const pairs = [
        ['USD', 'EUR'],
        ['USD', 'GBP'],
        ['USD', 'JPY']
      ]

      for (const [from, to] of pairs) {
        try {
          const response = await billingApi.getExchangeRate(tenantKey, from, to)
          exchangeRates.value[`${from}_${to}`] = response.data.rate
          exchangeRates.value[`${to}_${from}`] = 1 / response.data.rate
        } catch (err) {
          console.warn(`Failed to fetch exchange rate for ${from}/${to}:`, err)
        }
      }
    } catch (err) {
      console.error('Error fetching exchange rates:', err)
    }
  }

  const convertAmount = (amount, fromCurrency, toCurrency) => {
    if (!amount || fromCurrency === toCurrency) {
      return amount
    }

    const rate = exchangeRate.value(fromCurrency, toCurrency)
    return amount * rate
  }

  const formatPrice = (price, currency = displayCurrency.value) => {
    if (!price) return '0'

    const convertedPrice = convertAmount(price, 'USD', currency)
    const symbol = getCurrencySymbol(currency)

    if (currency === 'VND') {
      return `${symbol}${Math.round(convertedPrice).toLocaleString('vi-VN')}`
    } else {
      return `${symbol}${convertedPrice.toFixed(2)}`
    }
  }

  const formatPriceWithOriginal = (price) => {
    if (!autoConvert.value || !showOriginalPrice.value) {
      return formatPrice(price)
    }

    const displayPrice = formatPrice(price, displayCurrency.value)
    const originalPrice = formatPrice(price, 'USD')

    return `${displayPrice} (${originalPrice})`
  }

  const fetchSupportedCurrencies = async () => {
    try {
      const response = await billingApi.getSupportedCurrencies()
      supportedCurrencies.value = response.data
    } catch (err) {
      console.error('Error fetching supported currencies:', err)
    }
  }

  // Helper functions
  const getCurrencySymbol = (currency) => {
    const symbols = {
      'USD': '$',
      'VND': '₫',
      'EUR': '€',
      'GBP': '£',
      'JPY': '¥'
    }
    return symbols[currency] || currency
  }

  const getCurrencyName = (currency) => {
    const names = {
      'USD': 'US Dollar',
      'VND': 'Vietnamese Dong',
      'EUR': 'Euro',
      'GBP': 'British Pound',
      'JPY': 'Japanese Yen'
    }
    return names[currency] || currency
  }

  const shouldConvert = (fromCurrency) => {
    return autoConvert.value && fromCurrency !== displayCurrency.value
  }

  // Initialize
  const initialize = async () => {
    await fetchSupportedCurrencies()
    await fetchCurrencySettings()
  }

  return {
    // State
    displayCurrency,
    paymentCurrency,
    autoConvert,
    showOriginalPrice,
    exchangeRates,
    supportedCurrencies,
    loading,
    error,

    // Getters
    currentDisplayCurrency,
    currentPaymentCurrency,
    exchangeRate,

    // Actions
    fetchCurrencySettings,
    updateCurrencySettings,
    fetchExchangeRates,
    convertAmount,
    formatPrice,
    formatPriceWithOriginal,
    fetchSupportedCurrencies,
    shouldConvert,
    initialize
  }
})
