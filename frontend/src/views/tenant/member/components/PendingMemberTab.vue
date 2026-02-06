<script lang="ts" src="@/scripts/tenant/member/components/pendingMemberTab.ts"></script>

<template>
  <el-table
    :data="pendingMembers"
    v-loading="store.loading"
    style="width: 100%"
  >
    <el-table-column prop="email" label="Email" />

    <el-table-column label="Requested Role" width="180">
      <template #default="{ row }">
        {{ row.role }}
      </template>
    </el-table-column>

    <el-table-column label="Actions" width="200">
      <template #default="{ row }">
        <el-button
          type="success"
          size="small"
          :loading="store.loading"
          @click="approve(row.id)"
        >
          Approve
        </el-button>

        <el-button
          type="danger"
          size="small"
          :loading="store.loading"
          @click="reject(row.id)"
        >
          Reject
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>
