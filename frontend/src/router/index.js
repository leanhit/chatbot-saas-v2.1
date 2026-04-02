import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from '../stores/authStore';
import { useGatewayTenantStore } from '../stores/tenant/gateway/myTenantStore';
// Constants (giống frontend)
const TENANT_DATA = 'tenant_data'
const ACTIVE_TENANT_ID = 'active_tenant_id'  // ✅ Match store constant
// Default Pages
import Dashboard from "../views/Dashboard.vue";
// Auth Pages
import Login from "../views/auth/Login.vue";
import Register from "../views/auth/Register.vue";
import Profile from "../views/profile/Profile.vue";
import Tenant from "../views/tenant/gateway/Gateway.vue";
import Help from "../views/help/Help.vue";
import TenantOverview from "../views/tenant/overview/TenantOverview.vue";
import TenantMember from "../views/tenant/member/TenantMember.vue";
import TenantSettings from "../views/tenant/settings/TenantSettings.vue";
import BotManagement from "../views/penny/bots/BotManagement.vue";
import Connections from "../views/penny/connections/Connections.vue";
import Rules from "../views/penny/rules/Rules.vue";
import Messages from "../views/messages/chat/Chat.vue";
import CustomerData from "../views/customers/CustomerData.vue";
import ForgotPassword from "../views/auth/ForgotPassword.vue";
// Payment Pages (SimplePayment Only)
import PaymentDeposit from "../views/payment/Deposit.vue";
import PaymentHistory from "../views/payment/History.vue";
var appname = " - Windzo Dashboard Admin Template";
const routes = [
  // Root route - redirect to login
  {
    path: "/",
    redirect: "/login"
  },
  // Auth Routes (Outside main layout)
  {
    path: "/login",
    name: "login",
    component: Login,
    meta: { hideNav: true },
  },
  {
    path: "/register",
    name: "register", 
    component: Register,
    meta: { hideNav: true },
  },
  {
    path: "/auth/forgot-password",
    name: "forgot-password",
    component: ForgotPassword,
    meta: { hideNav: true },
  },
  {
    path: "/tenant-gateway",
    name: "tenant-gateway",
    component: Tenant,
    meta: { requiresAuth: true, hideNav: true },
  },
  // Standalone Profile Route
  {
    path: "/profile",
    name: "profile",
    component: Profile,
    meta: { requiresAuth: true, title: "Profile" + appname, skipTenantCheck: true },
  },
  {
    path: "/help",
    name: "help",
    component: Help,
    meta: { requiresAuth: true, title: "Help Center" + appname, skipTenantCheck: true },
  },
  
  {
    path: "/dashboard",
    name: "dasboard",
    component: Dashboard,
    meta: { requiresAuth: true, title: "Dashboard" + appname, skipTenantCheck: true },
  },
  //tenant  
  {
    path: "/tenant/overview",
    name: "tenant-overview",
    component: TenantOverview,
    meta: { requiresAuth: true, title: "Tenant Overview" + appname, skipTenantCheck: true },
  },
  {
    path: "/tenant/members",
    name: "tenant-members",
    component: TenantMember,
    meta: { requiresAuth: true, title: "Tenant Members" + appname, skipTenantCheck: true },
  },
  {
    path: "/tenant/settings",
    name: "tenant-settings",
    component: TenantSettings,
    meta: { requiresAuth: true, title: "Tenant Settings" + appname, skipTenantCheck: true },
  },
  {
    path: "/penny-bots",
    name: "penny-bots",
    component: BotManagement,
    meta: { requiresAuth: true, title: "Penny Bot Management" + appname, skipTenantCheck: true },
  },
  {
    path: "/penny-connections",
    name: "penny-connections",
    component: Connections,
    meta: { requiresAuth: true, title: "Penny Connections" + appname, skipTenantCheck: true },
  },
  {
    path: "/penny-rules",
    name: "penny-rules",
    component: Rules,
    meta: { requiresAuth: true, title: "Penny Rules" + appname, skipTenantCheck: true },
  },
  {
    path: "/messages",
    name: "messages",
    component: Messages,
    meta: { requiresAuth: true, title: "Messages" + appname, skipTenantCheck: true },
  },
  {
    path: "/customers",
    name: "customers",
    component: CustomerData,
    meta: { requiresAuth: true, title: "Customer Data" + appname, skipTenantCheck: true },
  },
  // Payment Routes (SimplePayment Only)
  {
    path: "/payment/deposit",
    name: "payment-deposit",
    component: PaymentDeposit,
    meta: { requiresAuth: true, title: "Payment Deposit" + appname, skipTenantCheck: true },
  },
  {
    path: "/payment/history",
    name: "payment-history",
    component: PaymentHistory,
    meta: { requiresAuth: true, title: "Payment History" + appname, skipTenantCheck: true },
  },
];
const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
  linkExactActiveClass: "exact-active",
});
// Navigation Guard (like frontend)
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();
  const tenantStore = useGatewayTenantStore();
  const token = authStore.token;
  // Check tenant from store or localStorage (updated for tenantKey)
  const storedTenantData = localStorage.getItem(TENANT_DATA);
  const activeTenantId = tenantStore.currentTenant?.tenantKey || (storedTenantData ? JSON.parse(storedTenantData).tenantKey : null);
  // If tenant in localStorage but not in store, load it (giống frontend)
  if (storedTenantData && !tenantStore.currentTenant) {
    const tenantData = JSON.parse(storedTenantData);
    tenantStore.currentTenant = tenantData;
  }
  // 1. If not logged in (giống frontend)
  if (!token) {
    if (to.meta.requiresAuth) {
      return next({ name: 'login', query: { redirect: to.fullPath } });
    }
    return next();
  }
  // 2. If logged in and trying to access login (giống frontend)
  if (to.name === 'login') {
    return activeTenantId ? next({ name: 'dashboard' }) : next({ name: 'tenant-gateway' });
  }
  // 3. If logged in but no tenant selected (and not on tenant gateway or routes that skip tenant check) (giống frontend)
  if (to.meta.requiresAuth && !activeTenantId && to.name !== 'tenant-gateway' && !to.meta.skipTenantCheck) {
    return next({ 
      name: 'tenant-gateway', 
      query: { redirect: to.fullPath } 
    });
  }
  next();
});
export default router;
// Debug: List all routes
router.getRoutes().forEach(route => {
});
