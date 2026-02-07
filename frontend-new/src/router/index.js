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
// Component Pages
import Valert from "../views/components/alert.vue";
import Vaccrodion from "../views/components/accordion.vue";
import Vbadges from "../views/components/badges.vue";
import Vbreadcumb from "../views/components/breadcumbs.vue";
import Vbutton from "../views/components/button.vue";
import Vcard from "../views/components/card.vue";
import Vdropdown from "../views/components/dropdown.vue";
import ForgotPassword from "../views/auth/ForgotPassword.vue";

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
  // Simple tenant routes (like forgot-password)
  {
    path: "/tenant-overview",
    name: "tenant-overview",
    component: TenantOverview,
    meta: { hideNav: false, requiresAuth: false },
  },
  {
    path: "/tenant-member", 
    name: "tenant-member",
    component: TenantMember,
    meta: { hideNav: false, requiresAuth: false },
  },
  {
    path: "/test-tenant-member",
    name: "test-tenant-member", 
    component: TenantMember,
    meta: { hideNav: false, requiresAuth: false },
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
  // Main Layout with nested routes (like frontend)
  {
    path: "/",
    component: Dashboard, // Temporary layout - will create proper layout later
    name: "LayoutMain",
    redirect: "/dashboard",
    meta: { requiresAuth: true }, // Protect all child routes
    children: [
      {
        path: "dashboard",
        name: "dashboard",
        component: Dashboard,
        meta: { title: "Dashboard " + appname },
      },
      {
        path: "tables",
        name: "tables",
        component: Tables,
        meta: { title: "Tables" + appname },
      },
      // Component routes
      {
        path: "component/alert",
        name: "Valert",
        component: Valert,
        meta: { title: "Alert" + appname },
      },
      {
        path: "component/accordion",
        name: "Vaccordion",
        component: Vaccrodion,
        meta: { title: "Accordion" + appname },
      },
      {
        path: "component/badge",
        name: "Vbadge",
        component: Vbadges,
        meta: { title: "Badge" + appname },
      },
      {
        path: "component/breadcumb",
        name: "Vbreadcumb",
        component: Vbreadcumb,
        meta: { title: "Breadcumb" + appname },
      },
      {
        path: "component/button",
        name: "Vbutton",
        component: Vbutton,
        meta: { title: "Button" + appname },
      },
      {
        path: "component/card",
        name: "Vcard",
        component: Vcard,
        meta: { title: "Card" + appname },
      },
      {
        path: "component/dropdown",
        name: "Vdropdown",
        component: Vdropdown,
        meta: { title: "Dropdown" + appname },
      },
      // Other pages
      {
        path: "blank",
        name: "blank",
        component: Blank,
        meta: { title: "Blank Page" + appname },
      },
      {
        path: "test-tenant",
        name: "test-tenant",
        component: TenantOverview,
        meta: { title: "Test Tenant" + appname, requiresAuth: false },
      },
    ]
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
  console.log('=== ROUTER GUARD DEBUG ===');
  console.log('Navigating to:', to.path, 'Name:', to.name);
  console.log('From:', from.path);
  console.log('RequiresAuth:', to.meta.requiresAuth);
  console.log('SkipTenantCheck:', to.meta.skipTenantCheck);
  
  const authStore = useAuthStore();
  const tenantStore = useGatewayTenantStore();

  const token = authStore.token;
  console.log('Auth token:', !!token);
  
  // Check tenant from store or localStorage (updated for tenantKey)
  const storedTenantData = localStorage.getItem(TENANT_DATA);
  const activeTenantId = tenantStore.currentTenant?.id || (storedTenantData ? JSON.parse(storedTenantData).id : null);
  console.log('Active tenant ID:', activeTenantId);
  console.log('Stored tenant data:', storedTenantData ? 'exists' : 'none');
  
  // If tenant in localStorage but not in store, load it (giống frontend)
  if (storedTenantData && !tenantStore.currentTenant) {
    const tenantData = JSON.parse(storedTenantData);
    tenantStore.currentTenant = tenantData;
    console.log('Loaded tenant from localStorage');
  }

  // 1. If not logged in (giống frontend)
  if (!token) {
    console.log('No token - checking if auth required');
    if (to.meta.requiresAuth) {
      console.log('Auth required, redirecting to login');
      return next({ name: 'login', query: { redirect: to.fullPath } });
    }
    console.log('No auth required, proceeding');
    return next();
  }

  // 2. If logged in and trying to access login (giống frontend)
  if (to.name === 'login') {
    console.log('Accessing login page while logged in');
    return activeTenantId ? next({ name: 'dashboard' }) : next({ name: 'tenant-gateway' });
  }

  // 3. If logged in but no tenant selected (and not on tenant gateway or routes that skip tenant check) (giống frontend)
  if (to.meta.requiresAuth && !activeTenantId && to.name !== 'tenant-gateway' && !to.meta.skipTenantCheck) {
    console.log('No tenant selected, redirecting to tenant-gateway');
    return next({ 
      name: 'tenant-gateway', 
      query: { redirect: to.fullPath } 
    });
  }

  console.log('Navigation allowed');
  next();
});

export default router;

// Debug: List all routes
console.log('=== ALL ROUTES ===');
router.getRoutes().forEach(route => {
  console.log(`${route.path} -> ${route.name} (${route.component?.name || 'Unknown'})`);
});
console.log('=== END ROUTES ===');
