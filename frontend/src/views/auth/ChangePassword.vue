<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- Header -->
      <div class="text-center">
        <div class="mx-auto h-12 w-12 flex items-center justify-center rounded-full bg-blue-100 dark:bg-blue-900">
          <Icon icon="fa6-solid:key" class="h-6 w-6 text-blue-600 dark:text-blue-300" />
        </div>
        <h2 class="mt-6 text-3xl font-extrabold text-gray-900 dark:text-white">
          {{ $t('auth.changePassword.title') }}
        </h2>
        <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">
          {{ $t('auth.changePassword.subtitle') }}
        </p>
      </div>
      
      <form class="mt-8 space-y-6" @submit.prevent="handleChangePassword">
        <div class="space-y-4">
          <!-- Current Password -->
          <div>
            <label for="currentPassword" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
              {{ $t('auth.changePassword.currentPassword') }}
            </label>
            <div class="mt-1 relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Icon icon="fa6-solid:lock" class="h-5 w-5 text-gray-400" />
              </div>
              <input
                id="currentPassword"
                v-model="form.currentPassword"
                name="currentPassword"
                type="password"
                autocomplete="current-password"
                required
                class="appearance-none relative block w-full pl-10 pr-3 py-2 border border-gray-300 dark:border-gray-600 placeholder-gray-500 dark:placeholder-gray-400 text-gray-900 dark:text-white bg-white dark:bg-gray-800 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                :class="{ 'border-red-500 dark:border-red-400': emptyFields && !form.currentPassword }"
                :placeholder="$t('auth.changePassword.currentPasswordPlaceholder')"
              />
            </div>
          </div>
          
          <!-- New Password -->
          <div>
            <label for="newPassword" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
              {{ $t('auth.changePassword.newPassword') }}
            </label>
            <div class="mt-1 relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Icon icon="fa6-solid:key" class="h-5 w-5 text-gray-400" />
              </div>
              <input
                id="newPassword"
                v-model="form.newPassword"
                name="newPassword"
                type="password"
                autocomplete="new-password"
                required
                class="appearance-none relative block w-full pl-10 pr-3 py-2 border border-gray-300 dark:border-gray-600 placeholder-gray-500 dark:placeholder-gray-400 text-gray-900 dark:text-white bg-white dark:bg-gray-800 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                :class="{ 'border-red-500 dark:border-red-400': emptyFields && !form.newPassword }"
                :placeholder="$t('auth.changePassword.newPasswordPlaceholder')"
              />
            </div>
          </div>
          
          <!-- Confirm Password -->
          <div>
            <label for="confirmPassword" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
              {{ $t('auth.changePassword.confirmPassword') }}
            </label>
            <div class="mt-1 relative">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <Icon icon="fa6-solid:lock" class="h-5 w-5 text-gray-400" />
              </div>
              <input
                id="confirmPassword"
                v-model="form.confirmPassword"
                name="confirmPassword"
                type="password"
                autocomplete="new-password"
                required
                class="appearance-none relative block w-full pl-10 pr-3 py-2 border border-gray-300 dark:border-gray-600 placeholder-gray-500 dark:placeholder-gray-400 text-gray-900 dark:text-white bg-white dark:bg-gray-800 rounded-md focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                :class="{ 'border-red-500 dark:border-red-400': emptyFields && !form.confirmPassword }"
                :placeholder="$t('auth.changePassword.confirmPasswordPlaceholder')"
              />
            </div>
          </div>
        </div>
        
        <!-- Error Message -->
        <div v-if="authStore.error" class="rounded-md bg-red-50 dark:bg-red-900/50 p-4">
          <div class="flex">
            <div class="flex-shrink-0">
              <Icon icon="fa6-solid:exclamation-triangle" class="h-5 w-5 text-red-400" />
            </div>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-red-800 dark:text-red-200">
                {{ $t('auth.changePassword.error') }}
              </h3>
              <div class="mt-2 text-sm text-red-700 dark:text-red-300">
                {{ authStore.error }}
              </div>
            </div>
          </div>
        </div>
        
        <!-- Success Message -->
        <div v-if="successMessage" class="rounded-md bg-green-50 dark:bg-green-900/50 p-4">
          <div class="flex">
            <div class="flex-shrink-0">
              <Icon icon="fa6-solid:check-circle" class="h-5 w-5 text-green-400" />
            </div>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-green-800 dark:text-green-200">
                {{ $t('auth.changePassword.success') }}
              </h3>
              <div class="mt-2 text-sm text-green-700 dark:text-green-300">
                {{ successMessage }}
              </div>
            </div>
          </div>
        </div>
        
        <!-- Submit Button -->
        <div>
          <button
            type="submit"
            :disabled="authStore.isLoading || passwordMismatch || !form.currentPassword || !form.newPassword || !form.confirmPassword"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span class="absolute left-0 inset-y-0 flex items-center pl-3" v-if="authStore.isLoading">
              <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
            </span>
            {{ authStore.isLoading ? $t('auth.changePassword.updating') : $t('auth.changePassword.updatePassword') }}
          </button>
        </div>
        
        <!-- Back to Profile -->
        <div class="text-center">
          <span class="text-sm text-gray-600 dark:text-gray-400">
            {{ $t('auth.changePassword.backToProfile') }}
          </span>
          <router-link to="/profile" class="ml-1 font-medium text-blue-600 hover:text-blue-500 dark:text-blue-400 dark:hover:text-blue-300">
            {{ $t('auth.changePassword.backToProfileLink') }}
          </router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { ref, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/authStore'
import { Icon } from '@iconify/vue'

export default {
  name: 'ChangePassword',
  components: {
    Icon
  },
  setup() {
    const router = useRouter()
    const authStore = useAuthStore()
    
    const form = reactive({
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    const successMessage = ref('')
    const passwordMismatch = computed(() => {
      return form.newPassword && form.confirmPassword && form.newPassword !== form.confirmPassword
    })

    const validateForm = () => {
      authStore.error = ''
      successMessage.value = ''

      // Check if passwords match
      if (form.newPassword !== form.confirmPassword) {
        authStore.error = 'Mật khẩu mới và xác nhận mật khẩu không khớp'
        return false
      }

      // Check password length
      if (form.newPassword.length < 6) {
        authStore.error = 'Mật khẩu mới phải có ít nhất 6 ký tự'
        return false
      }

      // Check if new password is same as current
      if (form.currentPassword === form.newPassword) {
        authStore.error = 'Mật khẩu mới phải khác mật khẩu hiện tại'
        return false
      }

      return true
    }

    const handleChangePassword = async () => {
      if (!validateForm()) {
        return
      }

      authStore.error = ''
      successMessage.value = ''

      try {
        await authStore.changePassword(
          form.currentPassword,
          form.newPassword,
          form.confirmPassword
        )
        
        successMessage.value = 'Đổi mật khẩu thành công! Bạn sẽ được chuyển về dashboard sau 3 giây.'
        
        // Clear form
        form.currentPassword = ''
        form.newPassword = ''
        form.confirmPassword = ''
        
        // Redirect to dashboard after 3 seconds
        setTimeout(() => {
          router.push('/dashboard')
        }, 3000)
        
      } catch (err) {
        console.error('Change password error:', err)
        
        if (err.response?.data?.message) {
          authStore.error = err.response.data.message
        } else if (err.response?.data?.error) {
          authStore.error = err.response.data.error
        } else if (err.message) {
          authStore.error = err.message
        } else {
          authStore.error = 'Đổi mật khẩu thất bại. Vui lòng thử lại.'
        }
      }
    }

    return {
      form,
      successMessage,
      passwordMismatch,
      authStore,
      handleChangePassword
    }
  }
}
</script>
