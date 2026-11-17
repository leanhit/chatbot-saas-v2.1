import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from '../stores/authStore';
import { useGatewayTenantStore } from '../stores/tenant/gateway/myTenantStore';
import coreRoutes from "./routes/core";
import tenantRoutes from "./routes/tenant";
import pennyRoutes from "./routes/penny";
import messagingRoutes from "./routes/messaging";
import routingRoutes from "./routes/routing";
import paymentRoutes from "./routes/payment";
import adminRoutes from "./routes/admin";
import analyticsRoutes from "./routes/analytics";
import trainingRoutes from "./routes/training";
import customerRoutes from "./routes/customer";

// Constants (giống frontend)
const TENANT_DATA = 'tenant_data'
const ACTIVE_TENANT_ID = 'active_tenant_id'  // ✅ Match store constant

const routes = [
  ...coreRoutes,
  ...tenantRoutes,
  ...pennyRoutes,
  ...messagingRoutes,
  ...routingRoutes,
  ...paymentRoutes,
  ...adminRoutes,
  ...analyticsRoutes,
  ...trainingRoutes,
  ...customerRoutes,
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
    const redirectTarget = to.query.redirect;
    if (redirectTarget && typeof redirectTarget === 'string' && redirectTarget.startsWith('/')) {
      if (redirectTarget.startsWith('/api/')) {
        const separator = redirectTarget.includes('?') ? '&' : '?';
        window.location.href = `${redirectTarget}${separator}authToken=${encodeURIComponent(token)}`;
        return;
      }
      return next(redirectTarget);
    }
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
  
  // 5. System admin routes (backend enforces hasRole('ADMIN') — this is UX guard only)
  if (to.meta.requiresSystemAdmin && !authStore.isAnyAdmin) {
    return next({ name: 'dashboard' });
  }
  
  next();
});
export default router;
// Debug: List all routes
router.getRoutes().forEach(route => {
});
