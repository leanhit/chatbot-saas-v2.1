<script lang="ts" src="@/scripts/tenant/overview/components/tenantAddressForm.ts"></script>

<template>
  <el-card v-loading="loading">
    <el-form label-width="120px" style="margin-top: 20px">
      <el-form-item :label="t('House Number')">
        <el-input v-model="form.houseNumber" />
      </el-form-item>

      <el-form-item :label="t('Street')">
        <el-input v-model="form.street" />
      </el-form-item>

      <el-form-item :label="t('Province')">
        <el-select
          v-model="form.provinceCode"
          filterable
          style="width: 100%"
        >
          <el-option
            v-for="p in provinces"
            :key="p.code"
            :label="p.name"
            :value="p.code"
          />
        </el-select>
      </el-form-item>

      <el-form-item :label="t('District')">
        <el-select
          v-model="form.districtCode"
          :disabled="!form.provinceCode"
          filterable
          style="width: 100%"
        >
          <el-option
            v-for="d in districtOptions"
            :key="d.code"
            :label="d.name"
            :value="d.code"
          />
        </el-select>
      </el-form-item>

      <el-form-item :label="t('Ward')">
        <el-select
          v-model="form.wardCode"
          :disabled="!form.districtCode"
          filterable
          style="width: 100%"
        >
          <el-option
            v-for="w in wardOptions"
            :key="w.code"
            :label="w.name"
            :value="w.code"
          />
        </el-select>
      </el-form-item>

      <div class="card-footer">
        <el-button type="primary" @click="save">
          {{ t('Save Address') }}
        </el-button>
      </div>
    </el-form>
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
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
  margin-top: 20px;
}

.info-icon {
  color: #909399;
  cursor: help;
}
</style>
