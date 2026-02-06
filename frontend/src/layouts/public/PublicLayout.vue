<template>
  <div class="public-layout">
    <!-- Public Navbar -->
    <PublicNavbar />

    <!-- Main Content -->
    <main class="public-main">
      <RouterView />
    </main>

    <!-- Public Footer -->
    <PublicFooter />
  </div>
</template>

<script setup lang="ts">
import PublicNavbar from './components/PublicNavbar.vue'
import PublicFooter from './components/PublicFooter.vue'
</script>

<style scoped>
.public-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

.public-main {
  flex: 1;
  width: 100%;
  padding-top: 72px; /* Account for fixed navbar height */
}
</style>
