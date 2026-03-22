<template>
  <div class="entitlements-page min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- Header -->
    <div class="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Entitlements & Usage</h1>
            <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
              Manage your feature entitlements and track usage
            </p>
          </div>
          <div class="flex items-center space-x-3">
            <!-- Category Filter -->
            <select v-model="filterCategory" class="text-sm border rounded px-3 py-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
              <option value="">All Categories</option>
              <option value="core">Core Features</option>
              <option value="bot">Bot Features</option>
              <option value="integration">Integration</option>
              <option value="storage">Storage</option>
              <option value="analytics">Analytics</option>
            </select>

            <!-- Status Filter -->
            <select v-model="filterStatus" class="text-sm border rounded px-3 py-2 dark:bg-gray-700 dark:border-gray-600 dark:text-white">
              <option value="">All Status</option>
              <option value="enabled">Enabled</option>
              <option value="disabled">Disabled</option>
              <option value="limited">Limited</option>
            </select>

            <button
              @click="$router.push('/billing/overview')"
              class="flex items-center px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-md text-sm font-medium text-gray-700 dark:text-gray-300 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700"
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
      <!-- Usage Overview -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600 dark:text-gray-400">Total Features</p>
              <p class="text-2xl font-bold text-gray-900 dark:text-white">{{ totalEntitlements }}</p>
            </div>
            <div class="w-12 h-12 bg-blue-100 dark:bg-blue-900 rounded-full flex items-center justify-center">
              <Icon icon="mdi:package-variant-closed" class="w-6 h-6 text-blue-600 dark:text-blue-400" />
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600 dark:text-gray-400">Active Features</p>
              <p class="text-2xl font-bold text-green-600 dark:text-green-400">{{ activeEntitlements }}</p>
            </div>
            <div class="w-12 h-12 bg-green-100 dark:bg-green-900 rounded-full flex items-center justify-center">
              <Icon icon="mdi:check-circle" class="w-6 h-6 text-green-600 dark:text-green-400" />
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600 dark:text-gray-400">At Limit</p>
              <p class="text-2xl font-bold text-yellow-600 dark:text-yellow-400">{{ atLimitEntitlements }}</p>
            </div>
            <div class="w-12 h-12 bg-yellow-100 dark:bg-yellow-900 rounded-full flex items-center justify-center">
              <Icon icon="mdi:alert" class="w-6 h-6 text-yellow-600 dark:text-yellow-400" />
            </div>
          </div>
        </div>

        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm font-medium text-gray-600 dark:text-gray-400">Over Usage</p>
              <p class="text-2xl font-bold text-red-600 dark:text-red-400">{{ overUsageEntitlements }}</p>
            </div>
            <div class="w-12 h-12 bg-red-100 dark:bg-red-900 rounded-full flex items-center justify-center">
              <Icon icon="mdi:alert-circle" class="w-6 h-6 text-red-600 dark:text-red-400" />
            </div>
          </div>
        </div>
      </div>

      <!-- Entitlements List -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <div class="flex items-center justify-between">
            <h2 class="text-lg font-semibold text-gray-900 dark:text-white">
              Feature Entitlements
            </h2>
            <div class="flex items-center space-x-2">
              <span class="text-sm text-gray-500 dark:text-gray-400">
                {{ filteredEntitlements.length }} features
              </span>
              <button
                @click="refreshEntitlements"
                :disabled="loading"
                class="p-2 text-gray-400 hover:text-gray-500 dark:hover:text-gray-300"
              >
                <Icon :icon="loading ? 'mdi:loading' : 'mdi:refresh'" 
                      :class="loading ? 'animate-spin' : ''" class="w-4 h-4" />
              </button>
            </div>
          </div>
        </div>
        
        <div class="divide-y divide-gray-200 dark:divide-gray-700">
          <!-- Loading State -->
          <div v-if="loading" class="p-8 text-center">
            <Icon icon="mdi:loading" class="w-8 h-8 animate-spin text-primary-600 mx-auto mb-3" />
            <p class="text-gray-500 dark:text-gray-400">Loading entitlements...</p>
          </div>

          <!-- Empty State -->
          <div v-else-if="filteredEntitlements.length === 0" class="p-8 text-center">
            <Icon icon="mdi:package-variant" class="w-12 h-12 text-gray-400 mx-auto mb-3" />
            <p class="text-gray-500 dark:text-gray-400">No entitlements found</p>
            <p class="text-sm text-gray-400 dark:text-gray-500 mt-1">
              Try adjusting your filters or upgrade your plan for more features.
            </p>
          </div>

          <!-- Entitlement Items -->
          <EntitlementItem
            v-for="entitlement in filteredEntitlements"
            :key="entitlement.id"
            :entitlement="entitlement"
            :usage="usage[entitlement.usageLimitType]"
            @upgrade="handleUpgrade"
          />
        </div>
      </div>

      <!-- Usage Analytics -->
      <div class="mt-8 bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-6">Usage Analytics</h2>
        
        <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
          <!-- Usage Chart -->
          <div>
            <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-4">Usage Trends</h3>
            <div class="h-64 bg-gray-50 dark:bg-gray-700 rounded-lg flex items-center justify-center">
              <p class="text-gray-500 dark:text-gray-400">Usage chart would go here</p>
            </div>
          </div>

          <!-- Top Features -->
          <div>
            <h3 class="text-sm font-medium text-gray-700 dark:text-gray-300 mb-4">Most Used Features</h3>
            <div class="space-y-3">
              <div
                v-for="(feature, index) in topUsedFeatures"
                :key="feature.name"
                class="flex items-center justify-between"
              >
                <div class="flex items-center space-x-3">
                  <span class="text-sm font-medium text-gray-500 dark:text-gray-400 w-6">{{ index + 1 }}</span>
                  <span class="text-sm text-gray-900 dark:text-white">{{ feature.name }}</span>
                </div>
                <div class="flex items-center space-x-2">
                  <div class="w-32 bg-gray-200 dark:bg-gray-600 rounded-full h-2">
                    <div
                      class="bg-primary-600 h-2 rounded-full"
                      :style="{ width: `${feature.percentage}%` }"
                    />
                  </div>
                  <span class="text-sm text-gray-500 dark:text-gray-400 w-12 text-right">{{ feature.percentage }}%</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Plan Comparison -->
      <div class="mt-8 bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-6">Plan Comparison</h2>
        <div class="text-center">
          <button
            @click="showUpgradeModal = true"
            class="inline-flex items-center px-6 py-3 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
          >
            <Icon icon="mdi:rocket-launch" class="w-4 h-4 mr-2" />
            Compare Plans & Upgrade
          </button>
          <p class="mt-2 text-sm text-gray-500 dark:text-gray-400">
            See how different plans compare and choose the right one for your needs
          </p>
        </div>
      </div>
    </div>

    <!-- Upgrade Modal -->
    <UpgradePlanModal
      v-if="showUpgradeModal"
      :current-plan="currentPlan"
      :available-plans="availablePlans"
      @close="showUpgradeModal = false"
      @upgrade="handleUpgrade"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useBillingStore } from '@/stores/billingStore'
import EntitlementItem from '@/components/billing/EntitlementItem.vue'
import UpgradePlanModal from '@/components/billing/UpgradePlanModal.vue'

const billingStore = useBillingStore()

// State
const loading = ref(false)
const filterCategory = ref('')
const filterStatus = ref('')
const showUpgradeModal = ref(false)

// Computed
const entitlements = computed(() => billingStore.entitlements)
const usage = computed(() => billingStore.usage)
const currentPlan = computed(() => billingStore.currentPlan)
const availablePlans = computed(() => billingStore.availablePlans)

const filteredEntitlements = computed(() => {
  let filtered = entitlements.value

  // Filter by category
  if (filterCategory.value) {
    filtered = filtered.filter(e => {
      const feature = getFeatureInfo(e.feature)
      return feature.category === filterCategory.value
    })
  }

  // Filter by status
  if (filterStatus.value) {
    filtered = filtered.filter(e => {
      const status = getEntitlementStatus(e)
      return status === filterStatus.value
    })
  }

  return filtered
})

const totalEntitlements = computed(() => entitlements.value.length)
const activeEntitlements = computed(() => 
  entitlements.value.filter(e => e.isEnabled).length
)
const atLimitEntitlements = computed(() => {
  return entitlements.value.filter(e => {
    const currentUsage = usage.value[e.usageLimitType] || 0
    return currentUsage >= e.limitValue && e.limitValue > 0
  }).length
})
const overUsageEntitlements = computed(() => {
  return entitlements.value.filter(e => {
    const currentUsage = usage.value[e.usageLimitType] || 0
    return currentUsage > e.limitValue && e.limitValue > 0
  }).length
})

// Mock top used features (in real app, this would come from analytics)
const topUsedFeatures = computed(() => [
  { name: 'API Calls', percentage: 85 },
  { name: 'Messages', percentage: 72 },
  { name: 'Storage', percentage: 45 },
  { name: 'Users', percentage: 38 },
  { name: 'Bots', percentage: 25 }
])

// Methods
const getFeatureInfo = (feature) => {
  const featureMap = {
    USER_MANAGEMENT: { category: 'core', name: 'User Management' },
    TENANT_MANAGEMENT: { category: 'core', name: 'Tenant Management' },
    API_ACCESS: { category: 'core', name: 'API Access' },
    CHATBOT_CREATION: { category: 'bot', name: 'Chatbot Creation' },
    CHATBOT_CUSTOMIZATION: { category: 'bot', name: 'Chatbot Customization' },
    MULTILINGUAL_SUPPORT: { category: 'bot', name: 'Multilingual Support' },
    FACEBOOK_INTEGRATION: { category: 'integration', name: 'Facebook Integration' },
    WHATSAPP_INTEGRATION: { category: 'integration', name: 'WhatsApp Integration' },
    WEBSITE_WIDGET: { category: 'integration', name: 'Website Widget' },
    FILE_UPLOAD: { category: 'storage', name: 'File Upload' },
    CLOUD_STORAGE: { category: 'storage', name: 'Cloud Storage' },
    ANALYTICS: { category: 'analytics', name: 'Analytics' },
    CUSTOM_REPORTS: { category: 'analytics', name: 'Custom Reports' }
  }
  return featureMap[feature] || { category: 'other', name: feature }
}

const getEntitlementStatus = (entitlement) => {
  if (!entitlement.isEnabled) return 'disabled'
  
  const currentUsage = usage.value[entitlement.usageLimitType] || 0
  if (entitlement.limitValue === 0) return 'unlimited'
  if (currentUsage >= entitlement.limitValue) return 'limited'
  return 'enabled'
}

const handleUpgrade = async (planId) => {
  try {
    await billingStore.upgradeSubscription(planId)
    showUpgradeModal.value = false
  } catch (error) {
    console.error('Failed to upgrade plan:', error)
  }
}

const refreshEntitlements = async () => {
  loading.value = true
  try {
    await billingStore.fetchEntitlements()
    await billingStore.fetchUsage()
  } finally {
    loading.value = false
  }
}

// Lifecycle
onMounted(async () => {
  await refreshEntitlements()
})
</script>
