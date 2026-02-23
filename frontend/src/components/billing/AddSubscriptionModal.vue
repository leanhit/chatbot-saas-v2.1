<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg max-w-4xl w-full mx-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
            Choose Subscription Plan
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
        <!-- Billing Cycle Toggle -->
        <div class="flex justify-center mb-8">
          <div class="bg-gray-100 dark:bg-gray-700 rounded-lg p-1 inline-flex">
            <button 
              v-for="cycle in billingCycles" 
              :key="cycle.value"
              @click="selectedBillingCycle = cycle.value"
              :class="[
                'px-6 py-2 rounded-md font-medium transition-colors',
                selectedBillingCycle === cycle.value 
                  ? 'bg-white dark:bg-gray-600 text-blue-600 dark:text-blue-400 shadow-sm' 
                  : 'text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-200'
              ]"
            >
              {{ cycle.label }}
              <span v-if="cycle.discount" class="ml-2 text-green-600 dark:text-green-400 text-sm">
                (Save {{ cycle.discount }}%)
              </span>
            </button>
          </div>
        </div>

        <!-- Plans Grid -->
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div 
            v-for="plan in availablePlans" 
            :key="plan.id"
            @click="selectPlan(plan)"
            :class="[
              'relative rounded-lg border-2 p-6 cursor-pointer transition-all',
              selectedPlan?.id === plan.id 
                ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20' 
                : 'border-gray-200 dark:border-gray-600 hover:border-gray-300 dark:hover:border-gray-500'
            ]"
          >
            <!-- Popular Badge -->
            <div v-if="plan.isPopular" class="absolute -top-3 left-1/2 transform -translate-x-1/2">
              <span class="bg-blue-600 text-white px-3 py-1 rounded-full text-xs font-medium">
                Most Popular
              </span>
            </div>

            <!-- Plan Header -->
            <div class="text-center mb-6">
              <div class="mb-4">
                <div :class="getPlanIconClass(plan.type)" class="inline-flex p-3 rounded-lg">
                  <Icon :icon="getPlanIcon(plan.type)" class="text-3xl" />
                </div>
              </div>
              <h3 class="text-xl font-bold text-gray-900 dark:text-gray-100 mb-2">
                {{ plan.name }}
              </h3>
              <p class="text-sm text-gray-600 dark:text-gray-400 mb-4">
                {{ plan.description }}
              </p>
              
              <!-- Pricing -->
              <div class="mb-2">
                <span class="text-4xl font-bold text-gray-900 dark:text-gray-100">
                  ${{ getPlanPrice(plan) }}
                </span>
                <span class="text-gray-600 dark:text-gray-400">
                  /{{ selectedBillingCycle.toLowerCase() }}
                </span>
              </div>
              
              <!-- Annual Savings -->
              <div v-if="selectedBillingCycle === 'YEARLY' && plan.annualSavings" class="text-green-600 dark:text-green-400 text-sm">
                Save ${{ plan.annualSavings }} per year
              </div>
            </div>

            <!-- Features -->
            <div class="space-y-3">
              <div 
                v-for="feature in plan.features" 
                :key="feature.name"
                class="flex items-start gap-2"
              >
                <Icon 
                  :icon="feature.included ? 'ic:baseline-check-circle' : 'ic:baseline-cancel'" 
                  :class="feature.included ? 'text-green-600 dark:text-green-400' : 'text-gray-400'"
                  class="mt-0.5"
                />
                <div>
                  <p :class="feature.included ? 'text-gray-900 dark:text-gray-100' : 'text-gray-400'">
                    {{ feature.name }}
                  </p>
                  <p v-if="feature.description" class="text-xs text-gray-600 dark:text-gray-400 mt-1">
                    {{ feature.description }}
                  </p>
                </div>
              </div>
            </div>

            <!-- Action Button -->
            <button 
              @click="selectPlan(plan)"
              :class="[
                'w-full mt-6 px-4 py-3 rounded-lg font-medium transition-colors',
                selectedPlan?.id === plan.id 
                  ? 'bg-blue-600 text-white hover:bg-blue-700' 
                  : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600'
              ]"
            >
              {{ selectedPlan?.id === plan.id ? 'Selected' : 'Select Plan' }}
            </button>
          </div>
        </div>

        <!-- Selected Plan Summary -->
        <div v-if="selectedPlan" class="bg-gray-50 dark:bg-gray-700 rounded-lg p-6 mb-6">
          <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
            Order Summary
          </h4>
          <div class="space-y-3">
            <div class="flex justify-between">
              <span class="text-gray-600 dark:text-gray-400">Plan:</span>
              <span class="font-medium text-gray-900 dark:text-gray-100">
                {{ selectedPlan.name }}
              </span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600 dark:text-gray-400">Billing Cycle:</span>
              <span class="font-medium text-gray-900 dark:text-gray-100">
                {{ selectedBillingCycle }}
              </span>
            </div>
            <div class="flex justify-between">
              <span class="text-gray-600 dark:text-gray-400">Price:</span>
              <span class="font-medium text-gray-900 dark:text-gray-100">
                ${{ getPlanPrice(selectedPlan) }} / {{ selectedBillingCycle.toLowerCase() }}
              </span>
            </div>
            <div v-if="setupFee > 0" class="flex justify-between">
              <span class="text-gray-600 dark:text-gray-400">Setup Fee:</span>
              <span class="font-medium text-gray-900 dark:text-gray-100">
                ${{ formatCurrency(setupFee) }}
              </span>
            </div>
            <div class="border-t border-gray-200 dark:border-gray-600 pt-3">
              <div class="flex justify-between">
                <span class="font-semibold text-gray-900 dark:text-gray-100">Total Due Today:</span>
                <span class="font-bold text-xl text-gray-900 dark:text-gray-100">
                  ${{ formatCurrency(firstBillAmount) }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Payment Method -->
        <div class="mb-6">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
            Payment Method
          </label>
          <div class="space-y-3">
            <label 
              v-for="method in paymentMethods" 
              :key="method.id"
              class="flex items-center p-3 border border-gray-200 dark:border-gray-600 rounded-lg cursor-pointer hover:bg-gray-50 dark:hover:bg-gray-700"
            >
              <input 
                type="radio" 
                v-model="selectedPaymentMethod" 
                :value="method.id"
                class="mr-3"
              />
              <div class="flex-1 flex items-center gap-3">
                <div class="p-2 bg-gray-100 dark:bg-gray-700 rounded-lg">
                  <Icon :icon="getPaymentMethodIcon(method.type)" class="text-lg" />
                </div>
                <div>
                  <p class="font-medium text-gray-900 dark:text-gray-100">
                    {{ getPaymentMethodName(method) }}
                  </p>
                </div>
              </div>
              <div v-if="method.isDefault" class="px-2 py-1 bg-green-100 dark:bg-green-900/20 text-green-600 dark:text-green-400 rounded-full text-xs font-medium">
                Default
              </div>
            </label>
          </div>
          
          <button 
            @click="showAddPaymentMethod = true"
            class="w-full mt-3 px-4 py-2 border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg text-gray-600 dark:text-gray-400 hover:border-gray-400 dark:hover:border-gray-500 hover:text-gray-700 dark:hover:text-gray-300 transition-colors"
          >
            <Icon icon="ic:baseline-add" class="mr-2" />
            Add Payment Method
          </button>
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
              <a href="#" class="text-blue-600 dark:text-blue-400 hover:underline">Terms of Service</a>
              and 
              <a href="#" class="text-blue-600 dark:text-blue-400 hover:underline">Billing Policy</a>
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
            @click="subscribeToPlan"
            :disabled="!canSubscribe"
            class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center gap-2"
          >
            <Icon v-if="isProcessing" icon="ic:baseline-refresh" class="animate-spin" />
            {{ isProcessing ? 'Processing...' : 'Subscribe Now' }}
          </button>
        </div>
      </div>
    </div>

    <!-- Add Payment Method Modal -->
    <AddPaymentMethodModal 
      v-if="showAddPaymentMethod"
      @close="showAddPaymentMethod = false"
      @added="onPaymentMethodAdded"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useBillingStore } from '@/stores/billing/billingStore'
import AddPaymentMethodModal from './AddPaymentMethodModal.vue'

const emit = defineEmits(['close', 'added'])

const billingStore = useBillingStore()

// State
const selectedPlan = ref(null)
const selectedBillingCycle = ref('MONTHLY')
const selectedPaymentMethod = ref(null)
const agreeToTerms = ref(false)
const isProcessing = ref(false)
const showAddPaymentMethod = ref(false)

// Billing cycles
const billingCycles = [
  { value: 'MONTHLY', label: 'Monthly' },
  { value: 'YEARLY', label: 'Yearly', discount: 20 }
]

// Mock available plans (in real app, these would come from API)
const availablePlans = ref([
  {
    id: 'starter',
    name: 'Starter',
    type: 'STARTER',
    description: 'Perfect for small teams getting started',
    monthlyPrice: 2900, // $29.00 in cents
    annualPrice: 29000, // $290.00 in cents
    annualSavings: 58,
    isPopular: false,
    features: [
      { name: 'Up to 5 users', included: true },
      { name: '10GB Storage', included: true },
      { name: 'Basic Support', included: true },
      { name: 'API Access', included: true, description: '1,000 requests/month' },
      { name: 'Advanced Analytics', included: false }
    ]
  },
  {
    id: 'professional',
    name: 'Professional',
    type: 'PROFESSIONAL',
    description: 'Best for growing businesses',
    monthlyPrice: 9900, // $99.00 in cents
    annualPrice: 95000, // $950.00 in cents
    annualSavings: 238,
    isPopular: true,
    features: [
      { name: 'Up to 20 users', included: true },
      { name: '100GB Storage', included: true },
      { name: 'Priority Support', included: true },
      { name: 'API Access', included: true, description: '10,000 requests/month' },
      { name: 'Advanced Analytics', included: true },
      { name: 'Custom Integrations', included: true }
    ]
  },
  {
    id: 'enterprise',
    name: 'Enterprise',
    type: 'ENTERPRISE',
    description: 'For large organizations with advanced needs',
    monthlyPrice: 29900, // $299.00 in cents
    annualPrice: 299000, // $2,990.00 in cents
    annualSavings: 598,
    isPopular: false,
    features: [
      { name: 'Unlimited Users', included: true },
      { name: 'Unlimited Storage', included: true },
      { name: '24/7 Phone Support', included: true },
      { name: 'API Access', included: true, description: 'Unlimited requests' },
      { name: 'Advanced Analytics', included: true },
      { name: 'Custom Integrations', included: true },
      { name: 'Dedicated Account Manager', included: true },
      { name: 'SLA Guarantee', included: true }
    ]
  }
])

// Mock payment methods
const paymentMethods = ref([
  {
    id: 'card_1',
    type: 'CREDIT_CARD',
    description: 'Visa ending in 4242',
    last4: '4242',
    isDefault: true
  },
  {
    id: 'paypal_1',
    type: 'PAYPAL',
    description: 'PayPal account',
    email: 'user@example.com',
    isDefault: false
  }
])

// Computed
const setupFee = computed(() => {
  return selectedPlan.value?.setupFee || 0
})

const firstBillAmount = computed(() => {
  if (!selectedPlan.value) return 0
  
  const planPrice = getPlanPrice(selectedPlan.value)
  return planPrice + setupFee.value
})

const canSubscribe = computed(() => {
  return selectedPlan.value && 
         selectedPaymentMethod.value && 
         agreeToTerms.value && 
         !isProcessing.value
})

// Methods
const getPlanIcon = (type) => {
  const iconMap = {
    'STARTER': 'ic:baseline-rocket-launch',
    'PROFESSIONAL': 'ic:baseline-business',
    'ENTERPRISE': 'ic:baseline-corporate-fare'
  }
  return iconMap[type] || 'ic:baseline-subscriptions'
}

const getPlanIconClass = (type) => {
  const classMap = {
    'STARTER': 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400',
    'PROFESSIONAL': 'bg-purple-100 text-purple-600 dark:bg-purple-900/20 dark:text-purple-400',
    'ENTERPRISE': 'bg-orange-100 text-orange-600 dark:bg-orange-900/20 dark:text-orange-400'
  }
  return classMap[type] || 'bg-gray-100 text-gray-600'
}

const getPlanPrice = (plan) => {
  if (selectedBillingCycle.value === 'YEARLY') {
    return (plan.annualPrice / 12).toFixed(2)
  }
  return (plan.monthlyPrice / 100).toFixed(2)
}

const getPaymentMethodIcon = (type) => {
  const iconMap = {
    'CREDIT_CARD': 'ic:baseline-credit-card',
    'DEBIT_CARD': 'ic:baseline-credit-card',
    'PAYPAL': 'ic:baseline-paypal',
    'BANK_TRANSFER': 'ic:baseline-account-balance'
  }
  return iconMap[type] || 'ic:baseline-credit-card'
}

const getPaymentMethodName = (method) => {
  if (method.type === 'CREDIT_CARD' || method.type === 'DEBIT_CARD') {
    return `${method.type === 'CREDIT_CARD' ? 'Credit Card' : 'Debit Card'} ending in ${method.last4}`
  }
  if (method.type === 'PAYPAL') {
    return `PayPal (${method.email})`
  }
  return method.description || method.type
}

const formatCurrency = (amount) => {
  if (!amount) return '0.00'
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount / 100) // Convert from cents
}

const selectPlan = (plan) => {
  selectedPlan.value = plan
}

const subscribeToPlan = async () => {
  if (!canSubscribe.value) return
  
  isProcessing.value = true
  
  try {
    const subscriptionData = {
      planId: selectedPlan.value.id,
      billingCycle: selectedBillingCycle.value,
      paymentMethodId: selectedPaymentMethod.value
    }
    
    const result = await billingStore.subscribeToPlan(subscriptionData)
    
    emit('added', result)
    emit('close')
    
    // Reset form
    selectedPlan.value = null
    selectedPaymentMethod.value = null
    agreeToTerms.value = false
    
  } catch (error) {
    console.error('Subscription failed:', error)
    // Show error message
  } finally {
    isProcessing.value = false
  }
}

const onPaymentMethodAdded = (newMethod) => {
  showAddPaymentMethod.value = false
  paymentMethods.value.push(newMethod)
  selectedPaymentMethod.value = newMethod.id
}

// Initialize
if (paymentMethods.value.length > 0 && !selectedPaymentMethod.value) {
  const defaultMethod = paymentMethods.value.find(m => m.isDefault)
  selectedPaymentMethod.value = defaultMethod?.id || paymentMethods.value[0].id
}
</script>

<style scoped>
.modal-backdrop {
  backdrop-filter: blur(4px);
}

.plan-card {
  transition: all 0.2s ease-in-out;
}

.plan-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.plan-card.selected {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.15);
}
</style>
