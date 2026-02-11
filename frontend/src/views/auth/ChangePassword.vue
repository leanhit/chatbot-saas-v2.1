<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <div>
        <h2 class="mt-6 text-center text-3xl font-extrabold text-gray-900">
          Đổi Mật Khẩu
        </h2>
        <p class="mt-2 text-center text-sm text-gray-600">
          Cập nhật mật khẩu tài khoản của bạn
        </p>
      </div>
      
      <form class="mt-8 space-y-6" @submit.prevent="handleChangePassword">
        <div class="rounded-md shadow-sm -space-y-px">
          <div>
            <label for="currentPassword" class="sr-only">Mật khẩu hiện tại</label>
            <input
              id="currentPassword"
              name="currentPassword"
              type="password"
              autocomplete="current-password"
              required
              v-model="form.currentPassword"
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-t-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
              placeholder="Mật khẩu hiện tại"
            />
          </div>
          <div>
            <label for="newPassword" class="sr-only">Mật khẩu mới</label>
            <input
              id="newPassword"
              name="newPassword"
              type="password"
              autocomplete="new-password"
              required
              v-model="form.newPassword"
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
              placeholder="Mật khẩu mới"
            />
          </div>
          <div>
            <label for="confirmPassword" class="sr-only">Xác nhận mật khẩu mới</label>
            <input
              id="confirmPassword"
              name="confirmPassword"
              type="password"
              autocomplete="new-password"
              required
              v-model="form.confirmPassword"
              class="appearance-none rounded-none relative block w-full px-3 py-2 border border-gray-300 placeholder-gray-500 text-gray-900 rounded-b-md focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 focus:z-10 sm:text-sm"
              placeholder="Xác nhận mật khẩu mới"
            />
          </div>
        </div>

        <!-- Error Messages -->
        <div v-if="error" class="rounded-md bg-red-50 p-4">
          <div class="flex">
            <div class="flex-shrink-0">
              <svg class="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd" />
              </svg>
            </div>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-red-800">
                Lỗi
              </h3>
              <div class="mt-2 text-sm text-red-700">
                <p>{{ error }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Success Message -->
        <div v-if="success" class="rounded-md bg-green-50 p-4">
          <div class="flex">
            <div class="flex-shrink-0">
              <svg class="h-5 w-5 text-green-400" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd" />
              </svg>
            </div>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-green-800">
                Thành công
              </h3>
              <div class="mt-2 text-sm text-green-700">
                <p>{{ success }}</p>
              </div>
            </div>
          </div>
        </div>

        <div>
          <button
            type="submit"
            :disabled="loading"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span v-if="loading" class="flex items-center">
              <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Đang xử lý...
            </span>
            <span v-else>
              Đổi Mật Khẩu
            </span>
          </button>
        </div>

        <div class="text-center">
          <router-link 
            to="/dashboard" 
            class="font-medium text-indigo-600 hover:text-indigo-500"
          >
            Quay lại Dashboard
          </router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import authAPI from '@/api/auth'

export default {
  name: 'ChangePassword',
  setup() {
    const router = useRouter()
    
    const form = reactive({
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    const loading = ref(false)
    const error = ref('')
    const success = ref('')

    const validateForm = () => {
      error.value = ''
      success.value = ''

      // Check if passwords match
      if (form.newPassword !== form.confirmPassword) {
        error.value = 'Mật khẩu mới và xác nhận mật khẩu không khớp'
        return false
      }

      // Check password length
      if (form.newPassword.length < 6) {
        error.value = 'Mật khẩu mới phải có ít nhất 6 ký tự'
        return false
      }

      // Check if new password is same as current
      if (form.currentPassword === form.newPassword) {
        error.value = 'Mật khẩu mới phải khác mật khẩu hiện tại'
        return false
      }

      return true
    }

    const handleChangePassword = async () => {
      if (!validateForm()) {
        return
      }

      loading.value = true
      error.value = ''
      success.value = ''

      try {
        await authAPI.changePassword(
          form.currentPassword,
          form.newPassword,
          form.confirmPassword
        )
        
        success.value = 'Đổi mật khẩu thành công! Bạn sẽ được chuyển về dashboard sau 3 giây.'
        
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
          error.value = err.response.data.message
        } else if (err.response?.data?.error) {
          error.value = err.response.data.error
        } else if (err.message) {
          error.value = err.message
        } else {
          error.value = 'Đổi mật khẩu thất bại. Vui lòng thử lại.'
        }
      } finally {
        loading.value = false
      }
    }

    return {
      form,
      loading,
      error,
      success,
      handleChangePassword
    }
  }
}
</script>
