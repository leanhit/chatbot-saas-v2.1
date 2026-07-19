<template>
  <div id="tenant-quick-stats" class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
    <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">Quick Stats</h3>
    <div class="space-y-3">
      <div class="flex justify-between items-center">
        <span class="text-sm text-gray-600 dark:text-gray-400">Active Users</span>
        <span class="text-sm font-medium text-gray-900 dark:text-white">{{ computedStats.activeUsers }}</span>
      </div>
      <div class="flex justify-between items-center">
        <span class="text-sm text-gray-600 dark:text-gray-400">Total Bots</span>
        <span class="text-sm font-medium text-gray-900 dark:text-white">{{ computedStats.totalBots }}</span>
      </div>
      <div class="flex justify-between items-center">
        <span class="text-sm text-gray-600 dark:text-gray-400">Storage Used</span>
        <span class="text-sm font-medium text-gray-900 dark:text-white">{{ computedStats.storageUsed }}</span>
      </div>
      <div class="flex justify-between items-center">
        <span class="text-sm text-gray-600 dark:text-gray-400">Total Messages</span>
        <span class="text-sm font-medium text-gray-900 dark:text-white">{{ computedStats.totalMessages.toLocaleString() }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'

export default {
  name: 'QuickStats',
  props: {
    stats: {
      type: Object,
      default: () => ({
        activeUsers: 0,
        totalBots: 0,
        storageUsed: '0 B',
        totalMessages: 0
      })
    },
    tenant: {
      type: Object,
      default: () => ({})
    }
  },
  setup(props) {
    const computedStats = computed(() => ({
      activeUsers:   props.stats.activeUsers   ?? (props.tenant?.memberCount ?? 0),
      totalBots:     props.stats.totalBots     ?? 0,
      storageUsed:   props.stats.storageUsed   ?? '0 B',
      totalMessages: props.stats.totalMessages ?? 0
    }))

    return {
      computedStats
    }
  }
}
</script>
