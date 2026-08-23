import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from '../stores/authStore';
import { useGatewayTenantStore } from '../stores/tenant/gateway/myTenantStore';
// Constants (giống frontend)
const TENANT_DATA = 'tenant_data'
const ACTIVE_TENANT_ID = 'active_tenant_id'  // ✅ Match store constant
// Default Pages
const Dashboard = () => import("../views/Dashboard.vue");
// Auth Pages
const Login = () => import("../views/auth/Login.vue");
const Register = () => import("../views/auth/Register.vue");
const Profile = () => import("../views/profile/Profile.vue");
const Tenant = () => import("../views/tenant/gateway/Gateway.vue");
const Help = () => import("../views/help/Help.vue");
const TenantOverview = () => import("../views/tenant/overview/TenantOverview.vue");
const TenantMember = () => import("../views/tenant/member/TenantMember.vue");
const TenantSettings = () => import("../views/tenant/settings/TenantSettings.vue");
const BotManagement = () => import("../views/penny/bots/BotManagement.vue");
const Connections = () => import("../views/penny/connections/Connections.vue");
const Rules = () => import("../views/penny/rules/Rules.vue");
const KnowledgeBaseList = () => import("../views/penny/knowledge-base/KnowledgeBaseList.vue");
const PennyMetrics = () => import("../views/penny/metrics/PennyMetrics.vue");
const PennyMonitoringDashboard = () => import("../views/penny/metrics/PennyMonitoringDashboard.vue");
const PennyAnalyticsDashboard = () => import("../views/penny/metrics/PennyAnalyticsDashboard.vue");
const EscalationTickets = () => import("../views/penny/escalation/EscalationTickets.vue");
const PennyBotConfig = () => import("../views/penny/config/PennyBotConfig.vue");
const Messages = () => import("../views/messages/chat/Chat.vue");
const CustomerData = () => import("../views/customers/CustomerData.vue");
const ForgotPassword = () => import("../views/auth/ForgotPassword.vue");
// Payment Pages (SimplePayment Only)
const PaymentDeposit = () => import("../views/payment/Deposit.vue");
const PaymentHistory = () => import("../views/payment/History.vue");
// Admin Pages
const BankAccountManagement = () => import("../views/admin/BankAccountManagement.vue");
const PackageManagement = () => import("../views/admin/PackageManagement.vue");
const DiscountManagement = () => import("../views/admin/DiscountManagement.vue");
const PaymentAnalytics = () => import("../views/admin/PaymentAnalytics.vue");
const WebhookManagement = () => import("../views/admin/WebhookManagement.vue");
const UserManagement = () => import("../views/admin/UserManagement.vue");
// Conversation Routing & Agent Management Pages
const AgentManagement = () => import("../views/admin/AgentManagement.vue");
const SkillsManagement = () => import("../views/admin/SkillsManagement.vue");
const RoutingRules = () => import("../views/admin/RoutingRules.vue");
const SLAMonitoring = () => import("../views/admin/SLAMonitoring.vue");
const EscalationTiers = () => import("../views/admin/EscalationTiers.vue");
const Notifications = () => import("../views/notification/Notifications.vue");
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
    path: "/notifications",
    name: "notifications",
    component: Notifications,
    meta: { requiresAuth: true, title: "Notification Center" + appname, skipTenantCheck: true },
  },
  
  {
    path: "/dashboard",
    name: "dashboard",
    component: Dashboard,
    meta: { requiresAuth: true, title: "Dashboard" + appname },
  },
  //tenant  
  {
    path: "/tenant/overview",
    name: "tenant-overview",
    component: TenantOverview,
    meta: { requiresAuth: true, title: "Tenant Overview" + appname },
  },
  {
    path: "/tenant/members",
    name: "tenant-members",
    component: TenantMember,
    meta: { requiresAuth: true, title: "Tenant Members" + appname },
  },
  {
    path: "/tenant/settings",
    name: "tenant-settings",
    component: TenantSettings,
    meta: { requiresAuth: true, title: "Tenant Settings" + appname },
  },
  {
    path: "/penny-bots",
    name: "penny-bots",
    component: BotManagement,
    meta: { requiresAuth: true, title: "Penny Bot Management" + appname },
  },
  {
    path: "/penny-connections",
    name: "penny-connections",
    component: Connections,
    meta: { requiresAuth: true, title: "Penny Connections" + appname },
  },
  {
    path: "/penny-rules",
    name: "penny-rules",
    component: Rules,
    meta: { requiresAuth: true, title: "Penny Rules" + appname },
  },
  {
    path: "/penny/bots/:botId/knowledge-base",
    name: "penny-knowledge-base",
    component: KnowledgeBaseList,
    meta: { requiresAuth: true, title: "Knowledge Base" + appname },
  },
  {
    path: "/penny/metrics",
    name: "penny-metrics",
    component: PennyMetrics,
    meta: { requiresAuth: true, title: "Penny Metrics" + appname },
  },
  {
    path: "/penny/monitoring",
    name: "penny-monitoring",
    component: PennyMonitoringDashboard,
    meta: { requiresAuth: true, title: "Penny Monitoring Dashboard" + appname },
  },
  {
    path: "/penny/analytics",
    name: "penny-analytics",
    component: PennyAnalyticsDashboard,
    meta: { requiresAuth: true, title: "Penny Analytics Dashboard" + appname },
  },
  {
    path: "/penny/bots/:botId/escalation",
    name: "penny-escalation",
    component: EscalationTickets,
    meta: { requiresAuth: true, title: "Escalation Tickets" + appname },
  },
  {
    path: "/penny/bots/:botId/config",
    name: "penny-bot-config",
    component: PennyBotConfig,
    meta: { requiresAuth: true, title: "Bot Configuration" + appname },
  },
  {
    path: "/messages",
    name: "messages",
    component: Messages,
    meta: { requiresAuth: true, title: "Messages" + appname },
  },
  {
    path: "/customers",
    name: "customers",
    component: CustomerData,
    meta: { requiresAuth: true, title: "Customer Data" + appname },
  },
  // Payment Routes (SimplePayment Only)
  {
    path: "/payment/deposit",
    name: "payment-deposit",
    component: PaymentDeposit,
    meta: { requiresAuth: true, requiresOwner: true, title: "Payment Deposit" + appname },
  },
  {
    path: "/payment/history",
    name: "payment-history",
    component: PaymentHistory,
    meta: { requiresAuth: true, requiresOwner: true, title: "Payment History" + appname },
  },
  // Admin Routes
  {
    path: "/admin/bank-account",
    name: "admin-bank-account",
    component: BankAccountManagement,
    meta: { requiresAuth: true, title: "Bank Account Management" + appname },
  },
  {
    path: "/admin/packages",
    name: "admin-packages",
    component: PackageManagement,
    meta: { requiresAuth: true, title: "Package Management" + appname },
  },
  {
    path: "/admin/discounts",
    name: "admin-discounts",
    component: DiscountManagement,
    meta: { requiresAuth: true, title: "Discount Management" + appname },
  },
  {
    path: "/admin/analytics",
    name: "admin-analytics",
    component: PaymentAnalytics,
    meta: { requiresAuth: true, title: "Payment Analytics" + appname },
  },
  {
    path: "/admin/webhooks",
    name: "admin-webhooks",
    component: WebhookManagement,
    meta: { requiresAuth: true, title: "Webhook Management" + appname },
  },
  {
    path: "/admin/users",
    name: "admin-users",
    component: UserManagement,
    meta: { requiresAuth: true, title: "User Management" + appname },
  },
  // Conversation Routing & Agent Management Routes
  {
    path: "/admin/agents",
    name: "admin-agents",
    component: AgentManagement,
    meta: { requiresAuth: true, title: "Agent Management" + appname },
  },
  {
    path: "/admin/skills",
    name: "admin-skills",
    component: SkillsManagement,
    meta: { requiresAuth: true, title: "Skills Management" + appname },
  },
  {
    path: "/admin/routing-rules",
    name: "admin-routing-rules",
    component: RoutingRules,
    meta: { requiresAuth: true, title: "Routing Rules" + appname },
  },
  {
    path: "/admin/sla-monitoring",
    name: "admin-sla-monitoring",
    component: SLAMonitoring,
    meta: { requiresAuth: true, title: "SLA Monitoring" + appname },
  },
  {
    path: "/admin/escalation-tiers",
    name: "admin-escalation-tiers",
    component: EscalationTiers,
    meta: { requiresAuth: true, title: "Escalation Tiers" + appname },
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
  
  // 4. Role-based access control
  if (to.meta.requiresOwner && tenantStore.currentTenant?.role !== 'OWNER') {
    return next({ name: 'dashboard' }); // redirect to dashboard if not owner
  }
  
  next();
});
export default router;
// Debug: List all routes
router.getRoutes().forEach(route => {
});
