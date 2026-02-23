<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
            Add Payment Method
          </h3>
          <button 
            @click="$emit('close')"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="ic:baseline-close" class="text-xl" />
          </button>
        </div>
      </div>

      <!-- Payment Method Type Selection -->
      <div class="p-6">
        <h4 class="text-lg font-medium text-gray-900 dark:text-gray-100 mb-4">
          Choose Payment Method Type
        </h4>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
          <button 
            v-for="type in paymentMethodTypes" 
            :key="type.id"
            @click="selectedType = type.id"
            :class="[
              'p-4 rounded-lg border-2 transition-all',
              selectedType === type.id 
                ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20' 
                : 'border-gray-200 dark:border-gray-600 hover:border-gray-300 dark:hover:border-gray-500'
            ]"
          >
            <div class="flex flex-col items-center text-center">
              <div :class="type.iconClass" class="p-3 rounded-lg mb-3">
                <Icon :icon="type.icon" class="text-2xl" />
              </div>
              <h5 class="font-semibold text-gray-900 dark:text-gray-100 mb-1">
                {{ type.name }}
              </h5>
              <p class="text-sm text-gray-600 dark:text-gray-400">
                {{ type.description }}
              </p>
            </div>
          </button>
        </div>

        <!-- Credit/Debit Card Form -->
        <div v-if="selectedType === 'CREDIT_CARD' || selectedType === 'DEBIT_CARD'" class="space-y-4">
          <h5 class="font-medium text-gray-900 dark:text-gray-100 mb-4">
            Card Information
          </h5>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Cardholder Name
              </label>
              <input 
                v-model="cardData.cardholderName"
                type="text"
                placeholder="John Doe"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                :class="{ 'border-red-300 dark:border-red-600': errors.cardholderName }"
              />
              <p v-if="errors.cardholderName" class="text-red-600 dark:text-red-400 text-sm mt-1">
                {{ errors.cardholderName }}
              </p>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Card Number
              </label>
              <div class="relative">
                <input 
                  v-model="cardData.cardNumber"
                  type="text"
                  placeholder="1234 5678 9012 3456"
                  maxlength="19"
                  @input="formatCardNumber"
                  class="w-full px-3 py-2 pr-12 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                  :class="{ 'border-red-300 dark:border-red-600': errors.cardNumber }"
                />
                <div class="absolute right-3 top-1/2 transform -translate-y-1/2">
                  <Icon :icon="getCardTypeIcon(cardData.cardNumber)" class="text-xl" />
                </div>
              </div>
              <p v-if="errors.cardNumber" class="text-red-600 dark:text-red-400 text-sm mt-1">
                {{ errors.cardNumber }}
              </p>
            </div>
          </div>
          
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Expiry Month
              </label>
              <select 
                v-model="cardData.expiryMonth"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                :class="{ 'border-red-300 dark:border-red-600': errors.expiryMonth }"
              >
                <option value="">MM</option>
                <option v-for="month in 12" :key="month" :value="month.toString().padStart(2, '0')">
                  {{ month.toString().padStart(2, '0') }}
                </option>
              </select>
              <p v-if="errors.expiryMonth" class="text-red-600 dark:text-red-400 text-sm mt-1">
                {{ errors.expiryMonth }}
              </p>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Expiry Year
              </label>
              <select 
                v-model="cardData.expiryYear"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                :class="{ 'border-red-300 dark:border-red-600': errors.expiryYear }"
              >
                <option value="">YY</option>
                <option v-for="year in expiryYears" :key="year" :value="year">
                  {{ year }}
                </option>
              </select>
              <p v-if="errors.expiryYear" class="text-red-600 dark:text-red-400 text-sm mt-1">
                {{ errors.expiryYear }}
              </p>
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
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                :class="{ 'border-red-300 dark:border-red-600': errors.cvv }"
              />
              <p v-if="errors.cvv" class="text-red-600 dark:text-red-400 text-sm mt-1">
                {{ errors.cvv }}
              </p>
            </div>
          </div>
        </div>

        <!-- PayPal Form -->
        <div v-else-if="selectedType === 'PAYPAL'" class="space-y-4">
          <h5 class="font-medium text-gray-900 dark:text-gray-100 mb-4">
            PayPal Information
          </h5>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              PayPal Email
            </label>
            <input 
              v-model="paypalData.email"
              type="email"
              placeholder="john.doe@example.com"
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              :class="{ 'border-red-300 dark:border-red-600': errors.email }"
            />
            <p v-if="errors.email" class="text-red-600 dark:text-red-400 text-sm mt-1">
              {{ errors.email }}
            </p>
          </div>
        </div>

        <!-- Bank Transfer Form -->
        <div v-else-if="selectedType === 'BANK_TRANSFER'" class="space-y-4">
          <h5 class="font-medium text-gray-900 dark:text-gray-100 mb-4">
            Bank Account Information
          </h5>
          
          <div>
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              Bank Name
            </label>
            <input 
              v-model="bankData.bankName"
              type="text"
              placeholder="Chase Bank"
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              :class="{ 'border-red-300 dark:border-red-600': errors.bankName }"
            />
            <p v-if="errors.bankName" class="text-red-600 dark:text-red-400 text-sm mt-1">
              {{ errors.bankName }}
            </p>
          </div>
          
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Account Number
              </label>
              <input 
                v-model="bankData.accountNumber"
                type="text"
                placeholder="1234567890"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                :class="{ 'border-red-300 dark:border-red-600': errors.accountNumber }"
              />
              <p v-if="errors.accountNumber" class="text-red-600 dark:text-red-400 text-sm mt-1">
                {{ errors.accountNumber }}
              </p>
            </div>
            
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Routing Number
              </label>
              <input 
                v-model="bankData.routingNumber"
                type="text"
                placeholder="123456789"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                :class="{ 'border-red-300 dark:border-red-600': errors.routingNumber }"
              />
              <p v-if="errors.routingNumber" class="text-red-600 dark:text-red-400 text-sm mt-1">
                {{ errors.routingNumber }}
              </p>
            </div>
          </div>
        </div>

        <!-- Set as Default -->
        <div class="mt-6">
          <label class="flex items-center gap-2">
            <input 
              type="checkbox" 
              v-model="setDefaultPayment"
              class="rounded"
            />
            <span class="text-sm text-gray-700 dark:text-gray-300">
              Set as default payment method
            </span>
          </label>
        </div>
      </div>

      <!-- Footer -->
      <div class="p-6 border-t border-gray-200 dark:border-gray-700">
        <div class="flex justify-between">
          <button 
            @click="$emit('close')"
            class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 transition-colors"
          >
            Cancel
          </button>
          
          <button 
            @click="addPaymentMethod"
            :disabled="!canSubmit || isSubmitting"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center gap-2"
          >
            <Icon v-if="isSubmitting" icon="ic:baseline-refresh" class="animate-spin" />
            {{ isSubmitting ? 'Adding...' : 'Add Payment Method' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const emit = defineEmits(['close', 'added'])

// State
const selectedType = ref('')
const isSubmitting = ref(false)
const setDefaultPayment = ref(true)

// Form data
const cardData = ref({
  cardholderName: '',
  cardNumber: '',
  expiryMonth: '',
  expiryYear: '',
  cvv: ''
})

const paypalData = ref({
  email: ''
})

const bankData = ref({
  bankName: '',
  accountNumber: '',
  routingNumber: ''
})

const errors = ref({})

// Payment method types
const paymentMethodTypes = [
  {
    id: 'CREDIT_CARD',
    name: 'Credit Card',
    description: 'Visa, Mastercard, American Express',
    icon: 'ic:baseline-credit-card',
    iconClass: 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400'
  },
  {
    id: 'DEBIT_CARD',
    name: 'Debit Card',
    description: 'Visa Debit, Mastercard Debit',
    icon: 'ic:baseline-credit-card',
    iconClass: 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
  },
  {
    id: 'PAYPAL',
    name: 'PayPal',
    description: 'Connect your PayPal account',
    icon: 'ic:baseline-paypal',
    iconClass: 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400'
  },
  {
    id: 'BANK_TRANSFER',
    name: 'Bank Transfer',
    description: 'Direct bank account transfer',
    icon: 'ic:baseline-account-balance',
    iconClass: 'bg-purple-100 text-purple-600 dark:bg-purple-900/20 dark:text-purple-400'
  }
]

// Computed
const expiryYears = computed(() => {
  const currentYear = new Date().getFullYear()
  const years = []
  for (let i = 0; i < 10; i++) {
    years.push(currentYear + i)
  }
  return years
})

const canSubmit = computed(() => {
  if (!selectedType.value) return false
  
  if (selectedType.value === 'CREDIT_CARD' || selectedType.value === 'DEBIT_CARD') {
    return cardData.value.cardholderName && 
           cardData.value.cardNumber && 
           cardData.value.expiryMonth && 
           cardData.value.expiryYear && 
           cardData.value.cvv
  }
  
  if (selectedType.value === 'PAYPAL') {
    return paypalData.value.email
  }
  
  if (selectedType.value === 'BANK_TRANSFER') {
    return bankData.value.bankName && 
           bankData.value.accountNumber && 
           bankData.value.routingNumber
  }
  
  return false
})

// Methods
const getCardTypeIcon = (cardNumber) => {
  const number = cardNumber.replace(/\s/g, '')
  if (number.startsWith('4')) return 'ic:baseline-credit-card' // Visa
  if (number.startsWith('5') || number.startsWith('2')) return 'ic:baseline-credit-card' // Mastercard
  if (number.startsWith('3')) return 'ic:baseline-credit-card' // Amex
  return 'ic:baseline-credit-card'
}

const formatCardNumber = (event) => {
  let value = event.target.value.replace(/\s/g, '')
  let formattedValue = value.match(/.{1,4}/g)?.join(' ') || value
  cardData.value.cardNumber = formattedValue
}

const validateForm = () => {
  errors.value = {}
  
  if (selectedType.value === 'CREDIT_CARD' || selectedType.value === 'DEBIT_CARD') {
    if (!cardData.value.cardholderName) {
      errors.value.cardholderName = 'Cardholder name is required'
    }
    
    if (!cardData.value.cardNumber) {
      errors.value.cardNumber = 'Card number is required'
    } else if (cardData.value.cardNumber.replace(/\s/g, '').length < 13) {
      errors.value.cardNumber = 'Invalid card number'
    }
    
    if (!cardData.value.expiryMonth) {
      errors.value.expiryMonth = 'Expiry month is required'
    }
    
    if (!cardData.value.expiryYear) {
      errors.value.expiryYear = 'Expiry year is required'
    }
    
    if (!cardData.value.cvv) {
      errors.value.cvv = 'CVV is required'
    } else if (cardData.value.cvv.length < 3) {
      errors.value.cvv = 'Invalid CVV'
    }
  }
  
  if (selectedType.value === 'PAYPAL') {
    if (!paypalData.value.email) {
      errors.value.email = 'Email is required'
    } else if (!paypalData.value.email.includes('@')) {
      errors.value.email = 'Invalid email address'
    }
  }
  
  if (selectedType.value === 'BANK_TRANSFER') {
    if (!bankData.value.bankName) {
      errors.value.bankName = 'Bank name is required'
    }
    
    if (!bankData.value.accountNumber) {
      errors.value.accountNumber = 'Account number is required'
    }
    
    if (!bankData.value.routingNumber) {
      errors.value.routingNumber = 'Routing number is required'
    }
  }
  
  return Object.keys(errors.value).length === 0
}

const addPaymentMethod = async () => {
  if (!validateForm()) return
  
  isSubmitting.value = true
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    let newPaymentMethod = {}
    
    if (selectedType.value === 'CREDIT_CARD' || selectedType.value === 'DEBIT_CARD') {
      newPaymentMethod = {
        id: 'pm_' + Date.now(),
        type: selectedType.value,
        cardType: 'VISA',
        last4: cardData.value.cardNumber.slice(-4),
        expiryMonth: cardData.value.expiryMonth,
        expiryYear: cardData.value.expiryYear,
        cardholderName: cardData.value.cardholderName,
        isDefault: setDefaultPayment.value,
        status: 'ACTIVE',
        createdAt: new Date().toISOString(),
        usage: {
          activeSubscriptions: 0,
          lastUsed: null,
          totalSpent: 0
        }
      }
    } else if (selectedType.value === 'PAYPAL') {
      newPaymentMethod = {
        id: 'pm_' + Date.now(),
        type: 'PAYPAL',
        email: paypalData.value.email,
        isDefault: setDefaultPayment.value,
        status: 'ACTIVE',
        createdAt: new Date().toISOString(),
        usage: {
          activeSubscriptions: 0,
          lastUsed: null,
          totalSpent: 0
        }
      }
    } else if (selectedType.value === 'BANK_TRANSFER') {
      newPaymentMethod = {
        id: 'pm_' + Date.now(),
        type: 'BANK_TRANSFER',
        bankName: bankData.value.bankName,
        last4: bankData.value.accountNumber.slice(-4),
        isDefault: setDefaultPayment.value,
        status: 'PENDING',
        createdAt: new Date().toISOString(),
        usage: {
          activeSubscriptions: 0,
          lastUsed: null,
          totalSpent: 0
        }
      }
    }
    
    emit('added', newPaymentMethod)
    
  } catch (error) {
    console.error('Failed to add payment method:', error)
  } finally {
    isSubmitting.value = false
  }
}
</script>

<style scoped>
.modal-backdrop {
  backdrop-filter: blur(4px);
}
</style>
