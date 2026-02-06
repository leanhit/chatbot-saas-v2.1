import { defineComponent, ref, watch, onUnmounted, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'

import { useGatewaySearchStore } from '@/stores/tenant/gateway/searchTenantStore'
import type { TenantSearchResponse } from '@/types/tenant'

export default defineComponent({
  name: 'SearchTenantTab',
  components: { Search },

  setup() {
    const { t } = useI18n()
    const searchStore = useGatewaySearchStore()

    const keyword = ref('')
    let debounceTimer: number | null = null

    const results = computed(() => searchStore.results)
    const loading = computed(() => searchStore.loading)

    const executeSearch = async () => {
      await searchStore.searchTenants(keyword.value.trim())
    }

    const handleManualSearch = async () => {
      if (!keyword.value.trim()) return
      await executeSearch()
    }

    const onJoinClick = async (tenantId: number) => {
      try {
        await searchStore.requestJoinTenant(String(tenantId))
        ElMessage.success(t('Tenant.JoinRequested'))

        // update UI immediately
        const tenant = searchStore.results.find(t => t.id === tenantId)
        if (tenant) {
          tenant.membershipStatus = 'PENDING'
        }
      } catch {
        ElMessage.error(t('Tenant.JoinFailed'))
      }
    }

    const maskEmail = (email?: string | null) => {
      if (!email) return ''
      const [name, domain] = email.split('@')
      if (!domain) return email
      return `${name.slice(0, 2)}***@${domain}`
    }

    watch(keyword, (newVal) => {
      if (debounceTimer) clearTimeout(debounceTimer)

      if (!newVal.trim()) {
        searchStore.clearResults()
        return
      }

      if (newVal.trim().length >= 2) {
        debounceTimer = window.setTimeout(() => {
          executeSearch()
        }, 500)
      }
    })

    onUnmounted(() => {
      if (debounceTimer) clearTimeout(debounceTimer)
      searchStore.clearResults()
    })

    return {
      t,
      Search,
      keyword,
      loading,
      results,
      handleManualSearch,
      onJoinClick,
      maskEmail
    }
  }
})