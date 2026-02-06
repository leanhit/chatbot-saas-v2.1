<script lang="ts" src="@/scripts/tenant/gateway/components/userInvitationsTab.ts"></script>

<template>
  <div class="user-invitations-wrapper h-100">
    <el-card shadow="never" class="full-height-card" v-loading="loading">
      <div class="table-container">
        <el-empty v-if="!invitations.length && !loading" :description="t('No pending invitations')">
          <template #description>
            <p class="text-muted">{{ t('You have no invitations to join any organization') }}</p>
          </template>
        </el-empty>

        <el-table 
          v-else 
          :data="invitations" 
          border 
          style="width: 100%" 
          :header-cell-style="{ background: '#f5f7fa' }"
        >
          <el-table-column prop="name" :label="t('Tenant Name')" min-width="200">
            <template #default="{ row }">
              <span class="fw-bold">{{ row.name || t('Unknown Tenant') }}</span>
            </template>
          </el-table-column>

          <el-table-column prop="role" :label="t('Role')" width="150" align="center">
            <template #default="{ row }">
              <el-tag size="small" effect="light">{{ formatRole(row.role) }}</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="invitedByName" :label="t('Invited By')" min-width="180">
            <template #default="{ row }">
              <div class="d-flex flex-column">
                <span>{{ row.invitedByName }}</span>
                <small class="text-muted">{{ row.email }}</small>
              </div>
            </template>
          </el-table-column>

          <el-table-column :label="t('Actions')" width="180" align="center" fixed="right">
            <template #default="{ row }">
              <el-button-group>
                <el-button 
                  type="success" 
                  size="small" 
                  :loading="loadingActions[row.id]?.accepting"
                  @click="handleAccept(row.id, row.token)" 
                >
                  {{ t('Accept') }}
                </el-button>
                <el-button 
                  type="danger" 
                  size="small" 
                  :loading="loadingActions[row.id]?.rejecting"
                  @click="handleReject(row.id, row.token)"
                >
                  {{ t('Reject') }}
                </el-button>
              </el-button-group>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.user-invitations-wrapper {
  width: 100%;
  display: flex;
  flex-direction: column;
}

.full-height-card {
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  /* Đảm bảo card luôn chiếm hết chiều ngang */
  width: 100%; 
}

/* Quan trọng: Để card body và table co giãn đúng */
:deep(.el-card__body) {
  flex: 1;
  padding: 15px;
  width: 100%;
  box-sizing: border-box;
}

.table-container {
  width: 100%;
  overflow-x: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>