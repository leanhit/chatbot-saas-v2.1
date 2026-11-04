<template>
  <div class="p-6">
    <!-- Header -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
          <Icon icon="mdi:crown" class="text-amber-500" />
          {{ t('customer.vipQueue') }}
        </h1>
        <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">{{ t('customer.vipQueueDesc') }}</p>
      </div>
      <div class="flex gap-2">
        <select
          v-model="sortBy"
          class="px-3 py-2 text-sm border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white"
        >
          <option value="lastContact">{{ t('customer.sortByLastContact') }}</option>
          <option value="satisfaction">{{ t('customer.sortBySatisfaction') }}</option>
          <option value="conversations">{{ t('customer.sortByConversations') }}</option>
        </select>
      </div>
    </div>

    <!-- Summary cards -->
    <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
      <div class="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-200 dark:border-gray-700 shadow-sm">
        <p class="text-xs text-gray-500 dark:text-gray-400">Enterprise</p>
        <p class="text-2xl font-bold text-amber-600 dark:text-amber-400 mt-1">{{ enterpriseCount }}</p>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-200 dark:border-gray-700 shadow-sm">
        <p class="text-xs text-gray-500 dark:text-gray-400">Premium</p>
        <p class="text-2xl font-bold text-purple-600 dark:text-purple-400 mt-1">{{ premiumCount }}</p>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-200 dark:border-gray-700 shadow-sm">
        <p class="text-xs text-gray-500 dark:text-gray-400">{{ t('customer.slaWarning') }}</p>
        <p class="text-2xl font-bold text-red-600 dark:text-red-400 mt-1">{{ slaWarningCount }}</p>
      </div>
      <div class="bg-white dark:bg-gray-800 rounded-xl p-4 border border-gray-200 dark:border-gray-700 shadow-sm">
        <p class="text-xs text-gray-500 dark:text-gray-400">{{ t('customer.avgSatisfaction') }}</p>
        <p class="text-2xl font-bold text-blue-600 dark:text-blue-400 mt-1">{{ avgSatisfaction }}</p>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-12">
      <Icon icon="mdi:loading" class="animate-spin text-4xl text-blue-600" />
    </div>

    <!-- Empty -->
    <div
      v-else-if="sortedVIPs.length === 0"
      class="text-center py-16 text-gray-400 dark:text-gray-500"
    >
      <Icon icon="mdi:crown-off" class="text-5xl mb-3" />
      <p class="text-sm">{{ t('customer.noVIPCustomers') }}</p>
    </div>

    <!-- VIP Grid -->
    <div v-else class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
      <div
        v-for="customer in sortedVIPs"
        :key="customer.id"
        class="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm hover:shadow-md transition-shadow cursor-pointer overflow-hidden"
        @click="goToDetail(customer)"
      >
        <!-- SLA warning bar -->
        <div
          v-if="isSLAWarning(customer)"
          class="bg-red-500 text-white text-[10px] font-semibold text-center py-1 tracking-wide"
        >
          ⚠ SLA WARNING — {{ daysSinceContact(customer) }} ngày chưa liên hệ
        </div>

        <div class="p-5">
          <div class="flex items-start gap-3 mb-4">
            <!-- Avatar -->
            <div class="relative flex-shrink-0">
              <img
                v-if="customer.avatarUrl"
                :src="customer.avatarUrl"
                class="h-12 w-12 rounded-full object-cover"
              />
              <div
                v-else
                class="h-12 w-12 rounded-full flex items-center justify-center text-white font-bold text-lg"
                :class="customer.tier === 'ENTERPRISE' ? 'bg-gradient-to-br from-amber-400 to-orange-500' : 'bg-gradient-to-br from-purple-400 to-indigo-600'"
              >
                {{ customer.name?.charAt(0)?.toUpperCase() || '?' }}
              </div>
            </div>
            <!-- Info -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 flex-wrap">
                <span class="text-sm font-semibold text-gray-900 dark:text-white truncate">{{ customer.name }}</span>
                <VIPBadge :tier="customer.tier" />
              </div>
              <p class="text-xs text-gray-500 dark:text-gray-400 truncate mt-0.5">{{ customer.email || customer.externalId }}</p>
            </div>
            <!-- Status dot -->
            <span
              :class="statusDotClass(customer.status)"
              class="w-2.5 h-2.5 rounded-full flex-shrink-0 mt-1"
              :title="customer.status"
            />
          </div>

          <!-- Stats row -->
          <div class="grid grid-cols-3 gap-2 text-center">
            <div class="bg-gray-50 dark:bg-gray-700/50 rounded-lg py-2">
              <p class="text-xs text-gray-500 dark:text-gray-400">Hội thoại</p>
              <p class="text-sm font-bold text-gray-900 dark:text-white">{{ customer.totalConversations || 0 }}</p>
            </div>
            <div class="bg-gray-50 dark:bg-gray-700/50 rounded-lg py-2">
              <p class="text-xs text-gray-500 dark:text-gray-400">Satisfaction</p>
              <p class="text-sm font-bold text-gray-900 dark:text-white">
                {{ customer.satisfactionScore ? customer.satisfactionScore.toFixed(1) : '-' }}
              </p>
            </div>
            <div class="bg-gray-50 dark:bg-gray-700/50 rounded-lg py-2">
              <p class="text-xs text-gray-500 dark:text-gray-400">Liên hệ cuối</p>
              <p class="text-sm font-bold" :class="isSLAWarning(customer) ? 'text-red-600 dark:text-red-400' : 'text-gray-900 dark:text-white'">
                {{ daysSinceContact(customer) }}d
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'
import customerProfileApi from '@/api/customerProfileApi'
import VIPBadge from '@/components/customer/VIPBadge.vue'

const { t } = useI18n()
const router = useRouter()

const customers = ref([])
const loading = ref(true)
const sortBy = ref('lastContact')

// SLA threshold: warn if no contact for > 7 days
const SLA_DAYS = 7

const vipCustomers = computed(() =>
  customers.value.filter(c => c.tier === 'ENTERPRISE' || c.tier === 'PREMIUM')
)

const sortedVIPs = computed(() => {
  const list = [...vipCustomers.value]
  if (sortBy.value === 'lastContact') {
    return list.sort((a, b) => new Date(b.lastContactAt) - new Date(a.lastContactAt))
  }
  if (sortBy.value === 'satisfaction') {
    return list.sort((a, b) => (b.satisfactionScore || 0) - (a.satisfactionScore || 0))
  }
  if (sortBy.value === 'conversations') {
    return list.sort((a, b) => (b.totalConversations || 0) - (a.totalConversations || 0))
  }
  return list
})

const enterpriseCount = computed(() => customers.value.filter(c => c.tier === 'ENTERPRISE').length)
const premiumCount = computed(() => customers.value.filter(c => c.tier === 'PREMIUM').length)
const slaWarningCount = computed(() => vipCustomers.value.filter(c => isSLAWarning(c)).length)

const avgSatisfaction = computed(() => {
  const scores = vipCustomers.value.map(c => c.satisfactionScore).filter(Boolean)
  if (!scores.length) return '-'
  return (scores.reduce((a, b) => a + b, 0) / scores.length).toFixed(1)
})

function daysSinceContact(customer) {
  if (!customer.lastContactAt) return 999
  return Math.floor((Date.now() - new Date(customer.lastContactAt)) / 86400000)
}

function isSLAWarning(customer) {
  return daysSinceContact(customer) > SLA_DAYS && customer.status === 'ACTIVE'
}

function statusDotClass(status) {
  const map = { ACTIVE: 'bg-green-500', INACTIVE: 'bg-gray-400', BLOCKED: 'bg-red-500', CHURNED: 'bg-yellow-500' }
  return map[status] || 'bg-gray-400'
}

function goToDetail(customer) {
  router.push({ name: 'customer-profile-detail', params: { id: customer.id } })
}

async function loadCustomers() {
  try {
    loading.value = true
    customers.value = await customerProfileApi.getCustomerProfiles()
  } catch (err) {
    console.error('Load VIP customers error:', err)
  } finally {
    loading.value = false
  }
}

onMounted(loadCustomers)
</script>
