<template>
  <div class="tenant-billing p-6">
    <!-- Header -->
    <div class="mb-8">
      <div class="flex justify-between items-start">
        <div>
          <h1 class="text-3xl font-bold text-gray-900 dark:text-gray-100 mb-2">
            {{ currentTenant?.name }} - Billing
          </h1>
          <p class="text-gray-600 dark:text-gray-400">
            Manage subscriptions, usage, and payments
          </p>
        </div>
        
        <!-- Quick Actions -->
        <div class="flex gap-3">
          <button 
            @click="openUpgradeModal"
            class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors flex items-center gap-2"
          >
            <Icon icon="ic:baseline-upgrade" />
            Upgrade Plan
          </button>
          <button 
            @click="openTopupModal"
            class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors flex items-center gap-2"
          >
            <Icon icon="ic:baseline-add-circle" />
            Top Up
          </button>
        </div>
      </div>
    </div>
    
    <!-- Billing Overview Cards -->
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
      <!-- Current Plan -->
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center justify-between mb-4">
          <div class="p-2 bg-blue-100 dark:bg-blue-900/20 rounded-lg">
            <Icon icon="ic:baseline-subscriptions" class="text-blue-600 dark:text-blue-400 text-xl" />
          </div>
          <span :class="planStatusClass" class="px-2 py-1 rounded-full text-xs font-medium">
            {{ currentSubscription?.status || 'NO_PLAN' }}
          </span>
        </div>
        <h3 class="text-sm font-medium text-gray-600 dark:text-gray-400 mb-1">Current Plan</h3>
        <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">
          {{ currentSubscription?.plan || 'Free' }}
        </p>
        <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
          ${{ currentSubscription?.monthlyPrice || 0 }}/month
        </p>
      </div>
      
      <!-- Monthly Usage -->
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center justify-between mb-4">
          <div class="p-2 bg-green-100 dark:bg-green-900/20 rounded-lg">
            <Icon icon="ic:baseline-trending-up" class="text-green-600 dark:text-green-400 text-xl" />
          </div>
          <span class="text-xs text-gray-500 dark:text-gray-400">
            {{ new Date().toLocaleDateString('en-US', { month: 'short' }) }}
          </span>
        </div>
        <h3 class="text-sm font-medium text-gray-600 dark:text-gray-400 mb-1">Monthly Usage</h3>
        <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">
          ${{ monthlyUsage }}
        </p>
        <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
          {{ usagePercentage }}% of limit
        </p>
      </div>
      
      <!-- Wallet Balance -->
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center justify-between mb-4">
          <div class="p-2 bg-purple-100 dark:bg-purple-900/20 rounded-lg">
            <Icon icon="ic:baseline-account-balance-wallet" class="text-purple-600 dark:text-purple-400 text-xl" />
          </div>
          <button 
            @click="refreshWallet"
            class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300"
          >
            <Icon icon="ic:baseline-refresh" />
          </button>
        </div>
        <h3 class="text-sm font-medium text-gray-600 dark:text-gray-400 mb-1">Wallet Balance</h3>
        <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">
          ${{ walletBalance }}
        </p>
        <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
          Available funds
        </p>
      </div>
      
      <!-- Active Features -->
      <div class="bg-white dark:bg-gray-800 rounded-lg p-6 border border-gray-200 dark:border-gray-700">
        <div class="flex items-center justify-between mb-4">
          <div class="p-2 bg-orange-100 dark:bg-orange-900/20 rounded-lg">
            <Icon icon="ic:baseline-apps" class="text-orange-600 dark:text-orange-400 text-xl" />
          </div>
          <span v-if="nearLimitCount > 0" class="text-xs text-yellow-600 dark:text-yellow-400">
            {{ nearLimitCount }} near limit
          </span>
        </div>
        <h3 class="text-sm font-medium text-gray-600 dark:text-gray-400 mb-1">Active Features</h3>
        <p class="text-2xl font-bold text-gray-900 dark:text-gray-100">
          {{ activeEntitlements }}
        </p>
        <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">
          {{ overLimitCount }} over limit
        </p>
      </div>
    </div>
    
    <!-- Tabs Navigation -->
    <div class="bg-white dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700 mb-6">
      <div class="flex border-b border-gray-200 dark:border-gray-700">
        <button 
          v-for="tab in tabs" 
          :key="tab.key"
          @click="activeTab = tab.key"
          :class="[
            'px-6 py-3 text-sm font-medium border-b-2 transition-colors',
            activeTab === tab.key 
              ? 'border-blue-500 text-blue-600 dark:text-blue-400' 
              : 'border-transparent text-gray-600 dark:text-gray-400 hover:text-gray-900 dark:hover:text-gray-200'
          ]"
        >
          <div class="flex items-center gap-2">
            <Icon :icon="tab.icon" />
            <span>{{ tab.label }}</span>
            <span v-if="tab.count" class="bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 px-2 py-0.5 rounded-full text-xs">
              {{ tab.count }}
            </span>
          </div>
        </button>
      </div>
    </div>
    
    <!-- Tab Content -->
    <div class="min-h-[400px]">
      <!-- Overview Tab -->
      <div v-if="activeTab === 'overview'" class="space-y-6">
        <!-- Entitlements Grid -->
        <div>
          <h2 class="text-xl font-semibold text-gray-900 dark:text-gray-100 mb-4">Feature Usage</h2>
          <div v-if="billingStore.isLoading" class="text-center py-8">
            <Icon icon="ic:baseline-refresh" class="animate-spin text-4xl text-gray-400" />
            <p class="text-gray-600 dark:text-gray-400 mt-2">Loading entitlements...</p>
          </div>
          <div v-else-if="entitlements.length === 0" class="text-center py-8">
            <Icon icon="ic:baseline-inbox" class="text-4xl text-gray-400" />
            <p class="text-gray-600 dark:text-gray-400 mt-2">No entitlements found</p>
          </div>
          <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            <EntitlementCard 
              v-for="entitlement in entitlements" 
              :key="entitlement.id"
              :entitlement="entitlement"
              @view-details="viewEntitlementDetails"
              @upgrade-limit="upgradeEntitlementLimit"
            />
          </div>
        </div>
      </div>
      
      <!-- Subscriptions Tab -->
      <div v-if="activeTab === 'subscriptions'">
        <SubscriptionsManager />
      </div>
      
      <!-- Wallet Tab -->
      <div v-if="activeTab === 'wallet'">
        <WalletManager />
      </div>
      
      <!-- Invoices Tab -->
      <div v-if="activeTab === 'invoices'">
        <InvoicesList />
      </div>
    </div>
    
    <!-- Modals -->
    <UpgradePlanModal 
      v-if="showUpgradeModal"
      :current-subscription="currentSubscription"
      @close="showUpgradeModal = false"
      @upgraded="onPlanUpgraded"
    />
    
    <TopupModal 
      v-if="showTopupModal"
      :current-wallet="currentWallet"
      @close="showTopupModal = false"
      @topped-up="onToppedUp"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useGatewayTenantStore } from '@/stores/tenant/gateway/myTenantStore'
import { useBillingStore } from '@/stores/billing/billingStore'
import { useWalletStore } from '@/stores/wallet/walletStore'
import EntitlementCard from '@/components/billing/EntitlementCard.vue'
import SubscriptionsManager from '@/components/billing/SubscriptionsManager.vue'
import WalletManager from '@/components/wallet/WalletManager.vue'
import InvoicesList from '@/components/billing/InvoicesList.vue'
import UpgradePlanModal from '@/components/billing/UpgradePlanModal.vue'
import TopupModal from '@/components/wallet/TopupModal.vue'

// Stores
const tenantStore = useGatewayTenantStore()
const billingStore = useBillingStore()
const walletStore = useWalletStore()

// State
const activeTab = ref('overview')
const showUpgradeModal = ref(false)
const showTopupModal = ref(false)

// Computed
const currentTenant = computed(() => tenantStore.currentTenant)
const currentSubscription = computed(() => billingStore.currentSubscription)
const entitlements = computed(() => billingStore.entitlements)
const currentWallet = computed(() => walletStore.currentWallet)

const monthlyUsage = computed(() => {
  return billingStore.totalMonthlyCost?.toFixed(2) || '0.00'
})

const walletBalance = computed(() => {
  return (walletStore.totalBalance / 100).toFixed(2) || '0.00'
})

const activeEntitlements = computed(() => {
  return entitlements.value.filter(e => e.isEnabled).length
})

const nearLimitCount = computed(() => {
  return billingStore.nearLimitEntitlements.length
})

const overLimitCount = computed(() => {
  return billingStore.overLimitEntitlements.length
})

const usagePercentage = computed(() => {
  if (!currentSubscription.value?.monthlyLimit) return 0
  return ((billingStore.totalMonthlyCost / currentSubscription.value.monthlyLimit) * 100).toFixed(1)
})

const planStatusClass = computed(() => {
  const status = currentSubscription.value?.status?.toUpperCase()
  switch (status) {
    case 'ACTIVE': return 'bg-green-100 text-green-600 dark:bg-green-900/20 dark:text-green-400'
    case 'TRIAL': return 'bg-blue-100 text-blue-600 dark:bg-blue-900/20 dark:text-blue-400'
    case 'EXPIRED': return 'bg-red-100 text-red-600 dark:bg-red-900/20 dark:text-red-400'
    case 'CANCELLED': return 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400'
    default: return 'bg-gray-100 text-gray-600 dark:bg-gray-900/20 dark:text-gray-400'
  }
})

const tabs = computed(() => [
  {
    key: 'overview',
    label: 'Overview',
    icon: 'ic:baseline-dashboard',
    count: entitlements.value.length
  },
  {
    key: 'subscriptions',
    label: 'Subscriptions',
    icon: 'ic:baseline-subscriptions',
    count: billingStore.activeSubscriptions.length
  },
  {
    key: 'wallet',
    label: 'Wallet',
    icon: 'ic:baseline-account-balance-wallet',
    count: walletStore.wallets.length
  },
  {
    key: 'invoices',
    label: 'Invoices',
    icon: 'ic:baseline-receipt-long',
    count: billingStore.invoices.length
  }
])

// Methods
const loadBillingData = async () => {
  if (!currentTenant.value?.id) return
  
  try {
    await Promise.all([
      billingStore.fetchTenantBilling(currentTenant.value.id),
      walletStore.fetchWallets(currentTenant.value.id),
      walletStore.fetchTransactions(walletStore.currentWallet?.id || '')
    ])
  } catch (error) {
    console.error('Failed to load billing data:', error)
  }
}

const refreshWallet = async () => {
  if (!currentTenant.value?.id) return
  await walletStore.fetchWallets(currentTenant.value.id)
}

const openUpgradeModal = () => {
  showUpgradeModal.value = true
}

const openTopupModal = () => {
  showTopupModal.value = true
}

const viewEntitlementDetails = (entitlement) => {
  // Navigate to entitlement details or open modal
  console.log('View entitlement details:', entitlement)
}

const upgradeEntitlementLimit = (entitlement) => {
  // Handle entitlement limit upgrade
  console.log('Upgrade entitlement limit:', entitlement)
}

const onPlanUpgraded = (newSubscription) => {
  // Refresh billing data after upgrade
  loadBillingData()
  showUpgradeModal.value = false
}

const onToppedUp = (transaction) => {
  // Refresh wallet data after topup
  loadBillingData()
  showTopupModal.value = false
}

// Lifecycle
onMounted(() => {
  loadBillingData()
})

// Watch for tenant changes
watch(currentTenant, (newTenant) => {
  if (newTenant?.id) {
    loadBillingData()
  }
}, { immediate: true })
</script>
