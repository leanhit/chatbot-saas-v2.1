import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import VueApexCharts from "vue3-apexcharts";
import PerfectScrollbar from "vue3-perfect-scrollbar";
import "vue3-perfect-scrollbar/dist/vue3-perfect-scrollbar.css";
import { createPinia } from "pinia";
// import "flowbite";
import "./assets/tailwind.css";
import "./assets/animate.css";
import "./assets/sass/css/windzo.css";
import vClickOutside from "click-outside-vue3";
import i18n from './locales';
import axios from './plugins/axios';
import ToastPlugin from './plugins/toast';
import { Icon } from '@iconify/vue';
const app = createApp(App);
app.use(router);
app.use(createPinia());
app.use(VueApexCharts);
app.use(PerfectScrollbar);
app.use(vClickOutside);
app.use(i18n);
app.use(ToastPlugin);
app.component('Icon', Icon);
// Initialize auth store
import { useAuthStore } from './stores/authStore'
import { useGatewayTenantStore } from './stores/tenant/gateway/myTenantStore'
import { useWalletStore } from './stores/walletStore'
import { useBillingStore } from './stores/billingStore'
import { usePaymentStore } from './stores/paymentStore'
import websocketService from './services/websocketService'
const authStore = useAuthStore()
const tenantStore = useGatewayTenantStore()
const walletStore = useWalletStore()
const billingStore = useBillingStore()
const paymentStore = usePaymentStore()
authStore.initialize()

// Initialize billing store when user is authenticated
authStore.$subscribe(async (state, prevState) => {
  if (state.isAuthenticated && !prevState.isAuthenticated) {
    // User just logged in, initialize billing data
    try {
      await billingStore.fetchSubscription()
      console.log('✅ [Main] Billing store initialized')
    } catch (error) {
      console.error('❌ [Main] Failed to initialize billing store:', error)
    }
    
    // Initialize WebSocket
    const token = localStorage.getItem('accessToken')
    if (token) {
      websocketService.connect(token)
    }
  } else if (!state.isAuthenticated && prevState.isAuthenticated) {
    // User just logged out, disconnect WebSocket
    websocketService.disconnect()
  }
})
// tenantStore doesn't have initialize method
// Global properties (giống frontend)
app.config.globalProperties.$http = axios
app.config.globalProperties.axios = axios
app.provide('axios', axios)
app.mount("#app");
