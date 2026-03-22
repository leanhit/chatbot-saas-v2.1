<template>
  <div class="fixed inset-0 z-50 overflow-y-auto">
    <div class="flex items-center justify-center min-h-screen pt-4 px-4 pb-20 text-center sm:block sm:p-0">
      <!-- Background overlay -->
      <div class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity" @click="$emit('close')"></div>

      <!-- Modal panel -->
      <div class="inline-block align-bottom bg-white dark:bg-gray-800 rounded-lg text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:align-middle sm:max-w-lg sm:w-full">
        <!-- Header -->
        <div class="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
          <div class="flex items-center justify-between">
            <div>
              <h3 class="text-lg leading-6 font-medium text-gray-900 dark:text-white">
                Add Payment Method
              </h3>
              <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
                Add a new payment method for your subscription
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
            <!-- Payment Method Type -->
            <div class="mb-6">
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Payment Method Type
              </label>
              <div class="grid grid-cols-2 gap-3">
                <button
                  v-for="type in paymentTypes"
                  :key="type.id"
                  type="button"
                  @click="selectedType = type.id"
                  :class="[
                    'p-3 rounded-lg border-2 transition-colors',
                    selectedType === type.id
                      ? 'border-primary-500 bg-primary-50 dark:bg-primary-900/20'
                      : 'border-gray-200 dark:border-gray-600 hover:border-gray-300 dark:hover:border-gray-500'
                  ]"
                >
                  <div class="text-center">
                    <Icon :icon="type.icon" class="w-6 h-6 mx-auto mb-1" :class="selectedType === type.id ? 'text-primary-600 dark:text-primary-400' : 'text-gray-600 dark:text-gray-400'" />
                    <p class="text-sm font-medium" :class="selectedType === type.id ? 'text-primary-600 dark:text-primary-400' : 'text-gray-700 dark:text-gray-300'">
                      {{ type.name }}
                    </p>
                  </div>
                </button>
              </div>
            </div>

            <!-- Credit Card Form -->
            <div v-if="selectedType === 'credit_card'" class="space-y-4">
              <!-- Card Number -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Card Number
                </label>
                <input
                  v-model="cardData.number"
                  type="text"
                  placeholder="1234 5678 9012 3456"
                  maxlength="19"
                  class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
                />
              </div>

              <!-- Expiry and CVV -->
              <div class="grid grid-cols-2 gap-4">
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    Expiry Date
                  </label>
                  <input
                    v-model="cardData.expiry"
                    type="text"
                    placeholder="MM/YY"
                    maxlength="5"
                    class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
                  />
                </div>
                <div>
                  <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                    CVV
                  </label>
                  <input
                    v-model="cardData.cvv"
                    type="text"
                    placeholder="123"
                    maxlength="4"
                    class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
                  />
                </div>
              </div>

              <!-- Cardholder Name -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Cardholder Name
                </label>
                <input
                  v-model="cardData.name"
                  type="text"
                  placeholder="John Doe"
                  class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
                />
              </div>

              <!-- Billing Address -->
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Billing Address
                </label>
                <input
                  v-model="cardData.billingAddress"
                  type="text"
                  placeholder="123 Main St, City, State 12345"
                  class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
                />
              </div>
            </div>

            <!-- PayPal Form -->
            <div v-else-if="selectedType === 'paypal'" class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  PayPal Email
                </label>
                <input
                  v-model="paypalData.email"
                  type="email"
                  placeholder="you@example.com"
                  class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  PayPal Password
                </label>
                <input
                  v-model="paypalData.password"
                  type="password"
                  placeholder="Your PayPal password"
                  class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
                />
              </div>
            </div>

            <!-- Bank Transfer Form -->
            <div v-else-if="selectedType === 'bank_transfer'" class="space-y-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Bank Name
                </label>
                <input
                  v-model="bankData.bankName"
                  type="text"
                  placeholder="Chase Bank"
                  class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Account Number
                </label>
                <input
                  v-model="bankData.accountNumber"
                  type="text"
                  placeholder="123456789"
                  class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                  Routing Number
                </label>
                <input
                  v-model="bankData.routingNumber"
                  type="text"
                  placeholder="123456789"
                  class="block w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-primary-500 focus:border-primary-500 sm:text-sm dark:bg-gray-700 dark:text-white"
                />
              </div>
            </div>

            <!-- Set as Default -->
            <div class="flex items-center">
              <input
                v-model="setDefault"
                type="checkbox"
                id="setDefault"
                class="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
              />
              <label for="setDefault" class="ml-2 block text-sm text-gray-700 dark:text-gray-300">
                Set as default payment method
              </label>
            </div>

            <!-- Security Notice -->
            <div class="p-4 bg-blue-50 dark:bg-blue-900/20 rounded-lg">
              <div class="flex">
                <div class="flex-shrink-0">
                  <Icon icon="mdi:shield-check" class="h-5 w-5 text-blue-400" />
                </div>
                <div class="ml-3">
                  <h3 class="text-sm font-medium text-blue-800 dark:text-blue-200">
                    Secure Payment
                  </h3>
                  <p class="mt-2 text-sm text-blue-700 dark:text-blue-300">
                    Your payment information is encrypted and secure. We never store your payment details on our servers.
                  </p>
                </div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex space-x-3 mt-6">
              <button
                type="button"
                @click="$emit('close')"
                class="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500"
              >
                Cancel
              </button>
              <button
                type="submit"
                :disabled="loading || !selectedType || !isValid"
                class="flex-1 px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <Icon v-if="loading" icon="mdi:loading" class="w-4 h-4 mr-2 animate-spin" />
                {{ loading ? 'Adding...' : 'Add Payment Method' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const emit = defineEmits(['close', 'add'])

// State
const selectedType = ref('')
const setDefault = ref(false)
const loading = ref(false)

// Form data
const cardData = ref({
  number: '',
  expiry: '',
  cvv: '',
  name: '',
  billingAddress: ''
})

const paypalData = ref({
  email: '',
  password: ''
})

const bankData = ref({
  bankName: '',
  accountNumber: '',
  routingNumber: ''
})

// Payment method types
const paymentTypes = [
  { id: 'credit_card', name: 'Credit Card', icon: 'mdi:credit-card' },
  { id: 'paypal', name: 'PayPal', icon: 'mdi:paypal' },
  { id: 'bank_transfer', name: 'Bank Transfer', icon: 'mdi:bank' }
]

// Computed
const isValid = computed(() => {
  if (!selectedType.value) return false

  switch (selectedType.value) {
    case 'credit_card':
      return cardData.value.number.length === 16 &&
             cardData.value.expiry.length === 5 &&
             cardData.value.cvv.length >= 3 &&
             cardData.value.name.trim() !== ''
    case 'paypal':
      return paypalData.value.email.includes('@') &&
             paypalData.value.password.length >= 6
    case 'bank_transfer':
      return bankData.value.bankName.trim() !== '' &&
             bankData.value.accountNumber.length >= 8 &&
             bankData.value.routingNumber.length >= 8
    default:
      return false
  }
})

// Methods
const handleSubmit = async () => {
  if (!isValid.value) return

  loading.value = true

  try {
    const paymentMethodData = {
      type: paymentTypes.find(t => t.id === selectedType.value)?.name,
      isDefault: setDefault.value,
      ...getFormData()
    }

    emit('add', paymentMethodData)
  } catch (error) {
    console.error('Failed to add payment method:', error)
  } finally {
    loading.value = false
  }
}

const getFormData = () => {
  switch (selectedType.value) {
    case 'credit_card':
      return cardData.value
    case 'paypal':
      return paypalData.value
    case 'bank_transfer':
      return bankData.value
    default:
      return {}
  }
}
</script>
