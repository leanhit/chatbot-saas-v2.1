<template>
  <span
    v-if="visible"
    :class="badgeClass"
    class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-semibold tracking-wide border"
    :title="label"
  >
    <Icon :icon="icon" class="text-xs" />
    <span>{{ label }}</span>
  </span>
</template>

<script setup>
import { computed } from 'vue'
import { Icon } from '@iconify/vue'

const props = defineProps({
  tier: { type: String, default: '' },
  size: { type: String, default: 'sm' }, // 'xs' | 'sm' | 'md'
})

const tierConfig = {
  ENTERPRISE: {
    label: 'Enterprise',
    icon: 'mdi:crown',
    class: 'bg-amber-50 dark:bg-amber-900/30 text-amber-700 dark:text-amber-300 border-amber-300 dark:border-amber-600',
  },
  PREMIUM: {
    label: 'Premium',
    icon: 'mdi:star-circle',
    class: 'bg-purple-50 dark:bg-purple-900/30 text-purple-700 dark:text-purple-300 border-purple-300 dark:border-purple-600',
  },
  BASIC: {
    label: 'Basic',
    icon: 'mdi:shield-check',
    class: 'bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300 border-blue-200 dark:border-blue-700',
  },
  FREE: {
    label: 'Free',
    icon: 'mdi:account',
    class: 'bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 border-gray-300 dark:border-gray-600',
  },
}

const config = computed(() => tierConfig[props.tier?.toUpperCase()] || null)
const visible = computed(() => !!config.value)
const label = computed(() => config.value?.label || '')
const icon = computed(() => config.value?.icon || 'mdi:account')
const badgeClass = computed(() => config.value?.class || '')
</script>
