<template>
  <div class="bg-white dark:bg-gray-800 shadow rounded-lg p-6">
    <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-4">Quick Stats</h3>
    <div class="space-y-3">
      <div class="flex justify-between items-center">
        <span class="text-sm text-gray-600 dark:text-gray-400">Active Users</span>
        <span class="text-sm font-medium text-gray-900 dark:text-white">{{ computedStats.activeUsers }}</span>
      </div>
      <div class="flex justify-between items-center">
        <span class="text-sm text-gray-600 dark:text-gray-400">Total Projects</span>
        <span class="text-sm font-medium text-gray-900 dark:text-white">{{ computedStats.totalProjects }}</span>
      </div>
      <div class="flex justify-between items-center">
        <span class="text-sm text-gray-600 dark:text-gray-400">Storage Used</span>
        <span class="text-sm font-medium text-gray-900 dark:text-white">{{ computedStats.storageUsed }}</span>
      </div>
      <div class="flex justify-between items-center">
        <span class="text-sm text-gray-600 dark:text-gray-400">API Calls</span>
        <span class="text-sm font-medium text-gray-900 dark:text-white">{{ computedStats.apiCalls }}</span>
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
        totalProjects: 0,
        storageUsed: '0 GB',
        apiCalls: '0'
      })
    },
    tenant: {
      type: Object,
      default: () => ({})
    }
  },
  setup(props) {
    // Compute stats based on tenant data or use provided stats
    const computedStats = computed(() => {
      // If tenant has real stats data, use it
      if (props.tenant?.stats) {
        return props.tenant.stats
      }
      
      // Otherwise use provided stats or compute from tenant data
      return {
        activeUsers: props.stats.activeUsers || (props.tenant?.memberCount || 0),
        totalProjects: props.stats.totalProjects || (props.tenant?.projectCount || 0),
        storageUsed: props.stats.storageUsed || '0 GB',
        apiCalls: props.stats.apiCalls || '0'
      }
    })
    
    return {
      computedStats
    }
  }
}
</script>
