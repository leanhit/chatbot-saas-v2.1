<template>
  <div class="p-4 sm:p-6 lg:p-8 max-w-7xl mx-auto">
    <!-- Header Hero Section -->
    <div class="flex flex-col md:flex-row md:items-center md:justify-between mb-8 pb-4 border-b border-gray-200 dark:border-gray-700">
      <div>
        <h1 class="text-3xl font-extrabold text-gray-900 dark:text-white tracking-tight flex items-center gap-3">
          <Icon icon="clarity:notification-line" class="text-blue-600 dark:text-blue-400" />
          {{ t('notifications.title', 'Notification Center') }}
          <span 
            v-if="notificationStore.unreadCount > 0"
            class="inline-flex items-center px-3 py-1 rounded-full text-sm font-semibold bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400"
          >
            {{ notificationStore.unreadCount }} {{ t('notifications.unread', 'unread') }}
          </span>
        </h1>
        <p class="mt-2 text-sm text-gray-500 dark:text-gray-400">
          {{ t('notifications.subtitle', 'Manage and track your workspace activities, payments, and system updates.') }}
        </p>
      </div>
      
      <!-- Bulk Action Bar -->
      <div class="mt-4 md:mt-0 flex items-center gap-3">
        <button
          @click="markAllAsRead"
          :disabled="!hasUnread"
          class="inline-flex items-center px-4 py-2 border border-gray-300 dark:border-gray-600 text-sm font-medium rounded-lg text-gray-700 dark:text-gray-200 bg-white dark:bg-gray-800 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 shadow-sm"
        >
          <Icon icon="mdi:check-all" class="mr-2 h-4 w-4 text-green-500" />
          {{ t('notifications.actions.markAllRead', 'Mark all as read') }}
        </button>
        <button
          @click="clearAll"
          :disabled="!hasNotifications"
          class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-lg text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-200 shadow-sm"
        >
          <Icon icon="mdi:trash-can-outline" class="mr-2 h-4 w-4" />
          {{ t('notifications.actions.clearAll', 'Clear all history') }}
        </button>
      </div>
    </div>

    <!-- Main Content Split Pane Layout -->
    <div class="grid grid-cols-1 lg:grid-cols-4 gap-8">
      
      <!-- Left Pane: Filter Sidebar -->
      <div class="lg:col-span-1 space-y-2">
        <nav class="space-y-1 bg-white dark:bg-gray-800 p-4 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700">
          <p class="text-xs font-semibold text-gray-400 dark:text-gray-500 uppercase tracking-wider px-3 mb-2">
            {{ t('notifications.filters.title', 'Filters') }}
          </p>
          <button
            v-for="filter in filters"
            :key="filter.id"
            @click="activeFilter = filter.id"
            :class="[
              activeFilter === filter.id
                ? 'bg-blue-50 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400'
                : 'text-gray-700 hover:bg-gray-50 hover:text-gray-900 dark:text-gray-300 dark:hover:bg-gray-700/50 dark:hover:text-white',
              'w-full flex items-center justify-between px-3 py-2.5 text-sm font-medium rounded-lg transition-all duration-200'
            ]"
          >
            <div class="flex items-center">
              <Icon :icon="filter.icon" class="mr-3 h-5 w-5 flex-shrink-0" />
              <span>{{ filter.name }}</span>
            </div>
            <span
              v-if="filter.count > 0"
              :class="[
                activeFilter === filter.id
                  ? 'bg-blue-100 text-blue-800 dark:bg-blue-950 dark:text-blue-300'
                  : 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300',
                'inline-block py-0.5 px-2.5 text-xs font-bold rounded-full'
              ]"
            >
              {{ filter.count }}
            </span>
          </button>
        </nav>
      </div>

      <!-- Right Pane: Notifications List -->
      <div class="lg:col-span-3">
        <!-- Notification list card -->
        <div class="bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 overflow-hidden">
          
          <div v-if="filteredNotifications.length === 0" class="p-16 text-center">
            <div class="inline-flex items-center justify-center h-16 w-16 rounded-full bg-blue-50 dark:bg-blue-950 text-blue-500 dark:text-blue-400 mb-4 animate-pulse">
              <Icon icon="mdi:bell-off-outline" class="h-8 w-8" />
            </div>
            <h3 class="text-lg font-medium text-gray-900 dark:text-white">
              {{ t('notifications.empty.title', 'All caught up!') }}
            </h3>
            <p class="mt-2 text-sm text-gray-500 dark:text-gray-400 max-w-sm mx-auto">
              {{ t('notifications.empty.desc', 'No notifications found for this filter. Check back later for system activities.') }}
            </p>
          </div>

          <div v-else class="divide-y divide-gray-100 dark:divide-gray-700">
            <div
              v-for="item in filteredNotifications"
              :key="item.id"
              :class="[
                !item.read ? 'bg-blue-50/40 dark:bg-blue-950/15' : '',
                'p-5 transition-all duration-200 hover:bg-gray-50/70 dark:hover:bg-gray-700/30 flex items-start justify-between gap-4 group border-l-4',
                getBorderClass(item)
              ]"
            >
              <div class="flex items-start gap-4 flex-1">
                <!-- Icon container -->
                <div 
                  :class="[
                    getIconBgClass(item.type),
                    'h-10 w-10 rounded-full flex items-center justify-center flex-shrink-0 shadow-sm transition-transform duration-300 group-hover:scale-105'
                  ]"
                >
                  <Icon :icon="getNotificationIcon(item.type)" :class="[getIconColorClass(item.type), 'h-5 w-5']" />
                </div>

                <!-- Info and message -->
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2 flex-wrap">
                    <h4 class="text-sm font-semibold text-gray-900 dark:text-white truncate">
                      {{ item.title }}
                    </h4>
                    <!-- Priority badge -->
                    <span 
                      v-if="item.priority === 'urgent' || item.priority === 'high'"
                      class="inline-flex items-center px-2 py-0.5 rounded text-xs font-semibold bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-400"
                    >
                      {{ t('notifications.priority.high', 'High Priority') }}
                    </span>
                    <!-- Unread badge -->
                    <span 
                      v-if="!item.read" 
                      class="inline-flex h-2 w-2 rounded-full bg-blue-600 ring-2 ring-blue-100 dark:ring-blue-900"
                    ></span>
                  </div>
                  
                  <p class="text-sm text-gray-600 dark:text-gray-300 mt-1 whitespace-pre-line leading-relaxed">
                    {{ item.message }}
                  </p>

                  <!-- Metadata and Action Buttons -->
                  <div class="flex items-center justify-between mt-3 flex-wrap gap-3">
                    <span class="text-xs text-gray-400 dark:text-gray-500 flex items-center gap-1.5">
                      <Icon icon="clarity:calendar-line" class="h-3.5 w-3.5" />
                      {{ formatTime(item.timestamp) }}
                    </span>

                    <!-- CTAs for tenant/payment/messages -->
                    <div class="flex items-center gap-2">
                      <!-- Primary CTA Button -->
                      <button
                        @click="handleNotificationClick(item)"
                        class="inline-flex items-center px-3 py-1.5 text-xs font-semibold rounded-lg bg-blue-50 text-blue-700 hover:bg-blue-100 dark:bg-blue-900/40 dark:text-blue-300 transition-colors duration-200"
                      >
                        <Icon icon="mdi:arrow-right-circle" class="mr-1.5 h-3.5 w-3.5" />
                        {{ getCtaLabel(item) }}
                      </button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Quick action controls (mark read, delete) -->
              <div class="flex items-center gap-1 opacity-80 group-hover:opacity-100 transition-opacity duration-200">
                <button
                  @click="toggleReadState(item)"
                  class="p-1.5 rounded-lg text-gray-400 hover:text-gray-600 hover:bg-gray-100 dark:text-gray-500 dark:hover:text-gray-300 dark:hover:bg-gray-700 transition-colors duration-150"
                  :title="item.read ? t('notifications.actions.markUnread', 'Mark as unread') : t('notifications.actions.markRead', 'Mark as read')"
                >
                  <Icon :icon="item.read ? 'mdi:email-outline' : 'mdi:email-open-outline'" class="h-5 w-5" />
                </button>
                <button
                  @click="deleteNotification(item.id)"
                  class="p-1.5 rounded-lg text-gray-400 hover:text-red-600 hover:bg-red-50 dark:text-gray-500 dark:hover:text-red-400 dark:hover:bg-red-950/30 transition-colors duration-150"
                  :title="t('notifications.actions.delete', 'Delete')"
                >
                  <Icon icon="mdi:trash-can-outline" class="h-5 w-5" />
                </button>
              </div>

            </div>
          </div>

        </div>
      </div>

    </div>
  </div>
</template>

<script>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Icon } from '@iconify/vue'
import { useNotificationStore } from '@/stores/notification/notificationStore'

export default {
  name: 'Notifications',
  components: {
    Icon
  },
  setup() {
    const { t } = useI18n()
    const router = useRouter()
    const notificationStore = useNotificationStore()
    const activeFilter = ref('all')

    const hasNotifications = computed(() => notificationStore.notifications.length > 0)
    const hasUnread = computed(() => notificationStore.unreadCount > 0)

    // Dynamic Filter Tabs definition
    const filters = computed(() => [
      {
        id: 'all',
        name: t('notifications.filters.all', 'All'),
        icon: 'mdi:bell-outline',
        count: notificationStore.notifications.length
      },
      {
        id: 'unread',
        name: t('notifications.filters.unread', 'Unread'),
        icon: 'mdi:email-unread-outline',
        count: notificationStore.unreadCount
      },
      {
        id: 'invitations',
        name: t('notifications.filters.invitations', 'Invitations & Requests'),
        icon: 'mdi:account-plus-outline',
        count: notificationStore.notifications.filter(n => 
          n.type === 'tenant_invitation' || n.type === 'join_request'
        ).length
      },
      {
        id: 'system',
        name: t('notifications.filters.system', 'System Alerts'),
        icon: 'mdi:alert-circle-outline',
        count: notificationStore.notifications.filter(n => 
          n.type === 'system_alert' || n.type === 'error' || n.type === 'warning' || n.type === 'license_expired'
        ).length
      }
    ])

    // Filter notifications based on active selection
    const filteredNotifications = computed(() => {
      const all = notificationStore.notifications
      switch (activeFilter.value) {
        case 'unread':
          return all.filter(n => !n.read)
        case 'invitations':
          return all.filter(n => n.type === 'tenant_invitation' || n.type === 'join_request')
        case 'system':
          return all.filter(n => n.type === 'system_alert' || n.type === 'error' || n.type === 'warning' || n.type === 'license_expired')
        default:
          return all
      }
    })

    // Priority color borders
    const getBorderClass = (item) => {
      if (!item.read) {
        return 'border-l-blue-500'
      }
      if (item.priority === 'urgent' || item.priority === 'high' || item.type === 'error') {
        return 'border-l-red-500'
      }
      if (item.type === 'warning' || item.type === 'system_alert' || item.type === 'license_expired') {
        return 'border-l-amber-500'
      }
      if (item.type === 'success' || item.type === 'payment_success') {
        return 'border-l-green-500'
      }
      return 'border-l-transparent'
    }

    // Icon helper classes
    const getNotificationIcon = (type) => {
      const icons = {
        'info': 'mdi:information',
        'success': 'mdi:check-circle',
        'payment_success': 'mdi:credit-card-check-outline',
        'warning': 'mdi:alert',
        'error': 'mdi:alert-circle',
        'system_alert': 'mdi:alert',
        'tenant_invitation': 'mdi:account-plus',
        'join_request': 'mdi:account-question',
        'agent_takeover': 'mdi:account-switch',
        'license_expired': 'mdi:clock-alert-outline'
      }
      return icons[type] || 'mdi:bell'
    }

    const getIconBgClass = (type) => {
      const classes = {
        'success': 'bg-green-50 dark:bg-green-950/40',
        'payment_success': 'bg-green-50 dark:bg-green-950/40',
        'warning': 'bg-amber-50 dark:bg-amber-950/40',
        'system_alert': 'bg-amber-50 dark:bg-amber-950/40',
        'error': 'bg-red-50 dark:bg-red-950/40',
        'license_expired': 'bg-red-50 dark:bg-red-950/40',
        'tenant_invitation': 'bg-blue-50 dark:bg-blue-950/40',
        'join_request': 'bg-purple-50 dark:bg-purple-950/40',
        'agent_takeover': 'bg-indigo-50 dark:bg-indigo-950/40'
      }
      return classes[type] || 'bg-gray-50 dark:bg-gray-800'
    }

    const getIconColorClass = (type) => {
      const classes = {
        'success': 'text-green-600 dark:text-green-400',
        'payment_success': 'text-green-600 dark:text-green-400',
        'warning': 'text-amber-600 dark:text-amber-400',
        'system_alert': 'text-amber-600 dark:text-amber-400',
        'error': 'text-red-600 dark:text-red-400',
        'license_expired': 'text-red-600 dark:text-red-400',
        'tenant_invitation': 'text-blue-600 dark:text-blue-400',
        'join_request': 'text-purple-600 dark:text-purple-400',
        'agent_takeover': 'text-indigo-600 dark:text-indigo-400'
      }
      return classes[type] || 'text-gray-600 dark:text-gray-400'
    }

    // Dynamic CTA labels
    const getCtaLabel = (item) => {
      const title = (item.title || '').toLowerCase()
      const type = (item.type || '').toLowerCase()

      if (type === 'tenant_invitation' || title.includes('invitation')) {
        return title.includes('accepted') ? t('notifications.cta.viewMembers', 'View Members') : t('notifications.cta.reviewInvitation', 'Review Invitation')
      }
      if (type === 'join_request' || title.includes('join request')) {
        return title.includes('approved') ? t('notifications.cta.enterWorkspace', 'Enter Workspace') : t('notifications.cta.reviewRequest', 'Review Request')
      }
      if (type === 'agent_takeover' || title.includes('takeover') || title.includes('conversation')) {
        return t('notifications.cta.goToChat', 'Go to Chat')
      }
      if (title.includes('payment') || title.includes('deposit') || type === 'payment_success') {
        return title.includes('success') ? t('notifications.cta.paymentHistory', 'Payment History') : t('notifications.cta.paymentDeposit', 'Deposit Now')
      }
      if (title.includes('tenant updated')) {
        return t('notifications.cta.tenantSettings', 'Tenant Settings')
      }
      
      return t('notifications.cta.viewDetails', 'View Details')
    }

    // Relative format time
    const formatTime = (timestamp) => {
      if (!timestamp) return ''
      
      const date = new Date(timestamp)
      const now = new Date()
      const diff = now - date
      
      if (diff < 60000) {
        return t('notifications.time.justNow', 'Just now')
      }
      if (diff < 3600000) {
        const minutes = Math.floor(diff / 60000)
        return t('notifications.time.minutesAgo', '{n}m ago', { n: minutes })
      }
      if (diff < 86400000) {
        const hours = Math.floor(diff / 3600000)
        return t('notifications.time.hoursAgo', '{n}h ago', { n: hours })
      }
      
      const days = Math.floor(diff / 86400000)
      return t('notifications.time.daysAgo', '{n}d ago', { n: days })
    }

    // Bulk actions
    const markAllAsRead = () => {
      notificationStore.acknowledgeAll()
    }

    const clearAll = () => {
      if (confirm(t('notifications.confirm.clearAll', 'Are you sure you want to clear all notification history?'))) {
        notificationStore.clearAll()
      }
    }

    // Single item actions
    const toggleReadState = (item) => {
      if (item.read) {
        item.read = false
        notificationStore.unreadCount++
        notificationStore.saveToStorage()
      } else {
        notificationStore.acknowledgeNotification(item.id)
      }
    }

    const deleteNotification = (id) => {
      notificationStore.removeNotification(id)
    }

    // Routing click handler (matching Header.vue logic)
    const handleNotificationClick = (item) => {
      // Mark as read locally and backend
      if (!item.read) {
        notificationStore.acknowledgeNotification(item.id)
      }

      const title = (item.title || '').toLowerCase()
      const type = (item.type || '').toLowerCase()

      if (type === 'tenant_invitation' || title.includes('invitation')) {
        if (title.includes('accepted')) {
          router.push('/tenant/members')
        } else {
          router.push('/tenant-gateway')
        }
      } else if (type === 'join_request' || title.includes('join request')) {
        if (title.includes('approved')) {
          router.push('/tenant-gateway')
        } else {
          router.push('/tenant/members')
        }
      } else if (title.includes('member')) {
        router.push('/tenant/members')
      } else if (type === 'agent_takeover' || title.includes('takeover') || title.includes('conversation')) {
        router.push('/messages')
      } else if (type === 'success' && title.includes('payment')) {
        router.push('/payment/history')
      } else if (type === 'error' && (title.includes('payment') || title.includes('expired'))) {
        router.push('/payment/deposit')
      } else if (title.includes('tenant updated')) {
        router.push('/tenant/settings')
      } else if (title.includes('role changed')) {
        router.push('/tenant/overview')
      } else {
        if (type === 'info' && title.includes('message')) {
          router.push('/messages')
        }
      }
    }

    return {
      t,
      notificationStore,
      activeFilter,
      filters,
      filteredNotifications,
      hasNotifications,
      hasUnread,
      getBorderClass,
      getNotificationIcon,
      getIconBgClass,
      getIconColorClass,
      getCtaLabel,
      formatTime,
      markAllAsRead,
      clearAll,
      toggleReadState,
      deleteNotification,
      handleNotificationClick
    }
  }
}
</script>

<style scoped>
/* High performance transitions */
.transition-transform {
  will-change: transform;
}
</style>
