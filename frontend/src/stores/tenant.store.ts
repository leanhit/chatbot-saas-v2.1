import { defineStore } from 'pinia'
import api from '@/plugins/axios'

export const useTenantStore = defineStore('tenant', {
  state: () => ({
    currentTenant: null,
    tenantList: [],
    isLoading: false
  }),

  actions: {
    setTenant(tenant: any) {
      this.currentTenant = tenant
      localStorage.setItem('tenant_id', tenant.id)
    },

    clear() {
      this.currentTenant = null
      this.tenantList = []
      localStorage.removeItem('tenant_id')
    },

    async loadUserTenants() {
      this.isLoading = true
      try {
        const response = await api.get('/tenants/me')
        this.tenantList = response.data.tenants || []
        
        // Auto-select if only one tenant
        if (this.tenantList.length === 1) {
          this.setTenant(this.tenantList[0])
        }
        
        return this.tenantList
      } catch (error) {
        console.error('Failed to load user tenants:', error)
        this.tenantList = []
        throw error
      } finally {
        this.isLoading = false
      }
    }
  }
})
