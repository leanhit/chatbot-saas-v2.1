<template>
  <div class="wallet-balance bg-gradient-to-br from-blue-600 to-purple-700 rounded-xl p-6 text-white shadow-lg">
    <!-- Header -->
    <div class="flex justify-between items-start mb-6">
      <div>
        <p class="text-blue-100 text-sm font-medium mb-1">Total Balance</p>
        <p class="text-3xl font-bold">
          ${{ formatCurrency(wallet?.balance || 0) }}
        </p>
        <p class="text-blue-100 text-sm mt-1">
          {{ wallet?.currency || 'USD' }}
        </p>
      </div>
      
      <!-- Status Badge -->
      <div class="flex flex-col items-end gap-2">
        <span :class="statusClass" class="px-3 py-1 rounded-full text-xs font-medium backdrop-blur-sm">
          {{ wallet?.status || 'UNKNOWN' }}
        </span>
        <div class="flex items-center gap-1 text-xs text-blue-100">
          <Icon icon="ic:baseline-account-balance-wallet" />
          <span>{{ wallet?.walletType || 'STANDARD' }}</span>
        </div>
      </div>
    </div>
    
    <!-- Balance Breakdown -->
    <div class="grid grid-cols-2 gap-4 mb-6">
      <div class="bg-white/10 backdrop-blur-sm rounded-lg p-3">
        <p class="text-blue-100 text-xs mb-1">Available</p>
        <p class="text-lg font-semibold">
          ${{ formatCurrency(wallet?.availableBalance || wallet?.balance || 0) }}
        </p>
      </div>
      <div class="bg-white/10 backdrop-blur-sm rounded-lg p-3">
        <p class="text-blue-100 text-xs mb-1">Frozen</p>
        <p class="text-lg font-semibold">
          ${{ formatCurrency(wallet?.frozenBalance || 0) }}
        </p>
      </div>
    </div>
    
    <!-- Quick Actions -->
    <div class="flex gap-3">
      <button 
        @click="$emit('topup')"
        class="flex-1 bg-white text-blue-600 px-4 py-2.5 rounded-lg text-sm font-medium hover:bg-blue-50 transition-colors flex items-center justify-center gap-2"
      >
        <Icon icon="ic:baseline-add-circle" />
        <span>Top Up</span>
      </button>
      <button 
        @click="$emit('transactions')"
        class="flex-1 bg-white/20 backdrop-blur-sm text-white px-4 py-2.5 rounded-lg text-sm font-medium hover:bg-white/30 transition-colors flex items-center justify-center gap-2"
      >
        <Icon icon="ic:baseline-receipt-long" />
        <span>Transactions</span>
      </button>
    </div>
    
    <!-- Warning for Low Balance -->
    <div 
      v-if="isLowBalance" 
      class="mt-4 p-3 bg-yellow-400/20 backdrop-blur-sm border border-yellow-400/30 rounded-lg"
    >
      <div class="flex items-center gap-2 text-yellow-100">
        <Icon icon="ic:baseline-warning" class="text-lg" />
        <span class="text-sm">
          Low balance warning. Consider topping up to avoid service interruption.
        </span>
      </div>
    </div>
    
    <!-- Monthly Statistics -->
    <div class="mt-6 pt-4 border-t border-white/20">
      <div class="grid grid-cols-3 gap-4 text-center">
        <div>
          <p class="text-blue-100 text-xs">Monthly Spent</p>
          <p class="text-sm font-semibold">${{ formatCurrency(monthlyStats.spent) }}</p>
        </div>
        <div>
          <p class="text-blue-100 text-xs">Monthly Top-up</p>
          <p class="text-sm font-semibold">${{ formatCurrency(monthlyStats.topup) }}</p>
        </div>
        <div>
          <p class="text-blue-100 text-xs">Transactions</p>
          <p class="text-sm font-semibold">{{ monthlyStats.count }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useWalletStore } from '@/stores/wallet/walletStore'

const props = defineProps({
  wallet: {
    type: Object,
    default: () => ({})
  },
  monthlyStats: {
    type: Object,
    default: () => ({
      spent: 0,
      topup: 0,
      count: 0
    })
  }
})

const emit = defineEmits(['topup', 'transactions'])

const walletStore = useWalletStore()

// Computed properties
const statusClass = computed(() => {
  const status = props.wallet?.status?.toUpperCase()
  switch (status) {
    case 'ACTIVE':
      return 'bg-green-400/80 text-green-900'
    case 'FROZEN':
      return 'bg-yellow-400/80 text-yellow-900'
    case 'CLOSED':
      return 'bg-red-400/80 text-red-900'
    case 'SUSPENDED':
      return 'bg-orange-400/80 text-orange-900'
    default:
      return 'bg-gray-400/80 text-gray-900'
  }
})

const isLowBalance = computed(() => {
  const balance = props.wallet?.balance || 0
  const availableBalance = props.wallet?.availableBalance || balance
  return availableBalance < 100 // Warning if less than $100
})

// Methods
const formatCurrency = (amount) => {
  if (!amount) return '0.00'
  return new Intl.NumberFormat('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount / 100) // Assuming amount is in cents
}
</script>

<style scoped>
.wallet-balance {
  background: linear-gradient(135deg, #2563eb 0%, #9333ea 100%);
  position: relative;
  overflow: hidden;
}

.wallet-balance::before {
  content: '';
  position: absolute;
  top: -50%;
  right: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.1) 0%, transparent 70%);
  animation: float 6s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) rotate(0deg); }
  50% { transform: translate(-20px, -20px) rotate(180deg); }
}
</style>
