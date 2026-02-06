<script lang="ts" src="@/scripts/tenant/member/components/inviteMemberModal.ts"></script>

<template>
  <el-dialog
    v-model="visible"
    :title="t('Invite New Member')"
    width="450px"
    @closed="resetForm"
    append-to-body
  >
    <el-form :model="form" label-position="top" ref="formRef">
      <el-form-item 
        :label="t('Email Address')" 
        prop="email"
        :rules="[{ required: true, type: 'email', message: t('Please enter a valid email'), trigger: 'blur' }]"
      >
        <el-input 
          v-model="form.email" 
          placeholder="user@example.com"
          @keyup.enter="handleInvite"
        />
      </el-form-item>

      <el-form-item :label="t('Assign Role')" prop="role">
        <el-select v-model="form.role" class="w-100">
          <el-option :label="t('Admin')" value="ADMIN" />
          <el-option :label="t('Member')" value="MEMBER" />
          <el-option :label="t('Editor')" value="EDITOR" />
          <el-option :label="t('Viewer')" value="VIEWER" />
        </el-select>
      </el-form-item>

      <el-form-item :label="t('Invitation Expiry (Days)')" prop="expiryDays">
        <el-input-number v-model="form.expiryDays" :min="1" :max="30" class="w-100" />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="visible = false">{{ t('Cancel') }}</el-button>
        <el-button type="primary" :loading="loading" @click="handleInvite">
          {{ t('Send Invitation') }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style scoped>
.w-100 { width: 100%; }
</style>