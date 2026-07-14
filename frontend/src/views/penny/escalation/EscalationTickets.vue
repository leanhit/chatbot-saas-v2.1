<template>
  <div class="escalation-tickets">
    <div class="page-header">
      <h1>Escalation Tickets</h1>
      <button @click="refreshTickets" class="btn btn-primary">
        <i class="fas fa-sync"></i> Refresh
      </button>
    </div>

    <!-- Filter Tabs -->
    <div class="filter-tabs">
      <button
        v-for="status in statusFilters"
        :key="status.value"
        @click="currentFilter = status.value"
        :class="['tab-btn', { active: currentFilter === status.value }]"
      >
        {{ status.label }}
      </button>
    </div>

    <!-- Tickets Table -->
    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>User ID</th>
            <th>Reason</th>
            <th>Status</th>
            <th>Priority</th>
            <th>Assigned Agent</th>
            <th>Created</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="loading">
            <td colspan="8" class="text-center">Loading...</td>
          </tr>
          <tr v-else-if="filteredTickets.length === 0">
            <td colspan="8" class="text-center">No tickets found</td>
          </tr>
          <tr v-for="ticket in filteredTickets" :key="ticket.id">
            <td>{{ ticket.id.substring(0, 8) }}...</td>
            <td>{{ ticket.userId }}</td>
            <td>{{ ticket.reason || '-' }}</td>
            <td>
              <span :class="['status-badge', statusClass(ticket.status)]">
                {{ ticket.status }}
              </span>
            </td>
            <td>
              <span :class="['priority-badge', priorityClass(ticket.priority)]">
                {{ ticket.priority }}
              </span>
            </td>
            <td>{{ ticket.assignedAgentId || '-' }}</td>
            <td>{{ formatDate(ticket.createdAt) }}</td>
            <td class="actions">
              <button
                v-if="ticket.status === 'PENDING'"
                @click="showAssignModal(ticket)"
                class="btn btn-sm btn-primary"
                title="Assign"
              >
                <i class="fas fa-user-plus"></i>
              </button>
              <button
                v-if="ticket.status === 'ASSIGNED' || ticket.status === 'PENDING'"
                @click="showResolveModal(ticket)"
                class="btn btn-sm btn-success"
                title="Resolve"
              >
                <i class="fas fa-check"></i>
              </button>
              <button
                v-if="ticket.status === 'PENDING'"
                @click="cancelTicket(ticket)"
                class="btn btn-sm btn-danger"
                title="Cancel"
              >
                <i class="fas fa-times"></i>
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Assign Modal -->
    <div v-if="showAssign" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h2>Assign Ticket</h2>
          <button @click="showAssign = false" class="btn-close">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Assign to Agent</label>
            <select v-model="assignForm.agentId" class="form-control">
              <option value="">Select agent</option>
              <option v-for="agent in availableAgents" :key="agent.id" :value="agent.id">
                {{ agent.name }}
              </option>
            </select>
          </div>
          <div class="modal-footer">
            <button @click="showAssign = false" class="btn btn-secondary">Cancel</button>
            <button @click="assignTicket" class="btn btn-primary">Assign</button>
          </div>
        </div>
      </div>
    </div>

    <!-- Resolve Modal -->
    <div v-if="showResolve" class="modal-overlay">
      <div class="modal">
        <div class="modal-header">
          <h2>Resolve Ticket</h2>
          <button @click="showResolve = false" class="btn-close">&times;</button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>Resolution Notes</label>
            <textarea v-model="resolveForm.notes" class="form-control" rows="4"></textarea>
          </div>
          <div class="modal-footer">
            <button @click="showResolve = false" class="btn btn-secondary">Cancel</button>
            <button @click="resolveTicket" class="btn btn-success">Resolve</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { pennyApi } from '@/api/pennyApi';

export default {
  name: 'EscalationTickets',
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
        { label: 'All', value: 'ALL' },
        { label: 'Pending', value: 'PENDING' },
        { label: 'Assigned', value: 'ASSIGNED' },
        { label: 'Resolved', value: 'RESOLVED' },
        { label: 'Cancelled', value: 'CANCELLED' }
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
        this.$toast.error('Failed to load tickets');
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
        this.$toast.error('Please select an agent');
        return;
      }
      
      try {
        await pennyApi.assignEscalationTicket(this.botId, this.selectedTicket.id, this.assignForm.agentId);
        
        // Update local state
        this.selectedTicket.status = 'ASSIGNED';
        this.selectedTicket.assignedAgentId = this.assignForm.agentId;
        
        this.$toast.success('Ticket assigned successfully');
        this.showAssign = false;
      } catch (error) {
        console.error('Error assigning ticket:', error);
        this.$toast.error('Failed to assign ticket');
      }
    },
    
    async resolveTicket() {
      try {
        await pennyApi.resolveEscalationTicket(this.botId, this.selectedTicket.id, this.resolveForm.notes);
        
        // Update local state
        this.selectedTicket.status = 'RESOLVED';
        this.selectedTicket.resolutionNotes = this.resolveForm.notes;
        this.selectedTicket.resolvedAt = new Date().toISOString();
        
        this.$toast.success('Ticket resolved successfully');
        this.showResolve = false;
        this.loadTickets();
      } catch (error) {
        console.error('Error resolving ticket:', error);
        this.$toast.error('Failed to resolve ticket');
      }
    },
    
    async cancelTicket(ticket) {
      if (!confirm('Are you sure you want to cancel this ticket?')) return;
      
      try {
        await pennyApi.cancelEscalationTicket(this.botId, ticket.id);
        
        ticket.status = 'CANCELLED';
        this.$toast.success('Ticket cancelled successfully');
      } catch (error) {
        console.error('Error cancelling ticket:', error);
        this.$toast.error('Failed to cancel ticket');
      }
    },
    
    statusClass(status) {
      switch (status) {
        case 'PENDING': return 'pending';
        case 'ASSIGNED': return 'assigned';
        case 'RESOLVED': return 'resolved';
        case 'CANCELLED': return 'cancelled';
        default: return '';
      }
    },
    
    priorityClass(priority) {
      switch (priority) {
        case 'LOW': return 'low';
        case 'NORMAL': return 'normal';
        case 'HIGH': return 'high';
        case 'URGENT': return 'urgent';
        default: return '';
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
.escalation-tickets {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
}

.filter-tabs {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.tab-btn {
  padding: 8px 16px;
  border: 1px solid #ddd;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.tab-btn.active {
  background: #007bff;
  color: white;
  border-color: #007bff;
}

.table-container {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #eee;
}

.data-table th {
  background: #f8f9fa;
  font-weight: 600;
}

.actions {
  display: flex;
  gap: 5px;
}

.status-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending {
  background: #fff3cd;
  color: #856404;
}

.status-badge.assigned {
  background: #cce5ff;
  color: #004085;
}

.status-badge.resolved {
  background: #d4edda;
  color: #155724;
}

.status-badge.cancelled {
  background: #f8d7da;
  color: #721c24;
}

.priority-badge {
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.priority-badge.low {
  background: #e2e3e5;
  color: #383d41;
}

.priority-badge.normal {
  background: #cce5ff;
  color: #004085;
}

.priority-badge.high {
  background: #ffeeba;
  color: #856404;
}

.priority-badge.urgent {
  background: #f8d7da;
  color: #721c24;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 8px;
  max-width: 500px;
  width: 90%;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h2 {
  margin: 0;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
}

.form-group {
  margin-bottom: 15px;
}

.form-group label {
  display: block;
  margin-bottom: 5px;
  font-weight: 500;
}

.form-control {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-primary {
  background: #007bff;
  color: white;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-success {
  background: #28a745;
  color: white;
}

.btn-danger {
  background: #dc3545;
  color: white;
}

.btn-sm {
  padding: 4px 8px;
  font-size: 12px;
}

.text-center {
  text-align: center;
  padding: 20px;
  color: #666;
}
</style>
