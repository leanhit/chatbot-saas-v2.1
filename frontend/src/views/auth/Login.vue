<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- Header -->
      <div class="text-center">
        <div class="mx-auto h-12 w-12 flex items-center justify-center rounded-full bg-blue-100 dark:bg-blue-900">
          <Icon icon="fa6-solid:user-lock" class="h-6 w-6 text-blue-600 dark:text-blue-300" />
        </div>
        <h2 class="mt-6 text-3xl font-extrabold text-gray-900 dark:text-white">
          {{ $t('auth.login.title') }}
        </h2>
        <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">
          {{ $t('auth.login.subtitle') }}
        </p>
      </div>
      <!-- Login Form -->
      <form class="mt-8 space-y-6" @submit.prevent="handleLogin">
        <div class="space-y-4">
          <!-- Email -->
          <div>
            <label for="email" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
              {{ $t('auth.login.email') }}
            </label>
            <div class="mt-1 relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Icon icon="fa6-solid:envelope" class="h-5 w-5 text-gray-400" />
              </div>
              <input
                id="email"
                v-model="form.email"
                name="email"
                type="email"
                autocomplete="email"
                required
                class="appearance-none relative block w-full pl-10 pr-3 py-2 border border-gray-300 dark:border-gray-600 placeholder-gray-500 dark:placeholder-gray-400 text-gray-900 dark:text-white bg-white dark:bg-gray-800 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                :class="{ 'border-red-500 dark:border-red-400': emptyFields && !form.email }"
                :placeholder="$t('auth.login.emailPlaceholder')"
              />
            </div>
          </div>
          <!-- Password -->
          <div>
            <label for="password" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
              {{ $t('auth.login.password') }}
            </label>
            <div class="mt-1 relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Icon icon="fa6-solid:lock" class="h-5 w-5 text-gray-400" />
              </div>
              <input
                id="password"
                v-model="form.password"
                name="password"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="current-password"
                required
                class="appearance-none relative block w-full pl-10 pr-10 py-2 border border-gray-300 dark:border-gray-600 placeholder-gray-500 dark:placeholder-gray-400 text-gray-900 dark:text-white bg-white dark:bg-gray-800 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                :class="{ 'border-red-500 dark:border-red-400': emptyFields && !form.password }"
                :placeholder="$t('auth.login.passwordPlaceholder')"
              />
              <div class="absolute inset-y-0 right-0 pr-3 flex items-center">
                <button
                  type="button"
                  @click="showPassword = !showPassword"
                  class="text-gray-400 hover:text-gray-500 focus:outline-none"
                >
                  <Icon :icon="showPassword ? 'fa6-solid:eye-slash' : 'fa6-solid:eye'" class="h-5 w-5" />
                </button>
              </div>
            </div>
          </div>
          <!-- Remember me -->
          <div class="flex items-center">
            <input
                id="remember-me"
                v-model="form.rememberMe"
                name="remember-me"
                type="checkbox"
                class="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 dark:border-gray-600 rounded"
              />
              <label for="remember-me" class="ml-2 block text-sm text-gray-900 dark:text-gray-300">
                {{ $t('auth.login.rememberMe') }}
              </label>
          </div>
        </div>
        <!-- Error Message -->
        <div v-if="authStore.error" class="rounded-md bg-red-50 dark:bg-red-900/20 p-4">
          <div class="flex">
            <div class="flex-shrink-0">
              <Icon icon="fa6-solid:exclamation-circle" class="h-5 w-5 text-red-400" />
            </div>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-red-800 dark:text-red-200">
                {{ $t('auth.login.error') }}
              </h3>
              <div class="mt-2 text-sm text-red-700 dark:text-red-300">
                {{ authStore.error }}
              </div>
            </div>
          </div>
        </div>
        <!-- Submit Button -->
        <div>
          <button
            type="submit"
            :disabled="authStore.isLoading"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span class="absolute left-0 inset-y-0 flex items-center pl-3" v-if="authStore.isLoading">
              <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
            </span>
            {{ authStore.isLoading ? $t('auth.login.signingIn') : $t('auth.login.signIn') }}
          </button>
        </div>
        <!-- Register Link -->
        <div class="text-center">
          <span class="text-sm text-gray-600 dark:text-gray-400">
            {{ $t('auth.login.noAccount') }}
          </span>
          <router-link to="/auth/register" class="ml-1 font-medium text-blue-600 hover:text-blue-500 dark:text-blue-400 dark:hover:text-blue-300">
            {{ $t('auth.login.signUp') }}
          </router-link>
        </div>
        
        <!-- Forgot Password Link -->
        <div class="text-center mt-2">
          <router-link to="/auth/forgot-password" class="font-medium text-blue-600 hover:text-blue-500 dark:text-blue-400 dark:hover:text-blue-300">
            {{ $t('auth.login.forgotPassword') }}
          </router-link>
        </div>
      </form>
    </div>
  </div>
</template>
<script>
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { Icon } from '@iconify/vue'
export default {
  name: 'Login',
  components: {
    Icon
  },
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()
    const form = reactive({
      email: '',
      password: '',
      rememberMe: authStore.rememberMe !== undefined ? authStore.rememberMe : true // Default checked nếu chưa có giá trị
    })
    const showPassword = ref(false)
    const emptyFields = ref(false)
    const isValidEmail = (email) => {
      const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      return regex.test(email)
    }
    
    // Watch để cập nhật form khi authStore.rememberMe thay đổi
    watch(() => authStore.rememberMe, (newValue) => {
      form.rememberMe = newValue !== undefined ? newValue : true
    })
    const handleLogin = async () => {
      // Reset error states
      emptyFields.value = false
      authStore.error = null
      // Validate form
      if (!form.email || !form.password) {
        emptyFields.value = true
        return
      }
      if (!isValidEmail(form.email)) {
        authStore.error = 'Please enter a valid email address'
        return
      }
      // Attempt login
      const result = await authStore.loginWithCredentials({
        email: form.email,
        password: form.password,
        rememberMe: form.rememberMe
      })
      // Navigation is handled by authStore.loginWithCredentials()
      // No need to manually navigate here
    }
    return {
      form,
      showPassword,
      emptyFields,
      authStore,
      handleLogin,
      isValidEmail
    }
  }
}
</script>
