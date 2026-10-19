import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
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
app.use(PerfectScrollbar);
app.use(vClickOutside);
app.use(i18n);
app.use(ToastPlugin);
app.component('Icon', Icon);
// Initialize auth store
import { useAuthStore } from './stores/authStore'
import { useGatewayTenantStore } from './stores/tenant/gateway/myTenantStore'
import { usePaymentStore } from './stores/paymentStore'
import websocketService from './services/websocketService'
import { presenceWebSocketService } from './services/presenceWebSocketService'
const authStore = useAuthStore()
const tenantStore = useGatewayTenantStore()
const paymentStore = usePaymentStore()
authStore.initialize()
tenantStore.initialize()

// Connect WebSocket immediately on load if token is already present
if (authStore.token) {
  websocketService.connect(authStore.token)
}

// Initialize WebSocket when user is authenticated
authStore.$subscribe(async (mutation, state) => {
  if (state.token) {
    // User just logged in or is active, initialize WebSocket
    websocketService.connect(state.token)
    
    // Connect presence WebSocket when user is authenticated and tenant is selected
    const tenantKey = localStorage.getItem('active_tenant_id')
    if (tenantKey) {
      presenceWebSocketService.connect(tenantKey)
    }
  } else {
    // User logged out, disconnect WebSocket
    websocketService.disconnect()
    presenceWebSocketService.disconnect()
  }
})

// Connect presence WebSocket when tenant changes
tenantStore.$subscribe(async (mutation, state) => {
  if (state.currentTenant?.tenantKey && authStore.token) {
    presenceWebSocketService.connect(state.currentTenant.tenantKey)
  }
})
// tenantStore doesn't have initialize method
// Global properties (giống frontend)
app.config.globalProperties.$http = axios
app.config.globalProperties.axios = axios
app.provide('axios', axios)
app.mount("#app");
