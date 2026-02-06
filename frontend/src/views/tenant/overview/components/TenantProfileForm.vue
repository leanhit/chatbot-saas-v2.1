<script lang="ts" src="@/scripts/tenant/overview/components/tenantProfileForm.ts"></script>

<template>
  <el-card>
    <el-form label-width="120px">
      <!-- <el-form-item :label="t('Legal Name')">
        <el-input v-model="form.legalName" />
      </el-form-item>-->

      <el-form-item :label="t('Tax Code')">
        <el-input v-model="form.taxCode" />
      </el-form-item>

      <el-form-item :label="t('Email')">
        <el-input v-model="form.contactEmail" />
      </el-form-item>

      <el-form-item :label="t('Phone')">
        <el-input v-model="form.contactPhone" />
      </el-form-item>
    </el-form>

    <div class="card-footer">
      <el-button type="primary" @click="save">
        {{ t('Save Profile') }}
      </el-button>
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
