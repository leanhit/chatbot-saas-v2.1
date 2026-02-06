<script lang="ts" src="@/scripts/tenant/member/components/inviteMemberTab.ts"></script>

<template>
  <div class="invitation-tab-container w-100">
    <el-table 
      :data="pendingInvitations" 
      style="width: 100%" 
      v-loading="loading"
      border
      :header-cell-style="{ background: '#f5f7fa' }"
    >
      <el-table-column prop="email" :label="t('Email')" min-width="200" />
      
      <el-table-column prop="role" :label="t('Role')" width="150" align="center">
        <template #default="{ row }">
          <el-tag size="small" effect="light">{{ row.role }}</el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="expiresAt" :label="t('Expires At')" width="180">
        <template #default="{ row }">
          <span class="small text-muted">{{ formatDate(row.expiresAt) }}</span>
        </template>
      </el-table-column>
      
      <el-table-column :label="t('Actions')" width="120" align="center" fixed="right">
        <template #default="{ row }">
          <el-popconfirm
            :title="t('Are you sure to revoke this invitation?')"
            @confirm="handleRevoke(row.id)"
            confirm-button-type="danger"
          >
            <template #reference>
              <el-button 
                type="danger" 
                :icon="Delete" 
                circle 
                size="small" 
                :title="t('Revoke')"
              />
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-empty 
      v-if="!pendingInvitations.length && !loading" 
      :description="t('No pending invitations')" 
    />
  </div>
</template>

<style scoped>
.invitation-tab-container {
  width: 100%;
}
</style>