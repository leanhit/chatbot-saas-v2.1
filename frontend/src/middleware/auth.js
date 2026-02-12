import { useAuthStore } from '@/stores/authStore'
export const authGuard = (to, from, next) => {
  const authStore = useAuthStore()
  // Initialize auth store if not already done
  if (!authStore.token && !authStore.user) {
    authStore.initialize()
  }
  // Check if route requires authentication
  const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
  if (requiresAuth && !authStore.isLoggedIn) {
    // Redirect to login page with return url
    next({
      path: '/auth/login',
      query: { redirect: to.fullPath }
    })
  } else if (to.path === '/auth/login' && authStore.isLoggedIn) {
    // If already logged in and trying to access login, redirect to dashboard
    next('/dashboard')
  } else if (to.path === '/register' && authStore.isLoggedIn) {
    // If already logged in and trying to access register, redirect to dashboard
    next('/dashboard')
  } else {
    // Proceed to route
    next()
  }
}
export const guestGuard = (to, from, next) => {
  const authStore = useAuthStore()
  // Initialize auth store if not already done
  if (!authStore.token && !authStore.user) {
    authStore.initialize()
  }
  // Only allow access to guest routes if not logged in
  if (authStore.isLoggedIn) {
    next('/dashboard')
  } else {
    next()
  }
}
