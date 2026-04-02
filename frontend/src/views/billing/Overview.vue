<template>
  <div class="billing-overview min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- Header with Subscription Status -->
    <div class="bg-gradient-to-r from-primary-600 to-primary-700 dark:from-primary-800 dark:to-primary-900">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-white">Billing Overview</h1>
            <p class="mt-1 text-primary-100">
              Manage your subscription and payment methods
            </p>
          </div>
          <div class="flex items-center space-x-4">
            <div class="text-right">
              <p class="text-primary-100 text-sm">Current Plan</p>
              <p class="text-white font-semibold">{{ currentPlan?.name || 'Free' }}</p>
            </div>
            <button
              @click="showUpgradeModal = true"
              class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-primary-600 bg-white hover:bg-primary-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-white"
            >
              <Icon icon="mdi:rocket-launch" class="w-4 h-4 mr-2" />
              Upgrade Plan
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Subscription Status Card -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-lg overflow-hidden mb-8">
        <div class="p-6">
          <div class="flex items-center justify-between mb-6">
            <div class="flex items-center space-x-4">
              <div class="w-12 h-12 bg-primary-100 dark:bg-primary-900 rounded-full flex items-center justify-center">
                <Icon :icon="getPlanIcon()" class="w-6 h-6 text-primary-600 dark:text-primary-400" />
              </div>
              <div>
                <h2 class="text-xl font-semibold text-gray-900 dark:text-white">
                  {{ currentPlan?.name || 'Free Plan' }}
                </h2>
                <p class="text-gray-500 dark:text-gray-400">
                  {{ currentPlan?.description || 'Basic features for getting started' }}
                </p>
              </div>
            </div>
            <div class="text-right">
              <p class="text-2xl font-bold text-gray-900 dark:text-white">
                {{ formatCurrency(currentPlan?.price || 0, currentPlan?.currency || 'USD') }}
                <span class="text-sm font-normal text-gray-500">/month</span>
              </p>
              <div class="flex items-center mt-1">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                      :class="getSubscriptionStatusClass()">
                  <Icon :icon="getSubscriptionStatusIcon()" class="w-3 h-3 mr-1" />
                  {{ subscription?.status || 'ACTIVE' }}
                </span>
              </div>
            </div>
          </div>

          <!-- Subscription Details -->
          <div class="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
            <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
              <div class="flex items-center justify-between mb-2">
                <span class="text-sm text-gray-500 dark:text-gray-400">Billing Cycle</span>
                <Icon icon="mdi:calendar-sync" class="w-4 h-4 text-gray-400" />
              </div>
              <p class="font-semibold text-gray-900 dark:text-white">
                {{ subscription?.billingCycle || 'Monthly' }}
              </p>
              <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">
                Next billing: {{ formatDate(subscription?.nextBillingDate) }}
              </p>
            </div>

            <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
              <div class="flex items-center justify-between mb-2">
                <span class="text-sm text-gray-500 dark:text-gray-400">Days Remaining</span>
                <Icon icon="mdi:clock-outline" class="w-4 h-4 text-gray-400" />
              </div>
              <p class="font-semibold text-gray-900 dark:text-white">
                {{ daysUntilExpiry }}
              </p>
              <div class="w-full bg-gray-200 dark:bg-gray-600 rounded-full h-2 mt-2">
                <div
                  class="bg-primary-600 h-2 rounded-full"
                  :style="{ width: `${progressPercentage}%` }"
                />
              </div>
            </div>

            <div class="bg-gray-50 dark:bg-gray-700 rounded-lg p-4">
              <div class="flex items-center justify-between mb-2">
                <span class="text-sm text-gray-500 dark:text-gray-400">Auto-Renewal</span>
                <Icon icon="mdi:autorenew" class="w-4 h-4 text-gray-400" />
              </div>
              <p class="font-semibold text-gray-900 dark:text-white">
                {{ subscription?.autoRenew ? 'Enabled' : 'Disabled' }}
              </p>
              <button
                @click="toggleAutoRenewal"
                class="text-xs text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300 mt-1"
              >
                {{ subscription?.autoRenew ? 'Disable' : 'Enable' }}
              </button>
            </div>
          </div>

          <!-- Quick Actions -->
          <div class="flex space-x-3">
            <button
              @click="showUpgradeModal = true"
              class="flex-1 flex items-center justify-center px-4 py-2 bg-primary-600 text-white font-medium rounded-lg hover:bg-primary-700 transition-colors"
            >
              <Icon icon="mdi:rocket-launch" class="w-4 h-4 mr-2" />
              Upgrade Plan
            </button>
            <button
              @click="showCancelModal = true"
              class="flex-1 flex items-center justify-center px-4 py-2 border border-gray-300 dark:border-gray-600 text-gray-700 dark:text-gray-300 font-medium rounded-lg hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
            >
              <Icon icon="mdi:close-circle-outline" class="w-4 h-4 mr-2" />
              Cancel Subscription
            </button>
          </div>
        </div>
      </div>

      <!-- Usage Overview -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
        <!-- Usage Limits -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">
            Usage Overview
          </h3>
          <div class="space-y-4">
            <UsageIndicator
              v-for="entitlement in keyEntitlements"
              :key="entitlement.id"
              :entitlement="entitlement"
              :usage="usage[entitlement.usageLimitType]"
            />
          </div>
          <button
            @click="$router.push('/billing/entitlements')"
            class="mt-4 w-full text-center text-sm text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300 font-medium"
          >
            View All Usage Details
            <Icon icon="mdi:arrow-right" class="w-4 h-4 ml-1 inline" />
          </button>
        </div>

        <!-- Payment Methods -->
        <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
          <div class="flex items-center justify-between mb-4">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">
              Payment Methods
            </h3>
            <button
              @click="showAddPaymentModal = true"
              class="text-sm text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300 font-medium"
            >
              Add New
              <Icon icon="mdi:plus" class="w-4 h-4 ml-1 inline" />
            </button>
          </div>
          
          <div class="space-y-3">
            <PaymentMethodCard
              v-for="method in paymentMethods"
              :key="method.id"
              :method="method"
              @default="setDefaultPayment"
              @remove="removePaymentMethod"
            />
          </div>
          
          <div v-if="paymentMethods.length === 0" class="text-center py-8">
            <Icon icon="mdi:credit-card-outline" class="w-12 h-12 text-gray-400 mx-auto mb-3" />
            <p class="text-gray-500 dark:text-gray-400">No payment methods</p>
            <button
              @click="showAddPaymentModal = true"
              class="mt-4 text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300 font-medium"
            >
              Add Payment Method
            </button>
          </div>
        </div>
      </div>

      <!-- Recent Invoices -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow">
        <div class="px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <div class="flex items-center justify-between">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white">
              Recent Invoices
            </h3>
            <button
              @click="$router.push('/billing/invoices')"
              class="text-sm text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300 font-medium"
            >
              View All
              <Icon icon="mdi:arrow-right" class="w-4 h-4 ml-1 inline" />
            </button>
          </div>
        </div>
        
        <div class="p-6">
          <div v-if="recentInvoices.length === 0" class="text-center py-8">
            <Icon icon="mdi:file-document-outline" class="w-12 h-12 text-gray-400 mx-auto mb-3" />
            <p class="text-gray-500 dark:text-gray-400">No invoices yet</p>
          </div>
          
          <div v-else class="space-y-4">
            <InvoiceItem
              v-for="invoice in recentInvoices"
              :key="invoice.id"
              :invoice="invoice"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- Modals -->
    <UpgradePlanModal
      v-if="showUpgradeModal"
      :current-plan="currentPlan"
      :available-plans="availablePlans"
      @close="showUpgradeModal = false"
      @upgrade="handleUpgrade"
    />
    
    <CancelSubscriptionModal
      v-if="showCancelModal"
      :subscription="subscription"
      @close="showCancelModal = false"
      @cancel="handleCancel"
    />
    
    <AddPaymentMethodModal
      v-if="showAddPaymentModal"
      @close="showAddPaymentModal = false"
      @add="handleAddPayment"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useBillingStore } from '@/stores/billingStore'
import UsageIndicator from '@/components/billing/UsageIndicator.vue'
import PaymentMethodCard from '@/components/billing/PaymentMethodCard.vue'
import InvoiceItem from '@/components/billing/InvoiceItem.vue'
import UpgradePlanModal from '@/components/billing/UpgradePlanModal.vue'
import CancelSubscriptionModal from '@/components/billing/CancelSubscriptionModal.vue'
import AddPaymentMethodModal from '@/components/billing/AddPaymentMethodModal.vue'

const billingStore = useBillingStore()

// State
const showUpgradeModal = ref(false)
const showCancelModal = ref(false)
const showAddPaymentModal = ref(false)

// Computed
const subscription = computed(() => billingStore.subscription)
const currentPlan = computed(() => billingStore.currentPlan)
const availablePlans = computed(() => billingStore.availablePlans)
const entitlements = computed(() => billingStore.entitlements)
const usage = computed(() => billingStore.usage)
const paymentMethods = computed(() => billingStore.paymentMethods)
const recentInvoices = computed(() => billingStore.recentInvoices)

// Key entitlements to show
const keyEntitlements = computed(() => {
  return entitlements.value.slice(0, 4) // Show top 4 most important
})

const daysUntilExpiry = computed(() => {
  if (!subscription.value?.endsAt) return null
  const today = new Date()
  const expiryDate = new Date(subscription.value.endsAt)
  const diffTime = Math.abs(expiryDate - today)
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24))
})

const progressPercentage = computed(() => {
  if (!subscription.value?.startsAt || !subscription.value?.endsAt) return 0
  const total = new Date(subscription.value.endsAt) - new Date(subscription.value.startsAt)
  const elapsed = new Date() - new Date(subscription.value.startsAt)
  return Math.min(Math.round((elapsed / total) * 100), 100)
})

// Methods
const loadData = async () => {
  console.log('🔄 [Overview] Loading billing data...')
  try {
    await billingStore.fetchSubscription()
    console.log('✅ [Overview] Billing data loaded')
  } catch (error) {
    console.error('❌ [Overview] Error loading billing data:', error)
  }
}

// Load data on mount
onMounted(() => {
  loadData()
})

const getPlanIcon = () => {
  const planIcons = {
    FREE: 'mdi:gift-outline',
    STARTER: 'mdi:rocket-outline',
    PROFESSIONAL: 'mdi:star-outline',
    ENTERPRISE: 'mdi:office-building-outline',
    CUSTOM: 'mdi:cog-outline'
  }
  return planIcons[currentPlan.value?.name] || 'mdi:help-circle-outline'
}

const getSubscriptionStatusClass = () => {
  const statusClasses = {
    ACTIVE: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    TRIAL: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
    SUSPENDED: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
    CANCELLED: 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200'
  }
  return statusClasses[subscription.value?.status] || 'bg-gray-100 text-gray-800'
}

const getSubscriptionStatusIcon = () => {
  const statusIcons = {
    ACTIVE: 'mdi:check-circle',
    TRIAL: 'mdi:clock-outline',
    SUSPENDED: 'mdi:pause-circle',
    CANCELLED: 'mdi:close-circle'
  }
  return statusIcons[subscription.value?.status] || 'mdi:help-circle'
}

const formatCurrency = (amount, currency) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: currency
  }).format(amount)
}

const formatDate = (date) => {
  if (!date) return 'N/A'
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  }).format(new Date(date))
}

const toggleAutoRenewal = async () => {
  await billingStore.toggleAutoRenewal(!subscription.value.autoRenew)
}

const setDefaultPayment = async (methodId) => {
  await billingStore.setDefaultPaymentMethod(methodId)
}

const removePaymentMethod = async (methodId) => {
  await billingStore.removePaymentMethod(methodId)
}

const handleUpgrade = async (planId) => {
  await billingStore.upgradeSubscription(planId)
  showUpgradeModal.value = false
}

const handleCancel = async (reason) => {
  await billingStore.cancelSubscription(reason)
  showCancelModal.value = false
}

const handleAddPayment = async (paymentData) => {
  await billingStore.addPaymentMethod(paymentData)
  showAddPaymentModal.value = false
}

// Lifecycle
onMounted(() => {
  billingStore.fetchSubscription()
  billingStore.fetchEntitlements()
  billingStore.fetchUsage()
  billingStore.fetchPaymentMethods()
  billingStore.fetchRecentInvoices()
})
</script>
