<template>
  <div class="relative">
    <!-- Empty state -->
    <div
      v-if="!events || events.length === 0"
      class="flex flex-col items-center justify-center py-12 text-gray-400 dark:text-gray-500"
    >
      <Icon icon="mdi:timeline-clock-outline" class="text-4xl mb-2" />
      <p class="text-sm">{{ emptyText || 'Chưa có hoạt động nào' }}</p>
    </div>

    <!-- Timeline list -->
    <ol v-else class="relative border-l-2 border-gray-200 dark:border-gray-700 ml-3">
      <li
        v-for="(event, index) in events"
        :key="event.id || index"
        class="mb-6 ml-6 last:mb-0"
      >
        <!-- Dot -->
        <span
          :class="getDotClass(event.type)"
          class="absolute -left-[11px] flex h-5 w-5 items-center justify-center rounded-full ring-4 ring-white dark:ring-gray-800"
        >
          <Icon :icon="getEventIcon(event.type)" class="text-xs text-white" />
        </span>

        <!-- Card -->
        <div
          class="bg-white dark:bg-gray-800 border border-gray-200 dark:border-gray-700 rounded-xl p-3 shadow-sm hover:shadow-md transition-shadow"
        >
          <div class="flex items-start justify-between gap-2">
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-gray-900 dark:text-white leading-snug truncate">
                {{ event.title }}
              </p>
              <p v-if="event.description" class="mt-0.5 text-xs text-gray-500 dark:text-gray-400 line-clamp-2">
                {{ event.description }}
              </p>
            </div>
            <div class="flex flex-col items-end flex-shrink-0 gap-1">
              <span
                v-if="event.badge"
                :class="getBadgeClass(event.type)"
                class="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-semibold"
              >
                {{ event.badge }}
              </span>
              <time class="text-[11px] text-gray-400 dark:text-gray-500 whitespace-nowrap">
                {{ formatTime(event.timestamp) }}
              </time>
            </div>
          </div>

          <!-- Link slot -->
          <router-link
            v-if="event.link"
            :to="event.link"
            class="mt-2 inline-flex items-center gap-1 text-xs text-blue-600 dark:text-blue-400 hover:underline"
          >
            <Icon icon="mdi:open-in-new" class="text-xs" />
            Xem chi tiết
          </router-link>
        </div>
      </li>
    </ol>
  </div>
</template>

<script setup>
const props = defineProps({
  events: { type: Array, default: () => [] },
  emptyText: { type: String, default: '' },
})

const typeConfig = {
  conversation: {
    dot: 'bg-blue-500',
    icon: 'mdi:message-text',
    badge: 'bg-blue-100 dark:bg-blue-900/40 text-blue-700 dark:text-blue-300',
  },
  message: {
    dot: 'bg-green-500',
    icon: 'mdi:chat',
    badge: 'bg-green-100 dark:bg-green-900/40 text-green-700 dark:text-green-300',
  },
  feedback: {
    dot: 'bg-yellow-500',
    icon: 'mdi:star',
    badge: 'bg-yellow-100 dark:bg-yellow-900/40 text-yellow-700 dark:text-yellow-300',
  },
  note: {
    dot: 'bg-purple-500',
    icon: 'mdi:note-text',
    badge: 'bg-purple-100 dark:bg-purple-900/40 text-purple-700 dark:text-purple-300',
  },
  escalation: {
    dot: 'bg-red-500',
    icon: 'mdi:alert-circle',
    badge: 'bg-red-100 dark:bg-red-900/40 text-red-700 dark:text-red-300',
  },
  default: {
    dot: 'bg-gray-400',
    icon: 'mdi:circle-small',
    badge: 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300',
  },
}

function getConfig(type) {
  return typeConfig[type] || typeConfig.default
}
function getDotClass(type) {
  return getConfig(type).dot
}
function getEventIcon(type) {
  return getConfig(type).icon
}
function getBadgeClass(type) {
  return getConfig(type).badge
}

function formatTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  const now = new Date()
  const diff = Math.floor((now - d) / 1000)
  if (diff < 60) return 'vừa xong'
  if (diff < 3600) return `${Math.floor(diff / 60)} phút trước`
  if (diff < 86400) return `${Math.floor(diff / 3600)} giờ trước`
  if (diff < 86400 * 7) return `${Math.floor(diff / 86400)} ngày trước`
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' })
}
</script>
