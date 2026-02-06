<template>
  <form @submit.prevent="handleRegister" class="auth-form">
    <h2>Register</h2>
    
    <input v-model="form.name" type="text" placeholder="Full Name" required />
    <input v-model="form.email" type="email" placeholder="Email" required />
    <input v-model="form.password" type="password" placeholder="Password" required />
    <input v-model="form.confirmPassword" type="password" placeholder="Confirm Password" required />
    
    <div v-if="error" class="error">{{ error }}</div>
    
    <button type="submit" :disabled="isLoading || !isFormValid">
      {{ isLoading ? 'Registering...' : 'Register' }}
    </button>
    
    <router-link to="/login">Already have an account? Login</router-link>
  </form>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useTenantStore } from '@/stores/tenant.store'
import { useRouter } from 'vue-router'
import api from '@/plugins/axios'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({ name: '', email: '', password: '', confirmPassword: '' })
const isLoading = computed(() => authStore.isLoading)
const error = computed(() => authStore.error)

const isFormValid = computed(() => {
  return form.value.name && form.value.email && form.value.password && 
         form.value.password === form.value.confirmPassword
})

const handleRegister = async () => {
  try {
    // Step 1: Register the user
    await authStore.register(form.value)
    
    // Step 2: Auto-login with the same credentials
    await authStore.login({
      email: form.value.email,
      password: form.value.password
    })
    
    // Step 3: Load current user data
    await authStore.loadCurrentUser()
    
    // Step 4: Fetch and store default tenant (same as login flow)
    const tenantRes = await api.get('/tenants/me')
    const tenantStore = useTenantStore()
    tenantStore.setTenant(tenantRes.data)
    
    // Load user's tenant list and auto-select if single tenant
    await tenantStore.loadUserTenants()
    
    // Step 5: Redirect to dashboard
    router.push('/modern/dashboard')
  } catch (err) {
    console.error('Registration or auto-login failed:', err)
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
  background: #28a745; 
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
