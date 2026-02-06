<script lang="ts" src="@/scripts/tenant/member/components/list.ts"></script>

<template>
  <div class="d-flex align-items-center justify-content-between mb-4">
    <div class="page-titles">
      <ol class="breadcrumb m-0">
        <li class="breadcrumb-item active">
          <a href="javascript:void(0)" class="fw-bold fs-5 text-primary">
            {{ viewSettings.title }}
          </a>
        </li>
      </ol>
    </div>
    <div class="header-actions d-flex flex-row">
    <el-button 
        type="success" 
        @click="openInviteModal" 
        class="d-flex align-items-center"
      >
        <el-icon class="me-1"><Plus /></el-icon>
        {{ t('Invite Member') }}
      </el-button>
      <el-button 
        type="primary" 
        @click="refreshDataFn()" 
        class="d-flex align-items-center"
      >
        <el-icon class="me-1"><Refresh /></el-icon>
        {{ t('Check') }}
      </el-button>
    </div>
  </div>

  <div class="tenant-gateway">
    <el-row :gutter="20">
  <el-col :span="24">
    <el-card shadow="never" class="content-tabs-card full-width-card">
          <el-tabs
            v-model="activeTab"
            class="custom-tenant-tabs"
            @tab-change="onTabChange"
          >
            <el-tab-pane :label="t('Active Members')" name="active-members">
              <div class="tab-content-wrapper pt-3" v-if="activeTab === 'active-members'">
                <ActiveMemberTab />
              </div>
            </el-tab-pane>
            
            <el-tab-pane :label="t('Pending Request')" name="pending-request">
              <div class="tab-content-wrapper pt-3" v-if="activeTab === 'pending-request'">
                <PendingMemberTab />
              </div>
            </el-tab-pane>
            
            <el-tab-pane :label="t('Pending Invitations')" name="pending-invitations">
              <div class="tab-content-wrapper pt-3" v-if="activeTab === 'pending-invitations'">
                <InviteMemberTab />
              </div>
            </el-tab-pane>            
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>

  <InviteMemberModal ref="inviteModalRef" />
</template>

<style scoped>
/* Tổng thể layout */
.tenant-gateway {
  background-color: transparent;
}

/* Card Sidebar */
.profile-sidebar-card {
  border-radius: 12px;
  border: 1px solid #ebeef5;
}

.profile-sidebar-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  align-items: center;
}

/* Custom Tabs */
.content-tabs-card {
  border-radius: 12px;
  border: 1px solid #ebeef5;
  min-height: 500px;
}

.custom-tenant-tabs :deep(.el-tabs__item) {
  font-size: 15px;
  font-weight: 500;
  height: 50px;
  line-height: 50px;
}

.custom-tenant-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #f0f2f5;
}

/* Icon Tooltip trong Tab */
.info-icon {
  font-size: 14px;
  color: #909399;
  transition: color 0.2s;
  cursor: help;
}

.info-icon:hover {
  color: #f56c6c; /* Chuyển đỏ nhẹ để gây chú ý vì dữ liệu chưa cập nhật */
}

/* Responsive tinh chỉnh */
@media (max-width: 768px) {
  .profile-sidebar-card {
    margin-bottom: 20px;
  }
}

.tab-content-wrapper {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(5px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>