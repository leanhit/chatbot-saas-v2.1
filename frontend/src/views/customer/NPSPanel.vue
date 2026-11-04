<template>
  <div class="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm p-6">
    <div class="flex items-center justify-between mb-5">
      <div>
        <h3 class="text-base font-semibold text-gray-900 dark:text-white">NPS Score</h3>
        <p class="text-xs text-gray-500 dark:text-gray-400 mt-0.5">Net Promoter Score (từ rating feedback)</p>
      </div>
      <div class="text-right">
        <p
          class="text-3xl font-bold"
          :class="npsColor"
        >{{ npsScore }}</p>
        <p class="text-xs text-gray-500 dark:text-gray-400">/ 100</p>
      </div>
    </div>

    <!-- NPS Gauge bar -->
    <div class="mb-6">
      <div class="relative h-4 bg-gradient-to-r from-red-400 via-yellow-300 to-green-500 rounded-full overflow-hidden">
        <!-- pointer -->
        <div
          class="absolute top-0 bottom-0 w-1 bg-white shadow-md rounded-full transition-all duration-700"
          :style="{ left: `calc(${gaugePercent}% - 2px)` }"
        />
      </div>
      <div class="flex justify-between text-[10px] text-gray-500 dark:text-gray-400 mt-1">
        <span>-100 (Detractors)</span>
        <span>0</span>
        <span>+100 (Promoters)</span>
      </div>
    </div>

    <!-- Breakdown -->
    <div v-if="total > 0" class="grid grid-cols-3 gap-3 mb-5">
      <div class="text-center rounded-xl p-3 bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800">
        <p class="text-xs text-green-700 dark:text-green-300 font-medium">Promoters</p>
        <p class="text-xl font-bold text-green-700 dark:text-green-300 mt-0.5">{{ promoters }}</p>
        <p class="text-[10px] text-green-600 dark:text-green-400">Rating 4–5</p>
        <p class="text-xs text-green-600 dark:text-green-400 font-semibold mt-1">{{ promoterPct }}%</p>
      </div>
      <div class="text-center rounded-xl p-3 bg-yellow-50 dark:bg-yellow-900/20 border border-yellow-200 dark:border-yellow-800">
        <p class="text-xs text-yellow-700 dark:text-yellow-300 font-medium">Passives</p>
        <p class="text-xl font-bold text-yellow-700 dark:text-yellow-300 mt-0.5">{{ passives }}</p>
        <p class="text-[10px] text-yellow-600 dark:text-yellow-400">Rating 3</p>
        <p class="text-xs text-yellow-600 dark:text-yellow-400 font-semibold mt-1">{{ passivePct }}%</p>
      </div>
      <div class="text-center rounded-xl p-3 bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800">
        <p class="text-xs text-red-700 dark:text-red-300 font-medium">Detractors</p>
        <p class="text-xl font-bold text-red-700 dark:text-red-300 mt-0.5">{{ detractors }}</p>
        <p class="text-[10px] text-red-600 dark:text-red-400">Rating 1–2</p>
        <p class="text-xs text-red-600 dark:text-red-400 font-semibold mt-1">{{ detractorPct }}%</p>
      </div>
    </div>

    <div v-else class="text-center py-4 text-gray-400 dark:text-gray-500 text-sm">
      Chưa có đủ dữ liệu feedback để tính NPS
    </div>

    <!-- Trend by month (last 3 months) -->
    <div v-if="monthlyData.length > 1">
      <p class="text-xs font-semibold text-gray-700 dark:text-gray-300 mb-2">Xu hướng theo tháng</p>
      <div class="flex gap-2">
        <div
          v-for="m in monthlyData"
          :key="m.label"
          class="flex-1 text-center"
        >
          <div class="relative h-16 bg-gray-100 dark:bg-gray-700 rounded-lg overflow-hidden">
            <div
              class="absolute bottom-0 left-0 right-0 rounded-lg transition-all duration-500"
              :class="m.score >= 0 ? 'bg-green-400 dark:bg-green-500' : 'bg-red-400 dark:bg-red-500'"
              :style="{ height: `${Math.abs(m.score)}%` }"
            />
            <div class="absolute inset-0 flex items-center justify-center">
              <span class="text-xs font-bold text-white drop-shadow z-10">{{ m.score > 0 ? '+' : '' }}{{ m.score }}</span>
            </div>
          </div>
          <p class="text-[10px] text-gray-500 dark:text-gray-400 mt-1">{{ m.label }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  /**
   * Array of feedback objects with { rating: number, submittedAt: string }
   */
  feedbackList: {
    type: Array,
    default: () => [],
  },
})

const rated = computed(() => props.feedbackList.filter(f => f.rating != null && f.rating >= 1 && f.rating <= 5))
const total = computed(() => rated.value.length)

const promoters = computed(() => rated.value.filter(f => f.rating >= 4).length)
const passives = computed(() => rated.value.filter(f => f.rating === 3).length)
const detractors = computed(() => rated.value.filter(f => f.rating <= 2).length)

const promoterPct = computed(() => total.value ? Math.round((promoters.value / total.value) * 100) : 0)
const passivePct = computed(() => total.value ? Math.round((passives.value / total.value) * 100) : 0)
const detractorPct = computed(() => total.value ? Math.round((detractors.value / total.value) * 100) : 0)

// NPS = %Promoters - %Detractors (range -100 to +100)
const npsScore = computed(() => {
  if (!total.value) return 'N/A'
  return promoterPct.value - detractorPct.value
})

const gaugePercent = computed(() => {
  if (npsScore.value === 'N/A') return 50
  // Map -100..+100 to 0..100%
  return ((npsScore.value + 100) / 200) * 100
})

const npsColor = computed(() => {
  if (npsScore.value === 'N/A') return 'text-gray-400 dark:text-gray-500'
  if (npsScore.value >= 50) return 'text-green-600 dark:text-green-400'
  if (npsScore.value >= 0) return 'text-blue-600 dark:text-blue-400'
  if (npsScore.value >= -20) return 'text-yellow-600 dark:text-yellow-400'
  return 'text-red-600 dark:text-red-400'
})

// Build monthly NPS for last 4 months
const monthlyData = computed(() => {
  if (!total.value) return []
  const months = {}
  rated.value.forEach(f => {
    const d = new Date(f.submittedAt)
    const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
    if (!months[key]) months[key] = { promoters: 0, detractors: 0, total: 0 }
    months[key].total++
    if (f.rating >= 4) months[key].promoters++
    if (f.rating <= 2) months[key].detractors++
  })

  return Object.entries(months)
    .sort(([a], [b]) => a.localeCompare(b))
    .slice(-4)
    .map(([key, data]) => {
      const [year, month] = key.split('-')
      const score = data.total
        ? Math.round(((data.promoters / data.total) - (data.detractors / data.total)) * 100)
        : 0
      return {
        label: `T${month}/${year.slice(2)}`,
        score,
      }
    })
})
</script>
