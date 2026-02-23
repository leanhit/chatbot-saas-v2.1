<template>
  <div class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
    <div class="bg-white dark:bg-gray-800 rounded-lg max-w-4xl w-full mx-4 max-h-[90vh] overflow-y-auto">
      <!-- Header -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="flex justify-between items-center">
          <div>
            <h3 class="text-xl font-semibold text-gray-900 dark:text-gray-100">
              Upgrade Entitlement
            </h3>
            <p class="text-gray-600 dark:text-gray-400">
              {{ entitlement.featureName }}
            </p>
          </div>
          <button 
            @click="$emit('close')"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="ic:baseline-close" class="text-xl" />
          </button>
        </div>
      </div>

      <!-- Current Entitlement Info -->
      <div class="p-6 border-b border-gray-200 dark:border-gray-700">
        <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
          <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
            Current Entitlement
          </h4>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div>
              <p class="text-sm text-gray-600 dark:text-gray-400">Current Limit</p>
              <p class="text-lg font-medium text-gray-900 dark:text-gray-100">
                {{ formatNumber(entitlement.limit) }} {{ entitlement.unit }}
              </p>
            </div>
            <div>
              <p class="text-sm text-gray-600 dark:text-gray-400">Current Usage</p>
              <p class="text-lg font-medium text-gray-900 dark:text-gray-100">
                {{ formatNumber(entitlement.currentUsage) }} {{ entitlement.unit }}
              </p>
            </div>
            <div>
              <p class="text-sm text-gray-600 dark:text-gray-400">Monthly Cost</p>
              <p class="text-lg font-medium text-gray-900 dark:text-gray-100">
                ${{ formatCurrency(entitlement.monthlyCost) }}
              </p>
            </div>
          </div>
          
          <!-- Current Usage Meter -->
          <div class="mt-4">
            <UsageMeter 
              :used="entitlement.currentUsage"
              :limit="entitlement.limit"
              :unit="entitlement.unit"
              :show-status="true"
            />
          </div>
        </div>
      </div>

      <!-- Upgrade Options -->
      <div class="p-6">
        <h4 class="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">
          Upgrade Options
        </h4>
        
        <!-- Upgrade Type Selection -->
        <div class="mb-6">
          <div class="flex gap-4">
            <button 
              v-for="type in upgradeTypes" 
              :key="type.id"
              @click="selectedUpgradeType = type.id"
              :class="[
                'flex-1 p-4 rounded-lg border-2 transition-all',
                selectedUpgradeType === type.id 
                  ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20' 
                  : 'border-gray-200 dark:border-gray-600 hover:border-gray-300 dark:hover:border-gray-500'
              ]"
            >
              <div class="text-center">
                <Icon :icon="type.icon" :class="type.iconClass" class="text-2xl mb-2" />
                <h5 class="font-semibold text-gray-900 dark:text-gray-100 mb-1">
                  {{ type.name }}
                </h5>
                <p class="text-sm text-gray-600 dark:text-gray-400">
                  {{ type.description }}
                </p>
              </div>
            </button>
          </div>
        </div>

        <!-- Tier Selection -->
        <div v-if="selectedUpgradeType === 'TIER'" class="mb-6">
          <h5 class="font-medium text-gray-900 dark:text-gray-100 mb-3">Select Tier</h5>
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div 
              v-for="tier in availableTiers" 
              :key="tier.id"
              @click="selectedTier = tier.id"
              :class="[
                'p-4 rounded-lg border-2 cursor-pointer transition-all',
                selectedTier === tier.id 
                  ? 'border-blue-500 bg-blue-50 dark:bg-blue-900/20' 
                  : 'border-gray-200 dark:border-gray-600 hover:border-gray-300 dark:hover:border-gray-500'
              ]"
            >
              <h6 class="font-semibold text-gray-900 dark:text-gray-100 mb-2">
                {{ tier.name }}
              </h6>
              <p class="text-2xl font-bold text-blue-600 dark:text-blue-400 mb-2">
                ${{ formatCurrency(tier.price) }}
                <span class="text-sm text-gray-600 dark:text-gray-400">/month</span>
              </p>
              <p class="text-sm text-gray-600 dark:text-gray-400 mb-3">
                {{ tier.limit }} {{ entitlement.unit }}
              </p>
              <ul class="text-sm text-gray-600 dark:text-gray-400 space-y-1">
                <li v-for="feature in tier.features" :key="feature" class="flex items-center gap-1">
                  <Icon icon="ic:baseline-check" class="text-green-500" />
                  {{ feature }}
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Custom Limit -->
        <div v-if="selectedUpgradeType === 'CUSTOM'" class="mb-6">
          <h5 class="font-medium text-gray-900 dark:text-gray-100 mb-3">Custom Limit</h5>
          <div class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
                Custom Limit ({{ entitlement.unit }})
              </label>
              <input 
                v-model="customLimit"
                type="number"
                :min="entitlement.currentUsage"
                :step="getStepSize()"
                class="w-full px-3 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-900 dark:text-gray-100 focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                :class="{ 'border-red-300 dark:border-red-600': errors.customLimit }"
              />
              <p v-if="errors.customLimit" class="text-red-600 dark:text-red-400 text-sm mt-1">
                {{ errors.customLimit }}
              </p>
            </div>
            
            <!-- Cost Calculation -->
            <div class="bg-blue-50 dark:bg-blue-900/20 rounded-lg p-4">
              <h6 class="font-medium text-blue-900 dark:text-blue-100 mb-2">Cost Calculation</h6>
              <div class="space-y-2 text-sm">
                <div class="flex justify-between">
                  <span class="text-blue-700 dark:text-blue-300">Additional Units:</span>
                  <span class="text-blue-900 dark:text-blue-100 font-medium">
                    {{ formatNumber(customLimit - entitlement.limit) }} {{ entitlement.unit }}
                  </span>
                </div>
                <div class="flex justify-between">
                  <span class="text-blue-700 dark:text-blue-300">Cost per Unit:</span>
                  <span class="text-blue-900 dark:text-blue-100 font-medium">
                    ${{ formatCurrency(entitlement.costPerUnit) }}
                  </span>
                </div>
                <div class="flex justify-between font-semibold text-base border-t border-blue-200 dark:border-blue-700 pt-2">
                  <span class="text-blue-900 dark:text-blue-100">Additional Monthly Cost:</span>
                  <span class="text-blue-900 dark:text-blue-100">
                    ${{ formatCurrency(getAdditionalCost()) }}
                  </span>
                </div>
                <div class="flex justify-between font-semibold text-base">
                  <span class="text-blue-900 dark:text-blue-100">New Monthly Total:</span>
                  <span class="text-blue-900 dark:text-blue-100">
                    ${{ formatCurrency(getNewMonthlyTotal()) }}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Pay-as-you-go -->
        <div v-if="selectedUpgradeType === 'PAYG'" class="mb-6">
          <div class="bg-yellow-50 dark:bg-yellow-900/20 rounded-lg p-4">
            <div class="flex items-start gap-3">
              <Icon icon="ic:baseline-info" class="text-yellow-600 dark:text-yellow-400 mt-0.5" />
              <div class="text-sm text-yellow-800 dark:text-yellow-200">
                <p class="font-medium mb-1">Pay-as-you-go</p>
                <p>You'll be charged ${{ formatCurrency(entitlement.costPerUnit) }} for each additional {{ entitlement.unit }} beyond your current limit.</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Upgrade Summary -->
        <div v-if="selectedUpgradeType && getUpgradeSummary()" class="bg-green-50 dark:bg-green-900/20 rounded-lg p-4">
          <h6 class="font-medium text-green-900 dark:text-green-100 mb-2">Upgrade Summary</h6>
          <div class="space-y-2 text-sm">
            <div class="flex justify-between">
              <span class="text-green-700 dark:text-green-300">Current Limit:</span>
              <span class="text-green-900 dark:text-green-100 font-medium">
                {{ formatNumber(entitlement.limit) }} {{ entitlement.unit }}
              </span>
            </div>
            <div class="flex justify-between">
              <span class="text-green-700 dark:text-green-300">New Limit:</span>
              <span class="text-green-900 dark:text-green-100 font-medium">
                {{ getUpgradeSummary().newLimit }} {{ entitlement.unit }}
              </span>
            </div>
            <div class="flex justify-between">
              <span class="text-green-700 dark:text-green-300">Additional Cost:</span>
              <span class="text-green-900 dark:text-green-100 font-medium">
                ${{ formatCurrency(getUpgradeSummary().additionalCost) }}/month
              </span>
            </div>
            <div class="flex justify-between font-semibold text-base border-t border-green-200 dark:border-green-700 pt-2">
              <span class="text-green-900 dark:text-green-100">New Monthly Total:</span>
              <span class="text-green-900 dark:text-green-100">
                ${{ formatCurrency(getUpgradeSummary().newTotal) }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="p-6 border-t border-gray-200 dark:border-gray-700">
        <div class="flex justify-between">
          <div class="text-sm text-gray-600 dark:text-gray-400">
            <p>Changes will take effect immediately</p>
            <p>You can downgrade at any time</p>
          </div>
          
          <div class="flex gap-3">
            <button 
              @click="$emit('close')"
              class="px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg bg-white dark:bg-gray-700 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-600 transition-colors"
            >
              Cancel
            </button>
            
            <button 
              @click="confirmUpgrade"
              :disabled="!canUpgrade || isProcessing"
              class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors flex items-center gap-2"
            >
              <Icon v-if="isProcessing" icon="ic:baseline-refresh" class="animate-spin" />
              {{ isProcessing ? 'Processing...' : 'Confirm Upgrade' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import UsageMeter from './UsageMeter.vue'

const props = defineProps({
  entitlement: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['close', 'upgraded'])

// State
const selectedUpgradeType = ref('')
const selectedTier = ref('')
const customLimit = ref('')
const isProcessing = ref(false)
const errors = ref({})

// Upgrade types
const upgradeTypes = [
  {
    id: 'TIER',
    name: 'Tier Upgrade',
    description: 'Upgrade to predefined tiers',
    icon: 'ic:baseline-arrow-upward',
    iconClass: 'text-blue-600 dark:text-blue-400'
  },
  {
    id: 'CUSTOM',
    name: 'Custom Limit',
    description: 'Set your own custom limit',
    icon: 'ic:baseline-tune',
    iconClass: 'text-purple-600 dark:text-purple-400'
  },
  {
    id: 'PAYG',
    name: 'Pay-as-you-go',
    description: 'Pay for additional usage',
    icon: 'ic:baseline-payments',
    iconClass: 'text-green-600 dark:text-green-400'
  }
]

// Available tiers
const availableTiers = ref([
  {
    id: 'PROFESSIONAL',
    name: 'Professional',
    price: 50,
    limit: 50000,
    features: ['Priority Support', 'Advanced Analytics', 'API Access']
  },
  {
    id: 'BUSINESS',
    name: 'Business',
    price: 100,
    limit: 100000,
    features: ['Priority Support', 'Advanced Analytics', 'API Access', 'Custom Integrations']
  },
  {
    id: 'ENTERPRISE',
    name: 'Enterprise',
    price: 250,
    limit: 250000,
    features: ['24/7 Support', 'Advanced Analytics', 'API Access', 'Custom Integrations', 'Dedicated Account Manager']
  }
])

// Computed
const canUpgrade = computed(() => {
  if (!selectedUpgradeType.value) return false
  
  if (selectedUpgradeType.value === 'TIER') {
    return selectedTier.value !== ''
  }
  
  if (selectedUpgradeType.value === 'CUSTOM') {
    return customLimit.value && !errors.value.customLimit
  }
  
  if (selectedUpgradeType.value === 'PAYG') {
    return true
  }
  
  return false
})

// Methods
const getStepSize = () => {
  // Determine appropriate step size based on entitlement type
  switch (props.entitlement.category) {
    case 'API':
      return 1000
    case 'STORAGE':
      return 10
    case 'USERS':
      return 1
    case 'BOTS':
      return 1
    case 'MESSAGING':
      return 1000
    default:
      return 1
  }
}

const getAdditionalCost = () => {
  if (selectedUpgradeType.value !== 'CUSTOM') return 0
  
  const additionalUnits = customLimit.value - props.entitlement.limit
  return additionalUnits * props.entitlement.costPerUnit
}

const getNewMonthlyTotal = () => {
  if (selectedUpgradeType.value === 'TIER') {
    const tier = availableTiers.value.find(t => t.id === selectedTier.value)
    return tier ? tier.price : props.entitlement.monthlyCost
  }
  
  if (selectedUpgradeType.value === 'CUSTOM') {
    return props.entitlement.monthlyCost + getAdditionalCost()
  }
  
  return props.entitlement.monthlyCost
}

const getUpgradeSummary = () => {
  if (!selectedUpgradeType.value) return null
  
  let newLimit = props.entitlement.limit
  let additionalCost = 0
  
  if (selectedUpgradeType.value === 'TIER') {
    const tier = availableTiers.value.find(t => t.id === selectedTier.value)
    if (tier) {
      newLimit = tier.limit
      additionalCost = tier.price - props.entitlement.monthlyCost
    }
  } else if (selectedUpgradeType.value === 'CUSTOM') {
    newLimit = customLimit.value
    additionalCost = getAdditionalCost()
  }
  
  return {
    newLimit: formatNumber(newLimit),
    additionalCost,
    newTotal: props.entitlement.monthlyCost + additionalCost
  }
}

const validateCustomLimit = () => {
  errors.value = {}
  
  if (!customLimit.value) {
    errors.value.customLimit = 'Custom limit is required'
    return false
  }
  
  const limit = parseInt(customLimit.value)
  
  if (isNaN(limit)) {
    errors.value.customLimit = 'Please enter a valid number'
    return false
  }
  
  if (limit <= props.entitlement.currentUsage) {
    errors.value.customLimit = `Limit must be greater than current usage (${formatNumber(props.entitlement.currentUsage)})`
    return false
  }
  
  if (limit <= props.entitlement.limit) {
    errors.value.customLimit = `Limit must be greater than current limit (${formatNumber(props.entitlement.limit)})`
    return false
  }
  
  return true
}

const formatNumber = (num) => {
  if (!num) return '0'
  return new Intl.NumberFormat('en-US').format(num)
}

const formatCurrency = (amount) => {
  if (!amount) return '0.00'
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount)
}

const confirmUpgrade = async () => {
  if (selectedUpgradeType.value === 'CUSTOM') {
    if (!validateCustomLimit()) return
  }
  
  if (!confirm('Are you sure you want to proceed with this upgrade?')) return
  
  isProcessing.value = true
  
  try {
    // Simulate API call
    await new Promise(resolve => setTimeout(resolve, 2000))
    
    // Create updated entitlement
    const updatedEntitlement = {
      ...props.entitlement,
      limit: selectedUpgradeType.value === 'TIER' 
        ? availableTiers.value.find(t => t.id === selectedTier.value)?.limit || props.entitlement.limit
        : selectedUpgradeType.value === 'CUSTOM'
        ? parseInt(customLimit.value)
        : props.entitlement.limit,
      monthlyCost: getNewMonthlyTotal(),
      updatedAt: new Date().toISOString()
    }
    
    emit('upgraded', updatedEntitlement)
    
  } catch (error) {
    console.error('Failed to upgrade entitlement:', error)
  } finally {
    isProcessing.value = false
  }
}
</script>

<style scoped>
.modal-backdrop {
  backdrop-filter: blur(4px);
}
</style>
