<template>
  <div v-if="isOpen" class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 backdrop-blur-sm">
    <div class="bg-white rounded-2xl shadow-2xl w-full max-w-4xl max-h-[90vh] overflow-hidden flex flex-col">
      <!-- Header -->
      <div class="bg-gradient-to-r from-blue-600 to-purple-600 px-6 py-4 flex justify-between items-center">
        <div class="flex items-center gap-3">
          <div class="w-12 h-12 rounded-full bg-white bg-opacity-20 flex items-center justify-center">
            <Icon icon="mdi:account-tie" class="text-white text-2xl" />
          </div>
          <div>
            <h2 class="text-xl font-bold text-white">{{ stats?.agentName || 'Agent' }}</h2>
            <p class="text-blue-100 text-sm">{{ t('agentStats.title') }}</p>
          </div>
        </div>
        <button @click="close" class="text-white hover:bg-white hover:bg-opacity-20 rounded-full p-2 transition">
          <Icon icon="mdi:close" class="text-2xl" />
        </button>
      </div>

      <!-- Period Selector -->
      <div class="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
        <div class="flex gap-2">
          <button
            v-for="p in ['7d', '30d', '90d']"
            :key="p"
            @click="changePeriod(p)"
            :class="[
              'px-4 py-2 rounded-lg font-medium transition',
              period === p
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            ]"
          >
            {{ t(`agentStats.period.${p}`) }}
          </button>
        </div>
        <div v-if="loading" class="flex items-center gap-2 text-gray-500">
          <Icon icon="mdi:loading" class="animate-spin" />
          <span>{{ t('agentStats.loading') }}</span>
        </div>
      </div>

      <!-- Content -->
      <div class="flex-1 overflow-y-auto">
        <!-- Tab Navigation -->
        <div class="border-b border-gray-200 px-6">
          <nav class="flex gap-4">
            <button
              @click="activeTab = 'overview'"
              :class="[
                'py-3 px-4 font-medium border-b-2 transition',
                activeTab === 'overview'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              ]"
            >
              {{ t('agentStats.tabs.overview') }}
            </button>
            <button
              @click="activeTab = 'charts'"
              :class="[
                'py-3 px-4 font-medium border-b-2 transition',
                activeTab === 'charts'
                  ? 'border-blue-600 text-blue-600'
                  : 'border-transparent text-gray-500 hover:text-gray-700'
              ]"
            >
              {{ t('agentStats.tabs.charts') }}
            </button>
          </nav>
        </div>

        <!-- Tab Content -->
        <div class="p-6">
          <!-- Overview Tab -->
          <div v-if="activeTab === 'overview'">
            <!-- KPI Cards Grid -->
            <div class="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
              <!-- Total Conversations -->
              <div class="bg-gradient-to-br from-blue-50 to-blue-100 rounded-xl p-4 border border-blue-200">
                <div class="flex items-center gap-2 mb-2">
                  <Icon icon="mdi:message-text" class="text-blue-600 text-xl" />
                  <span class="text-sm font-medium text-blue-800">{{ t('agentStats.kpi.total') }}</span>
                </div>
                <p class="text-3xl font-bold text-blue-900">{{ stats?.totalConversations || 0 }}</p>
              </div>

              <!-- Resolved -->
              <div class="bg-gradient-to-br from-green-50 to-green-100 rounded-xl p-4 border border-green-200">
                <div class="flex items-center gap-2 mb-2">
                  <Icon icon="mdi:check-circle" class="text-green-600 text-xl" />
                  <span class="text-sm font-medium text-green-800">{{ t('agentStats.kpi.resolved') }}</span>
                </div>
                <p class="text-3xl font-bold text-green-900">{{ stats?.resolvedCount || 0 }}</p>
              </div>

              <!-- Resolution Rate -->
              <div class="bg-gradient-to-br from-purple-50 to-purple-100 rounded-xl p-4 border border-purple-200">
                <div class="flex items-center gap-2 mb-2">
                  <Icon icon="mdi:chart-pie" class="text-purple-600 text-xl" />
                  <span class="text-sm font-medium text-purple-800">{{ t('agentStats.kpi.rate') }}</span>
                </div>
                <p class="text-3xl font-bold text-purple-900">{{ formatPercent(stats?.resolutionRate) }}</p>
              </div>

              <!-- Response Time -->
              <div class="bg-gradient-to-br from-orange-50 to-orange-100 rounded-xl p-4 border border-orange-200">
                <div class="flex items-center gap-2 mb-2">
                  <Icon icon="mdi:clock" class="text-orange-600 text-xl" />
                  <span class="text-sm font-medium text-orange-800">{{ t('agentStats.kpi.response') }}</span>
                </div>
                <p class="text-3xl font-bold text-orange-900">{{ formatTime(stats?.avgFirstResponseSeconds) }}</p>
              </div>
            </div>

            <!-- Second Row KPIs -->
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
              <!-- CSAT Score -->
              <div class="bg-gradient-to-br from-yellow-50 to-yellow-100 rounded-xl p-4 border border-yellow-200">
                <div class="flex items-center justify-between mb-2">
                  <div class="flex items-center gap-2">
                    <Icon icon="mdi:star" class="text-yellow-600 text-xl" />
                    <span class="text-sm font-medium text-yellow-800">{{ t('agentStats.kpi.csatScore') }}</span>
                  </div>
                  <span class="text-2xl font-bold text-yellow-900">{{ stats?.csatScore?.toFixed(1) || 'N/A' }}/5.0</span>
                </div>
                <div class="flex gap-1">
                  <Icon
                    v-for="i in 5"
                    :key="i"
                    :icon="i <= Math.round(stats?.csatScore || 0) ? 'mdi:star' : 'mdi:star-outline'"
                    :class="i <= Math.round(stats?.csatScore || 0) ? 'text-yellow-500' : 'text-gray-300'"
                    class="text-xl"
                  />
                </div>
              </div>

              <!-- SLA Breaches -->
              <div class="bg-gradient-to-br from-red-50 to-red-100 rounded-xl p-4 border border-red-200">
                <div class="flex items-center justify-between mb-2">
                  <div class="flex items-center gap-2">
                    <Icon icon="mdi:alert-circle" class="text-red-600 text-xl" />
                    <span class="text-sm font-medium text-red-800">{{ t('agentStats.kpi.slaBreaches') }}</span>
                  </div>
                  <span class="text-2xl font-bold text-red-900">{{ stats?.slaBreachCount || 0 }}</span>
                </div>
                <p class="text-sm text-red-600">{{ t('agentStats.kpi.violations') }}</p>
              </div>
            </div>

            <!-- Progress Bars -->
            <div class="space-y-4">
              <!-- Resolution Rate Progress -->
              <div>
                <div class="flex justify-between mb-2">
                  <span class="text-sm font-medium text-gray-700">{{ t('agentStats.kpi.resolutionRate') }}</span>
                  <span class="text-sm font-bold text-gray-900">{{ formatPercent(stats?.resolutionRate) }}</span>
                </div>
                <div class="w-full bg-gray-200 rounded-full h-3">
                  <div
                    class="bg-gradient-to-r from-purple-500 to-purple-600 h-3 rounded-full transition-all duration-500"
                    :style="{ width: `${stats?.resolutionRate || 0}%` }"
                  ></div>
                </div>
              </div>

              <!-- CSAT Progress -->
              <div>
                <div class="flex justify-between mb-2">
                  <span class="text-sm font-medium text-gray-700">{{ t('agentStats.kpi.csatProgress') }}</span>
                  <span class="text-sm font-bold text-gray-900">{{ ((stats?.csatScore || 0) / 5 * 100).toFixed(0) }}%</span>
                </div>
                <div class="w-full bg-gray-200 rounded-full h-3">
                  <div
                    class="bg-gradient-to-r from-yellow-400 to-yellow-500 h-3 rounded-full transition-all duration-500"
                    :style="{ width: `${((stats?.csatScore || 0) / 5 * 100)}%` }"
                  ></div>
                </div>
              </div>
            </div>
          </div>

          <!-- Charts Tab -->
          <div v-if="activeTab === 'charts'">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <!-- Resolution Rate RadialBar -->
              <div	class="bg-white rounded-xl p-4 border border-gray-200 shadow-sm">
                <h3 class="text-lg font-semibold text-gray-800 mb-4">{{ t('agentStats.charts.resolutionRate') }}</h3>
                <div class="h-64">
                  <apexchart
                    type="radialBar"
                    :options="resolutionRateChartOptions"
                    :series="[stats?.resolutionRate || 0]"
                  />
                </div>
              </div>

              <!-- CSAT RadialBar -->
              <div class="bg-white rounded-xl p-4 border border-gray-200 shadow-sm">
                <h3 class="text-lg font-semibold text-gray-800 mb-4">{{ t('agentStats.charts.csatScore') }}</h3>
                <div class="h-64">
                  <apexchart
                    type="radialBar"
                    :options="csatChartOptions"
                    :series="[(stats?.csatScore || 0) / 5 * 100]"
                  />
                </div>
              </div>

              <!-- Daily Conversations Bar Chart -->
              <div class="bg-white rounded-xl p-4 border border-gray-200 shadow-sm md:col-span-2">
                <h3 class="text-lg font-semibold text-gray-800 mb-4">{{ t('agentStats.charts.dailyConversationsSubtitle', { period: t(`agentStats.period.${period}`) }) }}</h3>
                <div class="h-64">
                  <apexchart
                    type="bar"
                    :options="dailyChartOptions"
                    :series="dailyChartSeries"
                  />
                </div>
              </div>

              <!-- Status Distribution Donut -->
              <div class="bg-white rounded-xl p-4 border border-gray-200 shadow-sm md:col-span-2">
                <h3 class="text-lg font-semibold text-gray-800 mb-4">{{ t('agentStats.charts.statusDistribution') }}</h3>
                <div class="h-64">
                  <apexchart
                    type="donut"
                    :options="statusChartOptions"
                    :series="statusChartSeries"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import agentApi from '@/api/agentApi'
import VueApexCharts from 'vue3-apexcharts'

const { t } = useI18n()

const props = defineProps({
  isOpen: Boolean,
  agentId: Number,
  agentName: String
})

const emit = defineEmits(['close'])

const activeTab = ref('overview')
const period = ref('7d')
const loading = ref(false)
const stats = ref(null)

// Load stats when modal opens or period changes
watch(() => props.isOpen, (newVal) => {
  if (newVal && props.agentId) {
    loadStats()
  }
})

watch(period, () => {
  if (props.isOpen && props.agentId) {
    loadStats()
  }
})

const loadStats = async () => {
  loading.value = true
  try {
    const response = await agentApi.getAgentConversationStats(props.agentId, period.value)
    stats.value = response.data
  } catch (error) {
    console.error('Failed to load agent stats:', error)
  } finally {
    loading.value = false
  }
}

const changePeriod = (newPeriod) => {
  period.value = newPeriod
}

const close = () => {
  emit('close')
}

const formatPercent = (value) => {
  return value ? `${value.toFixed(1)}%` : '0%'
}

const formatTime = (seconds) => {
  if (!seconds) return 'N/A'
  const minutes = Math.floor(seconds / 60)
  const secs = Math.floor(seconds % 60)
  return minutes > 0 ? `${minutes}m ${secs}s` : `${secs}s`
}

// Chart Options
const resolutionRateChartOptions = computed(() => ({
  chart: {
    type: 'radialBar',
    height: 250
  },
  plotOptions: {
    radialBar: {
      startAngle: -135,
      endAngle: 135,
      dataLabels: {
        name: {
          show: true,
          fontSize: '16px',
          color: '#374151'
        },
        value: {
          show: true,
          fontSize: '24px',
          fontWeight: 'bold',
          color: '#1F2937',
          formatter: (val) => `${val.toFixed(1)}%`
        }
      }
    }
  },
  fill: {
    type: 'gradient',
    gradient: {
      shade: 'dark',
      type: 'horizontal',
      shadeIntensity: 0.5,
      gradientToColors: ['#8B5CF6'],
      inverseColors: true,
      opacityFrom: 1,
      opacityTo: 1
    }
  },
  stroke: {
    lineCap: 'round'
  },
  labels: [t('agentStats.charts.resolutionRate')]
}))

const csatChartOptions = computed(() => ({
  chart: {
    type: 'radialBar',
    height: 250
  },
  plotOptions: {
    radialBar: {
      startAngle: -135,
      endAngle: 135,
      dataLabels: {
        name: {
          show: true,
          fontSize: '16px',
          color: '#374151'
        },
        value: {
          show: true,
          fontSize: '24px',
          fontWeight: 'bold',
          color: '#1F2937',
          formatter: (val) => `${(val / 20).toFixed(1)}/5`
        }
      }
    }
  },
  fill: {
    type: 'gradient',
    gradient: {
      shade: 'dark',
      type: 'horizontal',
      shadeIntensity: 0.5,
      gradientToColors: ['#F59E0B'],
      inverseColors: true,
      opacityFrom: 1,
      opacityTo: 1
    }
  },
  stroke: {
    lineCap: 'round'
  },
  labels: [t('agentStats.charts.csatScore')]
}))

const dailyChartOptions = computed(() => ({
  chart: {
    type: 'bar',
    height: 250,
    toolbar: { show: false }
  },
  plotOptions: {
    bar: {
      borderRadius: 8,
      columnWidth: '60%'
    }
  },
  dataLabels: { enabled: false },
  xaxis: {
    categories: stats.value?.dailyStats?.map(d => d.date) || [],
    labels: { rotate: -45 }
  },
  yaxis: {
    title: { text: t('agentStats.charts.dailyConversations') }
  },
  colors: ['#3B82F6', '#10B981'],
  legend: { position: 'top' }
}))

const dailyChartSeries = computed(() => [
  {
    name: t('agentStats.kpi.total'),
    data: stats.value?.dailyStats?.map(d => d.total) || []
  },
  {
    name: t('agentStats.kpi.resolved'),
    data: stats.value?.dailyStats?.map(d => d.resolved) || []
  }
])

const statusChartOptions = computed(() => ({
  chart: {
    type: 'donut',
    height: 250
  },
  labels: [t('agentStats.kpi.resolved'), t('agentStats.kpi.unresolved') || 'Unresolved', t('agentStats.kpi.pending') || 'Pending'],
  colors: ['#10B981', '#EF4444', '#F59E0B'],
  legend: { position: 'bottom' }
}))

const statusChartSeries = computed(() => [
  stats.value?.resolvedCount || 0,
  stats.value?.unresolvedCount || 0,
  stats.value?.pendingCount || 0
])
</script>

<style scoped>
/* Custom scrollbar for modal content */
.overflow-y-auto::-webkit-scrollbar {
  width: 8px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #a1a1a1;
}
</style>
