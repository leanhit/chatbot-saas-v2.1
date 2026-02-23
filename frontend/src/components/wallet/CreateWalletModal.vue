<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg max-w-md w-full mx-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-gray-100">
            Create New Wallet
          </h3>
          <button 
            @click="$emit('close')"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="ic:baseline-close" class="text-xl" />
          </button>
        </div>
      </div>

      <!-- Body -->
      <div class="p-6">
        <!-- Wallet Type Selection -->
        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Wallet Type
          </label>
          <div class="grid grid-cols-2 gap-3">
            <button 
              v-for="type in walletTypes" 
              :key="type.value"
              @click="walletType = type.value"
              :class="[
                'p-4 rounded-lg border-2 font-medium transition-colors',
                walletType === type.value 
                  ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400' 
                  : 'border-gray-200 dark:border-gray-600 hover:border-gray-300 dark:hover:border-gray-500'
              ]"
            >
              <Icon :icon="type.icon" class="text-2xl mb-2" />
              <p class="text-sm font-medium">{{ type.label }}</p>
              <p class="text-xs text-gray-600 dark:text-gray-400 mt-1">{{ type.description }}</p>
            </button>
          </div>
        </div>

        <!-- Wallet Name -->
        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Wallet Name
          </label>
          <input 
            v-model="walletName"
            type="text"
            placeholder="Enter wallet name"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
          <p v-if="errors.walletName" class="text-red-600 dark:text-red-400 text-sm mt-1">
            {{ errors.walletName }}
          </p>
        </div>

        <!-- Currency Selection -->
        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Currency
          </label>
          <select 
            v-model="selectedCurrency"
            class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          >
            <option v-for="currency in currencies" :key="currency.code" :value="currency.code">
              {{ currency.code }} - {{ currency.name }}
            </option>
          </select>
        </div>

        <!-- Initial Balance -->
        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Initial Balance (Optional)
          </label>
          <div class="relative">
            <span class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500 text-lg">$</span>
            <input 
              v-model="initialBalance"
              type="number"
              placeholder="0.00"
              min="0"
              step="0.01"
              class="w-full pl-8 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
            />
          </div>
          <p class="text-xs text-gray-600 dark:text-gray-400 mt-1">
            Leave empty to start with $0 balance
          </p>
        </div>

        <!-- Auto Top-up Settings -->
        <div class="mb-6">
          <label class="flex items-center gap-2 mb-3">
            <input 
              type="checkbox" 
              v-model="enableAutoTopup"
              class="rounded"
            />
            <span class="text-sm font-medium text-gray-700 dark:text-gray-300">
              Enable Auto Top-up
            </span>
          </label>
          
          <div v-if="enableAutoTopup" class="space-y-4 pl-6">
            <div>
              <label class="block text-sm text-gray-600 dark:text-gray-400 mb-2">
                When balance falls below:
              </label>
              <div class="relative">
                <span class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500 text-lg">$</span>
                <input 
                  v-model="autoTopupThreshold"
                  type="number"
                  placeholder="10.00"
                  min="1"
                  step="0.01"
                  class="w-full pl-8 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
            </div>
            
            <div>
              <label class="block text-sm text-gray-600 dark:text-gray-400 mb-2">
                Top-up amount:
              </label>
              <div class="relative">
                <span class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500 text-lg">$</span>
                <input 
                  v-model="autoTopupAmount"
                  type="number"
                  placeholder="50.00"
                  min="1"
                  step="0.01"
                  class="w-full pl-8 pr-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- Wallet Features -->
        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Wallet Features
          </label>
          <div class="space-y-3">
            <label class="flex items-center gap-2">
              <input 
                type="checkbox" 
                v-model="features.notifications"
                class="rounded"
              />
              <span class="text-sm text-gray-700 dark:text-gray-300">
                Email notifications for transactions
              </span>
            </label>
            
            <label class="flex items-center gap-2">
              <input 
                type="checkbox" 
                v-model="features.weeklyReports"
                class="rounded"
              />
              <span class="text-sm text-gray-700 dark:text-gray-300">
                Weekly spending reports
              </span>
            </label>
            
            <label class="flex items-center gap-2">
              <input 
                type="checkbox" 
                v-model="features.spendingLimits"
                class="rounded"
              />
              <span class="text-sm text-gray-700 dark:text-gray-300">
                Set spending limits
              </span>
            </label>
          </div>
        </div>

        <!-- Terms and Conditions -->
        <div class="mb-6">
          <label class="flex items-start gap-2">
            <input 
              type="checkbox" 
              v-model="agreeToTerms"
              class="mt-1"
            />
            <span class="text-sm text-gray-600 dark:text-gray-400">
              I agree to the 
              <a href="#" class="text-blue-600 dark:text-blue-400 hover:underline">Wallet Terms of Service</a>
              and 
              <a href="#" class="text-blue-600 dark:text-blue-400 hover:underline">Privacy Policy</a>
            </span>
          </label>
        </div>
      </div>

      <!-- Footer -->
      <div class="p-6 border-t border-gray-200 dark:border-gray-700">
        <div class="flex gap-3">
          <button 
            @click="$emit('close')"
            class="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
          >
            Cancel
          </button>
          <button 
            @click="createWallet"
            :disabled="!canCreate"
            class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center gap-2"
          >
            <Icon v-if="isCreating" icon="ic:baseline-refresh" class="animate-spin" />
            {{ isCreating ? 'Creating...' : 'Create Wallet' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useWalletStore } from '@/stores/wallet/walletStore'

const emit = defineEmits(['close', 'created'])

const walletStore = useWalletStore()

// State
const walletType = ref('PERSONAL')
const walletName = ref('')
const selectedCurrency = ref('USD')
const initialBalance = ref('')
const enableAutoTopup = ref(false)
const autoTopupThreshold = ref('')
const autoTopupAmount = ref('')
const agreeToTerms = ref(false)
const isCreating = ref(false)

const errors = ref({
  walletName: ''
})

const features = ref({
  notifications: true,
  weeklyReports: false,
  spendingLimits: false
})

// Wallet types
const walletTypes = [
  {
    value: 'PERSONAL',
    label: 'Personal',
    description: 'For personal use',
    icon: 'ic:baseline-person'
  },
  {
    value: 'BUSINESS',
    label: 'Business',
    description: 'For business expenses',
    icon: 'ic:baseline-business'
  },
  {
    value: 'SAVINGS',
    label: 'Savings',
    description: 'For saving goals',
    icon: 'ic:baseline-savings'
  },
  {
    value: 'SHARED',
    label: 'Shared',
    description: 'For shared expenses',
    icon: 'ic:baseline-groups'
  }
]

// Available currencies
const currencies = [
  { code: 'USD', name: 'US Dollar' },
  { code: 'EUR', name: 'Euro' },
  { code: 'GBP', name: 'British Pound' },
  { code: 'JPY', name: 'Japanese Yen' },
  { code: 'CAD', name: 'Canadian Dollar' },
  { code: 'AUD', name: 'Australian Dollar' }
]

// Computed
const canCreate = computed(() => {
  return walletName.value.trim() !== '' && 
         agreeToTerms.value && 
         !errors.value.walletName && 
         !isCreating.value
})

// Methods
const validateForm = () => {
  errors.value.walletName = ''
  
  if (walletName.value.trim() === '') {
    errors.value.walletName = 'Wallet name is required'
  } else if (walletName.value.length < 2) {
    errors.value.walletName = 'Wallet name must be at least 2 characters'
  } else if (walletName.value.length > 50) {
    errors.value.walletName = 'Wallet name must be less than 50 characters'
  }
}

const createWallet = async () => {
  validateForm()
  
  if (!canCreate.value) return
  
  isCreating.value = true
  
  try {
    const walletData = {
      name: walletName.value.trim(),
      type: walletType.value,
      currency: selectedCurrency.value,
      initialBalance: Math.round(parseFloat(initialBalance.value || '0') * 100), // Convert to cents
      autoTopup: enableAutoTopup.value ? {
        threshold: Math.round(parseFloat(autoTopupThreshold.value || '0') * 100),
        amount: Math.round(parseFloat(autoTopupAmount.value || '0') * 100)
      } : null,
      features: features.value
    }
    
    const result = await walletStore.createWallet(getCurrentTenantId(), walletData)
    
    emit('created', result)
    emit('close')
    
    // Reset form
    resetForm()
    
  } catch (error) {
    console.error('Failed to create wallet:', error)
    // Show error message
  } finally {
    isCreating.value = false
  }
}

const resetForm = () => {
  walletType.value = 'PERSONAL'
  walletName.value = ''
  selectedCurrency.value = 'USD'
  initialBalance.value = ''
  enableAutoTopup.value = false
  autoTopupThreshold.value = ''
  autoTopupAmount.value = ''
  agreeToTerms.value = false
  errors.value.walletName = ''
  features.value = {
    notifications: true,
    weeklyReports: false,
    spendingLimits: false
  }
}

const getCurrentTenantId = () => {
  return localStorage.getItem('ACTIVE_TENANT_ID')
}
</script>

<style scoped>
.modal-backdrop {
  backdrop-filter: blur(4px);
}

input[type="number"]::-webkit-inner-spin-button,
input[type="number"]::-webkit-outer-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

input[type="number"] {
  -moz-appearance: textfield;
}
</style>
