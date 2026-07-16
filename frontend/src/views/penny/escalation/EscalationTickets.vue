<template>
  <div class="penny-escalation">
    <!-- Header -->
    <div class="flex justify-between items-center mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 dark:text-white">
          {{ $t('penny.escalation.title') }}
        </h1>
        <p class="text-gray-600 dark:text-gray-400 mt-1">
          {{ $t('penny.escalation.subtitle') }}
        </p>
      </div>
      <div>
        <button
          @click="refreshTickets"
          :disabled="loading"
          class="inline-flex items-center px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/80 transition-colors disabled:opacity-50 text-sm font-medium"
        >
          <Icon icon="mdi:refresh" :class="{'animate-spin': loading}" class="mr-2" />
          {{ $t('penny.escalation.refresh') }}
        </button>
      </div>
    </div>

    <!-- Filter Tabs -->
    <div class="flex flex-wrap gap-2 mb-6">
      <button
        v-for="status in statusFilters"
        :key="status.value"
        @click="currentFilter = status.value"
        :class="[
          'px-4 py-2 text-sm font-medium rounded-md border transition-colors',
          currentFilter === status.value
            ? 'bg-primary text-white border-primary'
            : 'bg-white dark:bg-gray-800 text-gray-700 dark:text-gray-300 border-gray-300 dark:border-gray-700 hover:bg-gray-50 dark:hover:bg-gray-700'
        ]"
      >
        {{ $t(status.labelKey) }}
      </button>
    </div>

    <!-- Tickets Table -->
    <div class="bg-white dark:bg-gray-800 rounded-md border border-gray-200 dark:border-gray-700 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
          <thead class="bg-gray-50 dark:bg-gray-700">
            <tr>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('penny.escalation.id') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('penny.escalation.userId') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('penny.escalation.reason') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('penny.escalation.status') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('penny.escalation.priority') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('penny.escalation.assignedAgent') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('penny.escalation.created') }}
              </th>
              <th class="px-6 py-3 text-left text-xs font-semibold text-gray-500 dark:text-gray-300 uppercase tracking-wider">
                {{ $t('penny.escalation.actions') }}
              </th>
            </tr>
          </thead>
          <tbody class="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
            <tr v-if="loading">
              <td colspan="8" class="px-6 py-10 text-center text-gray-500">
                <Icon icon="mdi:loading" class="text-4xl text-gray-300 animate-spin mx-auto mb-2" />
                {{ $t('penny.escalation.loading') }}
              </td>
            </tr>
            <tr v-else-if="filteredTickets.length === 0">
              <td colspan="8" class="px-6 py-10 text-center text-gray-500">
                {{ $t('penny.escalation.noTickets') }}
              </td>
            </tr>
            <tr v-for="ticket in filteredTickets" :key="ticket.id" v-else>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ ticket.id.substring(0, 8) }}...
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ ticket.userId }}
              </td>
              <td class="px-6 py-4 text-sm text-gray-900 dark:text-white">
                {{ ticket.reason || '-' }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm">
                <span :class="['px-2.5 py-1 rounded-full text-xs font-medium', statusBadgeClass(ticket.status)]">
                  {{ $t('penny.escalation.statusFilters.' + ticket.status.toLowerCase()) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm">
                <span :class="['px-2.5 py-1 rounded-full text-xs font-medium', priorityBadgeClass(ticket.priority)]">
                  {{ $t('penny.escalation.priorities.' + ticket.priority.toLowerCase()) }}
                </span>
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ ticket.assignedAgentId || '-' }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-white">
                {{ formatDate(ticket.createdAt) }}
              </td>
              <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                <div class="flex gap-2">
                  <button
                    v-if="ticket.status === 'PENDING'"
                    @click="showAssignModal(ticket)"
                    class="inline-flex items-center p-1.5 bg-blue-100 text-blue-700 dark:bg-blue-900/50 dark:text-blue-300 rounded hover:bg-blue-200 dark:hover:bg-blue-900 transition-colors"
                    :title="$t('penny.escalation.assign')"
                  >
                    <Icon icon="mdi:user-add" class="text-lg" />
                  </button>
                  <button
                    v-if="ticket.status === 'ASSIGNED' || ticket.status === 'PENDING'"
                    @click="showResolveModal(ticket)"
                    class="inline-flex items-center p-1.5 bg-green-100 text-green-700 dark:bg-green-900/50 dark:text-green-300 rounded hover:bg-green-200 dark:hover:bg-green-900 transition-colors"
                    :title="$t('penny.escalation.resolve')"
                  >
                    <Icon icon="mdi:check" class="text-lg" />
                  </button>
                  <button
                    v-if="ticket.status === 'PENDING'"
                    @click="cancelTicket(ticket)"
                    class="inline-flex items-center p-1.5 bg-red-100 text-red-700 dark:bg-red-900/50 dark:text-red-300 rounded hover:bg-red-200 dark:hover:bg-red-900 transition-colors"
                    :title="$t('penny.escalation.cancel')"
                  >
                    <Icon icon="mdi:close" class="text-lg" />
                  </button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- Assign Modal -->
    <div v-if="showAssign" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div class="bg-white dark:bg-gray-800 rounded-lg max-w-md w-full border border-gray-200 dark:border-gray-700 shadow-xl overflow-hidden">
        <div class="flex justify-between items-center px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h2 class="text-lg font-bold text-gray-900 dark:text-white">
            {{ $t('penny.escalation.assignTicket') }}
          </h2>
          <button @click="showAssign = false" class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-250">
            <Icon icon="mdi:close" class="text-xl" />
          </button>
        </div>
        <div class="p-6">
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              {{ $t('penny.escalation.assignedAgent') }}
            </label>
            <select
              v-model="assignForm.agentId"
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-md bg-white dark:bg-gray-900 text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary text-sm"
            >
              <option value="">{{ $t('penny.escalation.selectAgent') }}</option>
              <option v-for="agent in availableAgents" :key="agent.id" :value="agent.id">
                {{ agent.name }}
              </option>
            </select>
          </div>
          <div class="flex justify-end gap-3 mt-6">
            <button
              @click="showAssign = false"
              class="px-4 py-2 border border-gray-300 dark:border-gray-700 rounded-md bg-white dark:bg-gray-900 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-800 text-sm font-medium transition-colors"
            >
              {{ $t('penny.escalation.cancel') }}
            </button>
            <button
              @click="assignTicket"
              class="px-4 py-2 bg-primary text-white rounded-md hover:bg-primary/80 text-sm font-medium transition-colors"
            >
              {{ $t('penny.escalation.assign') }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Resolve Modal -->
    <div v-if="showResolve" class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
      <div class="bg-white dark:bg-gray-800 rounded-lg max-w-md w-full border border-gray-200 dark:border-gray-700 shadow-xl overflow-hidden">
        <div class="flex justify-between items-center px-6 py-4 border-b border-gray-200 dark:border-gray-700">
          <h2 class="text-lg font-bold text-gray-900 dark:text-white">
            {{ $t('penny.escalation.resolve') }}
          </h2>
          <button @click="showResolve = false" class="text-gray-400 hover:text-gray-600 dark:hover:text-gray-250">
            <Icon icon="mdi:close" class="text-xl" />
          </button>
        </div>
        <div class="p-6">
          <div class="mb-4">
            <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-2">
              {{ $t('penny.escalation.resolutionNotes') }}
            </label>
            <textarea
              v-model="resolveForm.notes"
              class="w-full px-3 py-2 border border-gray-300 dark:border-gray-700 rounded-md bg-white dark:bg-gray-900 text-gray-900 dark:text-white focus:outline-none focus:ring-1 focus:ring-primary text-sm"
              rows="4"
              :placeholder="$t('penny.escalation.notesPlaceholder')"
            ></textarea>
          </div>
          <div class="flex justify-end gap-3 mt-6">
            <button
              @click="showResolve = false"
              class="px-4 py-2 border border-gray-300 dark:border-gray-700 rounded-md bg-white dark:bg-gray-900 text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-800 text-sm font-medium transition-colors"
            >
              {{ $t('penny.escalation.cancel') }}
            </button>
            <button
              @click="resolveTicket"
              class="px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 text-sm font-medium transition-colors"
            >
              {{ $t('penny.escalation.resolve') }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue';
import { pennyApi } from '@/api/pennyApi';

export default {
  name: 'EscalationTickets',
  components: {
    Icon
  },
  props: {
    botId: {
      type: String,
      required: true
    }
  },
  data() {
    return {
      tickets: [],
      loading: false,
      currentFilter: 'ALL',
      
      statusFilters: [
        { labelKey: 'penny.escalation.statusFilters.all', value: 'ALL' },
        { labelKey: 'penny.escalation.statusFilters.pending', value: 'PENDING' },
        { labelKey: 'penny.escalation.statusFilters.assigned', value: 'ASSIGNED' },
        { labelKey: 'penny.escalation.statusFilters.resolved', value: 'RESOLVED' },
        { labelKey: 'penny.escalation.statusFilters.cancelled', value: 'CANCELLED' }
      ],
      
      // Modals
      showAssign: false,
      showResolve: false,
      selectedTicket: null,
      
      // Forms
      assignForm: {
        agentId: ''
      },
      resolveForm: {
        notes: ''
      },
      
      // Mock agents - replace with actual agent list from backend
      availableAgents: [
        { id: 'agent1', name: 'Agent 1' },
        { id: 'agent2', name: 'Agent 2' },
        { id: 'agent3', name: 'Agent 3' }
      ]
    };
  },
  computed: {
    filteredTickets() {
      if (this.currentFilter === 'ALL') {
        return this.tickets;
      }
      return this.tickets.filter(ticket => ticket.status === this.currentFilter);
    }
  },
  mounted() {
    this.loadTickets();
  },
  methods: {
    async loadTickets() {
      this.loading = true;
      try {
        const response = await pennyApi.getEscalationTickets(this.botId);
        this.tickets = response.data.content || response.data;
      } catch (error) {
        console.error('Error loading tickets:', error);
        this.$toast.error(this.$t('penny.escalation.failedToLoad'));
      } finally {
        this.loading = false;
      }
    },
    
    refreshTickets() {
      this.loadTickets();
    },
    
    showAssignModal(ticket) {
      this.selectedTicket = ticket;
      this.assignForm.agentId = '';
      this.showAssign = true;
    },
    
    showResolveModal(ticket) {
      this.selectedTicket = ticket;
      this.resolveForm.notes = '';
      this.showResolve = true;
    },
    
    async assignTicket() {
      if (!this.assignForm.agentId) {
        this.$toast.error(this.$t('penny.escalation.pleaseSelectAgent'));
        return;
      }
      
      try {
        await pennyApi.assignEscalationTicket(this.botId, this.selectedTicket.id, this.assignForm.agentId);
        
        // Update local state
        this.selectedTicket.status = 'ASSIGNED';
        this.selectedTicket.assignedAgentId = this.assignForm.agentId;
        
        this.$toast.success(this.$t('penny.escalation.ticketAssigned'));
        this.showAssign = false;
      } catch (error) {
        console.error('Error assigning ticket:', error);
        this.$toast.error(this.$t('penny.escalation.failedToAssign'));
      }
    },
    
    async resolveTicket() {
      try {
        await pennyApi.resolveEscalationTicket(this.botId, this.selectedTicket.id, this.resolveForm.notes);
        
        // Update local state
        this.selectedTicket.status = 'RESOLVED';
        this.selectedTicket.resolutionNotes = this.resolveForm.notes;
        this.selectedTicket.resolvedAt = new Date().toISOString();
        
        this.$toast.success(this.$t('penny.escalation.ticketResolved'));
        this.showResolve = false;
        this.loadTickets();
      } catch (error) {
        console.error('Error resolving ticket:', error);
        this.$toast.error(this.$t('penny.escalation.failedToResolve'));
      }
    },
    
    async cancelTicket(ticket) {
      if (!confirm(this.$t('penny.escalation.cancelConfirm'))) return;
      
      try {
        await pennyApi.cancelEscalationTicket(this.botId, ticket.id);
        
        ticket.status = 'CANCELLED';
        this.$toast.success(this.$t('penny.escalation.ticketCancelled'));
      } catch (error) {
        console.error('Error cancelling ticket:', error);
        this.$toast.error(this.$t('penny.escalation.failedToCancel'));
      }
    },
    
    statusBadgeClass(status) {
      switch (status) {
        case 'PENDING':
          return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900/50 dark:text-yellow-300';
        case 'ASSIGNED':
          return 'bg-blue-100 text-blue-800 dark:bg-blue-900/50 dark:text-blue-300';
        case 'RESOLVED':
          return 'bg-green-100 text-green-800 dark:bg-green-900/50 dark:text-green-300';
        case 'CANCELLED':
          return 'bg-red-100 text-red-800 dark:bg-red-900/50 dark:text-red-300';
        default:
          return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300';
      }
    },
    
    priorityBadgeClass(priority) {
      switch (priority) {
        case 'LOW':
          return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300';
        case 'NORMAL':
          return 'bg-blue-100 text-blue-800 dark:bg-blue-900/50 dark:text-blue-300';
        case 'HIGH':
          return 'bg-orange-100 text-orange-800 dark:bg-orange-900/50 dark:text-orange-300';
        case 'URGENT':
          return 'bg-red-100 text-red-800 dark:bg-red-900/50 dark:text-red-300';
        default:
          return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300';
      }
    },
    
    formatDate(date) {
      if (!date) return '-';
      return new Date(date).toLocaleString();
    }
  }
};
</script>

<style scoped>
.penny-escalation {
  width: 100%;
  padding: 20px;
}

.animate-spin {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
