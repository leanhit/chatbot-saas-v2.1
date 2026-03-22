<template>
  <div class="subscription-page min-h-screen bg-gray-50 dark:bg-gray-900">
    <!-- Header -->
    <div class="bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700">
      <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-900 dark:text-white">Subscription</h1>
            <p class="mt-1 text-sm text-gray-500 dark:text-gray-400">
              Manage your subscription plan and billing
            </p>
          </div>
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

    <!-- Main Content -->
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <!-- Current Subscription -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow-lg overflow-hidden mb-8">
        <div class="bg-gradient-to-r from-primary-600 to-primary-700 dark:from-primary-800 dark:to-primary-900 p-6">
          <div class="flex items-center justify-between">
            <div>
              <h2 class="text-xl font-bold text-white">Current Plan</h2>
              <p class="text-primary-100 mt-1">{{ currentPlan?.name || 'Free Plan' }}</p>
            </div>
            <div class="text-right">
              <p class="text-3xl font-bold text-white">
                {{ formatCurrency(currentPlan?.price || 0, currentPlan?.currency || 'USD') }}
              </p>
              <p class="text-primary-100 text-sm">per {{ currentPlan?.billingCycle || 'month' }}</p>
            </div>
          </div>
        </div>

        <div class="p-6">
          <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
            <!-- Status -->
            <div class="text-center">
              <div class="w-16 h-16 mx-auto mb-3 rounded-full flex items-center justify-center"
                   :class="getStatusBackgroundClass()">
                <Icon :icon="getStatusIcon()" class="w-8 h-8" :class="getStatusIconClass()" />
              </div>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">Status</h3>
              <p class="text-gray-600 dark:text-gray-400">{{ subscription?.status || 'ACTIVE' }}</p>
            </div>

            <!-- Next Billing -->
            <div class="text-center">
              <div class="w-16 h-16 mx-auto mb-3 rounded-full bg-blue-100 dark:bg-blue-900 flex items-center justify-center">
                <Icon icon="mdi:calendar-clock" class="w-8 h-8 text-blue-600 dark:text-blue-400" />
              </div>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">Next Billing</h3>
              <p class="text-gray-600 dark:text-gray-400">{{ formatDate(subscription?.nextBillingDate) }}</p>
            </div>

            <!-- Auto-renewal -->
            <div class="text-center">
              <div class="w-16 h-16 mx-auto mb-3 rounded-full bg-green-100 dark:bg-green-900 flex items-center justify-center">
                <Icon icon="mdi:autorenew" class="w-8 h-8 text-green-600 dark:text-green-400" />
              </div>
              <h3 class="text-lg font-semibold text-gray-900 dark:text-white">Auto-renewal</h3>
              <p class="text-gray-600 dark:text-gray-400">{{ subscription?.autoRenew ? 'Enabled' : 'Disabled' }}</p>
              <button
                @click="toggleAutoRenewal"
                class="mt-2 text-sm text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300"
              >
                {{ subscription?.autoRenew ? 'Disable' : 'Enable' }}
              </button>
            </div>
          </div>

          <!-- Subscription Details -->
          <div class="mt-6 pt-6 border-t border-gray-200 dark:border-gray-700">
            <h3 class="text-lg font-semibold text-gray-900 dark:text-white mb-4">Subscription Details</h3>
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
              <div class="flex justify-between">
                <span class="text-gray-500 dark:text-gray-400">Subscription ID:</span>
                <span class="font-mono text-gray-900 dark:text-white">#{{ subscription?.id }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500 dark:text-gray-400">Started:</span>
                <span class="text-gray-900 dark:text-white">{{ formatDate(subscription?.startsAt) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500 dark:text-gray-400">Trial Ends:</span>
                <span class="text-gray-900 dark:text-white">{{ formatDate(subscription?.trialEndsAt) || 'N/A' }}</span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-500 dark:text-gray-400">Billing Method:</span>
                <span class="text-gray-900 dark:text-white">{{ subscription?.paymentMethod || 'Credit Card' }}</span>
              </div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="mt-6 flex space-x-3">
            <button
              @click="showUpgradeModal = true"
              class="flex-1 px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
            >
              <Icon icon="mdi:rocket-launch" class="w-4 h-4 mr-2" />
              Upgrade Plan
            </button>
            <button
              @click="showCancelModal = true"
              class="flex-1 px-4 py-2 border border-gray-300 dark:border-gray-600 rounded-lg text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 transition-colors"
            >
              <Icon icon="mdi:close-circle" class="w-4 h-4 mr-2" />
              Cancel Subscription
            </button>
          </div>
        </div>
      </div>

      <!-- Available Plans -->
      <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <h2 class="text-lg font-semibold text-gray-900 dark:text-white mb-6">Available Plans</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
          <div
            v-for="plan in availablePlans"
            :key="plan.id"
            :class="[
              'p-6 rounded-lg border-2 transition-all cursor-pointer',
              plan.id === currentPlan?.id
                ? 'border-primary-500 bg-primary-50 dark:bg-primary-900/20'
                : 'border-gray-200 dark:border-gray-600 hover:border-gray-300 dark:hover:border-gray-500'
            ]"
            @click="selectPlan(plan)"
          >
            <div class="text-center">
              <h3 class="text-xl font-bold text-gray-900 dark:text-white">{{ plan.name }}</h3>
              <p class="text-gray-600 dark:text-gray-400 mt-1">{{ plan.description }}</p>
              <div class="mt-4">
                <p class="text-3xl font-bold text-gray-900 dark:text-white">
                  {{ formatCurrency(plan.price, plan.currency) }}
                </p>
                <p class="text-sm text-gray-500 dark:text-gray-400">per {{ plan.billingCycle }}</p>
              </div>
              <div class="mt-4 space-y-2">
                <div v-for="feature in plan.features.slice(0, 4)" :key="feature" class="flex items-center text-sm text-left">
                  <Icon icon="mdi:check-circle" class="w-4 h-4 text-green-500 mr-2 flex-shrink-0" />
                  <span class="text-gray-700 dark:text-gray-300">{{ feature }}</span>
                </div>
              </div>
              <button
                v-if="plan.id !== currentPlan?.id"
                @click.stop="upgradeToPlan(plan)"
                class="mt-4 w-full px-4 py-2 bg-primary-600 text-white rounded-lg hover:bg-primary-700 transition-colors"
              >
                Select Plan
              </button>
              <div v-else class="mt-4 px-4 py-2 bg-green-100 dark:bg-green-900 text-green-800 dark:text-green-200 rounded-lg text-center">
                Current Plan
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Billing History -->
      <div class="mt-8 bg-white dark:bg-gray-800 rounded-lg shadow p-6">
        <div class="flex items-center justify-between mb-6">
          <h2 class="text-lg font-semibold text-gray-900 dark:text-white">Billing History</h2>
          <button
            @click="$router.push('/billing/invoices')"
            class="text-sm text-primary-600 dark:text-primary-400 hover:text-primary-700 dark:hover:text-primary-300"
          >
            View All Invoices
            <Icon icon="mdi:arrow-right" class="w-4 h-4 ml-1 inline" />
          </button>
        </div>
        <div class="space-y-3">
          <InvoiceItem
            v-for="invoice in recentInvoices.slice(0, 5)"
            :key="invoice.id"
            :invoice="invoice"
            compact
          />
        </div>
        <div v-if="recentInvoices.length === 0" class="text-center py-8">
          <Icon icon="mdi:receipt-text-outline" class="w-12 h-12 text-gray-400 mx-auto mb-3" />
          <p class="text-gray-500 dark:text-gray-400">No billing history</p>
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
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useBillingStore } from '@/stores/billingStore'
import InvoiceItem from '@/components/billing/InvoiceItem.vue'
import UpgradePlanModal from '@/components/billing/UpgradePlanModal.vue'
import CancelSubscriptionModal from '@/components/billing/CancelSubscriptionModal.vue'

const router = useRouter()
const billingStore = useBillingStore()

// State
const showUpgradeModal = ref(false)
const showCancelModal = ref(false)

// Computed
const subscription = computed(() => billingStore.subscription)
const currentPlan = computed(() => billingStore.currentPlan)
const availablePlans = computed(() => billingStore.availablePlans)
const recentInvoices = computed(() => billingStore.recentInvoices)

// Methods
const getStatusBackgroundClass = () => {
  const statusClasses = {
    ACTIVE: 'bg-green-100 dark:bg-green-900',
    TRIAL: 'bg-blue-100 dark:bg-blue-900',
    SUSPENDED: 'bg-red-100 dark:bg-red-900',
    CANCELLED: 'bg-gray-100 dark:bg-gray-900'
  }
  return statusClasses[subscription.value?.status] || 'bg-gray-100 dark:bg-gray-900'
}

const getStatusIcon = () => {
  const statusIcons = {
    ACTIVE: 'mdi:check-circle',
    TRIAL: 'mdi:clock-outline',
    SUSPENDED: 'mdi:pause-circle',
    CANCELLED: 'mdi:close-circle'
  }
  return statusIcons[subscription.value?.status] || 'mdi:help-circle'
}

const getStatusIconClass = () => {
  const statusClasses = {
    ACTIVE: 'text-green-600 dark:text-green-400',
    TRIAL: 'text-blue-600 dark:text-blue-400',
    SUSPENDED: 'text-red-600 dark:text-red-400',
    CANCELLED: 'text-gray-600 dark:text-gray-400'
  }
  return statusClasses[subscription.value?.status] || 'text-gray-600 dark:text-gray-400'
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
  try {
    await billingStore.toggleAutoRenewal(!subscription.value.autoRenew)
  } catch (error) {
    console.error('Failed to toggle auto renewal:', error)
  }
}

const selectPlan = (plan) => {
  if (plan.id !== currentPlan.value?.id) {
    showUpgradeModal.value = true
  }
}

const upgradeToPlan = async (plan) => {
  try {
    await billingStore.upgradeSubscription(plan.id)
    showUpgradeModal.value = false
  } catch (error) {
    console.error('Failed to upgrade plan:', error)
  }
}

const handleUpgrade = async (planId) => {
  try {
    await billingStore.upgradeSubscription(planId)
    showUpgradeModal.value = false
  } catch (error) {
    console.error('Failed to upgrade plan:', error)
  }
}

const handleCancel = async (reason) => {
  try {
    await billingStore.cancelSubscription(reason)
    showCancelModal.value = false
    // Redirect to overview after cancellation
    setTimeout(() => {
      router.push('/billing/overview')
    }, 2000)
  } catch (error) {
    console.error('Failed to cancel subscription:', error)
  }
}

// Lifecycle
onMounted(async () => {
  await billingStore.fetchSubscription()
  await billingStore.fetchAvailablePlans()
  await billingStore.fetchRecentInvoices()
})
</script>
