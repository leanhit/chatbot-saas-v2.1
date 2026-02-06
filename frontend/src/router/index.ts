// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth.js';
import ZoterDefault from '@/layouts/ZoterDefault.vue';
import AppShell from '@/layouts/modern/AppShell.vue';
import PublicLayout from '@/layouts/public/PublicLayout.vue';

const routes = [
    // Public Routes - No auth required
    {
        path: '/',
        component: PublicLayout,
        name: 'public-layout',
        meta: { requiresAuth: false },
        children: [
            {
                path: '',
                name: 'public-home',
                component: () => import('@/views/public/Home.vue'),
            },
            {
                path: 'features',
                name: 'public-features',
                component: () => import('@/views/public/Features.vue'),
            },
            {
                path: 'pricing',
                name: 'public-pricing',
                component: () => import('@/views/public/Pricing.vue'),
            },
            {
                path: 'guide',
                name: 'public-guide',
                component: () => import('@/views/public/Guide.vue'),
            },
            {
                path: 'careers',
                name: 'public-careers',
                component: () => import('@/views/public/Careers.vue'),
            }
        ]
    },
    {
        path: '/login',
        name: 'login',
        component: () => import('@/layouts/AuthLayout.vue'),
        children: [
            {
                path: '',
                name: 'login-page',
                component: () => import('@/views/Login.vue'),
            }
        ]
    },
    {
        path: '/register',
        name: 'register',
        component: () => import('@/layouts/AuthLayout.vue'),
        children: [
            {
                path: '',
                name: 'register-page',
                component: () => import('@/views/Register.vue'),
            }
        ]
    },
    {
        path: '/tenant-gateway',
        name: 'tenant-gateway',
        component: () => import('@/views/tenant/gateway/Index.vue'),
        meta: { requiresAuth: true },
    },
    {
        path: '/',
        component: ZoterDefault,
        name: 'LayoutZoter',
        redirect: '/modern/dashboard', // Changed from /home to modern dashboard
        meta: { 
            requiresAuth: true,
            deprecated: true // Mark entire layout as deprecated
        }, // Bảo vệ tất cả route con
        children: [
            {
                path: 'home',
                name: 'home',
                component: () => import('@/views/Home.vue'),
                meta: { 
                    title: 'Legacy Home - Deprecated',
                    deprecated: true,
                    requiresAuth: true 
                }
            },
            {
                path: 'help',
                name: 'help',
                component: () => import('@/views/help/Index.vue'),
            },
            {
                path: 'generate-embed-code',
                name: 'generate-embed-code',
                component: () => import('@/views/generate-embed-code/Index.vue'),
            },
            {
                path: 'profile',
                name: 'profile',
                component: () => import('@/views/profile/Index.vue'),
            },
            {
                path: 'takeover',
                name: 'takeover',
                component: () => import('@/views/takeover/Index.vue'),
            },
            // Image Manager
            {
                path: 'image-manager',
                name: 'image-manager',
                redirect: { name: 'images' },
                component: () => import('@/views/image-manager/ImageManagerLayout.vue'),
                children: [
                    {
                        path: 'images',
                        name: 'images',
                        component: () => import('@/views/image-manager/image/Index.vue'),
                        meta: { title: 'Image List' },
                    },
                    {
                        path: 'categories',
                        name: 'categories',
                        component: () => import('@/views/image-manager/category/Index.vue'),
                        meta: { title: 'Category List' },
                    },
                ],
            },
            // Bot Manager
            {
                path: 'bot-manager',
                name: 'bot-manager',
                redirect: { name: 'create-bot' }, // Sửa lại redirect đúng name
                component: () => import('@/views/bot-manager/BotManagerLayout.vue'),
                children: [
                    {
                        path: 'create-bot',
                        name: 'create-bot',
                        component: () => import('@/views/bot-manager/create-bot/Index.vue'),
                        meta: { title: 'Middleware Bot List' },
                    },
                    {
                        path: 'bot-botpress',
                        name: 'bot-botpress',
                        component: () => import('@/views/bot-manager/bot-botpress/Index.vue'),
                        meta: { title: 'Botpress Bot List' },
                    },
                ],
            },
            // Connection Manager
            {
                path: 'connection-manager',
                name: 'connection-manager',
                redirect: { name: 'auto-connect' },
                component: () => import('@/views/connection-manager/ConnectionManagerLayout.vue'),
                children: [
                    {
                        path: 'auto-connect',
                        name: 'auto-connect',
                        component: () => import('@/views/connection-manager/auto-connect/Index.vue'),
                        meta: { title: 'Auto Connect List' },
                    },
                    {
                        path: 'hand-connect',
                        name: 'hand-connect',
                        component: () => import('@/views/connection-manager/hand-connect/Index.vue'),
                        meta: { title: 'Hand Connect List' },
                    },
                ],
            },
            // Phone Review
            {
                path: 'phone-review',
                name: 'phone-review',
                redirect: { name: 'temp-user' },
                component: () => import('@/views/phone-review/PhoneReviewLayout.vue'),
                children: [
                    {
                        path: 'temp-user',
                        name: 'temp-user',
                        component: () => import('@/views/phone-review/temp-user/Index.vue'),
                        meta: { title: 'Temp User List' },
                    },
                    {
                        path: 'phone-captured',
                        name: 'phone-captured',
                        component: () => import('@/views/phone-review/phone-captured/Index.vue'),
                        meta: { title: 'Phone Captured List' },
                    },
                ],
            },
            // Tenant manager
            {
                path: 'tenant',
                name: 'tenant',
                redirect: { name: 'tenant' },
                component: () => import('@/views/tenant/TenantManagerLayout.vue'),
                children: [
                    {
                        path: 'overview',
                        name: 'overview',
                        component: () => import('@/views/tenant/overview/Index.vue'),
                        meta: { title: 'Overview' },
                    },
                    {
                        path: 'member',
                        name: 'member',
                        component: () => import('@/views/tenant/member/Index.vue'),
                        meta: { title: 'Member' },
                    },
                ],
            },
        ],
    },
    // Modern Layout Test Route (Independent)
    {
        path: '/modern',
        component: AppShell,
        name: 'modern-layout',
        meta: { requiresAuth: false }, // No auth required for testing
        children: [
            {
                path: '',
                name: 'modern-test',
                component: () => import('@/views/modern/TestView.vue'),
            },
            {
                path: 'dashboard',
                name: 'modern-dashboard',
                component: () => import('@/views/modern/dashboard/Index.vue'),
            }
        ]
    }
];

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes,
});

// Navigation Guard
router.beforeEach((to, from, next) => {
    const token = localStorage.getItem('access_token')
    const tenantId = localStorage.getItem('tenant_id')
    
    console.log('[ROUTER GUARD] Navigating to:', to.fullPath)
    console.log('[ROUTER GUARD] Token exists:', !!token)
    console.log('[ROUTER GUARD] Tenant ID exists:', !!tenantId)
    
    // Public routes - always accessible
    const publicRoutes = ['/', '/features', '/pricing', '/guide', '/careers']
    const isPublicRoute = publicRoutes.includes(to.path) || to.name === 'public-home' || to.name === 'public-features'
    
    if (isPublicRoute) {
        console.log('[ROUTER GUARD] Public route - allowing access')
        next()
        return
    }
    
    // Auth routes - redirect if already authenticated
    if (to.path === '/login' || to.path === '/register') {
        if (token && tenantId) {
            console.log('[ROUTER GUARD] Already authenticated - redirecting to dashboard')
            next('/modern/dashboard')
            return
        } else {
            console.log('[ROUTER GUARD] Auth route - allowing access')
            next()
            return
        }
    }
    
    // Modern routes - require both token and tenant
    if (to.path.startsWith('/modern')) {
        if (!token || !tenantId) {
            console.log('[ROUTER GUARD] Modern route - missing token or tenant, redirecting to login')
            next('/login')
            return
        }
    }
    
    // Protected routes - require authentication
    if (to.meta?.requiresAuth !== false) {
        if (!token) {
            console.log('[ROUTER GUARD] Not authenticated - redirecting to login')
            next('/login')
            return
        }
    }
    
    console.log('[ROUTER GUARD] All checks passed - allowing access')
    next()
})

export default router;