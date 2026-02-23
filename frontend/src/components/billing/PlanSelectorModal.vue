<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg max-w-5xl w-full mx-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
            Choose Your Plan
          </h3>
          <button 
            @click="$emit('close')"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="ic:baseline-close" class="text-xl" />
          </button>
        </div>
      </div>

      <!-- Billing Cycle Toggle -->
      <div class="p-6 bg-gray-50 dark:bg-gray-700">
        <div class="flex justify-center">
          <div class="bg-white dark:bg-gray-600 rounded-lg p-1 inline-flex">
            <button 
              v-for="cycle in billingCycles" 
              :key="cycle.value"
              @click="selectedBillingCycle = cycle.value"
              :class="[
                'px-6 py-2 rounded-md font-medium transition-colors',
                selectedBillingCycle === cycle.value 
                  ? 'bg-blue-600 text-white shadow-sm' 
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
      </div>

      <!-- Plans Grid -->
      <div class="p-6">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div 
            v-for="plan in availablePlans" 
            :key="plan.id"
            @click="selectPlan(plan)"
            :class="[
              'relative rounded-lg border-2 p-6 cursor-pointer transition-all',
              selectedPlan?.id === plan.id 
                ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20' 
                : 'border-gray-200 dark:border-gray-600 hover:border-gray-300 dark:hover:border-gray-500',
              plan.id === currentPlanId ? 'opacity-75 cursor-not-allowed' : ''
            ]"
          >
            <!-- Current Plan Badge -->
            <div v-if="plan.id === currentPlanId" class="absolute -top-3 left-1/2 transform -translate-x-1/2">
              <span class="bg-gray-600 text-white px-3 py-1 rounded-full text-xs font-medium">
                Current Plan
              </span>
            </div>

            <!-- Popular Badge -->
            <div v-if="plan.isPopular && plan.id !== currentPlanId" class="absolute -top-3 left-1/2 transform -translate-x-1/2">
              <span class="bg-green-600 text-white px-3 py-1 rounded-full text-xs font-medium">
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
            <div class="space-y-3 mb-6">
              <div 
                v-for="feature in plan.features" 
                :key="feature.name"
                class="flex items-start gap-2"
              >
                <Icon 
                  :icon="getFeatureIcon(feature, currentPlanFeatures)" 
                  :class="getFeatureIconClass(feature, currentPlanFeatures)"
                  class="mt-0.5"
                />
                <div>
                  <p :class="getFeatureTextClass(feature, currentPlanFeatures)">
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
              :disabled="plan.id === currentPlanId"
              :class="[
                'w-full px-4 py-3 rounded-lg font-medium transition-colors',
                selectedPlan?.id === plan.id 
                  ? 'bg-blue-600 text-white hover:bg-blue-700' 
                  : 'bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-200 dark:hover:bg-gray-600',
                plan.id === currentPlanId ? 'cursor-not-allowed opacity-50' : ''
              ]"
            >
              {{ getButtonText(plan) }}
            </button>
          </div>
        </div>
      </div>

      <!-- Selected Plan Summary -->
      <div v-if="selectedPlan && selectedPlan.id !== currentPlanId" class="p-6 border-t border-gray-200 dark:border-gray-700">
        <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
          Plan Change Summary
        </h4>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <!-- Current Plan Info -->
          <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
            <h5 class="font-medium text-gray-900 dark:text-gray-100 mb-3">Current Plan</h5>
            <div class="space-y-2">
              <div class="flex justify-between text-sm">
                <span class="text-gray-600 dark:text-gray-400">Plan:</span>
                <span class="font-medium text-gray-900 dark:text-gray-100">
                  {{ currentPlan?.name || 'Unknown' }}
                </span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-gray-600 dark:text-gray-400">Price:</span>
                <span class="font-medium text-gray-900 dark:text-gray-100">
                  ${{ formatCurrency(currentPlan?.monthlyPrice) }}/month
                </span>
              </div>
            </div>
          </div>

          <!-- New Plan Info -->
          <div class="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-4">
            <h5 class="font-medium text-blue-900 dark:text-blue-100 mb-3">New Plan</h5>
            <div class="space-y-2">
              <div class="flex justify-between text-sm">
                <span class="text-blue-600 dark:text-blue-400">Plan:</span>
                <span class="font-medium text-blue-900 dark:text-blue-100">
                  {{ selectedPlan.name }}
                </span>
              </div>
              <div class="flex justify-between text-sm">
                <span class="text-blue-600 dark:text-blue-400">Price:</span>
                <span class="font-medium text-blue-900 dark:text-blue-100">
                  ${{ getPlanPrice(selectedPlan) }}/month
                </span>
              </div>
            </div>
          </div>
        </div>

        <!-- Changes Summary -->
        <div class="mt-6 p-4 bg-yellow-50 dark:bg-yellow-900/20 rounded-lg">
          <h5 class="font-medium text-yellow-900 dark:text-yellow-100 mb-3">What Changes</h5>
          <div class="space-y-2">
            <div 
              v-for="change in getPlanChanges(selectedPlan)" 
              :key="change.feature"
              class="flex items-center gap-2 text-sm"
            >
              <Icon :icon="change.type === 'upgrade' ? 'ic:baseline-arrow-upward' : 'ic:baseline-arrow-downward'" 
                     :class="change.type === 'upgrade' ? 'text-green-600' : 'text-red-600'" />
              <span :class="change.type === 'upgrade' ? 'text-green-800 dark:text-green-200' : 'text-red-800 dark:text-red-200'">
                {{ change.feature }}: {{ change.from }} → {{ change.to }}
              </span>
            </div>
          </div>
        </div>

        <!-- Cost Impact -->
        <div class="mt-4 p-4 bg-gray-50 dark:bg-gray-700 rounded-lg">
          <div class="flex justify-between items-center">
            <span class="text-gray-600 dark:text-gray-400">Monthly Difference:</span>
            <span :class="getPriceDifferenceClass(selectedPlan)" class="font-bold text-lg">
              {{ getPriceDifferencePrefix(selectedPlan) }}${{ getPriceDifference(selectedPlan) }}/month
            </span>
          </div>
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
            @click="confirmSelection"
            :disabled="!canConfirm"
            class="flex-1 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center justify-center gap-2"
          >
            <Icon v-if="isConfirming" icon="ic:baseline-refresh" class="animate-spin" />
            {{ isConfirming ? 'Processing...' : 'Confirm Change' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  currentPlan: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'selected'])

// State
const selectedPlan = ref(null)
const selectedBillingCycle = ref('MONTHLY')
const isConfirming = ref(false)

// Billing cycles
const billingCycles = [
  { value: 'MONTHLY', label: 'Monthly' },
  { value: 'YEARLY', label: 'Yearly', discount: 20 }
]

// Available plans (in real app, these would come from API)
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

// Computed
const currentPlanId = computed(() => props.currentPlan?.id)
const currentPlanFeatures = computed(() => props.currentPlan?.features || [])

const canConfirm = computed(() => {
  return selectedPlan.value && 
         selectedPlan.value.id !== currentPlanId.value &&
         !isConfirming.value
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

const getFeatureIcon = (feature, currentFeatures) => {
  const currentFeature = currentFeatures.find(f => f.name === feature.name)
  if (!currentFeature) {
    return 'ic:baseline-add-circle' // New feature
  }
  if (feature.included && !currentFeature.included) {
    return 'ic:baseline-arrow-upward' // Upgrade
  }
  if (!feature.included && currentFeature.included) {
    return 'ic:baseline-arrow-downward' // Downgrade
  }
  return 'ic:baseline-check-circle' // Same
}

const getFeatureIconClass = (feature, currentFeatures) => {
  const currentFeature = currentFeatures.find(f => f.name === feature.name)
  if (!currentFeature) {
    return 'text-green-600 dark:text-green-400' // New feature
  }
  if (feature.included && !currentFeature.included) {
    return 'text-green-600 dark:text-green-400' // Upgrade
  }
  if (!feature.included && currentFeature.included) {
    return 'text-red-600 dark:text-red-400' // Downgrade
  }
  return 'text-gray-600 dark:text-gray-400' // Same
}

const getFeatureTextClass = (feature, currentFeatures) => {
  const currentFeature = currentFeatures.find(f => f.name === feature.name)
  if (!currentFeature) {
    return 'text-green-600 dark:text-green-400 font-medium' // New feature
  }
  return feature.included ? 'text-gray-900 dark:text-gray-100' : 'text-gray-400'
}

const getButtonText = (plan) => {
  if (plan.id === currentPlanId.value) {
    return 'Current Plan'
  }
  return selectedPlan.value?.id === plan.id ? 'Selected' : 'Select Plan'
}

const getPlanChanges = (newPlan) => {
  const changes = []
  
  newPlan.features.forEach(newFeature => {
    const currentFeature = currentPlanFeatures.value.find(f => f.name === newFeature.name)
    
    if (!currentFeature) {
      // New feature
      changes.push({
        feature: newFeature.name,
        type: 'upgrade',
        from: 'Not Available',
        to: newFeature.included ? 'Included' : 'Not Available'
      })
    } else if (newFeature.included !== currentFeature.included) {
      // Feature change
      changes.push({
        feature: newFeature.name,
        type: newFeature.included ? 'upgrade' : 'downgrade',
        from: currentFeature.included ? 'Included' : 'Not Available',
        to: newFeature.included ? 'Included' : 'Not Available'
      })
    }
  })
  
  return changes
}

const getPriceDifference = (plan) => {
  if (!props.currentPlan) return '0.00'
  const currentPrice = props.currentPlan.monthlyPrice / 100
  const newPrice = parseFloat(getPlanPrice(plan))
  return Math.abs(newPrice - currentPrice).toFixed(2)
}

const getPriceDifferencePrefix = (plan) => {
  if (!props.currentPlan) return ''
  const currentPrice = props.currentPlan.monthlyPrice / 100
  const newPrice = parseFloat(getPlanPrice(plan))
  return newPrice > currentPrice ? '+' : '-'
}

const getPriceDifferenceClass = (plan) => {
  if (!props.currentPlan) return 'text-gray-600'
  const currentPrice = props.currentPlan.monthlyPrice / 100
  const newPrice = parseFloat(getPlanPrice(plan))
  return newPrice > currentPrice ? 'text-red-600 dark:text-red-400' : 'text-green-600 dark:text-green-400'
}

const formatCurrency = (amount) => {
  if (!amount) return '0.00'
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount / 100) // Convert from cents
}

const selectPlan = (plan) => {
  if (plan.id !== currentPlanId.value) {
    selectedPlan.value = plan
  }
}

const confirmSelection = () => {
  if (!canConfirm.value) return
  
  isConfirming.value = true
  
  // Simulate API call
  setTimeout(() => {
    emit('selected', {
      plan: selectedPlan.value,
      billingCycle: selectedBillingCycle.value
    })
    isConfirming.value = false
  }, 1500)
}
</script>

<style scoped>
.modal-backdrop {
  backdrop-filter: blur(4px);
}

.plan-card {
  transition: all 0.2s ease-in-out;
}

.plan-card:hover:not(.opacity-75) {
  transform: translateY(-4px);
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
}

.plan-card.selected {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.15);
}
</style>
