<template>
  <div class="dashboard p-4">
    <!-- Header -->
    <div class="mt-2 w-full">
      <div class="lg:flex grid-cols-1 lg:space-y-0 space-y-3 gap-5 justify-between">
        <div>
          <p class="uppercase text-xs text-gray-700 font-semibold">{{ $t('dashboard.overview') }}</p>
          <h1 class="text-2xl text-gray-900 dark:text-gray-200 font-medium">
            {{ $t('dashboard.title') }}
          </h1>
        </div>
        <div class="flex gap-2">
          <button
            @click="refreshData"
            :disabled="loading"
            class="bg-white dark:bg-gray-800 hover:border-gray-200 dark:hover:bg-gray-700 dark:text-white dark:border-gray-700 border rounded py-2 px-5 flex items-center gap-2"
          >
            <Icon icon="mdi:refresh" :class="{'animate-spin': loading}" class="text-lg" />
            {{ $t('dashboard.refresh') }}
          </button>
          <button
            @click="openSettings"
            class="bg-primary border flex gap-2 text-white hover:bg-primary/80 dark:border-gray-700 rounded py-3 px-5"
          >
            <span class="icon text-2xl"><Icon icon="ic:twotone-plus" /></span>
            <span class="text">{{ $t('dashboard.configureBot') }}</span>
          </button>
        </div>
      </div>
    </div>

    <!-- Statistics Cards -->
    <div class="wrapper-card grid lg:grid-cols-4 grid-cols-1 md:grid-cols-2 gap-4 mt-6">
      <!-- Total Conversations -->
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-blue-200 rounded-full w-14 h-14 text-lg p-3 text-blue-600 mx-auto">
            <Icon icon="mdi:chat" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ stats.totalConversations }}
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">{{ $t('dashboard.totalConversations') }}</h2>
          <div class="flex items-center mt-2">
            <span :class="stats.conversationGrowth >= 0 ? 'text-green-500' : 'text-red-500'" class="text-sm flex items-center">
              <Icon :icon="stats.conversationGrowth >= 0 ? 'mdi:arrow-up' : 'mdi:arrow-down'" class="mr-1" />
              {{ Math.abs(stats.conversationGrowth) }}%
            </span>
            <span class="text-gray-400 text-sm ml-2">{{ $t('dashboard.vsLastMonth') }}</span>
          </div>
        </div>
      </div>

      <!-- Active Users -->
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-green-200 rounded-full w-14 h-14 text-lg p-3 text-green-600 mx-auto">
            <Icon icon="mdi:account-multiple" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ stats.activeUsers }}
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">{{ $t('dashboard.activeUsers') }}</h2>
          <div class="flex items-center mt-2">
            <span :class="stats.userGrowth >= 0 ? 'text-green-500' : 'text-red-500'" class="text-sm flex items-center">
              <Icon :icon="stats.userGrowth >= 0 ? 'mdi:arrow-up' : 'mdi:arrow-down'" class="mr-1" />
              {{ Math.abs(stats.userGrowth) }}%
            </span>
            <span class="text-gray-400 text-sm ml-2">{{ $t('dashboard.vsLastMonth') }}</span>
          </div>
        </div>
      </div>

      <!-- Bot Responses -->
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-purple-200 rounded-full w-14 h-14 text-lg p-3 text-purple-600 mx-auto">
            <Icon icon="mdi:robot" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ stats.botResponses }}
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">{{ $t('dashboard.botResponses') }}</h2>
          <div class="flex items-center mt-2">
            <span class="text-blue-500 text-sm flex items-center">
              <Icon icon="mdi:check-circle" class="mr-1" />
              {{ $t('dashboard.successRate') }}: {{ stats.responseRate?.toFixed(1) || '0.0' }}%
            </span>
          </div>
        </div>
      </div>

      <!-- Active Connections -->
      <div class="card bg-white dark:bg-gray-800 w-full rounded-md p-5 border dark:border-gray-700 flex">
        <div class="p-2 max-w-sm">
          <div class="bg-orange-200 rounded-full w-14 h-14 text-lg p-3 text-orange-600 mx-auto">
            <Icon icon="mdi:connection" class="text-2xl" />
          </div>
        </div>
        <div class="block p-2 w-full">
          <p class="font-semibold text-gray-900 dark:text-gray-200 text-xl">
            {{ stats.activeConnections }}
          </p>
          <h2 class="font-normal text-gray-400 text-md mt-1">{{ $t('dashboard.activeConnections') }}</h2>
          <div class="flex items-center mt-2">
            <span class="text-green-500 text-sm flex items-center">
              <Icon icon="mdi:check-circle" class="mr-1" />
              {{ $t('dashboard.allSystemsOnline') }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- Charts Section -->
    <div class="mt-6 lg:flex block lg:gap-6">
      <!-- Conversation Chart -->
      <div class="bg-white dark:bg-gray-800 p-6 lg:w-2/3 w-full rounded-md border dark:border-gray-700">
        <div class="flex justify-between items-center mb-4">
          <div>
            <h2 class="font-medium text-sm text-gray-800 dark:text-gray-200">{{ $t('dashboard.conversationTrends') }}</h2>
            <h1 class="font-semibold text-2xl text-gray-800 dark:text-gray-200">
              {{ stats.totalConversations }}
            </h1>
            <p class="text-gray-400 font-normal">{{ getPeriodLabel() }}</p>
          </div>
          <div class="flex gap-2">
            <button
              v-for="period in periods"
              :key="period.value"
              @click="selectedPeriod = period.value"
              :class="[
                'px-3 py-1 rounded text-sm',
                selectedPeriod === period.value
                  ? 'bg-primary text-white'
                  : 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300'
              ]"
            >
              {{ period.label }}
            </button>
          </div>
        </div>
        
        <!-- Chart Area -->
        <div class="h-64 flex items-center justify-center bg-gray-50 dark:bg-gray-700 rounded">
          <div v-if="chartLoading" class="text-center">
            <Icon icon="mdi:loading" class="text-6xl text-gray-300 animate-spin" />
            <p class="mt-2 text-gray-500">Loading chart data...</p>
          </div>
          <div v-else-if="chartData.length === 0" class="text-center py-10">
            <Icon icon="mdi:chart-line" class="text-6xl text-gray-300 mx-auto" />
            <p class="mt-2 text-gray-500">No data available for {{ periods.find(p => p.value === selectedPeriod)?.label }}</p>
          </div>
          <div v-else class="w-full px-4 pb-4 flex justify-center">
            <canvas ref="canvasRef" style="width: 100%; height: 200px; max-width: 100%;"></canvas>
          </div>
        </div>
      </div>

      <!-- Quick Actions & Online Members -->
      <div class="lg:w-1/3 w-full mt-6 lg:mt-0 space-y-6">
        <!-- Quick Actions -->
        <div class="bg-white dark:bg-gray-800 p-6 rounded-md border dark:border-gray-700">
          <h2 class="font-medium text-sm text-gray-800 dark:text-gray-200">{{ $t('dashboard.quickActions') }}</h2>
          
          <div class="space-y-3 mt-4">
            <router-link
              to="/messages"
              class="flex items-center p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg hover:bg-blue-100 dark:hover:bg-blue-900/30 transition-colors"
            >
              <Icon icon="mdi:message-text" class="text-blue-600 text-xl mr-3" />
              <div>
                <p class="font-medium text-gray-900 dark:text-gray-200">{{ $t('dashboard.viewMessages') }}</p>
                <p class="text-sm text-gray-500">{{ $t('dashboard.manageConversations') }}</p>
              </div>
            </router-link>

            <router-link
              to="/penny-rules"
              class="flex items-center p-3 bg-green-50 dark:bg-green-900/20 rounded-lg hover:bg-green-100 dark:hover:bg-green-900/30 transition-colors"
            >
              <Icon icon="mdi:robot" class="text-green-600 text-xl mr-3" />
              <div>
                <p class="font-medium text-gray-900 dark:text-gray-200">{{ $t('dashboard.botRules') }}</p>
                <p class="text-sm text-gray-500">{{ $t('dashboard.configureResponses') }}</p>
              </div>
            </router-link>

            <router-link
              to="/penny-connections"
              class="flex items-center p-3 bg-purple-50 dark:bg-purple-900/20 rounded-lg hover:bg-purple-100 dark:hover:bg-purple-900/30 transition-colors"
            >
              <Icon icon="mdi:facebook" class="text-purple-600 text-xl mr-3" />
              <div>
                <p class="font-medium text-gray-900 dark:text-gray-200">{{ $t('dashboard.connections') }}</p>
                <p class="text-sm text-gray-500">{{ $t('dashboard.managePlatforms') }}</p>
              </div>
            </router-link>

            <button
              @click="testBot"
              class="w-full flex items-center p-3 bg-orange-50 dark:bg-orange-900/20 rounded-lg hover:bg-orange-100 dark:hover:bg-orange-900/30 transition-colors"
            >
              <Icon icon="mdi:play-circle" class="text-orange-600 text-xl mr-3" />
              <div class="text-left">
                <p class="font-medium text-gray-900 dark:text-gray-200">{{ $t('dashboard.testBot') }}</p>
                <p class="text-sm text-gray-500">{{ $t('dashboard.runTestConversation') }}</p>
              </div>
            </button>
          </div>
        </div>

        <!-- Online Members -->
        <OnlineMembers v-if="tenantKey" :tenant-key="tenantKey" />
      </div>
    </div>

    <!-- Recent Activity -->
    <div class="mt-6 bg-white dark:bg-gray-800 p-6 rounded-md border dark:border-gray-700">
      <div class="flex justify-between items-center mb-4">
        <h2 class="font-medium text-sm text-gray-800 dark:text-gray-200">{{ $t('dashboard.recentActivity') }}</h2>
        <button @click="viewAllActivity" class="text-primary text-sm hover:underline">
          {{ $t('dashboard.viewAll') }}
        </button>
      </div>
      
      <div class="space-y-3">
        <div
          v-for="activity in recentActivity"
          :key="activity.id"
          class="flex items-center p-3 bg-gray-50 dark:bg-gray-700 rounded-lg"
        >
          <div :class="`p-2 rounded-full ${getActivityColor(activity.type)}`">
            <Icon :icon="getActivityIcon(activity.type)" class="text-white" />
          </div>
          <div class="ml-3 flex-1">
            <p class="font-medium text-gray-900 dark:text-gray-200">{{ activity.title }}</p>
            <p class="text-sm text-gray-500">{{ activity.description }}</p>
          </div>
          <span class="text-sm text-gray-400">{{ getRelativeTime(activity.timestamp) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick, computed } from 'vue'
import { Icon } from '@iconify/vue'
import { useRouter } from 'vue-router'
import { getRelativeTime } from '@/utils/dateUtils'
import axios from '@/plugins/axios'
import OnlineMembers from '@/components/OnlineMembers.vue'

const router = useRouter()

// Get tenant key from localStorage (use ref to prevent reactivity issues)
const tenantKey = ref(localStorage.getItem('active_tenant_id'))

// State
const selectedPeriod = ref('7d')
const loading = ref(false)
const chartData = ref([])
const chartLoading = ref(false)
const canvasRef = ref(null)

const periods = [
  { label: '7D', value: '7d' },
  { label: '1M', value: '1m' },
  { label: '3M', value: '3m' },
  { label: '1Y', value: '1y' }
]

const stats = ref({
  totalConversations: 0,
  conversationGrowth: 0,
  activeUsers: 0,
  userGrowth: 0,
  botResponses: 0,
  responseRate: 0,
  activeConnections: 0
})

const recentActivity = ref([])

// Methods
const refreshData = async () => {
  loading.value = true
  try {
    // Fetch real statistics, takeover and activities from API
    const [conversationStats, takeoverStats, activityStats] = await Promise.all([
      axios.get('/conversations/statistics').then(res => {
        return res.data
      }).catch(() => ({ totalConversations: 0, growthRate: 0, activeUsers: 0, userGrowth: 0, botResponses: 0, responseRate: 0, activeConnections: 0 })),
      
      axios.get('/takeover/active').then(res => {
        return res.data
      }).catch(() => []),

      axios.get('/conversations/activity?limit=10').then(res => {
        return res.data
      }).catch(() => [])
    ])
    
    // Update stats with real data
    stats.value = {
      totalConversations: conversationStats.totalConversations || 0,
      conversationGrowth: conversationStats.growthRate || 0,
      activeUsers: conversationStats.activeUsers || 0,
      userGrowth: conversationStats.userGrowth || 0,
      botResponses: conversationStats.botResponses || 0,
      responseRate: conversationStats.responseRate || 0,
      activeConnections: conversationStats.activeConnections || 0
    }

    recentActivity.value = activityStats.map((act, index) => ({
      id: act.id || index,
      type: act.type || 'conversation',
      title: act.title || 'Activity',
      description: act.description || '',
      timestamp: act.timestamp ? new Date(act.timestamp) : new Date()
    }))
    
  } catch (error) {
    console.error('Failed to fetch dashboard data:', error)
  } finally {
    loading.value = false
  }
}

const openSettings = () => {
  router.push('/penny-rules')
}

const testBot = () => {
  router.push('/penny-bots')
}

const viewAllActivity = () => {
  router.push('/messages')
}

const getActivityIcon = (type) => {
  const icons = {
    conversation: 'mdi:chat',
    bot_response: 'mdi:robot',
    connection: 'mdi:connection',
    takeover: 'mdi:hand-right'
  }
  return icons[type] || 'mdi:information'
}

const getActivityColor = (type) => {
  const colors = {
    conversation: 'bg-blue-500',
    bot_response: 'bg-green-500',
    connection: 'bg-purple-500',
    takeover: 'bg-orange-500'
  }
  return colors[type] || 'bg-gray-500'
}

const getPeriodLabel = () => {
  const labels = {
    '7d': 'Last 7 Days',
    '1m': 'Last 30 Days',
    '3m': 'Last 3 Months',
    '1y': 'Last 1 Year'
  }
  return labels[selectedPeriod.value] || 'Last 7 Days'
}

const drawChart = () => {
  if (!canvasRef.value || !chartData.value || chartData.value.length === 0) return
  const canvas = canvasRef.value
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const dpr = window.devicePixelRatio || 1
  const width = canvas.clientWidth
  const height = 200
  
  canvas.width = width * dpr
  canvas.height = height * dpr
  ctx.scale(dpr, dpr)

  ctx.clearRect(0, 0, width, height)

  const values = chartData.value.map(d => d.value)
  const maxVal = Math.max(...values, 5)

  // Draw Grid Lines
  ctx.strokeStyle = '#f3f4f6' // gray-100
  ctx.lineWidth = 1
  const gridRows = 4
  for (let i = 0; i <= gridRows; i++) {
    const y = 20 + (height - 60) * (i / gridRows)
    ctx.beginPath()
    ctx.moveTo(40, y)
    ctx.lineTo(width - 10, y)
    ctx.stroke()

    // Y Axis Text
    ctx.fillStyle = '#9ca3af' // gray-400
    ctx.font = '10px sans-serif'
    ctx.textAlign = 'right'
    ctx.fillText(Math.round(maxVal * (1 - i / gridRows)).toString(), 30, y + 3)
  }

  // Draw Bars
  const barPadding = 12
  const chartWidth = width - 50
  const barWidth = (chartWidth / chartData.value.length) - barPadding
  
  chartData.value.forEach((point, index) => {
    const val = point.value
    const barHeight = (val / maxVal) * (height - 60)
    const x = 40 + index * (barWidth + barPadding)
    const y = height - 40 - barHeight

    // Draw Gradient Bar
    const gradient = ctx.createLinearGradient(x, y, x, height - 40)
    gradient.addColorStop(0, '#3b82f6') // primary blue
    gradient.addColorStop(1, '#60a5fa')
    ctx.fillStyle = gradient
    
    ctx.beginPath()
    if (ctx.roundRect) {
      ctx.roundRect(x, y, barWidth, barHeight, [4, 4, 0, 0])
    } else {
      ctx.rect(x, y, barWidth, barHeight)
    }
    ctx.fill()

    // Draw X Label
    ctx.fillStyle = '#6b7280' // gray-500
    ctx.textAlign = 'center'
    ctx.font = '10px sans-serif'
    ctx.fillText(point.label, x + barWidth / 2, height - 20)
  })
}

// Load chart data based on selected period
const loadChartData = async () => {
  chartLoading.value = true
  try {
    const { data } = await axios.get(`/conversations/chart?period=${selectedPeriod.value}`)
    chartData.value = data
    nextTick(() => {
      drawChart()
    })
  } catch (error) {
    console.error('Failed to load chart data:', error)
    chartData.value = []
  } finally {
    chartLoading.value = false
  }
}

// Watch for period changes
watch(selectedPeriod, () => {
  loadChartData()
})

// Lifecycle
onMounted(() => {
  refreshData()
  loadChartData()
  window.addEventListener('resize', drawChart)
})
</script>

<style scoped>
.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
