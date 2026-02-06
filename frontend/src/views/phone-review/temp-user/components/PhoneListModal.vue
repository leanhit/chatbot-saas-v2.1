<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="480px"
    :close-on-click-modal="false"
    destroy-on-close
  >
    <div class="phone-list-container">
      <el-table
        :data="phones"
        height="300"
        border
        size="small"
        style="width: 100%"
      >
        <el-table-column
          label="#"
          type="index"
          width="60"
          align="center"
        />
        <el-table-column
          label="Số điện thoại"
          align="left"
          >
          <template #default="{ row }">
            <span>{{ row.phone }}</span> 
          </template>
        </el-table-column>
      </el-table>
    </div>

    <template #footer>
      <el-button type="primary" @click="closeModal">Đóng</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { defineProps, defineEmits } from 'vue'

const props = defineProps({
  visible: Boolean,
  phones: {
    type: Array,
    default: () => []
  },
  title: {
    type: String,
    default: 'Danh sách số điện thoại'
  }
})

const emit = defineEmits(['update:visible'])

function closeModal() {
  emit('update:visible', false)
}
</script>

<style scoped>
.phone-list-container {
  max-height: 320px;
  overflow-y: auto;
}
</style>
