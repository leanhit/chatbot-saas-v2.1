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
import TenantOverview from "../views/tenant/overview/TenantOverview.vue";
import TenantMember from "../views/tenant/member/TenantMember.vue";
import TenantSettings from "../views/tenant/settings/TenantSettings.vue";
// App Hub Pages
import AppHubDashboard from "../views/app-hub/Dashboard.vue";
import AppRegistry from "../views/app-hub/AppRegistry.vue";
import CreateApp from "../views/app-hub/CreateApp.vue";
// Billing Pages
import BillingDashboard from "../views/billing/Dashboard.vue";
import BillingSubscriptions from "../views/billing/Subscriptions.vue";
import BillingInvoices from "../views/billing/Invoices.vue";
import BillingEntitlements from "../views/billing/Entitlements.vue";
import BillingPaymentMethods from "../views/billing/PaymentMethods.vue";
// Wallet Pages
import WalletDashboard from "../views/wallet/Dashboard.vue";
import WalletTransactions from "../views/wallet/Transactions.vue";
// Penny Bot Pages
import PennyDashboard from "../views/penny/Dashboard.vue";
import PennyChat from "../views/penny/Chat.vue";
// Component Pages
import Valert from "../views/components/alert.vue";
import Vaccrodion from "../views/components/accordion.vue";
import Vbadges from "../views/components/badges.vue";
import Vbreadcumb from "../views/components/breadcumbs.vue";
import Vbutton from "../views/components/button.vue";
import Vcard from "../views/components/card.vue";
import Vdropdown from "../views/components/dropdown.vue";
import ForgotPassword from "../views/auth/ForgotPassword.vue";
import ChangePassword from "../views/auth/ChangePassword.vue";
// layouts
import Blank from "../views/layouts/Blank.vue";
// error page
import Page404 from "../views/layouts/error/404.vue";
import Page500 from "../views/layouts/error/500.vue";
import PageMaintenance from "../views/layouts/error/maintenance.vue";
import Tables from "../views/tables.vue";
var appname = " - Windzo Dashboard Admin Template";
const routes = [
  // Auth Routes (Outside main layout)
  {
    path: "/auth/login",
    name: "login",
    component: Login,
    meta: { hideNav: true },
  },
  {
    path: "/auth/register",
    name: "register", 
    component: Register,
    meta: { hideNav: true },
  },
  {
    path: "/auth/change-password",
    name: "change-password",
    component: ChangePassword,
    meta: { requiresAuth: true, hideNav: true },
  },
  {
    path: "/auth/forgot-password",
    name: "forgot-password",
    component: ForgotPassword,
    meta: { hideNav: true },
  },
  {
    path: "/tenant/gateway",
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
    path: "/dashboard",
    name: "dasboard",
    component: Dashboard,
    meta: { requiresAuth: true, title: "Dashboard" + appname, skipTenantCheck: true },
  },
  // Billing Routes
  {
    path: "/billing",
    name: "billing-overview",
    component: BillingDashboard,
    meta: { requiresAuth: true, title: "Billing Dashboard" + appname, skipTenantCheck: true },
  },
  {
    path: "/billing/subscriptions",
    name: "billing-subscriptions",
    component: BillingSubscriptions,
    meta: { requiresAuth: true, title: "Subscriptions" + appname, skipTenantCheck: true },
  },
  {
    path: "/billing/invoices",
    name: "billing-invoices",
    component: BillingInvoices,
    meta: { requiresAuth: true, title: "Invoices" + appname, skipTenantCheck: true },
  },
  {
    path: "/billing/entitlements",
    name: "billing-entitlements",
    component: BillingEntitlements,
    meta: { requiresAuth: true, title: "Usage & Entitlements" + appname, skipTenantCheck: true },
  },
  {
    path: "/billing/payment-methods",
    name: "billing-payment-methods",
    component: BillingPaymentMethods,
    meta: { requiresAuth: true, title: "Payment Methods" + appname, skipTenantCheck: true },
  },
  // Wallet Routes
  {
    path: "/wallet",
    name: "wallet-overview",
    component: WalletDashboard,
    meta: { requiresAuth: true, title: "Wallet Dashboard" + appname, skipTenantCheck: true },
  },
  {
    path: "/wallet/transactions",
    name: "wallet-transactions",
    component: WalletTransactions,
    meta: { requiresAuth: true, title: "Transactions" + appname, skipTenantCheck: true },
  },
  // Penny Bot Routes
  {
    path: "/penny",
    name: "penny-dashboard",
    component: PennyDashboard,
    meta: { requiresAuth: true, title: "Penny Bot Dashboard" + appname, skipTenantCheck: true },
  },
  {
    path: "/penny/chat",
    name: "penny-chat",
    component: PennyChat,
    meta: { requiresAuth: true, title: "Chat Interface" + appname, skipTenantCheck: true },
  },
  {
    path: "/penny/bots/create",
    name: "penny-create-bot",
    component: () => import("@/components/penny/CreateBotModal.vue"),
    meta: { requiresAuth: true, title: "Create Bot" + appname, skipTenantCheck: true },
  },
  {
    path: "/penny/analytics",
    name: "penny-analytics",
    component: () => import("@/components/penny/ChatHistoryModal.vue"),
    meta: { requiresAuth: true, title: "Analytics" + appname, skipTenantCheck: true },
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
  // App Hub Routes
  {
    path: "/app-hub",
    name: "app-hub-dashboard",
    component: AppHubDashboard,
    meta: { requiresAuth: true, title: "App Hub Dashboard" + appname, skipTenantCheck: true },
  },
  {
    path: "/app-hub/apps",
    name: "app-registry",
    component: AppRegistry,
    meta: { requiresAuth: true, title: "App Registry" + appname, skipTenantCheck: true },
  },
  {
    path: "/app-hub/apps/create",
    name: "create-app",
    component: CreateApp,
    meta: { requiresAuth: true, title: "Create App" + appname, skipTenantCheck: true },
  },
  // Error pages (No auth required)
  {
    path: "/:pathMatch(.*)*",
    name: "Page404",
    component: Page404,
    meta: { title: "Upps! 404" + appname, hideNav: true },
  },
  {
    path: "/500",
    name: "Page500",
    component: Page500,
    meta: { title: "Server internal Error" + appname, hideNav: true },
  },
  {
    path: "/maintenance",
    name: "maintenance",
    component: PageMaintenance,
    meta: {
      title: "Sorry the app has been Maintenance" + appname,
      hideNav: true,
    },
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
  
  // 1. Chưa có tài khoản => redirect to login
  if (!token) {
    if (to.meta.requiresAuth) {
      return next({ name: 'login', query: { redirect: to.fullPath } });
    }
    return next();
  }
  
  // 2. Đã đăng ký ok => trang login
  if (to.name === 'register') {
    return next({ name: 'login' });
  }
  
  // 3. Login ok chưa có tenant phải redirect tenant gateway
  if (to.name === 'login') {
    return activeTenantId ? next({ name: 'dashboard' }) : next({ name: 'tenant-gateway' });
  }
  
  // 4. Khi vào trang ứng dụng phải có đủ user và tenant
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
