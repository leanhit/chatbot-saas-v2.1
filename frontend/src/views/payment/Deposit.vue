<template>
  <div class="p-6">
    <div class="flex justify-between items-center mb-6">
      <div class="flex items-center">
        <Icon icon="mdi:bank-transfer" class="text-2xl text-blue-600 dark:text-blue-400 mr-3" />
        <h1 class="text-2xl font-bold text-gray-800 dark:text-white">
          Nạp Tiền Theo Gói
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
          Chọn Gói Dịch Vụ
        </h2>
      </div>
      
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <!-- Free Package -->
        <div 
          @click="paymentStore.selectPackage('free')"
          :class="[
            'bg-white dark:bg-gray-900 rounded-lg shadow p-6 border-2 cursor-pointer transition-all duration-200 hover:shadow-lg relative',
            paymentStore.currentPackage?.id === 'free'
              ? 'border-green-500 dark:border-green-400'
              : paymentStore.selectedPackage?.id === 'free'
              ? 'border-blue-500 dark:border-blue-400'
              : 'border-gray-200 dark:border-gray-700'
          ]"
        >
          <div 
            v-if="paymentStore.currentPackage?.id === 'free'"
            class="absolute -top-3 left-4 bg-green-500 dark:bg-green-600 text-white dark:text-white px-3 py-1 rounded-full text-xs font-semibold"
          >
            ĐANG DÙNG
          </div>
          <div 
            v-else-if="paymentStore.selectedPackage?.id === 'free'"
            class="absolute -top-3 left-4 bg-blue-500 dark:bg-blue-600 text-white dark:text-white px-3 py-1 rounded-full text-xs font-semibold"
          >
            ĐÃ CHỌN
          </div>
          <div class="text-center">
            <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-2">Free</h3>
            <div class="text-3xl font-bold text-green-600 dark:text-green-400 mb-4">Miễn phí</div>
            <p class="text-sm text-gray-700 dark:text-gray-300 mb-4">Dùng thử nghiệm</p>
            <ul class="text-left space-y-2 text-sm text-gray-700 dark:text-gray-300">
              <li class="flex items-center">
                <Icon icon="mdi:check-circle" class="text-green-500 dark:text-green-400 mr-2" />
                <span class="text-gray-700 dark:text-gray-300">100 tin nhắn/tháng</span>
              </li>
              <li class="flex items-center">
                <Icon icon="mdi:check-circle" class="text-green-500 dark:text-green-400 mr-2" />
                <span class="text-gray-700 dark:text-gray-300">1 chatbot</span>
              </li>
              <li class="flex items-center">
                <Icon icon="mdi:check-circle" class="text-green-500 dark:text-green-400 mr-2" />
                <span class="text-gray-700 dark:text-gray-300">Support cơ bản</span>
              </li>
            </ul>
          </div>
        </div>

        <!-- Pro Package -->
        <div 
          @click="paymentStore.selectPackage('pro')"
          :class="[
            'bg-white dark:bg-gray-900 rounded-lg shadow p-6 border-2 cursor-pointer transition-all duration-200 hover:shadow-lg relative',
            paymentStore.currentPackage?.id === 'pro'
              ? 'border-blue-500 dark:border-blue-400'
              : paymentStore.selectedPackage?.id === 'pro'
              ? 'border-blue-500 dark:border-blue-400'
              : 'border-gray-200 dark:border-gray-700'
          ]"
        >
          <div class="absolute -top-3 right-4 bg-red-500 dark:bg-red-600 text-white dark:text-white px-3 py-1 rounded-full text-xs font-semibold">
            POPULAR
          </div>
          <div 
            v-if="paymentStore.currentPackage?.id === 'pro'"
            class="absolute -top-3 left-4 bg-blue-500 dark:bg-blue-600 text-white dark:text-white px-3 py-1 rounded-full text-xs font-semibold"
          >
            ĐANG DÙNG
          </div>
          <div 
            v-else-if="paymentStore.selectedPackage?.id === 'pro'"
            class="absolute -top-3 left-4 bg-blue-500 dark:bg-blue-600 text-white dark:text-white px-3 py-1 rounded-full text-xs font-semibold"
          >
            ĐÃ CHỌN
          </div>
          <div class="text-center">
            <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-2">Pro</h3>
            <div class="text-3xl font-bold text-blue-600 dark:text-blue-300 mb-4">
              250.000 ₫
              <span class="text-sm text-gray-500 dark:text-gray-400">/tháng</span>
            </div>
            <p class="text-sm text-gray-700 dark:text-gray-300 mb-4">1 tháng sử dụng</p>
            <ul class="text-left space-y-2 text-sm text-gray-700 dark:text-gray-300">
              <li class="flex items-center">
                <Icon icon="mdi:check-circle" class="text-green-500 dark:text-green-400 mr-2" />
                <span class="text-gray-700 dark:text-gray-300">5.000 tin nhắn/tháng</span>
              </li>
              <li class="flex items-center">
                <Icon icon="mdi:check-circle" class="text-green-500 dark:text-green-400 mr-2" />
                <span class="text-gray-700 dark:text-gray-300">3 chatbots</span>
              </li>
              <li class="flex items-center">
                <Icon icon="mdi:check-circle" class="text-green-500 dark:text-green-400 mr-2" />
                <span class="text-gray-700 dark:text-gray-300">Support ưu tiên</span>
              </li>
              <li class="flex items-center">
                <Icon icon="mdi:check-circle" class="text-green-500 dark:text-green-400 mr-2" />
                <span class="text-gray-700 dark:text-gray-300">Analytics cơ bản</span>
              </li>
            </ul>
          </div>
        </div>

        <!-- Business Package -->
        <div 
          @click="paymentStore.selectPackage('business')"
          :class="[
            'bg-white dark:bg-gray-900 rounded-lg shadow p-6 border-2 cursor-pointer transition-all duration-200 hover:shadow-lg relative',
            paymentStore.currentPackage?.id === 'business'
              ? 'border-purple-500 dark:border-purple-400'
              : paymentStore.selectedPackage?.id === 'business'
              ? 'border-purple-500 dark:border-purple-400'
              : 'border-gray-200 dark:border-gray-700'
          ]"
        >
          <div 
            v-if="paymentStore.currentPackage?.id === 'business'"
            class="absolute -top-3 left-4 bg-purple-500 dark:bg-purple-600 text-white dark:text-white px-3 py-1 rounded-full text-xs font-semibold"
          >
            ĐANG DÙNG
          </div>
          <div 
            v-else-if="paymentStore.selectedPackage?.id === 'business'"
            class="absolute -top-3 left-4 bg-purple-500 dark:bg-purple-600 text-white dark:text-white px-3 py-1 rounded-full text-xs font-semibold"
          >
            ĐÃ CHỌN
          </div>
          <div class="text-center">
            <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-2">Business</h3>
            <div class="text-3xl font-bold text-purple-600 dark:text-purple-300 mb-4">
              500.000 ₫
              <span class="text-sm text-gray-500 dark:text-gray-400">/tháng</span>
            </div>
            <p class="text-sm text-gray-700 dark:text-gray-300 mb-4">1 tháng sử dụng</p>
            <ul class="text-left space-y-2 text-sm text-gray-700 dark:text-gray-300">
              <li class="flex items-center">
                <Icon icon="mdi:check-circle" class="text-green-500 dark:text-green-400 mr-2" />
                <span class="text-gray-700 dark:text-gray-300">15.000 tin nhắn/tháng</span>
              </li>
              <li class="flex items-center">
                <Icon icon="mdi:check-circle" class="text-green-500 dark:text-green-400 mr-2" />
                <span class="text-gray-700 dark:text-gray-300">10 chatbots</span>
              </li>
              <li class="flex items-center">
                <Icon icon="mdi:check-circle" class="text-green-500 dark:text-green-400 mr-2" />
                <span class="text-gray-700 dark:text-gray-300">Support 24/7</span>
              </li>
              <li class="flex items-center">
                <Icon icon="mdi:check-circle" class="text-green-500 dark:text-green-400 mr-2" />
                <span class="text-gray-700 dark:text-gray-300">Analytics nâng cao</span>
              </li>
              <li class="flex items-center">
                <Icon icon="mdi:check-circle" class="text-green-500 dark:text-green-400 mr-2" />
                <span class="text-gray-700 dark:text-gray-300">Custom integrations</span>
              </li>
            </ul>
          </div>
        </div>

        <!-- Enterprise Package -->
        <div 
          @click="paymentStore.selectPackage('enterprise')"
          :class="[
            'bg-white dark:bg-gray-900 rounded-lg shadow p-6 border-2 cursor-pointer transition-all duration-200 hover:shadow-lg relative',
            paymentStore.currentPackage?.id === 'enterprise'
              ? 'border-yellow-500 dark:border-yellow-400'
              : paymentStore.selectedPackage?.id === 'enterprise'
              ? 'border-yellow-500 dark:border-yellow-400'
              : 'border-gray-200 dark:border-gray-700'
          ]"
        >
          <div 
            v-if="paymentStore.currentPackage?.id === 'enterprise'"
            class="absolute -top-3 left-4 bg-yellow-500 dark:bg-yellow-600 text-white dark:text-white px-3 py-1 rounded-full text-xs font-semibold"
          >
            ĐANG DÙNG
          </div>
          <div 
            v-else-if="paymentStore.selectedPackage?.id === 'enterprise'"
            class="absolute -top-3 left-4 bg-yellow-500 dark:bg-yellow-600 text-white dark:text-white px-3 py-1 rounded-full text-xs font-semibold"
          >
            ĐÃ CHỌN
          </div>
          <div class="text-center">
            <h3 class="text-lg font-semibold text-gray-800 dark:text-white mb-2">Enterprise</h3>
            <div class="text-3xl font-bold text-yellow-600 dark:text-yellow-300 mb-4">
              1.000.000 ₫
              <span class="text-sm text-gray-500 dark:text-gray-400">/tháng</span>
            </div>
            <p class="text-sm text-gray-700 dark:text-gray-300 mb-4">1 tháng sử dụng</p>
            <ul class="text-left space-y-2 text-sm text-gray-700 dark:text-gray-300">
              <li class="flex items-center">
                <Icon icon="mdi:check-circle" class="text-green-500 dark:text-green-400 mr-2" />
                <span class="text-gray-700 dark:text-gray-300">Unlimited tin nhắn</span>
              </li>
              <li class="flex items-center">
                <Icon icon="mdi:check-circle" class="text-green-500 dark:text-green-400 mr-2" />
                <span class="text-gray-700 dark:text-gray-300">Unlimited chatbots</span>
              </li>
              <li class="flex items-center">
                <Icon icon="mdi:check-circle" class="text-green-500 dark:text-green-400 mr-2" />
                <span class="text-gray-700 dark:text-gray-300">Dedicated support</span>
              </li>
              <li class="flex items-center">
                <Icon icon="mdi:check-circle" class="text-green-500 dark:text-green-400 mr-2" />
                <span class="text-gray-700 dark:text-gray-300">Custom features</span>
              </li>
              <li class="flex items-center">
                <Icon icon="mdi:check-circle" class="text-green-500 dark:text-green-400 mr-2" />
                <span class="text-gray-700 dark:text-gray-300">SLA guarantee</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- Selected Package Summary -->
    <div v-if="paymentStore.selectedPackage && paymentStore.selectedPackage.price > 0" class="mb-8">
      <div class="flex items-center justify-center mb-6">
        <Icon icon="mdi:cart-check" class="text-2xl text-blue-600 dark:text-blue-400 mr-3" />
        <h3 class="text-lg font-semibold text-gray-800 dark:text-white">
          Tóm Tắt Thanh Toán
        </h3>
      </div>
        
      <div class="bg-white dark:bg-gray-900 rounded-lg shadow p-6">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <div>
            <div class="space-y-2">
              <div class="flex justify-between">
                <span class="text-gray-700 dark:text-gray-300">Gói dịch vụ:</span>
                <span class="font-semibold text-gray-800 dark:text-white">
                  {{ paymentStore.selectedPackage ? paymentStore.selectedPackage.name : 'Tùy chỉnh' }}
                </span>
              </div>
              <div class="flex justify-between">
                <span class="text-gray-700 dark:text-gray-300">Số tiền:</span>
                <span class="font-bold text-lg text-blue-700 dark:text-blue-300">
                  {{ paymentStore.formattedAmount }}
                </span>
              </div>
              <div v-if="paymentStore.selectedPackage" class="flex justify-between">
                <span class="text-gray-700 dark:text-gray-300">Thời hạn:</span>
                <span class="font-semibold text-gray-800 dark:text-white">
                  {{ paymentStore.selectedPackage.duration }}
                </span>
              </div>
            </div>
          </div>
          
          <div class="text-center">
            <button
              @click="paymentStore.createDeposit()"
              :disabled="!paymentStore.canCreatePayment"
              class="w-full bg-blue-600 text-white py-3 px-6 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 text-lg font-semibold"
            >
              <span v-if="paymentStore.loading" class="flex items-center justify-center">
                <Icon icon="eos-icons:loading" class="animate-spin mr-2" />
                Đang xử lý...
              </span>
              <span v-else>Nạp Tiền Ngay</span>
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- QR Code Display -->
    <div v-if="paymentStore.currentPayment" class="mt-6 bg-white dark:bg-gray-900 rounded-lg shadow p-6">
      <div class="flex items-center justify-center mb-6">
        <Icon icon="mdi:qrcode-scan" class="text-2xl text-blue-600 dark:text-blue-400 mr-3" />
        <h2 class="text-xl font-semibold text-gray-800 dark:text-white">
          Yêu Cầu Nạp Tiền Của Bạn
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
              QR Code đang được tạo...
            </div>
          </div>
          <p class="text-sm text-gray-600 dark:text-gray-400">
            Quét mã QR để thanh toán
          </p>
        </div>
        
        <!-- Payment Details -->
        <div>
          <div v-if="paymentStore.currentPayment" class="space-y-3">
            <div class="bg-gray-50 dark:bg-gray-700 p-3 rounded">
              <span class="text-gray-700 dark:text-gray-300 text-sm">Mã tham chiếu:</span>
              <div class="font-mono font-bold text-primary text-lg">{{ paymentStore.currentPayment.referenceCode }}</div>
            </div>
            
            <div class="flex justify-between items-center">
              <span class="text-gray-700 dark:text-gray-300">Số tiền:</span>
              <span class="font-bold text-lg text-gray-800 dark:text-white">{{ formatCurrency(paymentStore.currentPayment.amount) }}</span>
            </div>
            
            <div class="flex justify-between items-center">
              <span class="text-gray-700 dark:text-gray-300">Trạng thái:</span>
              <span 
                class="px-3 py-1 rounded-full text-sm font-semibold"
                :class="paymentStore.getStatusClass(paymentStore.currentPayment.status)"
              >
                {{ paymentStore.getStatusText(paymentStore.currentPayment.status) }}
              </span>
            </div>
            
            <div class="flex justify-between items-center">
              <span class="text-gray-700 dark:text-gray-300">Tạo lúc:</span>
              <span class="text-sm text-gray-600 dark:text-gray-400">{{ paymentStore.formattedCreatedAt }}</span>
            </div>
            
            <div v-if="paymentStore.currentPayment.expiresAt" class="flex justify-between items-center">
              <span class="text-gray-700 dark:text-gray-300">Hết hạn:</span>
              <span class="text-sm text-gray-600 dark:text-gray-400">{{ paymentStore.formattedExpiresAt }}</span>
            </div>
            
            <div v-if="paymentStore.currentPayment.completedAt" class="flex justify-between items-center">
              <span class="text-gray-700 dark:text-gray-300">Hoàn thành:</span>
              <span class="text-sm text-gray-600 dark:text-gray-400">{{ paymentStore.formattedCompletedAt }}</span>
            </div>
          </div>
          
          <div v-else class="text-center py-8">
            <Icon icon="mdi:clock-outline" class="text-6xl text-blue-300 dark:text-blue-400 mb-4" />
            <p class="text-blue-600 dark:text-blue-300 mb-4">Sẵn sàng tạo yêu cầu nạp tiền</p>
            <p class="text-sm text-blue-500 dark:text-blue-400">Nhấn nút "Nạp Tiền Ngay" để tạo yêu cầu thanh toán</p>
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
                Đang kiểm tra...
              </span>
              <span v-else>
                <Icon icon="mdi:refresh" class="mr-2" />
                Kiểm tra trạng thái
              </span>
            </button>
            
            <button
              @click="paymentStore.simulatePayment()"
              :disabled="!paymentStore.hasPendingPayment"
              class="flex-1 bg-green-500 text-white py-2 px-4 rounded hover:bg-green-600 disabled:opacity-50"
            >
              <Icon icon="mdi:play-circle" class="mr-2" />
              Giả lập thanh toán
            </button>
            
            <button
              @click="paymentStore.copyReferenceCode(paymentStore.currentPayment.referenceCode)"
              class="flex-1 bg-gray-500 text-white py-2 px-4 rounded hover:bg-gray-600"
            >
              <Icon icon="mdi:content-copy" class="mr-2" />
              Sao chép mã
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Free Package Confirmation -->
    <div v-if="paymentStore.selectedPackage && paymentStore.selectedPackage.price === 0" class="mt-6 bg-white dark:bg-gray-900 rounded-lg shadow p-6">
      <div class="flex items-center justify-center mb-6">
        <Icon icon="mdi:gift" class="text-2xl text-green-600 dark:text-green-400 mr-3" />
        <h3 class="text-lg font-semibold text-gray-800 dark:text-white">
          Xác Nhận Gói Miễn Phí
        </h3>
      </div>
      
      <div class="text-center">
        <p class="text-gray-600 dark:text-gray-300 mb-6">
          Bạn đã chọn gói miễn phí. Gói này sẽ được kích hoạt ngay lập tức.
        </p>
        
        <button
          @click="paymentStore.activateFreePackage()"
          class="bg-green-600 text-white py-3 px-6 rounded-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 text-lg font-semibold"
        >
          Kích hoạt gói miễn phí
        </button>
      </div>
    </div>

    <!-- Payment Instructions - MOVED TO BOTTOM -->
    <div v-if="paymentStore.bankInfo" class="mt-6 bg-white dark:bg-gray-900 rounded-lg shadow p-6">
      <div class="flex items-center justify-center mb-6">
        <Icon icon="mdi:bank" class="text-2xl text-blue-600 dark:text-blue-400 mr-3" />
        <h2 class="text-xl font-semibold text-gray-800 dark:text-white">
          Thông Tin Thanh Toán
        </h2>
      </div>
      
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div>
          <h3 class="font-semibold text-white dark:text-white mb-4">Thông tin chuyển khoản</h3>
          <div class="space-y-3">
            <div class="flex justify-between">
              <span class="text-white dark:text-white">Ngân hàng:</span>
              <span class="font-semibold text-white dark:text-white">{{ paymentStore.bankInfo.bankName }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-white dark:text-white">Số tài khoản:</span>
              <span class="font-mono font-semibold text-white dark:text-white">{{ paymentStore.bankInfo.accountNumber }}</span>
            </div>
            <div class="flex justify-between">
              <span class="text-white dark:text-white">Chủ tài khoản:</span>
              <span class="font-semibold text-white dark:text-white">{{ paymentStore.bankInfo.accountName }}</span>
            </div>
          </div>
        </div>
        
        <div>
          <h3 class="font-semibold text-white dark:text-white mb-4">Hướng dẫn thanh toán</h3>
          <ul class="space-y-2 text-sm text-white dark:text-white">
            <li class="flex items-start">
              <Icon icon="mdi:numeric-1-circle" class="text-blue-500 dark:text-blue-400 mr-2 mt-0.5" />
              <span>Chuyển khoản theo thông tin bên trên</span>
            </li>
            <li class="flex items-start">
              <Icon icon="mdi:numeric-2-circle" class="text-blue-500 dark:text-blue-400 mr-2 mt-0.5" />
              <span>Nội dung chuyển khoản: <strong v-if="paymentStore.currentPayment" class="text-white dark:text-white">{{ paymentStore.currentPayment.referenceCode }}</strong><span v-else class="text-white dark:text-white">[sẽ được tạo sau khi nạp tiền]</span></span>
            </li>
            <li class="flex items-start">
              <Icon icon="mdi:numeric-3-circle" class="text-blue-500 dark:text-blue-400 mr-2 mt-0.5" />
              <span>Thanh toán sẽ được tự động xác nhận</span>
            </li>
            <li class="flex items-start">
              <Icon icon="mdi:numeric-4-circle" class="text-blue-500 dark:text-blue-400 mr-2 mt-0.5" />
              <span>Gói dịch vụ sẽ được kích hoạt ngay sau khi thanh toán thành công</span>
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

    // Format currency function
    const formatCurrency = (amount) => {
      return new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND'
      }).format(amount)
    }

    // Load data on mount
    paymentStore.loadBankInfo()
    paymentStore.loadCurrentPackage()

    // Debug: Log current package
    console.log('Payment store current package:', paymentStore.currentPackage)
    console.log('Payment store selected package:', paymentStore.selectedPackage)

    return {
      paymentStore,
      formatCurrency
    }
  }
}
</script>
