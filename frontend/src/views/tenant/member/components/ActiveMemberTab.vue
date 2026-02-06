<script lang="ts" src="@/scripts/tenant/member/components/activeMemberTab.ts"></script>

<template>
  <div class="active-members-wrapper w-100">
    <el-table 
      :data="members" 
      v-loading="loading" 
      style="width: 100%"
      border
      :header-cell-style="{ background: '#f5f7fa' }"
    >
      <el-table-column prop="email" :label="t('Email')" min-width="220" />

      <el-table-column :label="t('Role')" width="180" align="center">
        <template #default="{ row }">
          <template v-if="row.role === 'OWNER'">
            <el-tag type="warning" effect="dark" size="small">OWNER</el-tag>
          </template>
          <el-select
            v-else
            v-model="row.role"
            size="small"
            :disabled="!canChangeRole(row.role)"
            @change="(val) => handleRoleChange(row, val)"
            class="role-select"
          >
            <el-option :label="t('Admin')" value="ADMIN" />
            <el-option :label="t('Member')" value="MEMBER" />
            <el-option :label="t('Editor')" value="EDITOR" />
            <el-option :label="t('Viewer')" value="VIEWER" />
          </el-select>
        </template>
      </el-table-column>

      <el-table-column :label="t('Actions')" width="160" align="center" fixed="right">
        <template #default="{ row }">
          <div class="d-flex justify-content-center gap-2">
            <el-tooltip :content="t('View Details')" placement="top">
              <el-button
                size="small"
                type="primary"
                circle
                @click="viewDetails(row)"
              >
                <el-icon><View /></el-icon>
              </el-button>
            </el-tooltip>

            <el-tooltip :content="t('Remove Member')" placement="top" v-if="row.role !== 'OWNER'">
              <el-button
                type="danger"
                size="small"
                circle
                @click="handleRemove(row)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.active-members-wrapper {
  width: 100%;
}
.role-select {
  width: 100%;
}
.gap-2 {
  gap: 8px;
}
</style>