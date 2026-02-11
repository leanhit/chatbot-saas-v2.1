<template>
  <div class="tenant-manager-layout">
    <router-view></router-view>
  </div>
</template>
<script>
export default {
  name: "TenantManagerLayout",
};
</script>
<style scoped>
.tenant-manager-layout {
  height: 100%;
  width: 100%;
}
</style>
