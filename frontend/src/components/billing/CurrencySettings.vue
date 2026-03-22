<template>
  <div class="currency-settings">
    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-6">
        💱 Currency Settings
      </h3>

      <!-- Loading State -->
      <div v-if="currencyStore.loading" class="text-center py-8">
        <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
        <p class="mt-2 text-gray-600 dark:text-gray-400">Loading currency settings...</p>
      </div>

      <!-- Settings Form -->
      <div v-else class="space-y-6">
        <!-- Display Currency -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Display Currency
          </label>
          <div class="grid grid-cols-2 gap-3">
            <button
              v-for="currency in currencyStore.supportedCurrencies"
              :key="currency.code"
              @click="updateDisplayCurrency(currency.code)"
              :class="[
                'p-3 rounded-lg border-2 transition-all duration-200',
                currencyStore.displayCurrency === currency.code
                  ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20'
                  : 'border-gray-300 dark:border-gray-600 hover:border-gray-400 dark:hover:border-gray-500'
              ]"
            >
              <div class="text-lg font-bold">{{ currency.symbol }}</div>
              <div class="text-sm text-gray-600 dark:text-gray-400">{{ currency.displayName }}</div>
              <div class="text-xs text-gray-500 dark:text-gray-500">{{ currency.code }}</div>
            </button>
          </div>
        </div>

        <!-- Payment Currency -->
        <div>
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Payment Currency
          </label>
          <div class="grid grid-cols-2 gap-3">
            <button
              v-for="currency in currencyStore.supportedCurrencies"
              :key="currency.code"
              @click="updatePaymentCurrency(currency.code)"
              :class="[
                'p-3 rounded-lg border-2 transition-all duration-200',
                currencyStore.paymentCurrency === currency.code
                  ? 'border-green-500 bg-green-50 dark:bg-green-900/20'
                  : 'border-gray-300 dark:border-gray-600 hover:border-gray-400 dark:hover:border-gray-500'
              ]"
            >
              <div class="text-lg font-bold">{{ currency.symbol }}</div>
              <div class="text-sm text-gray-600 dark:text-gray-400">{{ currency.displayName }}</div>
              <div class="text-xs text-gray-500 dark:text-gray-500">{{ currency.code }}</div>
            </button>
          </div>
        </div>

        <!-- Options -->
        <div class="space-y-4">
          <div class="flex items-center justify-between">
            <div>
              <label class="text-sm font-medium text-gray-700 dark:text-gray-300">
                Auto Convert Prices
              </label>
              <p class="text-xs text-gray-500 dark:text-gray-400">
                Automatically convert prices to your display currency
              </p>
            </div>
            <button
              @click="toggleAutoConvert"
              :class="[
                'relative inline-flex h-6 w-11 items-center rounded-full transition-colors',
                currencyStore.autoConvert ? 'bg-blue-600' : 'bg-gray-300 dark:bg-gray-600'
              ]"
            >
              <span
                :class="[
                  'inline-block h-4 w-4 transform rounded-full bg-white transition-transform',
                  currencyStore.autoConvert ? 'translate-x-6' : 'translate-x-1'
                ]"
              />
            </button>
          </div>

          <div class="flex items-center justify-between">
            <div>
              <label class="text-sm font-medium text-gray-700 dark:text-gray-300">
                Show Original Price
              </label>
              <p class="text-xs text-gray-500 dark:text-gray-400">
                Display original price alongside converted price
              </p>
            </div>
            <button
              @click="toggleShowOriginalPrice"
              :class="[
                'relative inline-flex h-6 w-11 items-center rounded-full transition-colors',
                currencyStore.showOriginalPrice ? 'bg-blue-600' : 'bg-gray-300 dark:bg-gray-600'
              ]"
            >
              <span
                :class="[
                  'inline-block h-4 w-4 transform rounded-full bg-white transition-transform',
                  currencyStore.showOriginalPrice ? 'translate-x-6' : 'translate-x-1'
                ]"
              />
            </button>
          </div>
        </div>

        <!-- Exchange Rate Info -->
        <div class="bg-gray-50 dark:bg-gray-700/50 rounded-lg p-4">
          <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Current Exchange Rates
          </h4>
          <div class="space-y-1 text-sm">
            <div class="flex justify-between">
              <span class="text-gray-600 dark:text-gray-400">1 USD</span>
              <span class="font-medium">
                = {{ getExchangeRate('USD', 'VND') }} VND
              </span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600 dark:text-gray-400">1 USD</span>
              <span class="font-medium">
                = {{ getExchangeRate('USD', 'EUR') }} EUR
              </span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600 dark:text-gray-400">Last updated</span>
              <span class="font-medium">{{ lastUpdated }}</span>
            </div>
          </div>
        </div>

        <!-- Preview -->
        <div class="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-4">
          <h4 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Price Preview
          </h4>
          <div class="space-y-2">
            <div class="flex justify-between items-center">
              <span class="text-gray-600 dark:text-gray-400">Starter Plan</span>
              <span class="font-bold text-lg">
                {{ currencyStore.formatPriceWithOriginal(29.99) }}
              </span>
            </div>
            <div class="flex justify-between items-center">
              <span class="text-gray-600 dark:text-gray-400">Professional Plan</span>
              <span class="font-bold text-lg">
                {{ currencyStore.formatPriceWithOriginal(99.99) }}
              </span>
            </div>
          </div>
        </div>

        <!-- Save Button -->
        <div class="flex justify-end space-x-3">
          <button
            @click="resetToDefaults"
            class="px-4 py-2 text-gray-700 bg-gray-200 dark:bg-gray-700 dark:text-gray-300 rounded-lg hover:bg-gray-300 dark:hover:bg-gray-600 transition-colors"
          >
            Reset to Defaults
          </button>
          <button
            @click="saveSettings"
            :disabled="saving"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="saving">Saving...</span>
            <span v-else>Save Changes</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useCurrencyStore } from '@/stores/currencyStore'

const currencyStore = useCurrencyStore()
const saving = ref(false)

const lastUpdated = computed(() => {
  // TODO: Get actual last updated time from API
  return '2 hours ago'
})

const getExchangeRate = (from, to) => {
  const rate = currencyStore.exchangeRate(from, to)
  return rate ? rate.toFixed(2) : '1.00'
}

const updateDisplayCurrency = (currency) => {
  currencyStore.displayCurrency = currency
}

const updatePaymentCurrency = (currency) => {
  currencyStore.paymentCurrency = currency
}

const toggleAutoConvert = () => {
  currencyStore.autoConvert = !currencyStore.autoConvert
}

const toggleShowOriginalPrice = () => {
  currencyStore.showOriginalPrice = !currencyStore.showOriginalPrice
}

const saveSettings = async () => {
  saving.value = true
  
  try {
    await currencyStore.updateCurrencySettings({
      displayCurrency: currencyStore.displayCurrency,
      paymentCurrency: currencyStore.paymentCurrency,
      autoConvert: currencyStore.autoConvert,
      showOriginalPrice: currencyStore.showOriginalPrice
    })
    
    // Show success message
    console.log('Currency settings saved successfully')
  } catch (error) {
    console.error('Failed to save currency settings:', error)
  } finally {
    saving.value = false
  }
}

const resetToDefaults = () => {
  currencyStore.displayCurrency = 'USD'
  currencyStore.paymentCurrency = 'USD'
  currencyStore.autoConvert = true
  currencyStore.showOriginalPrice = true
}

onMounted(() => {
  currencyStore.initialize()
})
</script>
