// Billing and Wallet Routes for Tenant Management
export const billingRoutes = [
  {
    path: '/tenant/billing',
    name: 'tenant-billing',
    component: () => import('@/views/tenant/Billing.vue'),
    meta: { 
      requiresAuth: true,
      title: 'Billing Dashboard',
      icon: 'ic:baseline-account-balance-wallet'
    }
  },
  {
    path: '/tenant/billing/subscriptions',
    name: 'tenant-subscriptions',
    component: () => import('@/views/tenant/Subscriptions.vue'),
    meta: { 
      requiresAuth: true,
      title: 'Subscriptions',
      icon: 'ic:baseline-subscriptions'
    }
  },
  {
    path: '/tenant/billing/wallet',
    name: 'tenant-wallet',
    component: () => import('@/views/tenant/Wallet.vue'),
    meta: { 
      requiresAuth: true,
      title: 'Wallet',
      icon: 'ic:baseline-account-balance-wallet'
    }
  },
  {
    path: '/tenant/billing/invoices',
    name: 'tenant-invoices',
    component: () => import('@/views/tenant/Invoices.vue'),
    meta: { 
      requiresAuth: true,
      title: 'Invoices',
      icon: 'ic:baseline-receipt-long'
    }
  },
  {
    path: '/tenant/billing/entitlements',
    name: 'tenant-entitlements',
    component: () => import('@/views/tenant/Entitlements.vue'),
    meta: { 
      requiresAuth: true,
      title: 'Feature Entitlements',
      icon: 'ic:baseline-apps'
    }
  },
  {
    path: '/tenant/billing/payment-methods',
    name: 'tenant-payment-methods',
    component: () => import('@/views/tenant/PaymentMethods.vue'),
    meta: { 
      requiresAuth: true,
      title: 'Payment Methods',
      icon: 'ic:baseline-credit-card'
    }
  }
]
