<template>
  <div class="pending-tenant-tab">
    <div class="text-center py-12">
      <Icon icon="mdi:clock" class="h-16 w-16 text-gray-400 mx-auto mb-4" />
      <h3 class="text-lg font-medium text-gray-900 dark:text-white mb-2">
        {{ $t('Pending Invitations') }}
      </h3>
      <p class="text-gray-600 dark:text-gray-400">
        No pending invitations at the moment.
      </p>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'
import { useI18n } from 'vue-i18n'

export default {
  name: 'PendingTenantTab',
  components: {
    Icon
  },
  setup() {
    const { t } = useI18n()
    
    return {
      t
    }
  }
}
</script>

<style scoped>
.pending-tenant-tab {
  width: 100%;
}
</style>
