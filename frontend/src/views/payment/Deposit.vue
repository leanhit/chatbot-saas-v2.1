<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <div class="flex items-center">
        <Icon icon="mdi:bank-transfer" class="text-2xl text-blue-600 dark:text-blue-400 mr-3" />
        <h1 class="text-2xl font-bold text-gray-800 dark:text-white">
          {{ $t('payment.title') }}
        </h1>
      </div>
    </div>

    <!-- Alert Messages -->
    <div v-if="paymentStore.message" class="mb-4 p-4 rounded-lg" :class="paymentStore.getMessageClass()">
      {{ paymentStore.message }}
    </div>

    <!-- Package Selection -->
    <div class="mb-8">
      <div class="flex items-center justify-center mb-6">
        <Icon icon="mdi:package-variant" class="text-2xl text-blue-600 dark:text-blue-400 mr-3" />
        <h2 class="text-xl font-semibold text-gray-800 dark:text-white">
          {{ $t('payment.selectPackage') }}
        </h2>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <!-- Dynamic Package Cards -->
        <div 
          v-for="pkg in paymentStore.packages"
          :key="pkg.packageId"
          @click="paymentStore.selectPackage(pkg.packageId)"
          :class="[
            'bg-white dark:bg-gray-900 rounded-lg shadow p-6 border-2 cursor-pointer transition-all duration-200 hover:shadow-lg relative',
            isCurrentPackage(pkg)
              ? 'border-green-500 dark:border-green-400'
              : isSelectedPackage(pkg)
              ? 'border-blue-500 dark:border-blue-400'
              : 'border-gray-200 dark:border-gray-700'
          ]"
        >
          <!-- Badge -->
          <div 
            v-if="pkg.badge"
            class="absolute -top-3 right-4 bg-red-500 dark:bg-red-600 text-white dark:text-white px-3 py-1 rounded-full text-xs font-semibold"
          >
            {{ $t(`payment.${pkg.badge.toLowerCase()}`) }}
          </div>
          
          <!-- Status labels -->
          <div 
            v-if="isCurrentPackage(pkg)"
            class="absolute -top-3 left-4 bg-green-500 dark:bg-green-600 text-white dark:text-white px-3 py-1 rounded-full text-xs font-semibold"
          >
            <Icon icon="mdi:check-circle" class="w-3 h-3 mr-1" />
            {{ $t('payment.currentlyUsing', 'Ðang dùng') || 'Ðang dùng' }}
          </div>
          <div 
            v-else-if="isSelectedPackage(pkg)"
            class="absolute -top-3 left-4 bg-blue-500 dark:bg-blue-600 text-white dark:text-white px-3 py-1 rounded-full text-xs font-semibold"
          >
            <Icon icon="mdi:cart" class="w-3 h-3 mr-1" />
            {{ $t('payment.selected') }}
          </div>
          
          <div class="text-center">
            <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-2">
              {{ getLocalizedPackageName(pkg) }}
            </h3>
            <div class="text-3xl font-bold mb-4" :class="getPriceColorClass(pkg.packageId)">
              <span v-if="pkg.price === 0">{{ getLocalizedPrice(pkg) }}</span>
              <template v-else>
                {{ formatCurrency(pkg.price) }}
                <span class="text-sm text-gray-500 dark:text-gray-400">/{{ getLocalizedDuration(pkg) }}</span>
              </template>
            </div>
            <p class="text-sm text-gray-700 dark:text-gray-300 mb-4">{{ getLocalizedDescription(pkg) }}</p>
            <ul class="text-left space-y-2 text-sm text-gray-700 dark:text-gray-300">
              <li v-for="feature in getPackageFeatures(pkg)" :key="feature" class="flex items-center">
                <Icon icon="mdi:check-circle" class="text-green-500 dark:text-green-400 mr-2" />
                <span class="text-gray-700 dark:text-gray-300">{{ feature }}</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- Payment Container: 1/4 + 3/4 Layout -->
    <div v-if="paymentStore.selectedPackage && paymentStore.selectedPackage.price > 0" class="grid grid-cols-1 lg:grid-cols-4 gap-6 mb-8">
      <!-- Selected Package Summary - 1/4 width -->
      <div class="lg:col-span-1">
        <div class="flex items-center justify-center mb-6">
          <Icon icon="mdi:cart-check" class="text-2xl text-blue-600 dark:text-blue-400 mr-3" />
          <h3 class="text-lg font-semibold text-gray-800 dark:text-white">
            {{ $t('payment.deposit.title') }}
          </h3>
        </div>
          
        <div class="bg-white dark:bg-gray-900 rounded-lg shadow p-6">
          <div class="space-y-4">
            <div>
              <span class="text-gray-700 dark:text-gray-300 text-sm">{{ $t('payment.selectPackage') }}:</span>
              <div class="font-semibold text-gray-800 dark:text-white mt-1">
                {{ paymentStore.selectedPackage ? paymentStore.selectedPackage.name : $t('payment.custom') }}
              </div>
            </div>
            
            <div>
              <span class="text-gray-700 dark:text-gray-300 text-sm">{{ $t('payment.amount') }}:</span>
              <div class="font-bold text-lg text-blue-700 dark:text-blue-300 mt-1">
                {{ paymentStore.formattedAmount }}
              </div>
            </div>
            
            <div v-if="paymentStore.selectedPackage">
              <span class="text-gray-700 dark:text-gray-300 text-sm">{{ $t('payment.duration') }}:</span>
              <div class="font-semibold text-gray-800 dark:text-white mt-1">
                {{ paymentStore.selectedPackage.duration }}
              </div>
            </div>
            
            <div class="pt-4">
              <button
                @click="paymentStore.createDeposit()"
                :disabled="!paymentStore.canCreatePayment"
                class="w-full bg-blue-600 text-white py-3 px-6 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 text-lg font-semibold"
              >
                <span v-if="paymentStore.loading" class="flex items-center justify-center">
                  <Icon icon="eos-icons:loading" class="animate-spin mr-2" />
                  {{ $t('payment.deposit.loading') }}
                </span>
                <span v-else>{{ $t('payment.deposit.proceed') }}</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- QR Code Display - 3/4 width -->
      <div v-if="paymentStore.currentPayment" class="lg:col-span-3">
        <div class="flex items-center justify-center mb-6">
          <Icon icon="mdi:qrcode-scan" class="text-2xl text-blue-600 dark:text-blue-400 mr-3" />
          <h2 class="text-xl font-semibold text-gray-800 dark:text-white">
            {{ $t('payment.bankInfo.title') }}
          </h2>
        </div>
        
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- QR Code -->
        <div class="text-center">
          <div class="bg-gray-100 dark:bg-gray-800 p-4 rounded-lg mb-4">
            <div v-if="paymentStore.currentPayment.qrContent">
              <!-- If qrContent is base64 image data -->
              <img 
                v-if="paymentStore.currentPayment.qrContent.startsWith('data:image')"
                :src="paymentStore.currentPayment.qrContent"
                alt="QR Code"
                class="mx-auto max-w-xs"
              />
              <!-- If qrContent is raw QR text data and we generated QR image -->
              <img 
                v-else-if="qrCodeImage"
                :src="qrCodeImage"
                alt="QR Code"
                class="mx-auto max-w-xs"
              />
              <!-- Fallback: display QR text -->
              <div v-else class="text-center">
                <div class="bg-white p-4 rounded inline-block">
                  <div class="text-xs text-gray-600 dark:text-gray-400 break-all max-w-xs">
                    {{ paymentStore.currentPayment.qrContent }}
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="text-gray-500 dark:text-gray-400">
              {{ $t('payment.qr.creating') }}
            </div>
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">
            {{ $t('payment.qr.scan') }}
          </p>
        </div>
        
        <!-- Payment Details -->
        <div>
          <div v-if="paymentStore.currentPayment" class="space-y-3">
            <div class="bg-gray-50 dark:bg-gray-700 p-3 rounded">
              <span class="text-gray-700 dark:text-gray-300 text-sm">{{ $t('payment.referenceCode') }}:</span>
              <div class="font-mono font-bold text-primary text-lg">{{ paymentStore.currentPayment.referenceCode }}</div>
            </div>
            
            <div class="flex justify-between items-center">
              <span class="text-gray-700 dark:text-gray-300">{{ $t('payment.amount') }}:</span>
              <span class="font-bold text-lg text-gray-800 dark:text-white">{{ formatCurrency(paymentStore.currentPayment.amount) }}</span>
            </div>
            
            <div class="flex justify-between items-center">
              <span class="text-gray-700 dark:text-gray-300">{{ $t('payment.status') }}:</span>
              <span 
                class="px-3 py-1 rounded-full text-sm font-semibold"
                :class="paymentStore.getStatusClass(paymentStore.currentPayment.status)"
              >
                {{ paymentStore.getStatusText(paymentStore.currentPayment.status) }}
              </span>
            </div>
            
            <div class="flex justify-between items-center">
              <span class="text-gray-700 dark:text-gray-300">{{ $t('payment.createdAt') }}:</span>
              <span class="text-sm text-gray-600 dark:text-gray-400">{{ paymentStore.formattedCreatedAt }}</span>
            </div>
            
            <div v-if="paymentStore.currentPayment.expiresAt" class="flex justify-between items-center">
              <span class="text-gray-700 dark:text-gray-300">{{ $t('payment.expiresAt') }}:</span>
              <span class="text-sm text-gray-600 dark:text-gray-400">{{ paymentStore.formattedExpiresAt }}</span>
            </div>
            
            <div v-if="paymentStore.currentPayment.completedAt" class="flex justify-between items-center">
              <span class="text-gray-700 dark:text-gray-300">{{ $t('payment.completedAt') }}:</span>
              <span class="text-sm text-gray-600 dark:text-gray-400">{{ paymentStore.formattedCompletedAt }}</span>
            </div>
          </div>
          
          <div v-else class="text-center py-8">
            <Icon icon="mdi:clock-outline" class="text-6xl text-blue-300 dark:text-blue-400 mb-4" />
            <p class="text-blue-600 dark:text-blue-300 mb-4">{{ $t('payment.readyToCreate') }}</p>
            <p class="text-sm text-blue-500 dark:text-blue-400">{{ $t('payment.clickCreateButton') }}</p>
          </div>

          <!-- Actions -->
          <div class="mt-6 flex flex-col sm:flex-row gap-3">
            <button
              @click="paymentStore.checkPaymentStatus()"
              :disabled="!paymentStore.canCheckStatus"
              class="flex-1 bg-blue-500 text-white py-2 px-4 rounded hover:bg-blue-600 disabled:opacity-50"
            >
              <span v-if="paymentStore.checkingStatus" class="flex items-center justify-center">
                <Icon icon="eos-icons:loading" class="animate-spin mr-2" />
                {{ $t('payment.checkingStatus') }}
              </span>
              <span v-else>
                <Icon icon="mdi:refresh" class="mr-2" />
                {{ $t('payment.checkStatus') }}
              </span>
            </button>
            
            <button
              @click="paymentStore.simulatePayment()"
              :disabled="!paymentStore.hasPendingPayment"
              class="flex-1 bg-green-500 text-white py-2 px-4 rounded hover:bg-green-600 disabled:opacity-50"
            >
              <Icon icon="mdi:play-circle" class="mr-2" />
              {{ $t('payment.simulatePayment') }}
            </button>
            
            <button
              @click="paymentStore.copyReferenceCode(paymentStore.currentPayment.referenceCode)"
              class="flex-1 bg-gray-500 text-white py-2 px-4 rounded hover:bg-gray-600"
            >
              <Icon icon="mdi:content-copy" class="mr-2" />
              {{ $t('payment.copyCode') }}
            </button>
          </div>
        </div>
      </div>
      </div>
    </div>

    <!-- Free Package Confirmation -->
    <div v-if="paymentStore.selectedPackage && paymentStore.selectedPackage.price === 0" class="mt-6 bg-white dark:bg-gray-900 rounded-lg shadow p-6">
      <div class="flex items-center justify-center mb-6">
        <Icon icon="mdi:gift" class="text-2xl text-green-600 dark:text-green-400 mr-3" />
        <h3 class="text-lg font-semibold text-gray-800 dark:text-white">
          {{ $t('payment.free.confirmTitle') }}
        </h3>
      </div>
      
      <div class="text-center">
        <p class="text-gray-600 dark:text-gray-300 mb-6">
          {{ $t('payment.free.confirmDescription') }}
        </p>
        
        <button
          @click="paymentStore.activateFreePackage()"
          class="bg-green-600 text-white py-3 px-6 rounded-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 text-lg font-semibold"
        >
          {{ $t('payment.free.activateButton') }}
        </button>
      </div>
    </div>

    <!-- Payment Instructions - MOVED TO BOTTOM -->
    <div v-if="paymentStore.bankInfo" class="mt-6 bg-white dark:bg-gray-900 rounded-lg shadow p-6">
      <div class="flex items-center justify-center mb-6">
        <Icon icon="mdi:bank" class="text-2xl text-blue-600 dark:text-blue-400 mr-3" />
        <h2 class="text-xl font-semibold text-gray-800 dark:text-white">
          {{ $t('payment.bankInfo.title') }}
        </h2>
      </div>
      
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div>
          <h3 class="font-semibold text-white dark:text-white mb-4">{{ $t('payment.bankInfo.transferInfo') }}</h3>
          <div class="space-y-3">
            <div class="flex justify-between">
              <span class="text-white dark:text-white">{{ $t('payment.bankInfo.bankName') }}:</span>
              <span class="font-semibold text-white dark:text-white">{{ paymentStore.bankInfo.bankName }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-white dark:text-white">{{ $t('payment.bankInfo.accountNumber') }}:</span>
              <span class="font-mono font-semibold text-white dark:text-white">{{ paymentStore.bankInfo.accountNumber }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-white dark:text-white">{{ $t('payment.bankInfo.accountName') }}:</span>
              <span class="font-semibold text-white dark:text-white">{{ paymentStore.bankInfo.accountName }}</span>
            </div>
          </div>
        </div>
        
        <div>
          <h3 class="font-semibold text-white dark:text-white mb-4">{{ $t('payment.bankInfo.instructions') }}</h3>
          <ul class="space-y-2 text-sm text-white dark:text-white">
            <li class="flex items-start">
              <Icon icon="mdi:numeric-1-circle" class="text-blue-500 dark:text-blue-400 mr-2 mt-0.5" />
              <span>{{ $t('payment.bankInfo.step1') }}</span>
            </li>
            <li class="flex items-start">
              <Icon icon="mdi:numeric-2-circle" class="text-blue-500 dark:text-blue-400 mr-2 mt-0.5" />
              <span>{{ $t('payment.bankInfo.step2') }}<strong v-if="paymentStore.currentPayment" class="text-white dark:text-white">{{ paymentStore.currentPayment.referenceCode }}</strong><span v-else class="text-white dark:text-white">[{{ $t('payment.referencePlaceholder') }}]</span></span>
            </li>
            <li class="flex items-start">
              <Icon icon="mdi:numeric-3-circle" class="text-blue-500 dark:text-blue-400 mr-2 mt-0.5" />
              <span>{{ $t('payment.bankInfo.step3') }}</span>
            </li>
            <li class="flex items-start">
              <Icon icon="mdi:numeric-4-circle" class="text-blue-500 dark:text-blue-400 mr-2 mt-0.5" />
              <span>{{ $t('payment.bankInfo.note') }}{{ $t('payment.activation.success') }}</span>
            </li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue'
import { usePaymentStore } from '@/stores/paymentStore'
import QRCode from 'qrcode'
import { useI18n } from 'vue-i18n'
import { watch, onMounted } from 'vue'

export default {
  name: 'PaymentDeposit',
  components: {
    Icon
  },
  data() {
    return {
      qrCodeImage: null
    }
  },
  watch: {
    'paymentStore.currentPayment.qrContent': {
      immediate: true,
      async handler(newQrContent) {
        if (newQrContent && !newQrContent.startsWith('data:image')) {
          try {
            this.qrCodeImage = await QRCode.toDataURL(newQrContent)
          } catch (error) {
            console.error('Error generating QR code:', error)
            this.qrCodeImage = null
          }
        } else {
          this.qrCodeImage = null
        }
      }
    }
  },
  setup() {
    const paymentStore = usePaymentStore()
    const { t, locale } = useI18n()

    // Watch for language changes and reload packages
    watch(locale, async (newLocale) => {
      console.log('ð ã Language changed to:', newLocale)
      console.log('ð ã Current locale value:', locale.value)
      await paymentStore.loadPackages()
    })

    // Watch for payment status changes and refresh current package when completed
    watch(() => paymentStore.currentPayment?.status, async (newStatus, oldStatus) => {
      if (newStatus === 'COMPLETED' && oldStatus !== 'COMPLETED') {
        console.log('ð Payment completed! Refreshing current package...')
        await paymentStore.loadCurrentPackage()
        console.log('ð Current package refreshed after payment completion')
      }
    })

    // Format currency function
    const formatCurrency = (amount) => {
      // Ensure amount is a number
      const numAmount = typeof amount === 'number' ? amount : parseFloat(amount) || 0
      
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(numAmount)
    }

    // Helper function to get price color class
    const getPriceColorClass = (packageId) => {
      const colorClasses = {
        free: 'text-green-600 dark:text-green-400',
        '3months': 'text-blue-600 dark:text-blue-300',
        '6months': 'text-purple-600 dark:text-purple-300',
        '12months': 'text-yellow-600 dark:text-yellow-300'
      }
      return colorClasses[packageId] || 'text-gray-600 dark:text-gray-400'
    }

    // Helper function to get package features with translation
    const getPackageFeatures = (pkg) => {
      const features = []
      const isVietnamese = locale.value === 'vi'
      
      // Message limit
      if (pkg.messageLimit && pkg.messageLimit > 0) {
        if (pkg.messageLimit >= 2147483647) {
          const unlimitedText = isVietnamese ? 'Không giới hạn' : 'Unlimited'
          const messagesText = isVietnamese ? 'tháng' : 'month'
          features.push(`${unlimitedText} ${messagesText}`)
        } else {
          const messagesText = isVietnamese ? 'tháng' : 'month'
          features.push(`${pkg.messageLimit.toLocaleString()} ${messagesText}`)
        }
      }
      
      // Chatbot limit
      if (pkg.chatbotLimit && pkg.chatbotLimit > 0) {
        if (pkg.chatbotLimit >= 2147483647) {
          const unlimitedText = isVietnamese ? 'Không giới hạn' : 'Unlimited'
          const chatbotsText = isVietnamese ? 'chatbot' : 'chatbots'
          features.push(`${unlimitedText} ${chatbotsText}`)
        } else {
          const chatbotsText = isVietnamese ? 'chatbot' : 'chatbots'
          features.push(`${pkg.chatbotLimit} ${chatbotsText}`)
        }
      }
      
      // User tenant creation limit (per user)
      const userTenantLimitText = isVietnamese ? 'Tối đa 4 tenant/user' : 'Maximum 4 tenants per user'
      features.push(userTenantLimitText)
      
      // Support features
      if (pkg.hasPrioritySupport) {
        features.push(isVietnamese ? 'Hỗ trợ ưu tiên' : 'Priority Support')
      }
      
      if (pkg.hasDedicatedSupport) {
        features.push(isVietnamese ? 'Hỗ trợ 24/7' : '24/7 Support')
      }
      
      if (pkg.hasAnalytics) {
        features.push(isVietnamese ? 'Phân tích cơ bản' : 'Basic Analytics')
      }
      
      if (pkg.hasAdvancedAnalytics) {
        features.push(isVietnamese ? 'Phân tích nâng cao' : 'Advanced Analytics')
      }
      
      if (pkg.hasCustomIntegrations) {
        features.push(isVietnamese ? 'Tích hợp tùy chỉnh' : 'Custom integrations')
      }
      
      if (pkg.hasCustomFeatures) {
        features.push(isVietnamese ? 'Tính năng tùy chỉnh' : 'Custom features')
      }
      
      if (pkg.hasSlaGuarantee) {
        features.push(isVietnamese ? 'Đảm bảo SLA' : 'SLA guarantee')
      }
      
      // Add basic support for free packages
      if (pkg.price === 0) {
        features.push(isVietnamese ? 'Hỗ trợ cơ bản' : 'Basic Support')
      }
      
      // Add connection limit
      if (pkg.connectionLimit && pkg.connectionLimit > 0) {
        if (pkg.connectionLimit >= 2147483647) {
          features.push(isVietnamese ? 'Kết nối không giới hạn' : 'Unlimited connections')
        } else {
          features.push(isVietnamese ? `${pkg.connectionLimit} kết nối` : `${pkg.connectionLimit} connections`)
        }
      }
      
      return features
    }
    
    // Helper functions for localization
    const getLocalizedPackageName = (pkg) => {
      const isVietnamese = locale.value === 'vi'
      console.log('🔍 getLocalizedPackageName - locale:', locale.value, 'isVietnamese:', isVietnamese)
      const nameMap = {
        free: isVietnamese ? 'Miễn phí' : 'Free',
        '3months': isVietnamese ? '3 Tháng' : '3 Months',
        '6months': isVietnamese ? '6 Tháng' : '6 Months',
        '12months': isVietnamese ? '12 Tháng' : '12 Months'
      }
      return nameMap[pkg.packageId] || pkg.name
    }
    
    const getLocalizedDescription = (pkg) => {
      const isVietnamese = locale.value === 'vi'
      const descMap = {
        free: isVietnamese ? 'Gói dùng thử miễn phí' : 'Free trial package',
        '3months': isVietnamese ? 'Gói 3 tháng không giới hạn' : 'Unlimited 3 months package',
        '6months': isVietnamese ? 'Gói 6 tháng không giới hạn' : 'Unlimited 6 months package',
        '12months': isVietnamese ? 'Gói 12 tháng không giới hạn' : 'Unlimited 12 months package'
      }
      return descMap[pkg.packageId] || pkg.description
    }
    
    const getLocalizedDuration = (pkg) => {
      const isVietnamese = locale.value === 'vi'
      const durationMap = {
        '1 month': isVietnamese ? '1 tháng' : '1 month',
        '3 months': isVietnamese ? '3 tháng' : '3 months',
        '6 months': isVietnamese ? '6 tháng' : '6 months',
        '12 months': isVietnamese ? '12 tháng' : '12 months'
      }
      return durationMap[pkg.duration] || pkg.duration
    }
    
    const getLocalizedPrice = (pkg) => {
      const isVietnamese = locale.value === 'vi'
      return isVietnamese ? 'Miễn phí' : 'Free'
    }
    
    // Helper methods for package matching
    const isCurrentPackage = (pkg) => {
      if (!paymentStore.currentPackage) return false
      // Check both possible ID fields for robust matching
      return paymentStore.currentPackage.id === pkg.packageId || 
             paymentStore.currentPackage.packageId === pkg.packageId ||
             paymentStore.currentPackage.id === pkg.id ||
             paymentStore.currentPackage.packageId === pkg.id
    }
    
    const isSelectedPackage = (pkg) => {
      if (!paymentStore.selectedPackage) return false
      // Check both possible ID fields for robust matching
      return paymentStore.selectedPackage.id === pkg.packageId || 
             paymentStore.selectedPackage.packageId === pkg.packageId ||
             paymentStore.selectedPackage.id === pkg.id ||
             paymentStore.selectedPackage.packageId === pkg.id
    }

    // Load data on mount
    const loadAllData = async () => {
      console.log('🔄 [Deposit] Loading all payment data...')
      try {
        await paymentStore.loadPackages()
        console.log('✅ [Deposit] Packages loaded')
        
        await paymentStore.loadBankInfo()
        console.log('✅ [Deposit] Bank info loaded')
        
        await paymentStore.loadCurrentPackage()
        console.log('✅ [Deposit] Current package loaded')
        
        console.log('✅ [Deposit] All payment data loaded successfully')
      } catch (error) {
        console.error('❌ [Deposit] Error loading payment data:', error)
      }
    }
    
    // Load data on mount
    onMounted(() => {
      loadAllData()
    })

    // Debug: Log current package
    console.log('Payment store current package:', paymentStore.currentPackage)
    console.log('Payment store selected package:', paymentStore.selectedPackage)
    console.log('Payment store packages:', paymentStore.packages)
    
    // Debug function to check package matching
    const debugPackageMatch = () => {
      if (paymentStore.currentPackage) {
        console.log('🔍 Current package ID:', paymentStore.currentPackage.id)
        console.log('🔍 Current package packageId:', paymentStore.currentPackage.packageId)
        paymentStore.packages.forEach(pkg => {
          const match = paymentStore.currentPackage.id === pkg.packageId || paymentStore.currentPackage.packageId === pkg.packageId
          console.log(`📦 Package ${pkg.packageId} matches: ${match}`)
        })
      }
    }
    
    // Call debug function
    debugPackageMatch()

    return {
      paymentStore,
      formatCurrency,
      getPriceColorClass,
      getPackageFeatures,
      getLocalizedPackageName,
      getLocalizedDescription,
      getLocalizedDuration,
      getLocalizedPrice,
      isCurrentPackage,
      isSelectedPackage
    }
  }
}
</script>
