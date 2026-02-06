<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-50 dark:bg-gray-900 py-12 px-4 sm:px-6 lg:px-8">
    <div class="max-w-md w-full space-y-8">
      <!-- Header -->
      <div class="text-center">
        <div class="mx-auto h-12 w-12 flex items-center justify-center rounded-full bg-yellow-100 dark:bg-yellow-900">
          <Icon icon="fa6-solid:key" class="h-6 w-6 text-yellow-600 dark:text-yellow-300" />
        </div>
        <h2 class="mt-6 text-3xl font-extrabold text-gray-900 dark:text-white">
          Forgot your password?
        </h2>
        <p class="mt-2 text-sm text-gray-600 dark:text-gray-400">
          Enter your email address and we'll send you a link to reset your password.
        </p>
      </div>

      <!-- Forgot Password Form -->
      <form class="mt-8 space-y-6" @submit.prevent="handleForgotPassword">
        <div class="space-y-4">
          <!-- Email -->
          <div>
            <label for="email" class="block text-sm font-medium text-gray-700 dark:text-gray-300">
              Email address
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
                class="appearance-none relative block w-full pl-10 pr-3 py-2 border border-gray-300 dark:border-gray-600 placeholder-gray-500 dark:placeholder-gray-400 text-gray-900 dark:text-white bg-white dark:bg-gray-800 rounded-md focus:outline-none focus:ring-yellow-500 focus:border-yellow-500 sm:text-sm"
                :class="{ 'border-red-500 dark:border-red-400': emptyFields && !form.email }"
                placeholder="Enter your email"
              />
            </div>
          </div>
        </div>

        <!-- Success Message -->
        <div v-if="successMessage" class="rounded-md bg-green-50 dark:bg-green-900/20 p-4">
          <div class="flex">
            <div class="flex-shrink-0">
              <Icon icon="fa6-solid:check-circle" class="h-5 w-5 text-green-400" />
            </div>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-green-800 dark:text-green-200">
                Success
              </h3>
              <div class="mt-2 text-sm text-green-700 dark:text-green-300">
                {{ successMessage }}
              </div>
            </div>
          </div>
        </div>

        <!-- Error Message -->
        <div v-if="errorMessage" class="rounded-md bg-red-50 dark:bg-red-900/20 p-4">
          <div class="flex">
            <div class="flex-shrink-0">
              <Icon icon="fa6-solid:exclamation-circle" class="h-5 w-5 text-red-400" />
            </div>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-red-800 dark:text-red-200">
                Error
              </h3>
              <div class="mt-2 text-sm text-red-700 dark:text-red-300">
                {{ errorMessage }}
              </div>
            </div>
          </div>
        </div>

        <!-- Submit Button -->
        <div>
          <button
            type="submit"
            :disabled="isLoading"
            class="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-yellow-600 hover:bg-yellow-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-yellow-500 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            <span class="absolute left-0 inset-y-0 flex items-center pl-3" v-if="isLoading">
              <svg class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
            </span>
            {{ isLoading ? 'Sending...' : 'Send reset link' }}
          </button>
        </div>

        <!-- Back to Login -->
        <div class="text-center">
          <span class="text-sm text-gray-600 dark:text-gray-400">
            Remember your password?
          </span>
          <router-link to="/login" class="ml-1 font-medium text-yellow-600 hover:text-yellow-500 dark:text-yellow-400 dark:hover:text-yellow-300">
            Back to login
          </router-link>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'

export default {
  name: 'ForgotPassword',
  components: {
    Icon
  },
  setup() {
    const router = useRouter()

    const form = reactive({
      email: ''
    })

    const isLoading = ref(false)
    const emptyFields = ref(false)
    const successMessage = ref('')
    const errorMessage = ref('')

    const isValidEmail = (email) => {
      const regex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
      return regex.test(email)
    }

    const handleForgotPassword = async () => {
      // Reset states
      emptyFields.value = false
      successMessage.value = ''
      errorMessage.value = ''

      // Validate form
      if (!form.email) {
        emptyFields.value = true
        return
      }

      if (!isValidEmail(form.email)) {
        errorMessage.value = 'Please enter a valid email address'
        return
      }

      isLoading.value = true

      try {
        // Simulate API call - replace with actual API call
        await new Promise(resolve => setTimeout(resolve, 2000))
        
        successMessage.value = 'Password reset link has been sent to your email address.'
        
        // Clear form
        form.email = ''
        
        // Redirect to login after 3 seconds
        setTimeout(() => {
          router.push('/login')
        }, 3000)
        
      } catch (error) {
        errorMessage.value = 'Failed to send reset link. Please try again.'
      } finally {
        isLoading.value = false
      }
    }

    return {
      form,
      isLoading,
      emptyFields,
      successMessage,
      errorMessage,
      handleForgotPassword,
      isValidEmail
    }
  }
}
</script>
