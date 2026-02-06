import { defineComponent, ref, onMounted, computed } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { useUserInvitationStore } from '@/stores/userInvitationStore'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'

export default defineComponent({
  name: 'UserInvitations',
  setup() {
    const { t } = useI18n()
    const invitationStore = useUserInvitationStore()
    const loading = ref(false)
    const loadingActions = ref<Record<number, { accepting: boolean; rejecting: boolean }>>({})

    const invitations = computed(() => invitationStore.pendingInvitations)

    const fetchInvitations = async () => {
      loading.value = true
      try {
        await invitationStore.fetchInvitations()
      } finally {
        loading.value = false
      }
    }

    onMounted(() => {
      fetchInvitations()
    })

    const formatRole = (role: string) => {
      const roles: Record<string, string> = {
        'ADMIN': t('Administrator'),
        'MEMBER': t('Member'),
        'EDITOR': t('Editor'),
        'VIEWER': t('Viewer')
      }
      return roles[role] || role
    }

    const formatDate = (dateString: string) => {
      return new Date(dateString).toLocaleDateString('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    }

    const handleAccept = async (invitationId: number) => {
      initLoadingAction(invitationId)
      loadingActions.value[invitationId].accepting = true
      try {
        const success = await invitationStore.acceptInvitation(invitationId)
        if (success) ElMessage.success(t('Invitation accepted successfully'))
      } finally {
        loadingActions.value[invitationId].accepting = false
      }
    }

    const handleReject = async (invitationId: number) => {
      initLoadingAction(invitationId)
      loadingActions.value[invitationId].rejecting = true
      try {
        const success = await invitationStore.rejectInvitation(invitationId)
        if (success) ElMessage.warning(t('Invitation rejected'))
      } finally {
        loadingActions.value[invitationId].rejecting = false
      }
    }

    const initLoadingAction = (id: number) => {
      if (!loadingActions.value[id]) {
        loadingActions.value[id] = { accepting: false, rejecting: false }
      }
    }

    return {
      t,
      Refresh,
      loading,
      invitations,
      loadingActions,
      fetchInvitations,
      formatRole,
      formatDate,
      handleAccept,
      handleReject
    }
  }
})