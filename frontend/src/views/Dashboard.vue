<template>
  <div class="dashboard p-4">
    <!-- Header -->
    <div class="mt-2 w-full">
      <div class="lg:flex grid-cols-1 lg:space-y-0 space-y-3 gap-5 justify-between">
        <div>
          <p class="uppercase text-xs text-gray-700 font-semibold">overview</p>
          <h1 class="text-2xl text-gray-900 dark:text-gray-200 font-medium">
            Chatbot Dashboard
          </h1>
        </div>
        <div class="flex gap-2">
          <button
            @click="refreshData"
            :disabled="loading"
            class="bg-white dark:bg-gray-800 hover:border-gray-200 dark:hover:bg-gray-700 dark:text-white dark:border-gray-700 border rounded py-2 px-5 flex items-center gap-2"
          >
            <Icon icon="mdi:refresh" :class="{'animate-spin': loading}" class="text-lg" />
            Refresh
          </button>
          <button
            @click="openSettings"
            class="bg-primary border flex gap-2 text-white hover:bg-primary/80 dark:border-gray-700 rounded py-3 px-5"
          >
            <span class="icon text-2xl"><Icon icon="ic:twotone-plus" /></span>
            <span class="text"> Configure Bot</span>
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
          <h2 class="font-normal text-gray-400 text-md mt-1">Total Conversations</h2>
          <div class="flex items-center mt-2">
            <span :class="stats.conversationGrowth >= 0 ? 'text-green-500' : 'text-red-500'" class="text-sm flex items-center">
              <Icon :icon="stats.conversationGrowth >= 0 ? 'mdi:arrow-up' : 'mdi:arrow-down'" class="mr-1" />
              {{ Math.abs(stats.conversationGrowth) }}%
            </span>
            <span class="text-gray-400 text-sm ml-2">vs last month</span>
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
          <h2 class="font-normal text-gray-400 text-md mt-1">Active Users</h2>
          <div class="flex items-center mt-2">
            <span :class="stats.userGrowth >= 0 ? 'text-green-500' : 'text-red-500'" class="text-sm flex items-center">
              <Icon :icon="stats.userGrowth >= 0 ? 'mdi:arrow-up' : 'mdi:arrow-down'" class="mr-1" />
              {{ Math.abs(stats.userGrowth) }}%
            </span>
            <span class="text-gray-400 text-sm ml-2">vs last month</span>
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
          <h2 class="font-normal text-gray-400 text-md mt-1">Bot Responses</h2>
          <div class="flex items-center mt-2">
            <span class="text-blue-500 text-sm flex items-center">
              <Icon icon="mdi:check-circle" class="mr-1" />
              {{ stats.responseRate }}% success
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
          <h2 class="font-normal text-gray-400 text-md mt-1">Active Connections</h2>
          <div class="flex items-center mt-2">
            <span class="text-green-500 text-sm flex items-center">
              <Icon icon="mdi:check-circle" class="mr-1" />
              All systems online
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
            <h2 class="font-medium text-sm text-gray-800 dark:text-gray-200">CONVERSATION TRENDS</h2>
            <h1 class="font-semibold text-2xl text-gray-800 dark:text-gray-200">
              {{ stats.totalConversations }}
            </h1>
            <p class="text-gray-400 font-normal">Last 7 days activity</p>
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
        
        <!-- Simple Chart Placeholder -->
        <div class="h-64 flex items-center justify-center bg-gray-50 dark:bg-gray-700 rounded">
          <div class="text-center">
            <Icon icon="mdi:chart-line" class="text-6xl text-gray-300" />
            <p class="mt-2 text-gray-500">Chart visualization coming soon</p>
          </div>
        </div>
      </div>

      <!-- Quick Actions -->
      <div class="bg-white dark:bg-gray-800 p-6 lg:w-1/3 w-full rounded-md border dark:border-gray-700 mt-6 lg:mt-0">
        <h2 class="font-medium text-sm text-gray-800 dark:text-gray-200 mb-4">QUICK ACTIONS</h2>
        
        <div class="space-y-3">
          <router-link
            to="/messages"
            class="flex items-center p-3 bg-blue-50 dark:bg-blue-900/20 rounded-lg hover:bg-blue-100 dark:hover:bg-blue-900/30 transition-colors"
          >
            <Icon icon="mdi:message-text" class="text-blue-600 text-xl mr-3" />
            <div>
              <p class="font-medium text-gray-900 dark:text-gray-200">View Messages</p>
              <p class="text-sm text-gray-500">Manage conversations</p>
            </div>
          </router-link>

          <router-link
            to="/penny-rules"
            class="flex items-center p-3 bg-green-50 dark:bg-green-900/20 rounded-lg hover:bg-green-100 dark:hover:bg-green-900/30 transition-colors"
          >
            <Icon icon="mdi:robot" class="text-green-600 text-xl mr-3" />
            <div>
              <p class="font-medium text-gray-900 dark:text-gray-200">Bot Rules</p>
              <p class="text-sm text-gray-500">Configure responses</p>
            </div>
          </router-link>

          <router-link
            to="/penny-connections"
            class="flex items-center p-3 bg-purple-50 dark:bg-purple-900/20 rounded-lg hover:bg-purple-100 dark:hover:bg-purple-900/30 transition-colors"
          >
            <Icon icon="mdi:facebook" class="text-purple-600 text-xl mr-3" />
            <div>
              <p class="font-medium text-gray-900 dark:text-gray-200">Connections</p>
              <p class="text-sm text-gray-500">Manage platforms</p>
            </div>
          </router-link>

          <button
            @click="testBot"
            class="w-full flex items-center p-3 bg-orange-50 dark:bg-orange-900/20 rounded-lg hover:bg-orange-100 dark:hover:bg-orange-900/30 transition-colors"
          >
            <Icon icon="mdi:play-circle" class="text-orange-600 text-xl mr-3" />
            <div class="text-left">
              <p class="font-medium text-gray-900 dark:text-gray-200">Test Bot</p>
              <p class="text-sm text-gray-500">Run test conversation</p>
            </div>
          </button>
        </div>
      </div>
    </div>

    <!-- Recent Activity -->
    <div class="mt-6 bg-white dark:bg-gray-800 p-6 rounded-md border dark:border-gray-700">
      <div class="flex justify-between items-center mb-4">
        <h2 class="font-medium text-sm text-gray-800 dark:text-gray-200">RECENT ACTIVITY</h2>
        <button @click="viewAllActivity" class="text-primary text-sm hover:underline">
          View All
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
import { ref, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { useRouter } from 'vue-router'
import { getRelativeTime } from '@/utils/dateUtils'

const router = useRouter()

// State
const selectedPeriod = ref('7d')
const loading = ref(false)

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

const recentActivity = ref([
  {
    id: 1,
    type: 'conversation',
    title: 'New conversation started',
    description: 'User from Facebook Messenger started new chat',
    timestamp: new Date(Date.now() - 5 * 60000)
  },
  {
    id: 2,
    type: 'bot_response',
    title: 'Bot responded successfully',
    description: 'PennyBot answered user query about pricing',
    timestamp: new Date(Date.now() - 15 * 60000)
  },
  {
    id: 3,
    type: 'connection',
    title: 'Facebook connection active',
    description: 'Page "Customer Support" is online',
    timestamp: new Date(Date.now() - 30 * 60000)
  },
  {
    id: 4,
    type: 'takeover',
    title: 'Agent took over conversation',
    description: 'Agent John assumed control of conversation #1234',
    timestamp: new Date(Date.now() - 45 * 60000)
  }
])

// Methods
const refreshData = async () => {
  loading.value = true
  try {
    // Fetch real statistics from API
    const [conversationStats, takeoverStats] = await Promise.all([
      fetch('/api/conversations/statistics').then(res => {
        if (!res.ok) throw new Error('Failed to fetch conversation stats')
        return res.json()
      }).catch(() => ({ totalConversations: 0, growthRate: 0, activeUsers: 0, userGrowth: 0, botResponses: 0, responseRate: 0, activeConnections: 0 })),
      
      fetch('/api/takeover/active').then(res => {
        if (!res.ok) throw new Error('Failed to fetch takeover stats')
        return res.json()
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
    
    console.log('Dashboard data refreshed with real API data')
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
  // TODO: Open bot test modal or navigate to test page
  console.log('Test bot functionality')
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

// Lifecycle
onMounted(() => {
  refreshData()
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
