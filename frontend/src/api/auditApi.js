// Tenant audit log API
import axios from '@/plugins/axios'

export const auditApi = {
  getAuditLogs: (params = {}) =>
    axios.get('/tenant/audit-logs', { params }),
}

export default auditApi
