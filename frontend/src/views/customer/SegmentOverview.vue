<template>
  <div class="p-6">
    <!-- Header -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white flex items-center gap-2">
          <Icon icon="mdi:chart-pie" class="text-blue-500" />
          {{ t('customer.segments') }}
        </h1>
        <p class="text-sm text-gray-500 dark:text-gray-400 mt-1">{{ t('customer.segmentsDesc') }}</p>
      </div>
    </div>

    <!-- Loading -->
    <div v-if="loading" class="text-center py-12">
      <Icon icon="mdi:loading" class="animate-spin text-4xl text-blue-600" />
    </div>

    <template v-else>
      <!-- Segment Overview Cards -->
      <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4 mb-8">
        <div
          v-for="seg in segments"
          :key="seg.tier"
          class="rounded-xl p-5 border shadow-sm cursor-pointer hover:shadow-md transition-all"
          :class="[seg.cardBg, activeTier === seg.tier ? 'ring-2 ' + seg.ring : '']"
          @click="activeTier = activeTier === seg.tier ? null : seg.tier"
        >
          <div class="flex items-center justify-between mb-3">
            <div :class="seg.iconBg" class="w-10 h-10 rounded-lg flex items-center justify-center">
              <Icon :icon="seg.icon" :class="seg.iconColor" class="text-xl" />
            </div>
            <span :class="seg.badge" class="text-xs font-bold px-2 py-1 rounded-full">{{ seg.tier }}</span>
          </div>
          <p class="text-3xl font-bold text-gray-900 dark:text-white">{{ seg.count }}</p>
          <p class="text-xs text-gray-500 dark:text-gray-400 mt-1">khách hàng</p>

          <div class="mt-3 pt-3 border-t border-gray-200/50 dark:border-gray-700/50 grid grid-cols-2 gap-2 text-center">
            <div>
              <p class="text-xs text-gray-500 dark:text-gray-400">Avg Sat.</p>
              <p class="text-sm font-semibold text-gray-900 dark:text-white">{{ seg.avgSatisfaction }}</p>
            </div>
            <div>
              <p class="text-xs text-gray-500 dark:text-gray-400">Avg Conv.</p>
              <p class="text-sm font-semibold text-gray-900 dark:text-white">{{ seg.avgConversations }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Segment Member List -->
      <div class="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm overflow-hidden">
        <div class="px-5 py-4 border-b border-gray-200 dark:border-gray-700 flex items-center justify-between">
          <h2 class="text-sm font-semibold text-gray-900 dark:text-white">
            {{ activeTier ? `Phân khúc: ${activeTier}` : 'Tất cả khách hàng' }}
            <span class="ml-2 text-xs text-gray-500 dark:text-gray-400">({{ displayedCustomers.length }})</span>
          </h2>
          <input
            v-model="search"
            type="text"
            placeholder="Tìm kiếm..."
            class="px-3 py-1.5 text-sm border border-gray-300 dark:border-gray-600 rounded-lg dark:bg-gray-700 dark:text-white w-48"
          />
        </div>

        <div class="divide-y divide-gray-200 dark:divide-gray-700">
          <div
            v-for="customer in displayedCustomers"
            :key="customer.id"
            class="flex items-center gap-4 px-5 py-3 hover:bg-gray-50 dark:hover:bg-gray-700/50 cursor-pointer transition-colors"
            @click="goToDetail(customer)"
          >
            <!-- Avatar -->
            <div
              class="h-9 w-9 rounded-full flex items-center justify-center text-white text-sm font-semibold flex-shrink-0"
              :class="getAvatarBg(customer.tier)"
            >
              {{ customer.name?.charAt(0)?.toUpperCase() || '?' }}
            </div>
            <!-- Name + meta -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2">
                <span class="text-sm font-medium text-gray-900 dark:text-white truncate">{{ customer.name }}</span>
                <VIPBadge :tier="customer.tier" />
              </div>
              <p class="text-xs text-gray-500 dark:text-gray-400 truncate">{{ customer.email || customer.externalId }}</p>
            </div>
            <!-- Stats -->
            <div class="hidden sm:flex items-center gap-4 text-xs text-gray-500 dark:text-gray-400">
              <span class="flex items-center gap-1">
                <Icon icon="mdi:message-text" class="text-blue-400" />
                {{ customer.totalConversations || 0 }}
              </span>
              <span class="flex items-center gap-1">
                <Icon icon="mdi:star" class="text-yellow-400" />
                {{ customer.satisfactionScore?.toFixed(1) || '-' }}
              </span>
              <span>{{ formatDate(customer.lastContactAt) }}</span>
            </div>
            <!-- Status -->
            <span
              :class="getStatusClass(customer.status)"
              class="hidden md:inline-flex px-2 py-0.5 text-[10px] font-semibold rounded-full"
            >
              {{ customer.status }}
            </span>
            <Icon icon="mdi:chevron-right" class="text-gray-400 flex-shrink-0" />
          </div>
        </div>

        <div
          v-if="displayedCustomers.length === 0"
          class="text-center py-12 text-gray-400 dark:text-gray-500"
        >
          <Icon icon="mdi:account-off" class="text-4xl mb-2" />
          <p class="text-sm">Không tìm thấy khách hàng</p>
        </div>
      </div>

      <!-- Note about backend integration -->
      <!-- TODO: Connect to /api/customer/segments when CustomerSegmentService is implemented -->
    </template>
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
const activeTier = ref(null)
const search = ref('')

const tierConfig = {
  ENTERPRISE: {
    icon: 'mdi:crown',
    iconBg: 'bg-amber-100 dark:bg-amber-900/40',
    iconColor: 'text-amber-600 dark:text-amber-400',
    cardBg: 'bg-white dark:bg-gray-800 border-amber-200 dark:border-amber-800',
    badge: 'bg-amber-100 dark:bg-amber-900/40 text-amber-700 dark:text-amber-300',
    ring: 'ring-amber-400',
  },
  PREMIUM: {
    icon: 'mdi:star-circle',
    iconBg: 'bg-purple-100 dark:bg-purple-900/40',
    iconColor: 'text-purple-600 dark:text-purple-400',
    cardBg: 'bg-white dark:bg-gray-800 border-purple-200 dark:border-purple-800',
    badge: 'bg-purple-100 dark:bg-purple-900/40 text-purple-700 dark:text-purple-300',
    ring: 'ring-purple-400',
  },
  BASIC: {
    icon: 'mdi:shield-check',
    iconBg: 'bg-blue-100 dark:bg-blue-900/40',
    iconColor: 'text-blue-600 dark:text-blue-400',
    cardBg: 'bg-white dark:bg-gray-800 border-blue-200 dark:border-blue-800',
    badge: 'bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-300',
    ring: 'ring-blue-400',
  },
  FREE: {
    icon: 'mdi:account',
    iconBg: 'bg-gray-100 dark:bg-gray-700',
    iconColor: 'text-gray-500 dark:text-gray-400',
    cardBg: 'bg-white dark:bg-gray-800 border-gray-200 dark:border-gray-700',
    badge: 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300',
    ring: 'ring-gray-400',
  },
}

const tierOrder = ['ENTERPRISE', 'PREMIUM', 'BASIC', 'FREE']

const segments = computed(() =>
  tierOrder.map(tier => {
    const group = customers.value.filter(c => c.tier === tier)
    const scores = group.map(c => c.satisfactionScore).filter(Boolean)
    const convs = group.map(c => c.totalConversations).filter(Boolean)
    const cfg = tierConfig[tier]
    return {
      tier,
      count: group.length,
      avgSatisfaction: scores.length ? (scores.reduce((a, b) => a + b, 0) / scores.length).toFixed(1) : '-',
      avgConversations: convs.length ? Math.round(convs.reduce((a, b) => a + b, 0) / convs.length) : 0,
      ...cfg,
    }
  })
)

const displayedCustomers = computed(() => {
  let list = activeTier.value
    ? customers.value.filter(c => c.tier === activeTier.value)
    : customers.value

  if (search.value) {
    const q = search.value.toLowerCase()
    list = list.filter(c =>
      c.name?.toLowerCase().includes(q) || c.email?.toLowerCase().includes(q) || c.externalId?.toLowerCase().includes(q)
    )
  }

  return list.sort((a, b) => {
    const tierRank = { ENTERPRISE: 0, PREMIUM: 1, BASIC: 2, FREE: 3 }
    return (tierRank[a.tier] || 3) - (tierRank[b.tier] || 3)
  })
})

function getAvatarBg(tier) {
  const map = {
    ENTERPRISE: 'bg-gradient-to-br from-amber-400 to-orange-500',
    PREMIUM: 'bg-gradient-to-br from-purple-400 to-indigo-600',
    BASIC: 'bg-gradient-to-br from-blue-400 to-cyan-600',
    FREE: 'bg-gradient-to-br from-gray-400 to-gray-600',
  }
  return map[tier] || 'bg-gray-400'
}

function getStatusClass(status) {
  const classes = {
    ACTIVE: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    INACTIVE: 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200',
    BLOCKED: 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200',
    CHURNED: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
  }
  return classes[status] || 'bg-gray-100 text-gray-800'
}

function formatDate(date) {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('vi-VN')
}

function goToDetail(customer) {
  router.push({ name: 'customer-profile-detail', params: { id: customer.id } })
}

async function loadCustomers() {
  try {
    loading.value = true
    customers.value = await customerProfileApi.getCustomerProfiles()
  } catch (err) {
    console.error('Load segments error:', err)
  } finally {
    loading.value = false
  }
}

onMounted(loadCustomers)
</script>
