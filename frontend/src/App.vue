<template>
  <!-- App -->
  <div class="flex min-h-screen bg-gray-50 font-lexend dark:bg-gray-900">
    <div
      v-if="!$route.meta.hideNav"
      :class="sidebar ? 'block lg:block' : 'hidden lg:hidden'"
      class="lg:flex-auto w-sidebar bg-white dark:bg-gray-800 border-r-2 dark:border-gray-700 lg:z-0 z-20 overflow-y-auto lg:sticky top-0 h-screen fixed"
    >
        <perfect-scrollbar class="h-screen">
          <Sidebar
            v-if="!$route.meta.hideNav"
            @sidebarToggle="close"
          />
        </perfect-scrollbar>
    </div>
    <div
      class="flex-auto w-full min-h-screen flex flex-col transition-colors"
      id="body-scroll"
    >
      <Header
        v-if="!$route.meta.hideNav"
        :sidebar-open="sidebar"
        @sidebarToggle="toggle"
      />
      <div class="flex-grow">
        <router-view v-slot="{ Component }">
          <transition
            name="slide-up"
            mode="out-in"
          >
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
      <Footer v-if="!$route.meta.hideNav" />
    </div>
  </div>
  <!-- End app -->
</template>
<script>
  // Vue components
  import Sidebar from "@/components/Sidebar";
  import Header from "@/components/Header";
  import Footer from "@/components/Footer";
  // npm-js
  import { useAuthStore } from '@/stores/authStore';
  import { ACTIVE_TENANT_ID } from '@/utils/constant';
  export default {
    name: "App",
    data() {
      return {
        sidebarDark: false,
        sidebar: true, // Default to visible on desktop
      };
    },
    components: {
      Header,
      Footer,
      Sidebar
    },
    methods: {
      open() {
        this.sidebar = true;
      },
      close() {
        this.sidebar = false;
      },
      toggle() {
        this.sidebar = !this.sidebar;
      },
    },
  };
</script>
<style>
  /*
  Enter and leave animations can use different
  durations and timing functions.
*/
  .slide-up-enter-active {
    transition: all 0.3s ease-out;
  }
  .slide-up-leave-active {
    transition: all 0.8s cubic-bezier(1, 0.5, 0.8, 1);
  }
  .slide-up-enter-from,
  .slide-up-leave-to {
    transform: translateY(20px);
    opacity: 0;
  }
</style>
