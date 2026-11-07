<template>
  <div class="max-w-2xl mx-auto p-6">
    <h1 class="text-2xl font-bold text-gray-900 dark:text-white mb-6">Two-Factor Authentication</h1>

    <div class="bg-white dark:bg-gray-800 rounded-lg shadow p-6">
      <div class="flex items-center justify-between mb-4">
        <div>
          <h2 class="text-lg font-semibold text-gray-900 dark:text-white">TOTP Authenticator</h2>
          <p class="text-sm text-gray-500 dark:text-gray-400">
            Use an authenticator app (Google Authenticator, Authy, 1Password...)
          </p>
        </div>
        <span
          class="px-3 py-1 rounded-full text-sm font-medium"
          :class="status.enabled ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' : 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200'"
        >
          {{ status.enabled ? 'Enabled' : status.pendingSetup ? 'Setup pending' : 'Disabled' }}
        </span>
      </div>

      <!-- Disabled state -->
      <div v-if="!status.enabled && !setupData">
        <p class="text-sm text-gray-600 dark:text-gray-300 mb-4">
          2FA adds an extra layer of security. After enabling, you'll need a code from your
          authenticator app each time you log in.
        </p>
        <button
          @click="startSetup"
          :disabled="loading"
          class="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:opacity-50"
        >
          {{ loading ? 'Generating...' : 'Set up 2FA' }}
        </button>
      </div>

      <!-- Setup: show secret + verify -->
      <div v-if="setupData && !status.enabled">
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Manual entry key
          </label>
          <code class="block bg-gray-100 dark:bg-gray-900 rounded p-3 text-sm break-all">
            {{ setupData.secret }}
          </code>
          <p class="text-xs text-gray-500 mt-1 break-all">{{ setupData.otpAuthUri }}</p>
        </div>
        <div class="mb-4">
          <label class="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">
            Enter 6-digit code to confirm
          </label>
          <input
            v-model="verifyCode"
            type="text"
            inputmode="numeric"
            maxlength="6"
            placeholder="000000"
            class="w-40 px-3 py-2 border rounded-lg dark:bg-gray-900 dark:border-gray-600 text-center text-lg tracking-widest"
          />
        </div>
        <div class="flex gap-3">
          <button
            @click="confirmEnable"
            :disabled="loading || verifyCode.length !== 6"
            class="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 disabled:opacity-50"
          >
            Enable 2FA
          </button>
          <button @click="cancelSetup" class="px-4 py-2 text-gray-600 dark:text-gray-300 hover:underline">
            Cancel
          </button>
        </div>
      </div>

      <!-- Enabled state -->
      <div v-if="status.enabled">
        <p class="text-sm text-gray-600 dark:text-gray-300 mb-4">
          Two-factor authentication is active. Enter your current authenticator code to disable it.
        </p>
        <div class="flex gap-3 items-center">
          <input
            v-model="disableCode"
            type="text"
            inputmode="numeric"
            maxlength="6"
            placeholder="000000"
            class="w-40 px-3 py-2 border rounded-lg dark:bg-gray-900 dark:border-gray-600 text-center text-lg tracking-widest"
          />
          <button
            @click="confirmDisable"
            :disabled="loading || disableCode.length !== 6"
            class="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 disabled:opacity-50"
          >
            Disable 2FA
          </button>
        </div>
      </div>

      <p v-if="message" class="mt-4 text-sm" :class="messageOk ? 'text-green-600' : 'text-red-600'">
        {{ message }}
      </p>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import complianceApi from '@/api/complianceApi'

const status = ref({ enabled: false, pendingSetup: false })
const setupData = ref(null)
const verifyCode = ref('')
const disableCode = ref('')
const loading = ref(false)
const message = ref('')
const messageOk = ref(true)

async function loadStatus() {
  try {
    const { data } = await complianceApi.get2faStatus()
    status.value = data.data || data
  } catch (e) {
    console.error('Failed to load 2FA status', e)
  }
}

async function startSetup() {
  loading.value = true
  message.value = ''
  try {
    const { data } = await complianceApi.setup2fa()
    setupData.value = data.data || data
  } catch (e) {
    message.value = e.response?.data?.message || 'Failed to generate secret'
    messageOk.value = false
  } finally {
    loading.value = false
  }
}

async function confirmEnable() {
  loading.value = true
  message.value = ''
  try {
    await complianceApi.enable2fa(verifyCode.value)
    status.value.enabled = true
    setupData.value = null
    verifyCode.value = ''
    message.value = '2FA enabled successfully'
    messageOk.value = true
  } catch (e) {
    message.value = e.response?.data?.message || 'Invalid code'
    messageOk.value = false
  } finally {
    loading.value = false
  }
}

function cancelSetup() {
  setupData.value = null
  verifyCode.value = ''
}

async function confirmDisable() {
  loading.value = true
  message.value = ''
  try {
    await complianceApi.disable2fa(disableCode.value)
    status.value.enabled = false
    disableCode.value = ''
    message.value = '2FA disabled'
    messageOk.value = true
  } catch (e) {
    message.value = e.response?.data?.message || 'Invalid code'
    messageOk.value = false
  } finally {
    loading.value = false
  }
}

onMounted(loadStatus)
</script>
