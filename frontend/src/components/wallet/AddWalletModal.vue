<template>
  <div class="fixed inset-0 z-50 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
      <!-- Background overlay -->
      <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" @click="$emit('close')"></div>

      <!-- Modal panel -->
      <div class="inline-block align-bottom bg-white dark:bg-gray-800 rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-md sm:w-full">
        <!-- Header -->
        <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-lg leading-6 font-medium text-gray-900 dark:text-white">
                Add New Wallet
              </h3>
              <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
                Create a new wallet for different currency
              </p>
            </div>
            <button
              @click="$emit('close')"
              class="text-gray-400 hover:text-gray-500 dark:hover:text-gray-300"
            >
              <Icon icon="mdi:close" class="w-6 h-6" />
            </button>
          </div>
        </div>

        <!-- Body -->
        <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <form @submit.prevent="handleSubmit">
            <!-- Currency Selection -->
            <div class="mb-6">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Currency
              </label>
              <div class="grid grid-cols-2 gap-3">
                <button
                  v-for="currency in availableCurrencies"
                  :key="currency.code"
                  type="button"
                  @click="selectedCurrency = currency.code"
                  :class="[
                    'p-3 rounded-lg border-2 transition-colors',
                    selectedCurrency === currency.code
                      ? 'border-primary-500 bg-primary-50 dark:bg-primary-900/20'
                      : 'border-gray-200 dark:border-gray-600 hover:border-gray-300 dark:hover:border-gray-500'
                  ]"
                >
                  <div class="text-center">
                    <Icon :icon="currency.icon" class="w-6 h-6 mx-auto mb-1" :class="selectedCurrency === currency.code ? 'text-primary-600 dark:text-primary-400' : 'text-gray-600 dark:text-gray-400'" />
                    <p class="text-sm font-medium" :class="selectedCurrency === currency.code ? 'text-primary-600 dark:text-primary-400' : 'text-gray-700 dark:text-gray-300'">
                      {{ currency.code }}
                    </p>
                    <p class="text-xs text-gray-500 dark:text-gray-400">{{ currency.name }}</p>
                  </div>
                </button>
              </div>
            </div>

            <!-- Wallet Name (Optional) -->
            <div class="mb-6">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Wallet Name (Optional)
              </label>
              <input
                v-model="walletName"
                type="text"
                placeholder="My {{ selectedCurrency }} Wallet"
                class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
              />
            </div>

            <!-- Initial Balance (Optional) -->
            <div class="mb-6">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Initial Balance (Optional)
              </label>
              <div class="relative">
                <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                  <span class="text-gray-500 dark:text-gray-400 sm:text-sm">{{ selectedCurrency }}</span>
                </div>
                <input
                  v-model.number="initialBalance"
                  type="number"
                  step="0.01"
                  min="0"
                  placeholder="0.00"
                  class="block w-full pl-12 pr-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
                />
              </div>
            </div>

            <!-- Info -->
            <div class="mb-6 p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
              <div class="flex">
                <div class="flex-shrink-0">
                  <Icon icon="mdi:information-outline" class="h-5 w-5 text-blue-400" />
                </div>
                <div class="ml-3">
                  <p class="text-sm text-blue-800 dark:text-blue-200">
                    You can top up this wallet later using your preferred payment method. Each currency wallet is managed separately.
                  </p>
                </div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex space-x-3">
              <button
                type="button"
                @click="$emit('close')"
                class="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
              >
                Cancel
              </button>
              <button
                type="submit"
                :disabled="loading || !selectedCurrency"
                class="flex-1 px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <Icon v-if="loading" icon="mdi:loading" class="w-4 h-4 mr-2 animate-spin" />
                {{ loading ? 'Creating...' : 'Create Wallet' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useWalletStore } from '@/stores/walletStore'
import { useAuthStore } from '@/stores/authStore'
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'

const emit = defineEmits(['close', 'success'])

const walletStore = useWalletStore()
const authStore = useAuthStore()
const tenantStore = useGatewayTenantStore()

// State
const selectedCurrency = ref('')
const walletName = ref('')
const initialBalance = ref(0)
const loading = ref(false)

// Available currencies
const availableCurrencies = [
  { code: 'USD', name: 'US Dollar', icon: 'mdi:currency-usd' },
  { code: 'EUR', name: 'Euro', icon: 'mdi:currency-eur' },
  { code: 'GBP', name: 'British Pound', icon: 'mdi:currency-gbp' },
  { code: 'JPY', name: 'Japanese Yen', icon: 'mdi:currency-jpy' },
  { code: 'VND', name: 'Vietnamese Dong', icon: 'mdi:currency-sign' }
]

// Methods
const handleSubmit = async () => {
  if (!selectedCurrency.value) return

  loading.value = true

  try {
    const walletData = {
      currency: selectedCurrency.value,
      name: walletName.value || `My ${selectedCurrency.value} Wallet`,
      userId: authStore.userId,
      tenantId: tenantStore.currentTenant?.id
    }

    const wallet = await walletStore.createWallet(walletData)
    
    // If initial balance > 0, create a topup transaction
    if (initialBalance.value > 0) {
      await walletStore.topup(wallet.id, initialBalance.value, 'system')
    }

    emit('success', wallet)
  } catch (error) {
    console.error('Failed to create wallet:', error)
    // You might want to show an error message here
  } finally {
    loading.value = false
  }
}
</script>
