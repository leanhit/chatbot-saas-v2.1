<template>
  <div class="payment-methods-page min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- Header -->
    <div class="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Payment Methods</h1>
            <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
              Manage your payment methods and billing preferences
            </p>
          </div>
          <div class="flex items-center space-x-3">
            <button
              @click="showAddPaymentModal = true"
              class="flex items-center px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
            >
              <Icon icon="mdi:plus" class="w-4 h-4 mr-2" />
              Add Payment Method
            </button>
            <button
              @click="$router.push('/billing/overview')"
              class="flex items-center px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <Icon icon="mdi:arrow-left" class="w-4 h-4 mr-2" />
              Back to Overview
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Payment Methods List -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white">
              Your Payment Methods
            </h2>
            <span class="text-sm text-gray-500 dark:text-gray-400">
              {{ paymentMethods.length }} methods
            </span>
          </div>
        </div>
        
        <div class="divide-y divide-gray-200 dark:divide-gray-700">
          <!-- Loading State -->
          <div v-if="loading" class="p-8 text-center">
            <Icon icon="mdi:loading" class="w-8 h-8 animate-spin text-primary-600 mx-auto mb-3" />
            <p class="text-gray-500 dark:text-gray-400">Loading payment methods...</p>
          </div>

          <!-- Empty State -->
          <div v-else-if="paymentMethods.length === 0" class="p-8 text-center">
            <Icon icon="mdi:credit-card-outline" class="w-12 h-12 text-gray-400 mx-auto mb-3" />
            <p class="text-gray-500 dark:text-gray-400">No payment methods</p>
            <p class="text-sm text-gray-400 dark:text-gray-500 mt-1">
              Add a payment method to manage your subscriptions
            </p>
            <button
              @click="showAddPaymentModal = true"
              class="mt-4 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
            >
              Add Payment Method
            </button>
          </div>

          <!-- Payment Methods -->
          <PaymentMethodCard
            v-for="method in paymentMethods"
            :key="method.id"
            :method="method"
            @default="handleSetDefault"
            @remove="handleRemove"
          />
        </div>
      </div>

      <!-- Billing Preferences -->
      <div class="mt-8 bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-6">Billing Preferences</h2>
        
        <div class="space-y-6">
          <!-- Default Payment Method -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Default Payment Method
            </label>
            <select
              v-model="defaultPaymentMethod"
              @change="handleDefaultChange"
              class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
            >
              <option value="">Select default payment method</option>
              <option v-for="method in paymentMethods" :key="method.id" :value="method.id">
                {{ method.type }} - {{ method.last4 }}
              </option>
            </select>
            <p class="mt-2 text-sm text-gray-500 dark:text-gray-400">
              This payment method will be used for automatic renewals
            </p>
          </div>

          <!-- Billing Notifications -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Billing Notifications
            </label>
            <div class="space-y-3">
              <label class="flex items-center">
                <input
                  v-model="preferences.emailNotifications"
                  type="checkbox"
                  class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                />
                <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">
                  Email notifications for billing events
                </span>
              </label>
              <label class="flex items-center">
                <input
                  v-model="preferences.renewalReminders"
                  type="checkbox"
                  class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                />
                <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">
                  Renewal reminders (7 days before)
                </span>
              </label>
              <label class="flex items-center">
                <input
                  v-model="preferences.failedPaymentAlerts"
                  type="checkbox"
                  class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                />
                <span class="ml-2 text-sm text-gray-700 dark:text-gray-300">
                  Failed payment alerts
                </span>
              </label>
            </div>
          </div>

          <!-- Billing Address -->
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Billing Address
            </label>
            <textarea
              v-model="preferences.billingAddress"
              rows="3"
              placeholder="Enter your billing address"
              class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
            ></textarea>
            <p class="mt-2 text-sm text-gray-500 dark:text-gray-400">
              This address will be used for billing invoices
            </p>
          </div>

          <!-- Save Preferences -->
          <div class="flex justify-end">
            <button
              @click="savePreferences"
              :disabled="savingPreferences"
              class="px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <Icon v-if="savingPreferences" icon="mdi:loading" class="w-4 h-4 mr-2 animate-spin" />
              {{ savingPreferences ? 'Saving...' : 'Save Preferences' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Security Information -->
      <div class="mt-8 bg-blue-50 dark:bg-blue-900/20 rounded-lg p-6">
        <div class="flex">
          <div class="flex-shrink-0">
            <Icon icon="mdi:shield-check" class="h-6 w-6 text-blue-400" />
          </div>
          <div class="ml-3">
            <h3 class="text-sm font-medium text-blue-800 dark:text-blue-200">
              Secure Payment Processing
            </h3>
            <div class="mt-2 text-sm text-blue-700 dark:text-blue-300">
              <p>Your payment information is encrypted and secure. We use industry-standard security measures to protect your data.</p>
              <ul class="mt-2 list-disc list-inside space-y-1">
                <li>PCI DSS compliant payment processing</li>
                <li>256-bit SSL encryption</li>
                <li>Payment details are tokenized and stored securely</li>
                <li>Regular security audits and monitoring</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Add Payment Method Modal -->
    <AddPaymentMethodModal
      v-if="showAddPaymentModal"
      @close="showAddPaymentModal = false"
      @add="handleAddPaymentMethod"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useBillingStore } from '@/stores/billingStore'
import PaymentMethodCard from '@/components/billing/PaymentMethodCard.vue'
import AddPaymentMethodModal from '@/components/billing/AddPaymentMethodModal.vue'

const billingStore = useBillingStore()

// State
const loading = ref(false)
const showAddPaymentModal = ref(false)
const savingPreferences = ref(false)
const defaultPaymentMethod = ref('')

// Preferences
const preferences = ref({
  emailNotifications: true,
  renewalReminders: true,
  failedPaymentAlerts: true,
  billingAddress: ''
})

// Computed
const paymentMethods = computed(() => billingStore.paymentMethods)

// Methods
const handleSetDefault = async (methodId) => {
  try {
    await billingStore.setDefaultPaymentMethod(methodId)
    defaultPaymentMethod.value = methodId
  } catch (error) {
    console.error('Failed to set default payment method:', error)
  }
}

const handleRemove = async (methodId) => {
  try {
    await billingStore.removePaymentMethod(methodId)
    
    // If the removed method was the default, clear the selection
    if (defaultPaymentMethod.value === methodId) {
      defaultPaymentMethod.value = ''
    }
  } catch (error) {
    console.error('Failed to remove payment method:', error)
  }
}

const handleAddPaymentMethod = async (paymentMethod) => {
  try {
    await billingStore.addPaymentMethod(paymentMethod)
    showAddPaymentModal.value = false
  } catch (error) {
    console.error('Failed to add payment method:', error)
  }
}

const handleDefaultChange = async () => {
  if (defaultPaymentMethod.value) {
    await handleSetDefault(defaultPaymentMethod.value)
  }
}

const savePreferences = async () => {
  savingPreferences.value = true
  
  try {
    // In a real app, this would save to backend
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // Show success message
    alert('Preferences saved successfully!')
  } catch (error) {
    console.error('Failed to save preferences:', error)
  } finally {
    savingPreferences.value = false
  }
}

// Lifecycle
onMounted(async () => {
  loading.value = true
  
  try {
    await billingStore.fetchPaymentMethods()
    
    // Set default payment method if exists
    const defaultMethod = paymentMethods.value.find(m => m.isDefault)
    if (defaultMethod) {
      defaultPaymentMethod.value = defaultMethod.id
    }
  } catch (error) {
    console.error('Failed to fetch payment methods:', error)
  } finally {
    loading.value = false
  }
})
</script>
