<template>
  <span
    :class="[
      'inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium',
      badgeClass
    ]"
  >
    <Icon v-if="role === TenantRole.OWNER" icon="mdi:crown" class="h-3 w-3 mr-1" />
    <Icon v-if="role === TenantRole.ADMIN" icon="mdi:shield-account" class="h-3 w-3 mr-1" />
    <Icon v-if="role === TenantRole.EDITOR" icon="mdi:pencil" class="h-3 w-3 mr-1" />
    <Icon v-if="role === TenantRole.MEMBER" icon="mdi:account" class="h-3 w-3 mr-1" />
    {{ roleLabel }}
  </span>
</template>

<script setup>
import { computed } from 'vue'
import { Icon } from '@iconify/vue'
import { TenantRole } from '@/types/tenant'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  role: {
    type: String,
    default: 'MEMBER'
  },
  size: {
    type: String,
    default: 'default', // 'small' | 'default' | 'large'
  }
})

const { t } = useI18n()

const badgeClass = computed(() => {
  const baseClasses = {
    small: 'px-2 py-0.5 text-[10px]',
    default: 'px-2.5 py-0.5 text-xs',
    large: 'px-3 py-1 text-sm'
  }

  const sizeClass = baseClasses[props.size] || baseClasses.default

  const roleClasses = {
    [TenantRole.OWNER]: 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200',
    [TenantRole.ADMIN]: 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200',
    [TenantRole.EDITOR]: 'bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-200',
    [TenantRole.MEMBER]: 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200',
    'NONE': 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300'
  }

  return `${sizeClass} ${roleClasses[props.role] || roleClasses.MEMBER}`
})

const roleLabel = computed(() => {
  const labels = {
    [TenantRole.OWNER]: t('tenant.member.owner') || 'Owner',
    [TenantRole.ADMIN]: t('tenant.member.admin') || 'Admin',
    [TenantRole.EDITOR]: t('tenant.member.editor') || 'Editor',
    [TenantRole.MEMBER]: t('tenant.member.member') || 'Member',
    'NONE': t('tenant.member.none') || 'None'
  }

  return labels[props.role] || props.role
})
</script>
