<script lang="ts" src="@/scripts/tenant/overview/components/tenantBasicForm.ts"></script>

<template>
  <el-card >
    <el-form label-width="120px">
      <el-form-item :label="t('Tenant Name')">
        <el-input v-model="form.name" />
      </el-form-item>

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item :label="t('Status')">
            <el-select v-model="form.status" style="width: 100%" disabled>
              <el-option :label="t('ACTIVE')" value="ACTIVE" />
              <el-option :label="t('SUSPENDED')" value="SUSPENDED" />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item :label="t('Expires At')">
            <el-date-picker
              v-model="form.expiresAt"
              type="date"
              format="DD/MM/YYYY"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="16">
        <el-col :span="24">
          <el-form-item :label="t('Visibility')">
            <el-radio-group v-model="form.visibility">
              <el-radio-button label="PUBLIC">{{ t('Public') }}</el-radio-button>
              <el-radio-button label="PRIVATE">{{ t('Private') }}</el-radio-button>
            </el-radio-group>
            <div class="el-form-item__description">
              {{ t('Public tenants are visible to everyone, private tenants are only visible to members.') }}
            </div>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <div class="card-footer">
      <el-button type="primary" @click="save">Save Tenant</el-button>
    </div>
  </el-card>
</template>


<style scoped>
.card-title {
  font-size: 16px;
  font-weight: 600;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}
</style>
