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

const app = createApp(App);
app.use(router);
app.use(createPinia());
app.use(VueApexCharts);
app.use(PerfectScrollbar);
app.use(vClickOutside);
app.use(i18n);
app.use(ToastPlugin);

// Initialize auth store
import { useAuthStore } from './stores/authStore'
import { useGatewayTenantStore } from './stores/tenant/gateway/myTenantStore'
const authStore = useAuthStore()
const tenantStore = useGatewayTenantStore()
authStore.initialize()
// tenantStore doesn't have initialize method

// Global properties (giống frontend)
app.config.globalProperties.$http = axios
app.config.globalProperties.axios = axios
app.provide('axios', axios)

app.mount("#app");
