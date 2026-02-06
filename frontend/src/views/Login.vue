<template>
  <form @submit.prevent="handleLogin" class="auth-form">
    <h2>Login</h2>
    
    <input v-model="form.email" type="email" placeholder="Email" required />
    <input v-model="form.password" type="password" placeholder="Password" required />
    
    <div v-if="error" class="error">{{ error }}</div>
    
    <button type="submit" :disabled="isLoading">
      {{ isLoading ? 'Logging in...' : 'Login' }}
    </button>
    
    <router-link to="/register">Need an account? Register</router-link>
  </form>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { useTenantStore } from '@/stores/tenant.store'
import api from '@/plugins/axios'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({ email: '', password: '' })
const isLoading = computed(() => authStore.isLoading)
const error = computed(() => authStore.error)

// Clear any existing token when entering Login page
onMounted(() => {
  console.log('[LOGIN PAGE] Clearing existing tokens to ensure clean login')
  authStore.clearTokens()
})

const handleLogin = async () => {
  try {
    await authStore.login(form.value)
    await authStore.loadCurrentUser()
    
    // Fetch and store default tenant
    const tenantRes = await api.get('/tenants/me')
    const tenantStore = useTenantStore()
    tenantStore.setTenant(tenantRes.data)
    
    // Load user's tenant list and auto-select if single tenant
    await tenantStore.loadUserTenants()
    
    router.push('/modern/dashboard')
  } catch (err) {
    console.error('Login failed:', err)
  }
}
</script>

<style scoped>
.auth-form { 
  background: white; 
  padding: 2rem; 
  border-radius: 8px; 
  box-shadow: 0 2px 10px rgba(0,0,0,0.1); 
  width: 100%; 
}

.auth-form input { 
  width: 100%; 
  padding: 0.75rem; 
  margin-bottom: 1rem; 
  border: 1px solid #ddd; 
  border-radius: 4px; 
  box-sizing: border-box;
}

.auth-form button { 
  width: 100%; 
  padding: 0.75rem; 
  background: #007bff; 
  color: white; 
  border: none; 
  border-radius: 4px; 
  cursor: pointer; 
}

.auth-form button:disabled { 
  background: #6c757d; 
}

.error { 
  color: #dc3545; 
  margin-bottom: 1rem; 
  padding: 0.75rem; 
  background: #f8d7da; 
  border-radius: 4px; 
}

.auth-form a { 
  display: block; 
  text-align: center; 
  margin-top: 1rem; 
  color: #007bff; 
  text-decoration: none; 
}

.auth-form a:hover {
  text-decoration: underline;
}
</style>
