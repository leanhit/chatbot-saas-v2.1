<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <div class="flex items-center">
        <Icon icon="mdi:chart-line" class="text-2xl text-blue-600 dark:text-blue-400 mr-3" />
        <h1 class="text-2xl font-bold text-gray-800 dark:text-white">Phân tích thanh toán</h1>
      </div>
    </div>

    <div v-if="loading" class="flex justify-center py-8">
      <Icon icon="eos-icons:loading" class="animate-spin text-4xl text-blue-600" />
    </div>

    <div v-else>
      <!-- Summary Cards -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6 mb-6">
        <div class="bg-white dark:bg-gray-900 rounded-lg shadow p-6">
          <div class="flex items-center">
            <Icon icon="mdi:cash" class="text-3xl text-green-600 dark:text-green-400 mr-4" />
            <div>
              <p class="text-sm text-gray-600 dark:text-gray-400">Doanh thu tháng</p>
              <p class="text-2xl font-bold text-gray-800 dark:text-white">{{ formatCurrency(dashboardData.monthlyRevenue?.totalRevenue || 0) }}</p>
            </div>
          </div>
        </div>
        <div class="bg-white dark:bg-gray-900 rounded-lg shadow p-6">
          <div class="flex items-center">
            <Icon icon="mdi:calendar-week" class="text-3xl text-blue-600 dark:text-blue-400 mr-4" />
            <div>
              <p class="text-sm text-gray-600 dark:text-gray-400">Doanh thu tuần</p>
              <p class="text-2xl font-bold text-gray-800 dark:text-white">{{ formatCurrency(dashboardData.weeklyRevenue?.totalRevenue || 0) }}</p>
            </div>
          </div>
        </div>
        <div class="bg-white dark:bg-gray-900 rounded-lg shadow p-6">
          <div class="flex items-center">
            <Icon icon="mdi:check-circle" class="text-3xl text-green-600 dark:text-green-400 mr-4" />
            <div>
              <p class="text-sm text-gray-600 dark:text-gray-400">Thanh toán thành công</p>
              <p class="text-2xl font-bold text-gray-800 dark:text-white">{{ dashboardData.monthlyRevenue?.completedCount || 0 }}</p>
            </div>
          </div>
        </div>
        <div class="bg-white dark:bg-gray-900 rounded-lg shadow p-6">
          <div class="flex items-center">
            <Icon icon="mdi:percent" class="text-3xl text-purple-600 dark:text-purple-400 mr-4" />
            <div>
              <p class="text-sm text-gray-600 dark:text-gray-400">Tỷ lệ chuyển đổi</p>
              <p class="text-2xl font-bold text-gray-800 dark:text-white">{{ conversionRate }}%</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Payment Trends -->
      <div class="bg-white dark:bg-gray-900 rounded-lg shadow p-6 mb-6">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">Xu hướng thanh toán</h2>
        <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Tổng số thanh toán</p>
            <p class="text-xl font-bold text-gray-800 dark:text-white">{{ dashboardData.trends?.totalPayments || 0 }}</p>
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Thành công</p>
            <p class="text-xl font-bold text-green-600 dark:text-green-400">{{ dashboardData.trends?.completedCount || 0 }}</p>
          </div>
          <div>
            <p class="text-sm text-gray-600 dark:text-gray-400">Thất bại</p>
            <p class="text-xl font-bold text-red-600 dark:text-red-400">{{ dashboardData.trends?.failedCount || 0 }}</p>
          </div>
        </div>
      </div>

      <!-- Top Users -->
      <div class="bg-white dark:bg-gray-900 rounded-lg shadow p-6">
        <h2 class="text-lg font-semibold text-gray-800 dark:text-white mb-4">Người dùng chi tiêu nhiều nhất</h2>
        <div v-if="topUsers.length === 0" class="text-center py-4 text-gray-500 dark:text-gray-400">
          Chưa có dữ liệu
        </div>
        <div v-else class="space-y-3">
          <div v-for="(user, index) in topUsers" :key="user.userId" class="flex items-center justify-between p-3 bg-gray-50 dark:bg-gray-800 rounded">
            <div class="flex items-center">
              <span class="w-8 h-8 flex items-center justify-center bg-blue-600 text-white rounded-full mr-3">{{ index + 1 }}</span>
              <div>
                <p class="font-medium text-gray-800 dark:text-white">{{ user.userName || user.userEmail }}</p>
                <p class="text-sm text-gray-600 dark:text-gray-400">{{ user.userEmail }}</p>
              </div>
            </div>
            <p class="font-bold text-gray-800 dark:text-white">{{ formatCurrency(user.totalSpent) }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'
import paymentAPI from '@/api/paymentApi'
import { ref, computed, onMounted } from 'vue'

export default {
  name: 'PaymentAnalytics',
  components: { Icon },
  setup() {
    const dashboardData = ref({})
    const topUsers = ref([])
    const loading = ref(false)

    const conversionRate = computed(() => {
      const total = dashboardData.value.trends?.totalPayments || 0
      const completed = dashboardData.value.trends?.completedCount || 0
      if (total === 0) return 0
      return ((completed / total) * 100).toFixed(1)
    })

    const loadDashboardData = async () => {
      loading.value = true
      try {
        const response = await paymentAPI.getDashboardAnalytics()
        dashboardData.value = response.data || {}
      } catch (error) {
        console.error('Error loading dashboard data:', error)
      } finally {
        loading.value = false
      }
    }

    const loadTopUsers = async () => {
      try {
        const now = new Date()
        const startDate = new Date(now.getFullYear(), now.getMonth(), 1).toISOString()
        const endDate = now.toISOString()
        const response = await paymentAPI.getTopUsers(10, startDate, endDate)
        topUsers.value = response.data || []
      } catch (error) {
        console.error('Error loading top users:', error)
      }
    }

    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    }

    onMounted(() => {
      loadDashboardData()
      loadTopUsers()
    })

    return {
      dashboardData,
      topUsers,
      loading,
      conversionRate,
      formatCurrency
    }
  }
}
</script>
