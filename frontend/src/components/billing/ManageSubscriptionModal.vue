<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
            Manage Subscription
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
        <!-- Current Plan Info -->
        <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-6 mb-6">
          <div class="flex justify-between items-start">
            <div>
              <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-2">
                {{ subscription.plan }}
              </h4>
              <p class="text-gray-600 dark:text-gray-400 mb-4">
                {{ subscription.description || 'Current subscription plan' }}
              </p>
              <div class="space-y-2">
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600 dark:text-gray-400">Monthly Price:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">
                    ${{ formatCurrency(subscription.monthlyPrice) }}
                  </span>
                </div>
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600 dark:text-gray-400">Billing Cycle:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">
                    {{ subscription.billingCycle || 'Monthly' }}
                  </span>
                </div>
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600 dark:text-gray-400">Next Payment:</span>
                  <span class="font-medium text-gray-900 dark:text-gray-100">
                    {{ formatDate(subscription.nextBillingDate) }}
                  </span>
                </div>
                <div class="flex justify-between text-sm">
                  <span class="text-gray-600 dark:text-gray-400">Status:</span>
                  <span :class="getStatusClass(subscription.status)" class="px-2 py-1 rounded-full text-xs font-medium">
                    {{ subscription.status }}
                  </span>
                </div>
              </div>
            </div>
            <div class="p-3 bg-blue-100 dark:bg-blue-900/20 rounded-lg">
              <Icon :icon="getPlanIcon(subscription.plan)" class="text-blue-600 dark:text-blue-400 text-2xl" />
            </div>
          </div>
        </div>

        <!-- Management Options -->
        <div class="space-y-6">
          <!-- Change Plan -->
          <div class="border border-gray-200 dark:border-gray-600 rounded-lg p-4">
            <h4 class="font-semibold text-gray-900 dark:text-gray-100 mb-3">Change Plan</h4>
            <p class="text-sm text-gray-600 dark:text-gray-400 mb-4">
              Upgrade or downgrade your subscription plan
            </p>
            <button 
              @click="showPlanSelector = true"
              class="w-full px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center justify-center gap-2"
            >
              <Icon icon="ic:baseline-upgrade" />
              Change Plan
            </button>
          </div>

          <!-- Billing Cycle -->
          <div class="border border-gray-200 dark:border-gray-600 rounded-lg p-4">
            <h4 class="font-semibold text-gray-900 dark:text-gray-100 mb-3">Billing Cycle</h4>
            <p class="text-sm text-gray-600 dark:text-gray-400 mb-4">
              Switch between monthly and annual billing
            </p>
            <div class="grid grid-cols-2 gap-3">
              <button 
                v-for="cycle in billingCycles" 
                :key="cycle.value"
                @click="changeBillingCycle(cycle.value)"
                :class="[
                  'p-3 rounded-lg border-2 font-medium transition-colors',
                  subscription.billingCycle === cycle.value 
                    ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20 text-blue-600 dark:text-blue-400' 
                    : 'border-gray-200 dark:border-gray-600 hover:border-gray-300 dark:hover:border-gray-500'
                ]"
                :disabled="isChangingCycle"
              >
                <div class="text-center">
                  <p class="font-medium">{{ cycle.label }}</p>
                  <p v-if="cycle.savings" class="text-xs text-green-600 dark:text-green-400 mt-1">
                    Save {{ cycle.savings }}%
                  </p>
                </div>
              </button>
            </div>
          </div>

          <!-- Payment Method -->
          <div class="border border-gray-200 dark:border-gray-600 rounded-lg p-4">
            <h4 class="font-semibold text-gray-900 dark:text-gray-100 mb-3">Payment Method</h4>
            <p class="text-sm text-gray-600 dark:text-gray-400 mb-4">
              Update your payment method
            </p>
            <div class="space-y-3">
              <div 
                v-for="method in paymentMethods" 
                :key="method.id"
                class="flex items-center p-3 border border-gray-200 dark:border-gray-600 rounded-lg"
              >
                <div class="flex-1 flex items-center gap-3">
                  <div class="p-2 bg-gray-100 dark:bg-gray-700 rounded-lg">
                    <Icon :icon="getPaymentMethodIcon(method.type)" class="text-lg" />
                  </div>
                  <div>
                    <p class="font-medium text-gray-900 dark:text-gray-100">
                      {{ getPaymentMethodName(method) }}
                    </p>
                    <p v-if="method.isDefault" class="text-xs text-green-600 dark:text-green-400">
                      Default payment method
                    </p>
                  </div>
                </div>
                <button 
                  @click="updatePaymentMethod(method.id)"
                  :disabled="isUpdatingPayment"
                  class="px-3 py-1 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 text-sm"
                >
                  Use
                </button>
              </div>
            </div>
            <button 
              @click="showAddPaymentMethod = true"
              class="w-full mt-3 px-4 py-2 border-2 border-dashed border-gray-300 dark:border-gray-600 rounded-lg text-gray-600 dark:text-gray-400 hover:border-gray-400 dark:hover:border-gray-500 hover:text-gray-700 dark:hover:text-gray-300 transition-colors"
            >
              <Icon icon="ic:baseline-add" class="mr-2" />
              Add Payment Method
            </button>
          </div>

          <!-- Pause/Resume -->
          <div v-if="canPause" class="border border-gray-200 dark:border-gray-600 rounded-lg p-4">
            <h4 class="font-semibold text-gray-900 dark:text-gray-100 mb-3">Pause Subscription</h4>
            <p class="text-sm text-gray-600 dark:text-gray-400 mb-4">
              Temporarily pause your subscription
            </p>
            <div class="bg-yellow-50 dark:bg-yellow-900/20 rounded-lg p-3 mb-4">
              <div class="flex items-start gap-2">
                <Icon icon="ic:baseline-info" class="text-yellow-600 dark:text-yellow-400 mt-0.5" />
                <div class="text-sm text-yellow-800 dark:text-yellow-200">
                  <p class="font-medium mb-1">What happens when you pause:</p>
                  <ul class="list-disc list-inside space-y-1 text-xs">
                    <li>You'll continue to be billed at a reduced rate</li>
                    <li>Some features may be limited during pause</li>
                    <li>You can resume anytime</li>
                  </ul>
                </div>
              </div>
            </div>
            <button 
              @click="pauseSubscription"
              :disabled="isPausing"
              class="w-full px-4 py-2 bg-yellow-600 text-white rounded-lg hover:bg-yellow-700 disabled:opacity-50 transition-colors flex items-center justify-center gap-2"
            >
              <Icon v-if="isPausing" icon="ic:baseline-refresh" class="animate-spin" />
              {{ isPausing ? 'Pausing...' : 'Pause Subscription' }}
            </button>
          </div>

          <!-- Cancel -->
          <div class="border border-red-200 dark:border-red-600 rounded-lg p-4">
            <h4 class="font-semibold text-red-900 dark:text-red-100 mb-3">Cancel Subscription</h4>
            <p class="text-sm text-red-600 dark:text-red-400 mb-4">
              Cancel your subscription and lose access to premium features
            </p>
            <div class="bg-red-50 dark:bg-red-900/20 rounded-lg p-3 mb-4">
              <div class="flex items-start gap-2">
                <Icon icon="ic:baseline-warning" class="text-red-600 dark:text-red-400 mt-0.5" />
                <div class="text-sm text-red-800 dark:text-red-200">
                  <p class="font-medium mb-1">Before you cancel:</p>
                  <ul class="list-disc list-inside space-y-1 text-xs">
                    <li>All data will be preserved for 30 days</li>
                    <li>You can reactivate within 30 days</li>
                    <li>No refunds for partial months</li>
                  </ul>
                </div>
              </div>
            </div>
            <button 
              @click="confirmCancel"
              :disabled="isCancelling"
              class="w-full px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50 transition-colors flex items-center justify-center gap-2"
            >
              <Icon v-if="isCancelling" icon="ic:baseline-refresh" class="animate-spin" />
              {{ isCancelling ? 'Cancelling...' : 'Cancel Subscription' }}
            </button>
          </div>
        </div>
      </div>

      <!-- Modals -->
      <PlanSelectorModal 
        v-if="showPlanSelector"
        :current-plan="subscription.plan"
        @close="showPlanSelector = false"
        @selected="onPlanSelected"
      />

      <AddPaymentMethodModal 
        v-if="showAddPaymentMethod"
        @close="showAddPaymentMethod = false"
        @added="onPaymentMethodAdded"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useBillingStore } from '@/stores/billing/billingStore'
import PlanSelectorModal from './PlanSelectorModal.vue'
import AddPaymentMethodModal from './AddPaymentMethodModal.vue'

const props = defineProps({
  subscription: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'updated'])

const billingStore = useBillingStore()

// State
const showPlanSelector = ref(false)
const showAddPaymentMethod = ref(false)
const isChangingCycle = ref(false)
const isUpdatingPayment = ref(false)
const isPausing = ref(false)
const isCancelling = ref(false)

// Billing cycles
const billingCycles = [
  { value: 'MONTHLY', label: 'Monthly' },
  { value: 'YEARLY', label: 'Yearly', savings: 20 }
]

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
const canPause = computed(() => {
  return props.subscription.status === 'ACTIVE' && 
         props.subscription.plan !== 'ENTERPRISE' // Enterprise plans can't be paused
})

// Methods
const getPlanIcon = (plan) => {
  const iconMap = {
    'STARTER': 'ic:baseline-rocket-launch',
    'PROFESSIONAL': 'ic:baseline-business',
    'ENTERPRISE': 'ic:baseline-corporate-fare'
  }
  return iconMap[plan?.toUpperCase()] || 'ic:baseline-subscriptions'
}

const getStatusClass = (status) => {
  switch (status?.toUpperCase()) {
    case 'ACTIVE':
      return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
    case 'PAUSED':
      return 'bg-yellow-100 text-yellow-600 dark:bg-yellow-900/20 dark:text-yellow-400'
    case 'CANCELLED':
      return 'bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400'
    default:
      return 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400'
  }
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

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const changeBillingCycle = async (newCycle) => {
  isChangingCycle.value = true
  
  try {
    await billingStore.changeBillingCycle(props.subscription.id, newCycle)
    emit('updated', { ...props.subscription, billingCycle: newCycle })
  } catch (error) {
    console.error('Failed to change billing cycle:', error)
  } finally {
    isChangingCycle.value = false
  }
}

const updatePaymentMethod = async (paymentMethodId) => {
  isUpdatingPayment.value = true
  
  try {
    await billingStore.updatePaymentMethod(props.subscription.id, paymentMethodId)
    emit('updated', { ...props.subscription, paymentMethodId })
  } catch (error) {
    console.error('Failed to update payment method:', error)
  } finally {
    isUpdatingPayment.value = false
  }
}

const pauseSubscription = async () => {
  if (!confirm('Are you sure you want to pause your subscription?')) return
  
  isPausing.value = true
  
  try {
    await billingStore.pauseSubscription(props.subscription.id)
    emit('updated', { ...props.subscription, status: 'PAUSED' })
  } catch (error) {
    console.error('Failed to pause subscription:', error)
  } finally {
    isPausing.value = false
  }
}

const confirmCancel = () => {
  if (!confirm('Are you sure you want to cancel your subscription? This action cannot be undone.')) return
  
  const reason = prompt('Please tell us why you\'re cancelling (optional):')
  cancelSubscription(reason)
}

const cancelSubscription = async (reason = '') => {
  isCancelling.value = true
  
  try {
    await billingStore.cancelSubscription(props.subscription.id, reason)
    emit('updated', { ...props.subscription, status: 'CANCELLED' })
    emit('close')
  } catch (error) {
    console.error('Failed to cancel subscription:', error)
  } finally {
    isCancelling.value = false
  }
}

const onPlanSelected = (newPlan) => {
  showPlanSelector.value = false
  emit('updated', { ...props.subscription, plan: newPlan.plan })
}

const onPaymentMethodAdded = (newMethod) => {
  showAddPaymentMethod.value = false
  paymentMethods.value.push(newMethod)
}
</script>

<style scoped>
.modal-backdrop {
  backdrop-filter: blur(4px);
}
</style>
