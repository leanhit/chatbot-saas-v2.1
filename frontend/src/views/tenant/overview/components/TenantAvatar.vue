<script lang="ts" src="@/scripts/tenant/overview/components/tenantAvatar.ts"></script>

<template>
  <el-card class="avatar-card" v-loading="loading">
    <div class="avatar-container" v-if="tenant">
      <el-upload
        :show-file-list="false"
        :auto-upload="false"
        :before-upload="beforeUpload"
        :on-change="onChange"
        class="avatar-uploader"
      >
        <div class="avatar-wrapper">
          <el-avatar
            :size="120"
            :src="avatarUrl"
            class="avatar"
          >
            {{ fallback }}
          </el-avatar>

          <div class="avatar-mask">
            <el-icon><Camera /></el-icon>
            <span>Đổi ảnh</span>
          </div>
        </div>
      </el-upload>

      <div class="tenant-name">
        {{ tenant?.name }}
      </div>
    </div>
  </el-card>
</template>

<style scoped>
.avatar-card {
  height: 100%;
}

.avatar-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 20px 0;
}

.avatar-uploader {
  height: 120px;
}

.avatar-wrapper {
  position: relative;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
}

.avatar {
  background-color: #409eff;
  font-size: 40px;
  font-weight: bold;
}

.avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  font-size: 14px;
}

.avatar-wrapper:hover .avatar-mask {
  opacity: 1;
}

.tenant-name {
  font-weight: 600;
  font-size: 18px;
  color: #303133;
}
</style>